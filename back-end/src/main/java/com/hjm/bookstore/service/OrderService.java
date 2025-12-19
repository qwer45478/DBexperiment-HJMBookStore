package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.ShoppingHist;
import com.hjm.bookstore.entity.UserInfo;
import com.hjm.bookstore.dto.OrderConfirmRequest;
import com.hjm.bookstore.dto.OrderConfirmResponse;
import com.hjm.bookstore.dto.OrderSearchRequest;
import com.hjm.bookstore.dto.PageResponse;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.ShoppingHistRepository;
import com.hjm.bookstore.repository.UserInfoRepository;
import com.hjm.bookstore.repository.UserAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserAddressRepository userAddressRepository;

    /**
     * 创建订单
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
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
        order.setOrderStatus(1); // 1-运送中
        order.setAddress(null); // 单个购买时地址为空，需要从订单确认页面传入

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
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
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
     * 获取用户订单历史（带分页）
     */
    public PageResponse<Map<String, Object>> getUserOrders(OrderSearchRequest request) {
        // 验证分页参数
        request.validate();
        
        // 构建排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request.getSortBy() != null) {
            Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, request.getSortBy());
        }
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
        
        // 查询订单
        Page<ShoppingHist> orderPage;
        if (request.getUserId() != null && request.getOrderStatus() != null) {
            orderPage = shoppingHistRepository.findByUserIdAndOrderStatus(request.getUserId(), request.getOrderStatus(), pageable);
        } else if (request.getUserId() != null) {
            orderPage = shoppingHistRepository.findByUserId(request.getUserId(), pageable);
        } else {
            orderPage = shoppingHistRepository.findAll(pageable);
        }
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShoppingHist order : orderPage.getContent()) {
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
                orderInfo.put("actualPay", order.getActualPay());
                orderInfo.put("orderStatus", order.getOrderStatus());
                orderInfo.put("address", order.getAddress() != null ? order.getAddress() : "无");
                orderInfo.put("createdAt", order.getCreatedAt());
                result.add(orderInfo);
            }
        }
        
        return PageResponse.of(result, request.getPage(), request.getSize(), orderPage.getTotalElements());
    }

    /**
     * 获取用户订单历史（旧版本，保持兼容性）
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
                orderInfo.put("actualPay", order.getActualPay());
                orderInfo.put("orderStatus", order.getOrderStatus());
                orderInfo.put("address", order.getAddress() != null ? order.getAddress() : "无");
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
    
    /**
     * 订单确认页面数据准备
     */
    public OrderConfirmResponse prepareOrderConfirmation(OrderConfirmRequest request) {
        OrderConfirmResponse response = new OrderConfirmResponse();
        
        // 获取用户地址
        List<String> addresses = userAddressRepository.findAddressesByUserId(request.getUserId());
        response.setAddresses(addresses);
        
        // 计算总价
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderConfirmResponse.CartItem> cartItems = new ArrayList<>();
        
        for (Map<String, Integer> item : request.getItems()) {
            Integer bookId = item.get("bookId");
            Integer quantity = item.get("quantity");
            
            Optional<BooksInfo> bookOpt = booksInfoRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                BooksInfo book = bookOpt.get();
                OrderConfirmResponse.CartItem cartItem = new OrderConfirmResponse.CartItem();
                cartItem.setBookId(book.getBookId());
                cartItem.setBookName(book.getBookName());
                cartItem.setAuthor(book.getAuthor());
                cartItem.setPrice(book.getPrice());
                cartItem.setQuantity(quantity);
                cartItem.setSubtotal(book.getPrice().multiply(new BigDecimal(quantity)));
                cartItems.add(cartItem);
                
                totalAmount = totalAmount.add(cartItem.getSubtotal());
            }
        }
        
        response.setItems(cartItems);
        response.setTotalAmount(totalAmount);
        
        // 获取可用优惠券
        List<OrderConfirmResponse.CouponInfo> coupons = couponService.getAvailableCoupons(request.getUserId(), totalAmount);
        response.setCoupons(coupons);
        
        return response;
    }
    
    /**
     * 创建确认后的订单
     */
    @Transactional
    public List<ShoppingHist> createConfirmedOrders(Integer userId, OrderConfirmRequest request) {
        List<ShoppingHist> orders = new ArrayList<>();
        
        // 获取用户信息
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 计算总原价用于优惠券验证
        BigDecimal totalOriginalPrice = BigDecimal.ZERO;
        for (OrderConfirmRequest.OrderItemRequest item : request.getItems()) {
            BooksInfo book = booksInfoRepository.findById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("书籍不存在: " + item.getBookId()));
            totalOriginalPrice = totalOriginalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        // 计算优惠券折扣总额
        BigDecimal totalCouponDiscount = BigDecimal.ZERO;
        if (request.getCouponId() != null && !request.getCouponId().trim().isEmpty()) {
            totalCouponDiscount = couponService.calculateCouponDiscount(request.getCouponId(), totalOriginalPrice);
            if (totalCouponDiscount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("优惠券不可用");
            }
        }
        
        // 创建订单
        for (OrderConfirmRequest.OrderItemRequest item : request.getItems()) {
            BooksInfo book = booksInfoRepository.findById(item.getBookId())
                    .orElseThrow(() -> new RuntimeException("书籍不存在: " + item.getBookId()));
            
            if (book.getStatus() == 0) {
                throw new RuntimeException("书籍已下架: " + book.getBookName());
            }
            
            if (book.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + book.getBookName());
            }
            
            BigDecimal itemOriginalPrice = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            // 按比例分摊优惠券折扣到每个订单项
            BigDecimal itemCouponDiscount = BigDecimal.ZERO;
            if (totalCouponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                itemCouponDiscount = totalCouponDiscount
                    .multiply(itemOriginalPrice)
                    .divide(totalOriginalPrice, 2, BigDecimal.ROUND_HALF_UP);
            }
            
            // 计算该订单项的实付金额
            BigDecimal itemFinalAmount = userLevelService.calculateFinalAmount(
                user.getUserLevel(), itemOriginalPrice, itemCouponDiscount);
            
            ShoppingHist order = new ShoppingHist();
            order.setUserId(userId);
            order.setBookId(item.getBookId());
            order.setQuantity(item.getQuantity());
            order.setUnitPrice(book.getPrice());
            order.setTotalPrice(itemOriginalPrice);
            order.setActualPay(itemFinalAmount);
            order.setOrderStatus(1); // 1-运送中
            order.setAddress(request.getAddress());
            
            ShoppingHist savedOrder = shoppingHistRepository.save(order);
            orders.add(savedOrder);
            
            // 更新书籍销量和库存
            bookService.updateSales(item.getBookId(), item.getQuantity());
        }
        
        // 更新用户消费金额和等级（使用实际支付金额总和）
        BigDecimal totalActualPay = orders.stream()
            .map(ShoppingHist::getActualPay)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        user.setTotalSpending(user.getTotalSpending().add(totalActualPay));
        user.updateUserLevel();
        userInfoRepository.save(user);
        
        // 使用优惠券（如果有）
        if (request.getCouponId() != null && !request.getCouponId().trim().isEmpty()) {
            boolean couponUsed = couponService.useCoupon(userId, request.getCouponId());
            if (!couponUsed) {
                log.warn("优惠券使用失败，但订单已创建: userId={}, couponId={}", userId, request.getCouponId());
            }
        }
        
        log.info("创建确认订单成功: 用户{} 订单数量{} 总金额{}", userId, orders.size(), totalActualPay);
        return orders;
    }
    
    /**
     * 取消订单
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public void cancelOrder(Long orderId) {
        Optional<ShoppingHist> orderOpt = shoppingHistRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("订单不存在");
        }
        
        ShoppingHist order = orderOpt.get();
        if (order.getOrderStatus() == 0) {
            throw new RuntimeException("订单已取消");
        }
        
        if (order.getOrderStatus() == 2) {
            throw new RuntimeException("订单已完成，无法取消");
        }
        
        // 更新订单状态为已取消
        order.setOrderStatus(0);
        shoppingHistRepository.save(order);
        
        // 恢复库存
        bookService.restoreStock(order.getBookId(), order.getQuantity());
        
        log.info("订单取消成功: 订单ID {}", orderId);
    }
}
