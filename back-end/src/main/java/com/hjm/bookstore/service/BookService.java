package com.hjm.bookstore.service;

import com.hjm.bookstore.dto.BookSearchRequest;
import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.utils.ExcelImportUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 书籍服务类
 */
@Service
@Slf4j
public class BookService {

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreService scoreService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 获取所有书籍（包括上架和下架）
     */
    public List<BooksInfo> getAllBooks() {
        List<BooksInfo> books = booksInfoRepository.findAll();
        return setRealTimeRatingsForBooks(books);
    }

    /**
     * 根据ID获取书籍
     */
    public Optional<BooksInfo> getBookById(Integer bookId) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo bookWithRealRating = getBookWithRealTimeRating(bookOpt.get());
            return Optional.of(bookWithRealRating);
        }
        return Optional.empty();
    }

    /**
     * 高级搜索
     */
    public List<BooksInfo> searchBooks(BookSearchRequest request) {
        Specification<BooksInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只查询上架的书籍
            predicates.add(cb.equal(root.get("status"), 1));

            // 书名模糊查询
            if (request.getBookName() != null && !request.getBookName().isEmpty()) {
                predicates.add(cb.like(root.get("bookName"), "%" + request.getBookName() + "%"));
            }

            // 作者模糊查询
            if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
                predicates.add(cb.like(root.get("author"), "%" + request.getAuthor() + "%"));
            }

            // 分类查询
            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(request.getCategories()));
            }

            // 销量范围
            if (request.getMinSales() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sales"), request.getMinSales()));
            }
            if (request.getMaxSales() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sales"), request.getMaxSales()));
            }

            // 价格范围
            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }

            // 评分范围
            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getMinRating()));
            }
            if (request.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.getMaxRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<BooksInfo> results = booksInfoRepository.findAll(spec);

        // 排序
        if (request.getSortBy() != null) {
            Comparator<BooksInfo> comparator = null;
            switch (request.getSortBy()) {
                case "sales":
                    comparator = Comparator.comparing(BooksInfo::getSales);
                    break;
                case "rating":
                    comparator = Comparator.comparing(BooksInfo::getRating);
                    break;
                case "price":
                    comparator = Comparator.comparing(BooksInfo::getPrice);
                    break;
            }

            if (comparator != null) {
                if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                    comparator = comparator.reversed();
                }
                results = results.stream().sorted(comparator).collect(Collectors.toList());
            }
        }

        // 为搜索结果设置实时评分
        return setRealTimeRatingsForBooks(results);
    }

    /**
     * 获取销量排行榜
     */
    public List<BooksInfo> getSalesRanking() {
        return booksInfoRepository.findTop10ByStatusOrderBySalesDesc(1);
    }

    /**
     * 获取月销量排行榜
     */
    public List<BooksInfo> getMonthlySalesRanking() {
        return booksInfoRepository.findTop10ByStatusOrderByMonthlySalesDesc(1);
    }

    /**
     * 获取评分排行榜
     */
    public List<BooksInfo> getRatingRanking() {
        List<BooksInfo> books = booksInfoRepository.findTop10ByStatusOrderByRatingDesc(1);
        return setRealTimeRatingsForBooks(books);
    }

    /**
     * 添加或更新书籍
     */
    @Transactional
    public BooksInfo saveBook(BooksInfo book) {
        return booksInfoRepository.save(book);
    }

    /**
     * 下架书籍
     */
    @Transactional
    public void removeBook(Integer bookId) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setStatus(0);
            booksInfoRepository.save(book);
            log.info("书籍下架成功: {}", bookId);
        }
    }

    /**
     * 上架书籍
     */
    @Transactional
    public void restoreBook(Integer bookId) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setStatus(1);
            booksInfoRepository.save(book);
        }
    }

    /**
     * 删除书籍（从数据库中彻底删除）
     */
    @Transactional
    public void deleteBook(Integer bookId) {
        // 先删除关联的图片文件
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            if (book.getBookImage() != null && !book.getBookImage().isEmpty()) {
                try {
                    String imagePath = book.getBookImage();
                    if (imagePath.startsWith("uploads/")) {
                        Path filePath = Paths.get(imagePath);
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            log.info("删除书籍图片文件: {}", imagePath);
                        }
                    }
                } catch (IOException e) {
                    log.warn("删除书籍图片文件失败: {}", e.getMessage());
                }
            }
        }
        
        // 从数据库中删除书籍
        booksInfoRepository.deleteById(bookId);
        log.info("书籍删除成功: {}", bookId);
    }

    /**
     * 批量删除书籍
     */
    @Transactional
    public void deleteBooksInBatch(List<Integer> bookIds) {
        for (Integer bookId : bookIds) {
            deleteBook(bookId);
        }
    }

    /**
     * 批量下架书籍
     */
    @Transactional
    public void removeBooksInBatch(List<Integer> bookIds) {
        for (Integer bookId : bookIds) {
            removeBook(bookId);
        }
    }

    /**
     * 更新库存
     */
    @Transactional
    public void updateStock(Integer bookId, Integer quantity) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setStock(book.getStock() + quantity);
            booksInfoRepository.save(book);
        }
    }

    /**
     * 更新销量
     */
    @Transactional
    public void updateSales(Integer bookId, Integer quantity) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setSales(book.getSales() + quantity);
            book.setMonthlySales(book.getMonthlySales() + quantity);
            book.setStock(book.getStock() - quantity);
            booksInfoRepository.save(book);
        }
    }
    
    /**
     * 恢复库存（取消订单时使用）
     */
    @Transactional
    public void restoreStock(Integer bookId, Integer quantity) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setStock(book.getStock() + quantity);
            book.setSales(Math.max(0, book.getSales() - quantity));
            book.setMonthlySales(Math.max(0, book.getMonthlySales() - quantity));
            booksInfoRepository.save(book);
            log.info("恢复库存成功: 书籍{} 数量{}", bookId, quantity);
        }
    }

    /**
     * 保存书籍并上传图片
     */
    @Transactional
    public BooksInfo saveBookWithImage(String bookData, MultipartFile imageFile) throws IOException {
        // 解析JSON数据
        BooksInfo book = objectMapper.readValue(bookData, BooksInfo.class);
        
        // 如果有图片文件，先保存书籍获取ID，再上传图片
        if (imageFile != null && !imageFile.isEmpty()) {
            // 先保存书籍获取ID
            BooksInfo savedBook = booksInfoRepository.save(book);
            
            // 上传图片
            String imagePath = uploadImageForBook(savedBook.getBookId(), imageFile);
            
            // 更新书籍的图片路径
            savedBook.setBookImage(imagePath);
            return booksInfoRepository.save(savedBook);
        } else {
            // 没有图片直接保存
            return booksInfoRepository.save(book);
        }
    }

    /**
     * 为指定书籍上传图片
     */
    private String uploadImageForBook(Integer bookId, MultipartFile imageFile) throws IOException {
        // 创建上传目录
        String imageUploadPath = uploadDir + "/images/books";
        Path uploadPath = Paths.get(imageUploadPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成文件名：书籍ID.jpg
        String originalFilename = imageFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension == null) {
            extension = "jpg"; // 默认扩展名
        }
        String fileName = bookId + "." + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        // 返回相对路径用于数据库存储
        return "uploads/images/books/" + fileName;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 从Excel文件批量导入书籍信息
     * 
     * @param file Excel文件
     * @return 导入结果统计
     * @throws IOException 文件读取异常
     * @throws IllegalArgumentException 文件格式或内容错误
     */
    @Transactional
    public ExcelImportResult importBooksFromExcel(MultipartFile file) throws IOException, IllegalArgumentException {
        log.info("开始导入Excel文件: {}", file.getOriginalFilename());
        
        // 解析Excel文件
        List<ExcelImportUtils.BookImportData> bookDataList = ExcelImportUtils.parseExcelFile(file);
        
        int successCount = 0;
        int skipCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (int i = 0; i < bookDataList.size(); i++) {
            ExcelImportUtils.BookImportData importData = bookDataList.get(i);
            int rowNum = i + 2; // Excel行号（从2开始，因为第1行是表头）
            
            try {
                // 检查是否已存在相同的书籍（根据书名、作者、出版社判断）
                if (isBookExists(importData.getBookName(), importData.getAuthor(), importData.getPublisher())) {
                    skipCount++;
                    log.info("跳过重复书籍: 第{}行, 书名: {}", rowNum, importData.getBookName());
                    continue;
                }
                
                // 转换为BooksInfo实体
                BooksInfo book = convertToBookEntity(importData);
                
                // 保存到数据库
                booksInfoRepository.save(book);
                successCount++;
                
                log.info("成功导入书籍: 第{}行, 书名: {}, ID: {}", rowNum, importData.getBookName(), book.getBookId());
                
            } catch (Exception e) {
                String errorMsg = String.format("第%d行导入失败: %s", rowNum, e.getMessage());
                errorMessages.add(errorMsg);
                log.error(errorMsg, e);
            }
        }
        
        ExcelImportResult result = new ExcelImportResult();
        result.setTotalCount(bookDataList.size());
        result.setSuccessCount(successCount);
        result.setSkipCount(skipCount);
        result.setErrorCount(errorMessages.size());
        result.setErrorMessages(errorMessages);
        
        log.info("Excel导入完成 - 总数: {}, 成功: {}, 跳过: {}, 错误: {}", 
                result.getTotalCount(), result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());
        
        return result;
    }

    /**
     * 检查书籍是否已存在
     */
    private boolean isBookExists(String bookName, String author, String publisher) {
        return booksInfoRepository.findByBookNameAndAuthorAndPublisher(bookName, author, publisher).isPresent();
    }

    /**
     * 获取书籍的实时评分（如果数据库中的评分为0，则从user_score表计算实际评分）
     */
    private BooksInfo getBookWithRealTimeRating(BooksInfo book) {
        if (book != null && (book.getRating() == null || book.getRating().compareTo(BigDecimal.ZERO) == 0)) {
            // 如果数据库中的评分为0，尝试从user_score表计算实际评分
            try {
                Double avgRating = scoreService.getAverageRatingByBookId(book.getBookId());
                if (avgRating != null && avgRating > 0) {
                    // 创建新的书籍对象，避免修改数据库中的原始数据
                    BooksInfo bookWithRealRating = new BooksInfo();
                    // 复制所有属性
                    bookWithRealRating.setBookId(book.getBookId());
                    bookWithRealRating.setBookName(book.getBookName());
                    bookWithRealRating.setCategory(book.getCategory());
                    bookWithRealRating.setAuthor(book.getAuthor());
                    bookWithRealRating.setBookImage(book.getBookImage());
                    bookWithRealRating.setDescription(book.getDescription());
                    bookWithRealRating.setPublisher(book.getPublisher());
                    bookWithRealRating.setPrice(book.getPrice());
                    bookWithRealRating.setRating(BigDecimal.valueOf(avgRating).setScale(1, BigDecimal.ROUND_HALF_UP));
                    bookWithRealRating.setStock(book.getStock());
                    bookWithRealRating.setSales(book.getSales());
                    bookWithRealRating.setMonthlySales(book.getMonthlySales());
                    bookWithRealRating.setStatus(book.getStatus());
                    bookWithRealRating.setCreatedAt(book.getCreatedAt());
                    bookWithRealRating.setUpdatedAt(book.getUpdatedAt());
                    
                    return bookWithRealRating;
                }
            } catch (Exception e) {
                log.warn("计算书籍 {} 的实时评分失败: {}", book.getBookId(), e.getMessage());
            }
        }
        return book;
    }

    /**
     * 为书籍列表设置实时评分
     */
    private List<BooksInfo> setRealTimeRatingsForBooks(List<BooksInfo> books) {
        if (books == null || books.isEmpty()) {
            return books;
        }
        
        return books.stream()
                .map(this::getBookWithRealTimeRating)
                .collect(Collectors.toList());
    }

    /**
     * 将导入数据转换为书籍实体
     */
    private BooksInfo convertToBookEntity(ExcelImportUtils.BookImportData importData) {
        BooksInfo book = new BooksInfo();
        book.setBookName(importData.getBookName());
        book.setCategory(importData.getCategory());
        book.setAuthor(importData.getAuthor());
        book.setDescription(importData.getDescription());
        book.setPublisher(importData.getPublisher());
        book.setPrice(importData.getPrice());
        book.setStock(importData.getStock());
        book.setRating(BigDecimal.ZERO); // 初始评分为0
        book.setSales(0); // 初始销量为0
        book.setMonthlySales(0); // 初始月销量为0
        book.setStatus(1); // 默认上架状态
        book.setBookImage(null); // Excel导入时不设置图片
        
        return book;
    }

    /**
     * Excel导入结果统计类
     */
    public static class ExcelImportResult {
        private int totalCount;
        private int successCount;
        private int skipCount;
        private int errorCount;
        private List<String> errorMessages;

        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }

        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

        public List<String> getErrorMessages() { return errorMessages; }
        public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }
    }
}
