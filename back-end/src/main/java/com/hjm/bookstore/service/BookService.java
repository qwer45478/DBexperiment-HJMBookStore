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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

        List<BooksInfo> books = booksInfoRepository.findAll(spec);
        return setRealTimeRatingsForBooks(books);
    }

    /**
     * 获取首页推荐书籍（个性化推荐 + 销量推荐）
     */
    public Map<String, List<BooksInfo>> getHomePageRecommendations(Integer userId) {
        Map<String, List<BooksInfo>> recommendations = new HashMap<>();
        
        // 个性化推荐
        List<BooksInfo> personalized = new ArrayList<>();
        if (userId != null) {
            personalized = new RecommendationService().recommendBooks(userId, 6);
        }
        
        // 销量推荐（如果没有足够的个性化推荐，则用销量推荐补充）
        List<BooksInfo> salesBased = booksInfoRepository.findTop10ByStatusOrderBySalesDesc(1);
        int needMore = 6 - personalized.size();
        if (needMore > 0 && salesBased.size() > personalized.size()) {
            personalized.addAll(
                salesBased.subList(personalized.size(), 
                                   Math.min(personalized.size() + needMore, salesBased.size()))
            );
        }
        
        recommendations.put("personalized", personalized);
        recommendations.put("topSales", salesBased.stream().limit(6).collect(Collectors.toList()));

        // 月销量推荐
        List<BooksInfo> monthlySales = booksInfoRepository.findTop10ByStatusOrderByMonthlySalesDesc(1);
        recommendations.put("monthlySales", monthlySales.stream().limit(6).collect(Collectors.toList()));

        return recommendations;
    }

    /**
     * 获取排行榜数据
     */
    public Map<String, List<BooksInfo>> getRankings() {
        Map<String, List<BooksInfo>> rankings = new HashMap<>();
        
        // 评分排行榜
        List<BooksInfo> ratingRank = booksInfoRepository.findTop50ByStatusOrderByRatingDesc(1);
        rankings.put("rating", ratingRank.stream().limit(20).collect(Collectors.toList()));

        // 总销量排行榜
        List<BooksInfo> salesRank = booksInfoRepository.findTop50ByStatusOrderBySalesDesc(1);
        rankings.put("sales", salesRank.stream().limit(20).collect(Collectors.toList()));

        // 月销量排行榜
        List<BooksInfo> monthlyRank = booksInfoRepository.findTop50ByStatusOrderByMonthlySalesDesc(1);
        rankings.put("monthly", monthlyRank.stream().limit(20).collect(Collectors.toList()));

        return rankings;
    }

    /**
     * 添加书籍
     */
    @Transactional
    public BooksInfo addBook(BooksInfo book) {
        // 设置初始值
        if (book.getRating() == null) book.setRating(BigDecimal.ZERO);
        if (book.getStock() == null) book.setStock(0);
        if (book.getSales() == null) book.setSales(0);
        if (book.getMonthlySales() == null) book.setMonthlySales(0);
        if (book.getStatus() == null) book.setStatus(1);
        
        BooksInfo savedBook = booksInfoRepository.save(book);
        log.info("添加书籍成功: {}", savedBook.getBookName());
        return savedBook;
    }

    /**
     * 更新书籍
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public BooksInfo updateBook(BooksInfo book) {
        Optional<BooksInfo> existingBookOpt = booksInfoRepository.findById(book.getBookId());
        if (existingBookOpt.isEmpty()) {
            throw new RuntimeException("书籍不存在");
        }
        
        BooksInfo existingBook = existingBookOpt.get();
        // 更新非null字段
        if (book.getBookName() != null) existingBook.setBookName(book.getBookName());
        if (book.getCategory() != null) existingBook.setCategory(book.getCategory());
        if (book.getAuthor() != null) existingBook.setAuthor(book.getAuthor());
        if (book.getBookImage() != null) existingBook.setBookImage(book.getBookImage());
        if (book.getDescription() != null) existingBook.setDescription(book.getDescription());
        if (book.getPublisher() != null) existingBook.setPublisher(book.getPublisher());
        if (book.getPrice() != null) existingBook.setPrice(book.getPrice());
        if (book.getStatus() != null) existingBook.setStatus(book.getStatus());
        
        BooksInfo updatedBook = booksInfoRepository.save(existingBook);
        log.info("更新书籍成功: {}", updatedBook.getBookName());
        return updatedBook;
    }

    /**
     * 上架书籍
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
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
     * 下架书籍
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public void removeBook(Integer bookId) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            book.setStatus(0);
            booksInfoRepository.save(book);
        }
    }

    /**
     * 更新库存
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
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
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public void updateSales(Integer bookId, Integer quantity) {
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            // 检查库存是否足够
            if (book.getStock() < quantity) {
                throw new RuntimeException("库存不足，无法完成购买");
            }
            
            book.setSales(book.getSales() + quantity);
            book.setMonthlySales(book.getMonthlySales() + quantity);
            book.setStock(book.getStock() - quantity);
            booksInfoRepository.save(book);
        } else {
            throw new RuntimeException("书籍不存在");
        }
    }
    
    /**
     * 恢复库存（取消订单时使用）
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
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
    public BooksInfo saveBookWithImage(MultipartFile imageFile, BooksInfo book) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "images", "books");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 保存文件
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath);
            
            // 设置图片路径
            book.setBookImage(uploadDir + "/images/books/" + fileName);
        }
        
        return addBook(book);
    }

    /**
     * 批量导入书籍（从Excel）
     */
    @Transactional
    public List<BooksInfo> importBooksFromExcel(MultipartFile excelFile) throws IOException {
        List<BooksInfo> books = ExcelImportUtils.parseBooksFromExcel(excelFile);
        List<BooksInfo> savedBooks = new ArrayList<>();
        
        for (BooksInfo book : books) {
            BooksInfo savedBook = addBook(book);
            savedBooks.add(savedBook);
        }
        
        log.info("批量导入书籍成功，共导入 {} 本", savedBooks.size());
        return savedBooks;
    }

    /**
     * 为书籍列表设置实时评分
     */
    private List<BooksInfo> setRealTimeRatingsForBooks(List<BooksInfo> books) {
        return books.stream()
                .map(this::getBookWithRealTimeRating)
                .collect(Collectors.toList());
    }

    /**
     * 为单本书籍设置实时评分
     */
    private BooksInfo getBookWithRealTimeRating(BooksInfo book) {
        BigDecimal realTimeRating = scoreService.calculateAverageScore(book.getBookId());
        book.setRating(realTimeRating);
        return book;
    }
}