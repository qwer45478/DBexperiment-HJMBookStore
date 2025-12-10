package com.hjm.bookstore.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入工具类
 * 用于处理书籍信息的批量导入
 */
@Slf4j
public class ExcelImportUtils {

    /**
     * Excel列名常量
     */
    private static final String[] EXPECTED_HEADERS = {
        "书本名", "分类", "作者", "简介", "出版社", "价格", "库存"
    };

    /**
     * 解析Excel文件并返回书籍信息列表
     * 
     * @param file Excel文件
     * @return 书籍信息列表
     * @throws IOException 文件读取异常
     * @throws IllegalArgumentException 文件格式或内容不正确
     */
    public static List<BookImportData> parseExcelFile(MultipartFile file) throws IOException, IllegalArgumentException {
        // 验证文件类型
        if (!isExcelFile(file)) {
            throw new IllegalArgumentException("只支持.xlsx格式的Excel文件");
        }

        List<BookImportData> bookList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 验证表头
            validateHeaders(sheet);
            
            // 读取数据行（从第二行开始）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmptyRow(row)) {
                    continue; // 跳过空行
                }
                
                try {
                    BookImportData bookData = parseRowToBookData(row);
                    bookList.add(bookData);
                } catch (Exception e) {
                    log.warn("解析第{}行数据失败: {}", rowIndex + 1, e.getMessage());
                    throw new IllegalArgumentException("第" + (rowIndex + 1) + "行数据格式错误: " + e.getMessage());
                }
            }
        }
        
        return bookList;
    }

    /**
     * 验证是否为Excel文件
     */
    private static boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        if (originalFilename == null) {
            return false;
        }
        
        return originalFilename.toLowerCase().endsWith(".xlsx") ||
               (contentType != null && contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    /**
     * 验证Excel表头
     */
    private static void validateHeaders(Sheet sheet) throws IllegalArgumentException {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("Excel文件缺少表头行");
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = getCellValueAsString(cell);
            if (!EXPECTED_HEADERS[i].equals(headerValue)) {
                throw new IllegalArgumentException("表头格式不正确，第" + (i + 1) + "列应为'" + EXPECTED_HEADERS[i] + "'，实际为'" + headerValue + "'");
            }
        }
    }

    /**
     * 判断是否为空行
     */
    private static boolean isEmptyRow(Row row) {
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 解析行数据为书籍信息对象
     */
    private static BookImportData parseRowToBookData(Row row) {
        BookImportData bookData = new BookImportData();
        
        // 书本名 (必填)
        String bookName = getCellValueAsString(row.getCell(0));
        if (bookName == null || bookName.trim().isEmpty()) {
            throw new IllegalArgumentException("书本名不能为空");
        }
        bookData.setBookName(bookName.trim());
        
        // 分类 (必填)
        String category = getCellValueAsString(row.getCell(1));
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        // 验证分类格式（A-Z的单个字母）
        category = category.trim().toUpperCase();
        if (category.length() != 1 || category.charAt(0) < 'A' || category.charAt(0) > 'Z') {
            throw new IllegalArgumentException("分类必须是A-Z的单个字母");
        }
        bookData.setCategory(category);
        
        // 作者 (必填)
        String author = getCellValueAsString(row.getCell(2));
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("作者不能为空");
        }
        bookData.setAuthor(author.trim());
        
        // 简介 (选填)
        String description = getCellValueAsString(row.getCell(3));
        bookData.setDescription(description != null ? description.trim() : null);
        
        // 出版社 (必填)
        String publisher = getCellValueAsString(row.getCell(4));
        if (publisher == null || publisher.trim().isEmpty()) {
            throw new IllegalArgumentException("出版社不能为空");
        }
        bookData.setPublisher(publisher.trim());
        
        // 价格 (必填)
        String priceStr = getCellValueAsString(row.getCell(5));
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("价格不能为空");
        }
        try {
            BigDecimal price = new BigDecimal(priceStr.trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("价格不能为负数");
            }
            bookData.setPrice(price);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("价格格式不正确，必须是数字");
        }
        
        // 库存 (必填)
        String stockStr = getCellValueAsString(row.getCell(6));
        if (stockStr == null || stockStr.trim().isEmpty()) {
            throw new IllegalArgumentException("库存不能为空");
        }
        try {
            int stock = Integer.parseInt(stockStr.trim());
            if (stock < 0) {
                throw new IllegalArgumentException("库存不能为负数");
            }
            bookData.setStock(stock);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("库存格式不正确，必须是整数");
        }
        
        return bookData;
    }

    /**
     * 获取单元格的字符串值
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 处理数字，避免科学计数法
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * 书籍导入数据传输对象
     */
    public static class BookImportData {
        private String bookName;
        private String category;
        private String author;
        private String description;
        private String publisher;
        private BigDecimal price;
        private Integer stock;

        // Getters and Setters
        public String getBookName() { return bookName; }
        public void setBookName(String bookName) { this.bookName = bookName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}
