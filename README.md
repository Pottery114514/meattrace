# MeatTrace · 肉类食品全链路溯源平台

> 面向肉类供应链的四段式批次溯源系统：正向全链路查询、反向一键召回、企业级数据隔离、消费者扫码验真。

<p>
  <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white">
  <img alt="Redis" src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white">
  <img alt="status" src="https://img.shields.io/badge/status-Phase%200%20in%20progress-yellow">
</p>

---

## 背景

依据《食品安全法》对食品生产经营者建立进货查验与追溯记录的要求，肉类从养殖到零售需保留完整流转凭证。现实中一次质量事件的处置往往不是"查到来源"这么简单，而是**反向定位所有受影响产品并召回**。

本系统将养殖、屠宰、批发、零售四个环节抽象为可串联的批次实体，使任一终端产品可反查源头，也使任一上游批次可正向推导出全部下游产品。

## 核心设计

### 1. 四段式批次链

四张批次表通过「上游批次主键」串联，而非业务批次码：

```
farm_product_batch ──→ slaughter_product_batch ──→ wholesaler_product_batch ──→ retailer_product_batch
        │                        │                            │                          │
        └────────────────────────┴────────────────────────────┴──────────────────────────┘
                                          node_enterprise（ent_type 1~4）
```

选主键而非业务码的原因：主键短且定长、索引效率高、业务码可能被修改、外键可约束完整性。

### 2. 反向召回

给定任一上游批次，递归推导全部下游批次并批量标记召回。实现方案在 Phase 6 落地，两种候选（MySQL 8 递归 CTE / 批次关系表）的取舍记录见 [`docs/DECISIONS.md`](docs/DECISIONS.md)。

### 3. 行级数据权限

企业用户只能操作自身批次。通过自定义 `@DataScope` 注解 + AOP，从 JWT 中取 `entId` 并**强制覆盖**查询条件——是覆盖而非校验，防止越权传参绕过。

## 技术栈

| 分层 | 选型 |
| --- | --- |
| 运行时 | Java 21 |
| 框架 | Spring Boot 3.5 |
| 持久层 | MyBatis + XML 手写 SQL + PageHelper |
| 数据库 | MySQL 8（递归 CTE） |
| 缓存 | Redis 7 |
| 安全 | Spring Security 6 + JWT（Phase 7 接入） |
| 接口文档 | Knife4j 4.5 |
| 前端 | Vue 3 + Vite + Element Plus + ECharts |
| 部署 | Docker Compose + Nginx |

## 工程结构

```
meattrace/
├── src/main/java/com/meattrace/
│   ├── MeatTraceApplication.java
│   ├── common/
│   │   ├── api/           # R<T> 统一响应、PageResult<T> 分页结构
│   │   └── exception/     # ErrorCode、BizException、GlobalExceptionHandler
│   ├── config/            # Knife4j、WebMvc(CORS)
│   ├── controller/        # REST 接口层
│   ├── service/           # 业务逻辑层
│   ├── mapper/            # MyBatis 接口
│   ├── entity/            # 数据库实体
│   ├── dto/               # 入参对象
│   ├── vo/                # 出参对象
│   ├── security/          # 认证鉴权（Phase 7）
│   ├── aspect/            # 数据权限、操作日志（Phase 7/8）
│   └── util/
├── src/main/resources/
│   ├── application.yml    # 公共配置
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── mapper/            # MyBatis XML
└── docs/
    └── DECISIONS.md       # 设计决策记录
```

## 快速启动

**前置**：JDK 21、Maven 3.9+、MySQL 8、Redis 7。

```bash
# 1. 建库
mysql -uroot -p -e "CREATE DATABASE meattrace DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 启动（默认 dev profile，读取环境变量 DB_* / REDIS_*，缺省值见 application-dev.yml）
mvn spring-boot:run
```

访问：

| 地址 | 说明 |
| --- | --- |
| http://localhost:8080/api/system/ping | 健康检查 |
| http://localhost:8080/api/doc.html | 接口文档 |

> 数据库与 Redis 的账号密码通过环境变量注入（`DB_USERNAME` / `DB_PASSWORD` / `REDIS_HOST` 等），不要把真实密码写进配置文件。

## 接口响应约定

```json
{ "code": 200, "msg": "success", "data": {}, "timestamp": 1761638400000 }
```

| code 段 | 含义 | HTTP 状态码 |
| --- | --- | --- |
| 200 | 成功 | 200 |
| 400 / 401 / 403 / 404 / 405 / 409 / 500 | 协议层错误 | 与 code 一致 |
| 1xxx | 认证与授权 | 200 |
| 2xxx | 通用数据操作 | 200 |
| 3xxx | 业务规则（批次链 / 溯源 / 召回） | 200 |

业务错误不污染 HTTP 语义，也不影响接口成功率统计；协议层错误让网关与监控能正确识别。

## 开发进度

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| Phase 0 | 工程地基（统一响应、全局异常、接口文档） | 进行中 |
| Phase 1 | 数据模型（11 张表 + 索引 + 造数脚本） | 待开始 |
| Phase 2 | 区域模块（省市 CRUD + 联动） | 待开始 |
| Phase 3 | 企业模块（分组校验 + BCrypt） | 待开始 |
| Phase 4 | 批次链（四环节录入 + 事务 + 状态机） | 待开始 |
| Phase 5 | 溯源查询 + 二维码 + 消费者 H5 | 待开始 |
| Phase 6 | 一键召回（反向链路追溯） | 待开始 |
| Phase 7 | 鉴权与行级数据权限 | 待开始 |
| Phase 8 | 缓存与性能优化 | 待开始 |
| Phase 9 | 数据大屏 | 待开始 |
| Phase 10 | 工程化（测试、Docker、CI、文档） | 待开始 |

## 许可

MIT
