package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.ShoppingHist;
import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.ShoppingHistRepository;
import com.hjm.bookstore.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单服务类
 */
@Service
@Slf4j
public class OrderService {

    @Autowired
    private ShoppingHistRepository shoppingHistRepository;

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BookService bookService;

    /**
     * 创建订单
     */
    @Transactional
    public ShoppingHist createOrder(Integer userId, Integer bookId, Integer quantity) {
        // 检查用户
        Optional<UserInfo> userOpt = userInfoRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        // 检查书籍
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("书籍不存在");
        }

        BooksInfo book = bookOpt.get();
        if (book.getStatus() == 0) {
            throw new RuntimeException("该书籍已下架");
        }

        if (book.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        // 创建订单
        ShoppingHist order = new ShoppingHist();
        order.setUserId(userId);
        order.setBookId(bookId);
        order.setQuantity(quantity);
        order.setUnitPrice(book.getPrice());
        order.setTotalPrice(book.getPrice().multiply(new BigDecimal(quantity)));
        order.setOrderStatus(1);

        ShoppingHist savedOrder = shoppingHistRepository.save(order);

        // 更新书籍销量和库存
        bookService.updateSales(bookId, quantity);

        // 更新用户消费金额和等级
        UserInfo user = userOpt.get();
        user.setTotalSpending(user.getTotalSpending().add(order.getTotalPrice()));
        user.updateUserLevel();
        userInfoRepository.save(user);

        log.info("创建订单成功: 用户{} 购买书籍{} 数量{}", userId, bookId, quantity);
        return savedOrder;
    }

    /**
     * 批量创建订单（购物车结算）
     */
    @Transactional
    public List<ShoppingHist> createBatchOrders(Integer userId, List<Map<String, Integer>> items) {
        List<ShoppingHist> orders = new ArrayList<>();
        
        for (Map<String, Integer> item : items) {
            Integer bookId = item.get("bookId");
            Integer quantity = item.get("quantity");
            ShoppingHist order = createOrder(userId, bookId, quantity);
            orders.add(order);
        }

        return orders;
    }

    /**
     * 获取用户订单历史
     */
    public List<Map<String, Object>> getUserOrders(Integer userId) {
        List<ShoppingHist> orders = shoppingHistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ShoppingHist order : orders) {
            Optional<BooksInfo> bookOpt = booksInfoRepository.findById(order.getBookId());
            if (bookOpt.isPresent()) {
                BooksInfo book = bookOpt.get();
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getOrderId());
                orderInfo.put("bookId", book.getBookId());
                orderInfo.put("bookName", book.getBookName());
                orderInfo.put("author", book.getAuthor());
                orderInfo.put("bookImage", book.getBookImage());
                orderInfo.put("quantity", order.getQuantity());
                orderInfo.put("unitPrice", order.getUnitPrice());
                orderInfo.put("totalPrice", order.getTotalPrice());
                orderInfo.put("orderStatus", order.getOrderStatus());
                orderInfo.put("createdAt", order.getCreatedAt());
                result.add(orderInfo);
            }
        }

        return result;
    }

    /**
     * 获取订单统计数据（管理员）
     */
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取最近30天的每日销量
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        List<Object[]> dailySales = shoppingHistRepository.getDailySalesStatistics(startDate);
        
        List<String> dates = new ArrayList<>();
        List<Integer> sales = new ArrayList<>();
        for (Object[] row : dailySales) {
            dates.add(row[0].toString());
            sales.add(((Number) row[1]).intValue());
        }
        
        statistics.put("dailySales", Map.of("dates", dates, "sales", sales));

        // 获取今日销量前8的书籍
        List<Object[]> topBooks = shoppingHistRepository.getTodayTopBooks();
        List<Map<String, Object>> topBooksList = new ArrayList<>();
        
        int limit = Math.min(8, topBooks.size());
        for (int i = 0; i < limit; i++) {
            Object[] row = topBooks.get(i);
            Integer bookId = (Integer) row[0];
            Integer totalQuantity = ((Number) row[1]).intValue();
            
            Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                BooksInfo book = bookOpt.get();
                Map<String, Object> bookInfo = new HashMap<>();
                bookInfo.put("bookName", book.getBookName());
                bookInfo.put("sales", totalQuantity);
                topBooksList.add(bookInfo);
            }
        }
        
        statistics.put("topBooks", topBooksList);

        return statistics;
    }
}
