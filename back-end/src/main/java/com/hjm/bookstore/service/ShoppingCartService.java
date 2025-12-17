package com.hjm.bookstore.service;

import com.hjm.bookstore.entity.BooksInfo;
import com.hjm.bookstore.entity.ShoppingCart;
import com.hjm.bookstore.repository.BooksInfoRepository;
import com.hjm.bookstore.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 购物车服务类
 */
@Service
@Slf4j
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private BooksInfoRepository booksInfoRepository;

    /**
     * 获取用户购物车列表（包含书籍详情）
     */
    public List<Map<String, Object>> getCartItems(Integer userId) {
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ShoppingCart item : cartItems) {
            Optional<BooksInfo> bookOpt = booksInfoRepository.findById(item.getBookId());
            if (bookOpt.isPresent()) {
                BooksInfo book = bookOpt.get();
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("cartId", item.getCartId());
                cartItem.put("bookId", book.getBookId());
                cartItem.put("bookName", book.getBookName());
                cartItem.put("author", book.getAuthor());
                cartItem.put("price", book.getPrice());
                cartItem.put("bookImage", book.getBookImage());
                cartItem.put("quantity", item.getQuantity());
                cartItem.put("stock", book.getStock());
                result.add(cartItem);
            }
        }

        return result;
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public ShoppingCart addToCart(Integer userId, Integer bookId, Integer quantity) {
        // 检查书籍是否存在
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

        // 检查购物车中是否已存在该书籍
        Optional<ShoppingCart> existingCart = shoppingCartRepository.findByUserIdAndBookId(userId, bookId);
        
        if (existingCart.isPresent()) {
            // 更新数量
            ShoppingCart cart = existingCart.get();
            int newQuantity = cart.getQuantity() + quantity;
            if (newQuantity > book.getStock()) {
                throw new RuntimeException("库存不足");
            }
            cart.setQuantity(newQuantity);
            return shoppingCartRepository.save(cart);
        } else {
            // 新增购物车项
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setQuantity(quantity);
            return shoppingCartRepository.save(cart);
        }
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    @Retryable(value = {ObjectOptimisticLockingFailureException.class, RuntimeException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public ShoppingCart updateQuantity(Integer cartId, Integer quantity) {
        Optional<ShoppingCart> cartOpt = shoppingCartRepository.findById(cartId);
        if (cartOpt.isEmpty()) {
            throw new RuntimeException("购物车项不存在");
        }

        ShoppingCart cart = cartOpt.get();
        Optional<BooksInfo> bookOpt = booksInfoRepository.findById(cart.getBookId());
        
        if (bookOpt.isPresent()) {
            BooksInfo book = bookOpt.get();
            if (quantity > book.getStock()) {
                throw new RuntimeException("库存不足");
            }
        }

        cart.setQuantity(quantity);
        return shoppingCartRepository.save(cart);
    }

    /**
     * 删除购物车项
     */
    @Transactional
    public void removeCartItem(Integer cartId) {
        shoppingCartRepository.deleteById(cartId);
        log.info("删除购物车项: {}", cartId);
    }

    /**
     * 清空用户购物车
     */
    @Transactional
    public void clearCart(Integer userId) {
        shoppingCartRepository.deleteByUserId(userId);
        log.info("清空用户购物车: {}", userId);
    }
}