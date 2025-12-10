-- 哈籍迷书城购物系统数据库
-- Database: book_store_database

DROP DATABASE IF EXISTS book_store_database;
CREATE DATABASE book_store_database DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_store_database;

-- 用户信息表
CREATE TABLE user_info (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，从100000开始',
    username VARCHAR(48) NOT NULL COMMENT '用户名，最多16个中文字符',
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '用户电话号码',
    password VARCHAR(64) NOT NULL COMMENT '用户密码（加密存储）',
    user_level TINYINT DEFAULT 0 COMMENT '用户等级（0-5级）',
    total_spending DECIMAL(10, 2) DEFAULT 0.00 COMMENT '累计消费金额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) AUTO_INCREMENT = 100000 COMMENT '用户信息表';

-- 书籍信息表
CREATE TABLE books_info (
    book_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '书籍ID',
    book_name VARCHAR(200) NOT NULL COMMENT '书籍名',
    category CHAR(1) NOT NULL COMMENT '书本分类（A-Z）',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    book_image VARCHAR(500) COMMENT '书籍图片路径',
    description TEXT COMMENT '书本简介',
    publisher VARCHAR(100) NOT NULL COMMENT '出版社名字',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    rating DECIMAL(3, 1) DEFAULT 0.0 COMMENT '评分（0-10）',
    stock INT DEFAULT 0 COMMENT '库存量',
    sales INT DEFAULT 0 COMMENT '总销量',
    monthly_sales INT DEFAULT 0 COMMENT '月销量',
    status TINYINT DEFAULT 1 COMMENT '货物状态（1-上架，0-下架）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_author (author),
    INDEX idx_book_name (book_name),
    INDEX idx_sales (sales DESC),
    INDEX idx_rating (rating DESC),
    INDEX idx_monthly_sales (monthly_sales DESC)
) COMMENT '书籍信息表';

-- 用户评分表
CREATE TABLE user_score (
    user_id INT NOT NULL COMMENT '用户ID',
    book_id INT NOT NULL COMMENT '书籍ID',
    score TINYINT NOT NULL COMMENT '评分（1-10）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
    PRIMARY KEY (user_id, book_id),
    FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books_info(book_id) ON DELETE CASCADE
) COMMENT '用户评分表';

-- 历史订单表
CREATE TABLE shopping_hist (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    user_id INT NOT NULL COMMENT '用户ID',
    book_id INT NOT NULL COMMENT '书籍ID',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    total_price DECIMAL(10, 2) NOT NULL COMMENT '总价',
    order_status TINYINT DEFAULT 1 COMMENT '订单状态（1-已完成，0-已取消）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books_info(book_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at DESC)
) COMMENT '历史订单表';

-- 用户地址表
CREATE TABLE user_add (
    address_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id INT NOT NULL COMMENT '用户ID',
    address VARCHAR(500) NOT NULL COMMENT '地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址（1-是，0-否）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) COMMENT '用户地址表';

-- 管理员信息表
CREATE TABLE admin_info (
    admin_id VARCHAR(8) PRIMARY KEY COMMENT '管理员ID（8位字母数字组合）',
    admin_password VARCHAR(64) NOT NULL COMMENT '管理员密码（加密存储）',
    admin_level TINYINT NOT NULL COMMENT '管理员等级（1或2）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '管理员信息表';

-- 购物车表
CREATE TABLE shopping_cart (
    cart_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    user_id INT NOT NULL COMMENT '用户ID',
    book_id INT NOT NULL COMMENT '书籍ID',
    quantity INT DEFAULT 1 COMMENT '数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books_info(book_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book (user_id, book_id)
) COMMENT '购物车表';

-- 首页轮播图表
CREATE TABLE carousel_items (
    carousel_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    book_id INT NOT NULL COMMENT '书籍ID',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (book_id) REFERENCES books_info(book_id) ON DELETE CASCADE
) COMMENT '首页轮播图表';

-- 插入默认管理员账号
INSERT INTO admin_info (admin_id, admin_password, admin_level) VALUES 
('admin001', '$2a$10$5ixNE5dQNpq9vQhLqWKOJeEH0wKGGKZ3jfMxVfJQHqLqGqXqJ5K5O', 2); -- 密码: 123123

-- 插入测试数据
INSERT INTO books_info (book_name, category, author, book_image, description, publisher, price, rating, stock, sales, monthly_sales, status) VALUES
('活着', 'I', '余华', '/images/books/huozhe.jpg', '一个关于生命韧性的故事', '作家出版社', 28.00, 9.5, 100, 1520, 85, 1),
('三体', 'I', '刘慈欣', '/images/books/santi.jpg', '科幻巨作，探索宇宙文明', '重庆出版社', 23.00, 9.3, 150, 2340, 120, 1),
('百年孤独', 'I', '加西亚·马尔克斯', '/images/books/bainian.jpg', '魔幻现实主义经典', '南海出版公司', 39.50, 9.2, 80, 980, 45, 1),
('平凡的世界', 'I', '路遥', '/images/books/pingfan.jpg', '普通人的奋斗史诗', '北京十月文艺出版社', 79.00, 9.4, 60, 1650, 92, 1),
('人类简史', 'K', '尤瓦尔·赫拉利', '/images/books/renlei.jpg', '从动物到上帝的人类发展史', '中信出版社', 68.00, 9.1, 120, 1120, 78, 1),
('Python编程：从入门到实践', 'T', 'Eric Matthes', '/images/books/python.jpg', 'Python编程入门经典教程', '人民邮电出版社', 89.00, 9.0, 200, 2100, 156, 1),
('算法导论', 'T', 'Thomas H. Cormen', '/images/books/suanfa.jpg', '算法领域的权威教材', '机械工业出版社', 128.00, 9.6, 50, 560, 32, 1),
('红楼梦', 'I', '曹雪芹', '/images/books/honglou.jpg', '中国古典文学巅峰之作', '人民文学出版社', 59.00, 9.7, 90, 1890, 67, 1);
