package com.hjm.bookstore.controller;

import com.hjm.bookstore.common.Result;
import com.hjm.bookstore.dto.AdminBookSearchRequest;
import com.hjm.bookstore.dto.BookSearchRequest;
import com.hjm.bookstore.dto.PageResponse;
import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.service.BookService;
import com.hjm.bookstore.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 书籍控制器
 */
@RestController
@RequestMapping("/books")
@Slf4j
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private RecommendationService recommendationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 获取所有书籍
     */
    @GetMapping("/list")
    public Result<List<BooksInfo>> getAllBooks() {
        try {
            List<BooksInfo> books = bookService.getAllBooks();
            return Result.success(books);
        } catch (Exception e) {
            log.error("获取书籍列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 管理员获取书籍列表（带分页）
     */
    @PostMapping("/admin/search")
    public Result<PageResponse<BooksInfo>> getAdminBooksWithPagination(@RequestBody AdminBookSearchRequest request) {
        try {
            PageResponse<BooksInfo> result = bookService.searchAdminBooks(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取管理员书籍列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取书籍详情
     */
    @GetMapping("/{id}")
    public Result<BooksInfo> getBookById(@PathVariable Integer id) {
        try {
            return bookService.getBookById(id)
                    .map(Result::success)
                    .orElse(Result.error("书籍不存在"));
        } catch (Exception e) {
            log.error("获取书籍详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索书籍（带分页）
     */
    @PostMapping("/search")
    public Result<PageResponse<BooksInfo>> searchBooks(@RequestBody BookSearchRequest request) {
        try {
            PageResponse<BooksInfo> result = bookService.searchBooks(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 搜索书籍（旧版本，保持兼容性）
     */
    @PostMapping("/search/all")
    public Result<List<BooksInfo>> searchBooksWithoutPagination(@RequestBody BookSearchRequest request) {
        try {
            List<BooksInfo> books = bookService.searchBooksWithoutPagination(request);
            return Result.success(books);
        } catch (Exception e) {
            log.error("搜索书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取排行榜
     */
    @GetMapping("/rankings")
    public Result<Map<String, List<BooksInfo>>> getRankings() {
        try {
            Map<String, List<BooksInfo>> rankings = new HashMap<>();
            rankings.put("sales", bookService.getSalesRanking());
            rankings.put("monthlySales", bookService.getMonthlySalesRanking());
            rankings.put("rating", bookService.getRatingRanking());
            return Result.success(rankings);
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取个性化推荐
     */
    @GetMapping("/recommend/{userId}")
    public Result<List<BooksInfo>> getRecommendations(@PathVariable Integer userId) {
        try {
            List<BooksInfo> recommendations = recommendationService.recommendBooks(userId, 10);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取推荐失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加或更新书籍（管理员）
     */
    @PostMapping("/save")
    public Result<BooksInfo> saveBook(@RequestBody BooksInfo book) {
        try {
            BooksInfo savedBook = bookService.saveBook(book);
            return Result.success("保存成功", savedBook);
        } catch (Exception e) {
            log.error("保存书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下架书籍（管理员）
     */
    @DeleteMapping("/remove/{id}")
    public Result<Void> removeBook(@PathVariable Integer id) {
        try {
            bookService.removeBook(id);
            return Result.success("下架成功", null);
        } catch (Exception e) {
            log.error("下架书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上架书籍（管理员）
     */
    @PutMapping("/restore/{id}")
    public Result<Void> restoreBook(@PathVariable Integer id) {
        try {
            bookService.restoreBook(id);
            return Result.success("上架成功", null);
        } catch (Exception e) {
            log.error("上架书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除书籍（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteBook(@PathVariable Integer id) {
        try {
            bookService.deleteBook(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除书籍（管理员）
     */
    @PostMapping("/delete/batch")
    public Result<Void> deleteBooksInBatch(@RequestBody List<Integer> bookIds) {
        try {
            bookService.deleteBooksInBatch(bookIds);
            return Result.success("批量删除成功", null);
        } catch (Exception e) {
            log.error("批量删除失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量下架书籍（管理员）
     */
    @PostMapping("/remove/batch")
    public Result<Void> removeBooksInBatch(@RequestBody List<Integer> bookIds) {
        try {
            bookService.removeBooksInBatch(bookIds);
            return Result.success("批量下架成功", null);
        } catch (Exception e) {
            log.error("批量下架失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传书籍图片（管理员）
     */
    @PostMapping("/upload-image")
    public Result<String> uploadBookImage(@RequestParam("file") MultipartFile file, 
                                          @RequestParam("bookId") Integer bookId) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 创建上传目录
            String imageUploadPath = uploadDir + "/images/books";
            Path uploadPath = Paths.get(imageUploadPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名：书籍ID.jpg
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            if (extension == null) {
                extension = "jpg"; // 默认扩展名
            }
            String fileName = bookId + "." + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回相对路径用于数据库存储
            String relativePath = "uploads/images/books/" + fileName;
            log.info("图片上传成功: {}", relativePath);
            
            return Result.success("上传成功", relativePath);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存书籍并上传图片（管理员）
     */
    @PostMapping("/save-with-image")
    public Result<BooksInfo> saveBookWithImage(@RequestParam("bookData") String bookData,
                                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // 这里需要手动解析JSON字符串为BooksInfo对象
            // 简化起见，先实现上传图片接口，后续可以优化
            BooksInfo book = bookService.saveBookWithImage(bookData, imageFile);
            return Result.success("保存成功", book);
        } catch (Exception e) {
            log.error("保存书籍失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从Excel文件批量导入书籍（管理员）
     */
    @PostMapping("/import-excel")
    public Result<BookService.ExcelImportResult> importBooksFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                return Result.error("只支持.xlsx格式的Excel文件");
            }

            // 导入书籍
            BookService.ExcelImportResult result = bookService.importBooksFromExcel(file);
            
            String message = String.format("导入完成！总数: %d, 成功: %d, 跳过: %d, 错误: %d", 
                    result.getTotalCount(), result.getSuccessCount(), result.getSkipCount(), result.getErrorCount());
            
            return Result.success(message, result);
        } catch (IllegalArgumentException e) {
            log.error("Excel导入参数错误", e);
            return Result.error("文件格式错误: " + e.getMessage());
        } catch (IOException e) {
            log.error("Excel导入IO错误", e);
            return Result.error("文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excel导入失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
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
}
