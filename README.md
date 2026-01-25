# base-admin

个人使用的管理系统基座：Spring Boot 3 + Vue 3。

## 1) 后端（backend）

### 依赖

- JDK 17
- MySQL 8.0

### 初始化数据库（手工导入）

本仓库提供：

- 表结构：`.kiro/specs/admin-base/schema.sql`
- 种子数据：`.kiro/specs/admin-base/seed.sql`（包含默认账号 `admin/admin123`）

示例（用你自己的连接信息替换）：

```bash
mysql -h <MYSQL_HOST> -P <MYSQL_PORT> -u <MYSQL_USER> -p
```

进入 MySQL 后：

```sql
CREATE DATABASE admin_base DEFAULT CHARACTER SET utf8mb4;
USE admin_base;
SOURCE <PATH_TO_REPO>/.kiro/specs/admin-base/schema.sql;
SOURCE <PATH_TO_REPO>/.kiro/specs/admin-base/seed.sql;
```

### 配置环境变量

后端通过环境变量读取 MySQL 连接信息（避免提交明文密码）：

- `MYSQL_HOST`（默认 `172.25.48.1`）
- `MYSQL_PORT`（默认 `3307`）
- `MYSQL_DB`（默认 `admin_base`）
- `MYSQL_USER`（默认 `root`）
- `MYSQL_PASSWORD`（无默认值）

### 运行

```bash
cd backend
mvn test
mvn spring-boot:run
```

## 2) 前端（frontend）

### 依赖

- Node.js（已在本机验证过 Node 22）

### 运行

```bash
cd frontend
npm install
npm run dev
```

前端 dev 代理已配置：`/api` → `http://localhost:8080`。
