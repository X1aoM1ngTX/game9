# Game9 游戏分发平台后端

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/X1aoM1ngTX/game9)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0%2B-red)](https://redis.io/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-blueviolet)](https://baomidou.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](https://opensource.org/licenses/MIT)

## 项目简介

Game9 是一个基于 Spring Boot 3.3.4 构建的游戏分发平台后端系统，提供完整的游戏管理、用户认证、社交功能和内容管理服务。

## 技术栈

- **后端框架**: Spring Boot 3.3.4 (Java 17)
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.7
- **缓存**: Redis
- **文档**: OpenAPI 3.0 (Swagger)
- **安全**: Spring Security Crypto
- **任务调度**: Spring Scheduling
- **文件存储**: 本地存储 + Cloudflare R2

## 核心功能

### 用户管理
- 用户注册与登录
- 用户资料管理
- 密码重置
- 邮箱验证

### 游戏管理
- 游戏信息CRUD
- 游戏库存管理
- 游戏购买功能
- 游戏状态管理
- Steam API集成

### 社交功能
- 好友系统
- 在线状态管理
- 好友分组

### 游戏评价
- 游戏评分系统
- 用户评论功能

### 用户库与愿望单
- 已购游戏管理
- 游戏愿望单

### 内容管理
- 新闻公告管理
- 系统通知

## 项目结构

```
game9/
├── src/main/java/com/xm/game9/
│   ├── controller/          # REST API控制器
│   ├── service/            # 业务逻辑层
│   ├── mapper/             # MyBatis数据访问层
│   ├── model/              # 数据模型
│   │   ├── domain/         # 实体类
│   │   ├── request/        # 请求DTO
│   │   └── vo/             # 视图对象
│   ├── config/             # 配置类
│   ├── exception/          # 异常处理
│   ├── utils/              # 工具类
│   └── job/                # 定时任务
├── src/main/resources/
│   ├── mapper/             # MyBatis映射文件
│   ├── sql/                # 数据库脚本
│   ├── application.yml     # 主配置文件
│   └── application-{env}.yml # 环境配置
└── uploads/                # 文件上传目录
```

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 数据库设置
1. 创建数据库：`xmgame`
2. 执行数据库脚本：`src/main/resources/sql/create_database.sql`
3. 默认数据库连接：root/123456（开发环境）

### 启动应用

#### 开发环境
```bash
./mvnw spring-boot:run
```

#### 生产环境
```bash
./mvnw clean package
java -jar target/game9-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 访问地址
- API服务：http://localhost:8080/api
- API文档：http://localhost:8080/api/swagger-ui.html

## 配置说明

### 环境配置
- `dev`: 开发环境（默认）
- `test`: 测试环境
- `prod`: 生产环境

### 关键配置项
- **数据库**: `spring.datasource`
- **Redis**: `spring.data.redis`
- **文件上传**: `file.upload-dir`
- **邮件服务**: `spring.mail`
- **CORS**: `app.cors.allowed-origins`

## API规范

### 响应格式
所有API响应统一使用 `BaseResponse` 包装：
```json
{
  "code": 0,
  "data": {},
  "message": "success"
}
```

### 错误码
标准错误码定义在 `ErrorCode` 类中

## 部署

### Docker部署
```bash
# 构建镜像
docker build -t game9 .

# 运行容器
docker run -p 8080:8080 game9
```

### 生产环境
- 使用Cloudflare R2存储文件
- 配置环境变量
- 启用Redis缓存

## 开发指南

### 代码规范
- 使用Lombok简化代码
- 统一异常处理
- RESTful API设计
- 参数验证

### 测试
```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=UserServiceTest
```

## 许可证

本项目仅供学习和研究使用。