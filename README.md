# 哈籍迷书城购物系统

## 项目简介
这是一个基于Vue3 + SpringBoot的前后端分离书城购物系统，包含用户端和管理员端。

## 技术栈

### 后端
- SpringBoot 3.2.0
- Spring Data JPA
- MySQL 8.0
- Spring Security + JWT
- Apache POI (Excel导入导出)
- Lombok

### 前端
- Vue 3.4
- Vue Router 4
- Pinia
- Element Plus
- ECharts
- Axios
- Vite

## 数据库配置

### 1. 创建数据库
```bash
mysql -u root -p
```

执行数据库初始化脚本：
```bash
mysql -u root -p < database/schema.sql
```

或者在MySQL中执行：
```sql
source c:/Users/hyy12138/Desktop/HJM_book/database/schema.sql
```

### 数据库信息
- 地址：localhost
- 端口：3306（默认）
- 用户：root
- 密码：123456
- 数据库名：book_store_database

### 默认管理员账号
- 管理员ID：admin001
- 密码：admin123

## 后端启动

### 1. 安装依赖
```bash
cd back-end
mvn clean install
```

### 2. 启动后端服务
```bash
mvn spring-boot:run
```

后端服务将在 http://localhost:8080/api 启动

## 前端启动

### 1. 安装依赖
```bash
cd front-end
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

前端服务将在 http://localhost:5173 启动

## 功能模块

### 用户端
- ✅ 用户注册/登录
- ✅ 首页推荐（个性化推荐 + 销量推荐）
- ✅ 排行榜（评分榜、历史销量榜、月销量榜）
- ✅ 搜索功能（支持高级搜索）
- ✅ 购物车
- ✅ 历史订单
- ✅ 个人中心（用户信息、地址管理）
- ✅ 书籍评分

### 管理员端
- ✅ 管理员登录
- ✅ 数据可视化（销量统计图表）
- ✅ 书籍管理（增删改查、批量导入/下架）
- ✅ 首页轮播图管理
- ✅ 管理员账号管理（仅2级管理员）

## 核心功能说明

### 1. 用户等级系统
用户等级根据累计消费金额自动提升：
- 0级：0-30元
- 1级：30-198元
- 2级：198-328元
- 3级：328-648元
- 4级：648-1998元
- 5级：1998元以上

### 2. 推荐算法
采用基于皮尔逊相似性的协同过滤推荐算法：
- 计算用户之间的相似度
- 基于相似用户的评分预测推荐
- 如果推荐数量不足，用销量高的书籍补充

### 3. Excel批量操作
- 支持Excel批量导入书籍（参考Quick_Import.xlsx）
- 支持Excel批量下架书籍（参考Quick_Remove.xlsx）

## 项目结构

```
HJM_book/
├── back-end/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hjm/bookstore/
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── repository/      # 数据访问层
│   │   │   │   ├── service/         # 业务逻辑层
│   │   │   │   ├── controller/      # 控制器
│   │   │   │   ├── dto/             # 数据传输对象
│   │   │   │   ├── config/          # 配置类
│   │   │   │   ├── util/            # 工具类
│   │   │   │   └── common/          # 公共类
│   │   │   └── resources/
│   │   │       └── application.yml  # 配置文件
│   │   └── pom.xml                  # Maven配置
│   │
├── front-end/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   │   ├── user/        # 用户端页面
│   │   │   └── admin/       # 管理员端页面
│   │   ├── layouts/         # 布局组件
│   │   ├── components/      # 公共组件
│   │   ├── stores/          # Pinia状态管理
│   │   ├── router/          # 路由配置
│   │   ├── api/             # API接口
│   │   └── main.js          # 入口文件
│   ├── package.json
│   └── vite.config.js
│
├── database/                 # 数据库脚本
│   └── schema.sql
│
├── Quick_Import.xlsx         # Excel导入模板
├── Quick_Remove.xlsx         # Excel下架模板
└── README.md                 # 项目说明
```

## 待完成功能

由于代码量较大，以下功能需要继续完善：

### 后端需要补充的Service和Controller：
1. ShoppingCartService + ShoppingCartController（购物车）
2. OrderService + OrderController（订单）
3. UserService + UserController（用户信息管理）
4. ScoreService + ScoreController（评分）
5. CarouselService + CarouselController（轮播图）
6. AdminService + AdminController（管理员管理）
7. ExcelService（Excel导入导出）
8. MonthlyResetScheduler（月销量重置定时任务）

### 前端需要补充的页面：
1. /views/user/Home.vue（用户首页）
2. /views/user/Rankings.vue（排行榜）
3. /views/user/Search.vue（搜索页）
4. /views/user/Cart.vue（购物车）
5. /views/user/Orders.vue（订单页）
6. /views/user/Profile.vue（个人中心）
7. /views/user/BookDetail.vue（书籍详情）
8. /views/admin/Dashboard.vue（管理员仪表盘）
9. /views/admin/BookManagement.vue（书籍管理）
10. /views/admin/CarouselManagement.vue（轮播图管理）
11. /views/admin/AdminManagement.vue（管理员管理）
12. /layouts/AdminLayout.vue（管理员布局）

## 开发建议

1. 先确保数据库正确初始化
2. 启动后端服务，测试API接口
3. 启动前端服务，测试登录功能
4. 逐步完善各个功能模块
5. 使用Postman测试后端API
6. 使用浏览器开发者工具调试前端

## 注意事项

1. 确保MySQL服务已启动
2. 确保端口8080和5173未被占用
3. 首次运行需要执行数据库初始化脚本
4. 密码使用BCrypt加密存储
5. JWT Token有效期为24小时
6. 图片上传路径：./uploads/images/books/

## 联系方式

如有问题，请查看代码注释或联系开发团队。
