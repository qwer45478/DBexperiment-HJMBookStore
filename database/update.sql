-- 订单系统及结算机制数据库更新脚本
-- 执行时间：2025-12-16

-- 1. 更新shopping_hist表结构
-- 添加地址列，修改order_status列为3选一（0-已取消，1-运送中，2-已完成）
ALTER TABLE shopping_hist 
ADD COLUMN address VARCHAR(500) COMMENT '用户选择的地址' AFTER total_price,
MODIFY COLUMN order_status TINYINT DEFAULT 1 COMMENT '订单状态（0-已取消，1-运送中，2-已完成）';

-- 2. 创建coupon_type表（优惠券类型表）
CREATE TABLE coupon_type (
    coupon_id VARCHAR(10) NOT NULL COMMENT '优惠券ID（主键，第一类优惠券ID为01，之后自动递增）',
    coupon_name VARCHAR(16) NOT NULL COMMENT '优惠券名（可重复，长度不多于16字节）',
    discount_amount INT NOT NULL COMMENT '优惠券额度（不可为空，不可为负）',
    min_amount INT NOT NULL DEFAULT 0 COMMENT '优惠券限制（满多少元可用，不可为空，不可为负，0为无限制）',
    valid_days INT NOT NULL DEFAULT 0 COMMENT '优惠券时效（多少天内可用，不可为空，不可为负，0为无时效限制）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (coupon_id),
    CONSTRAINT chk_discount_amount CHECK (discount_amount >= 0),
    CONSTRAINT chk_min_amount CHECK (min_amount >= 0),
    CONSTRAINT chk_valid_days CHECK (valid_days >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券类型表';

-- 3. 创建user_coup表（用户优惠券表）
CREATE TABLE user_coup (
    user_id INT NOT NULL COMMENT '用户ID（主键，外键）',
    coupon_id VARCHAR(10) NOT NULL COMMENT '优惠券ID（主键，外键）',
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '优惠券获取时间',
    PRIMARY KEY (user_id, coupon_id),
    CONSTRAINT fk_user_coup_user FOREIGN KEY (user_id) REFERENCES user_info (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_coup_coupon FOREIGN KEY (coupon_id) REFERENCES coupon_type (coupon_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';

-- 4. 插入默认优惠券数据（ID为01的优惠券，供5级用户每日领取）
INSERT INTO coupon_type (coupon_id, coupon_name, discount_amount, min_amount, valid_days) 
VALUES ('01', '每日优惠券', 5, 0, 7);

-- 5. 更新现有订单数据，将order_status从原来的1-已完成，0-已取消映射到新的状态
-- 由于原状态：1-已完成，0-已取消
-- 新状态：0-已取消，1-运送中，2-已完成
UPDATE shopping_hist SET order_status = 2 WHERE order_status = 1; -- 已完成状态从1改为2
-- 已取消状态保持为0不变

-- 6. 为性能优化添加索引
CREATE INDEX idx_shopping_hist_user_status ON shopping_hist(user_id, order_status);
CREATE INDEX idx_user_coup_user ON user_coup(user_id);
CREATE INDEX idx_user_coup_obtained ON user_coup(obtained_at);
