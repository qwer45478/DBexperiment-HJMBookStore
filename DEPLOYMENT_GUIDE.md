# 哈籍迷书城 - 部署和启动指南

## 快速开始

### 第一步：数据库初始化

1. 确保MySQL已安装并运行
2. 打开命令行，登录MySQL：
```bash
mysql -u root -p
# 输入密码：123456
```

3. 执行数据库脚本：
```sql
source C:/Users/hyy12138/Desktop/HJM_book/database/schema.sql
```

或者直接在MySQL Workbench中打开并执行 `database/schema.sql` 文件

4. 验证数据库创建成功：
```sql
USE book_store_database;
SHOW TABLES;
```

应该看到以下表：
- admin_info
- books_info
- carousel_items
- shopping_cart
- shopping_hist
- user_add
- user_info
- user_score

### 第二步：启动后端服务

1. 打开命令行，进入后端目录：
```bash
cd C:\Users\hyy12138\Desktop\HJM_book\back-end
```

2. 安装Maven依赖（首次运行）：
```bash
mvn clean install
```

3. 启动SpringBoot应用：
```bash
mvn spring-boot:run
```

4. 等待启动完成，看到类似以下日志：
```
Started BookstoreApplication in X.XXX seconds
```

5. 测试后端是否正常运行：
访问 http://localhost:8080/api

### 第三步：启动前端服务

1. 打开新的命令行窗口，进入前端目录：
```bash
cd C:\Users\hyy12138\Desktop\HJM_book\front-end
```

2. 安装npm依赖（首次运行）：
```bash
npm install
```

如果npm安装速度慢，可以使用国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 看到以下信息表示启动成功：
```
VITE v5.x.x  ready in XXX ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

5. 打开浏览器访问：http://localhost:5173

### 第四步：登录测试

#### 测试管理员登录
- 管理员ID：`admin001`
- 密码：`admin123`

#### 测试用户注册
1. 点击"用户注册"标签
2. 填写信息：
   - 用户名：任意（不超过16个字符）
   - 手机号：11位手机号（如：13800138000）
   - 密码：字母+数字组合，6-24位（如：abc123）
3. 点击注册

## 常见问题

### 1. 数据库连接失败
**错误信息**：`Communications link failure`

**解决方案**：
- 检查MySQL服务是否启动
- 检查数据库配置（`back-end/src/main/resources/application.yml`）
- 确认用户名密码正确（root/123456）

### 2. 端口被占用
**错误信息**：`Port 8080 is already in use` 或 `Port 5173 is already in use`

**解决方案**：
- 后端端口8080被占用：
  修改 `application.yml` 中的 `server.port`
  
- 前端端口5173被占用：
  修改 `vite.config.js` 中的 `server.port`

### 3. Maven依赖下载失败
**解决方案**：
配置Maven使用阿里云镜像，编辑 `~/.m2/settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### 4. npm安装失败
**解决方案**：
```bash
# 清除缓存
npm cache clean --force

# 使用国内镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
npm install
```

### 5. 跨域问题
如果出现跨域错误，检查：
- 后端 `SecurityConfig.java` 中的CORS配置
- 前端 `vite.config.js` 中的proxy配置

## 开发工具推荐

### 后端开发
- **IDE**: IntelliJ IDEA（推荐）或 Eclipse
- **数据库管理**: MySQL Workbench 或 Navicat
- **API测试**: Postman 或 Apifox

### 前端开发
- **IDE**: VS Code（推荐）
- **浏览器**: Chrome（带开发者工具）
- **Vue DevTools**: Chrome扩展

## 项目结构说明

### 后端关键文件
```
back-end/
├── pom.xml                          # Maven配置
├── src/main/resources/
│   └── application.yml              # 应用配置（数据库、端口等）
└── src/main/java/com/hjm/bookstore/
    ├── BookstoreApplication.java    # 启动类
    ├── entity/                      # 数据库实体
    ├── repository/                  # 数据访问层
    ├── service/                     # 业务逻辑层
    ├── controller/                  # API控制器
    └── config/                      # 配置类
```

### 前端关键文件
```
front-end/
├── package.json                     # npm配置
├── vite.config.js                   # Vite配置
├── index.html                       # HTML入口
└── src/
    ├── main.js                      # 应用入口
    ├── App.vue                      # 根组件
    ├── router/index.js              # 路由配置
    ├── stores/user.js               # 状态管理
    ├── api/index.js                 # API接口
    ├── views/                       # 页面组件
    └── layouts/                     # 布局组件
```

## 下一步开发

当前已完成的功能：
- ✅ 数据库设计和初始化
- ✅ 后端基础架构（Entity、Repository、部分Service和Controller）
- ✅ 前端基础架构（路由、状态管理、API封装）
- ✅ 登录注册功能
- ✅ 用户端布局
- ✅ 首页和排行榜页面

待完成的功能：
- ⏳ 后端剩余Service和Controller（购物车、订单、评分等）
- ⏳ 前端剩余页面（搜索、购物车、订单、个人中心等）
- ⏳ 管理员端完整功能
- ⏳ Excel导入导出功能
- ⏳ 图片上传功能
- ⏳ 月销量重置定时任务

## 技术支持

如遇到问题：
1. 查看控制台日志
2. 检查浏览器开发者工具的Network和Console
3. 参考README.md中的项目说明
4. 查看代码注释

## 生产环境部署

### 后端打包
```bash
cd back-end
mvn clean package
```
生成的jar文件在 `target/bookstore-1.0.0.jar`

运行：
```bash
java -jar target/bookstore-1.0.0.jar
```

### 前端打包
```bash
cd front-end
npm run build
```
生成的文件在 `dist/` 目录，可以部署到Nginx或其他Web服务器

### 环境变量配置
生产环境需要修改：
- 数据库连接信息
- JWT密钥
- 文件上传路径
- 跨域配置
