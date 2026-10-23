# 智选AI商城（ssm-ai-mall）

基于 **SSM（Spring + SpringMVC + MyBatis）** 的小型电商系统，含完整前台商城 + 管理后台，并结合 AI 实现了**个性化推荐引擎**与**AI 智能客服**。

> 已在 JDK 8 + MySQL 8.4 环境完成全链路验证：注册 → 登录 → 浏览 → 加购 → 下单（扣库存）→ 支付 → 发货 → 收货 → 评价数据回流推荐，管理端商品/分类/订单/用户管理与仪表盘均可用。

## 功能总览

| 端 | 模块 |
|----|------|
| 前台商城 | 首页（AI猜你喜欢/热卖榜）、分类导航、关键词搜索+排序+分页、商品详情（买了又买）、购物车（增删改/勾选结算）、收货地址管理、下单（购物车结算/立即购买）、模拟支付、我的订单（支付/取消/确认收货）、AI客服对话页 |
| 管理后台 | 仪表盘（今日订单/销售额、近7天趋势图、分类销售占比）、商品管理（CRUD/上下架/图片上传/搜索分页）、分类管理、订单管理（搜索/发货/详情）、用户管理（搜索/禁用/角色） |
| AI 能力 | 基于共现的协同过滤推荐（ItemCF 近似）、AI 智能客服（配置 Key 后走大模型；未配置时商品名匹配 + 规则引擎兜底）、用户行为采集 |

## 快速开始

### 0. 环境要求
- JDK 8+、Maven 3.6+
- MySQL 8.x（5.7 亦可）

### 1. 初始化数据库
```bash
mysql -uroot -p < db/init.sql
```
脚本会创建 `ai_mall` 库、10 张表并写入示例数据（6 分类、28 商品、5 用户、行为日志、历史订单）。

### 2. 配置数据库连接
复制 `src/main/resources/jdbc.properties.example` 为同目录下的 `jdbc.properties`，改成你的 MySQL 地址与账号密码：
```properties
jdbc.url=jdbc:mysql://127.0.0.1:3306/ai_mall?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的密码
```

### 3. 一键运行
```bash
mvn jetty:run
```
浏览器打开 <http://localhost:18080/>

- 演示账号：`user1 / 123456`
- 管理员：`admin / 123456`（登录后访问 `/admin`）
- 打包部署：`mvn package`，将 `target/ssm-ai-mall.war` 放入 Tomcat 9 即可（Tomcat 10+ 使用 jakarta 包名，不兼容，勿用）。

### 4.（可选）接入大模型客服
编辑 `src/main/resources/app.properties`：
```properties
app.ai.enabled=true
app.ai.base-url=https://open.bigmodel.cn/api/paas/v4
app.ai.api-key=你的Key
app.ai.model=glm-4-flash
```
兼容所有 OpenAI 格式接口（DeepSeek / Kimi / 智谱 / 通义）。**不配置也能跑**：客服自动降级为"商品库检索 + 规则引擎"，保证演示不翻车。

## 技术亮点（简历/答辩重点）

### 1. 订单事务与防超卖
`OrderServiceImpl` 使用 Spring 声明式事务（`@Transactional(rollbackFor = Exception.class)`）：
- 下单流程：校验地址 → 锁定购物车勾选项 → 生成订单/明细 → **条件更新扣库存** → 清空购物车，任一环节失败整体回滚；
- 防超卖 SQL：`UPDATE product SET stock = stock - #{qty} WHERE id = #{id} AND stock >= #{qty}`，影响行数为 0 即库存不足，抛异常触发回滚；
- 订单号使用「时间戳 + 随机数」，状态流转用条件更新（`WHERE status = 期望状态`）保证幂等：支付回调重复通知不会二次生效，取消订单自动归还库存。

### 2. AI 个性化推荐（基于共现的 ItemCF 近似）
- 行为采集：浏览(1分)/加购(3分)/购买(5分) 实时写入 `behavior_log`；
- 推荐算法：一条自连接聚合 SQL 求"商品共现矩阵"（同一用户交互过的商品对），按加权协同分排序，过滤已交互与下架商品；当前是未归一化的共现分，热门商品会偏置，生产建议改余弦/条件概率并加缓存；
- 冷启动：新用户无行为时自动降级为"近7天热门商品"，结果不足用热卖补齐；
- 商品详情页"买了又买"复用同一共现查询。全部纯 Java+SQL 实现，零外部依赖，可平滑升级为 Redis 缓存相似度矩阵。

### 3. AI 智能客服（配置 Key 后启用；离线走匹配 + 规则）
回复优先级：**商品库实时检索**（命中商品名直接返回真实价格/库存；这是名称匹配，不是向量语义 RAG）→ **大模型**（`app.ai.enabled=true` 且配置 Key，上下文注入热门商品清单）→ **规则引擎**（关键词意图识别）→ **通用话术**。每条对话与回复来源（llm/db/rule）入库 `chat_message`。默认未开启大模型，保证演示不依赖外部服务。

### 4. SSM 工程实践
- MyBatis 动态 SQL：商品多条件查询（`<where>/<if>/<choose>` 排序白名单防注入）、批量插入明细（`<foreach>`）、购物车 `ON DUPLICATE KEY UPDATE` 防重复加购；
- PageHelper 物理分页；订单列表采用「主表分页 + 批量查明细回填」避免 JOIN 嵌套集合分页错位与 N+1 查询；
- 全局异常处理（`@RestControllerAdvice`）统一 JSON 返回；登录/角色双拦截器（页面跳转与 API 401/403 分流）；文件上传白名单校验；参数校验（Hibernate Validator + 前端双重校验）。

## 项目结构
```
ssm-ai-mall
├── db/init.sql                  # 建库建表 + 示例数据
└── src/main
    ├── java/com/shop
    │   ├── ai/                  # RecommenderService(ItemCF)、ChatService、OpenAiCompatClient、RuleChatClient
    │   ├── common/              # Result、PageResult、BizException、全局异常、上传工具
    │   ├── dao/                 # 10 个 Mapper 接口
    │   ├── entity/              # 9 个实体
    │   ├── interceptor/         # 登录拦截器、管理员拦截器
    │   ├── service/             # 用户/分类/商品/购物车/订单/行为/统计
    │   └── web/                 # 前台 + /api 控制器、web/admin 管理后台控制器
    ├── resources
    │   ├── spring/              # applicationContext.xml（数据源/事务/MyBatis）、spring-mvc.xml
    │   ├── mapper/              # 10 个 Mapper XML（动态SQL）
    │   ├── jdbc.properties、app.properties（AI配置）、logback.xml
    └── webapp
        ├── WEB-INF/jsp/         # front/ 前台页面、admin/ 后台页面
        └── static/              # css、js（本地 Chart.js）、img（商品占位图）
```

## 常见问题
- **端口冲突**：Jetty 默认 18080，在 `pom.xml` 的 `jetty-maven-plugin` 配置里修改；
- **图片上传**：保存在应用运行目录 `upload/` 下（演示用），生产建议上传 OSS/MinIO；
- **验证码/Redis/RabbitMQ**：定位为进阶扩展方向——Redis 可缓存热门商品与推荐结果、RabbitMQ 可做削峰异步下单，见架构演进路线。

## 架构演进路线（面试可讲）
1. Redis 缓存热门商品/推荐结果，缓存击穿用互斥锁或逻辑过期；
2. RabbitMQ 异步下单：下单接口只做库存校验+入队，消费者落库，提升峰值吞吐；
3. 推荐引擎升级：相似度矩阵离线计算 + Redis 存储，支持实时召回；
4. 部署：Nginx + Tomcat 集群 + MySQL 主从读写分离。


## 🔒 安全修复

- **管理后台存储型 XSS**：订单详情不再把收货人/电话/地址直接拼进 JS 字符串；`AdminOrderController` 用 Jackson 输出 JSON（并转义 `< > &`），前端对每个动态字段再做 HTML 转义后才写入 `innerHTML`。
- **支付回调越权**：`/api/pay/notify` 现在要求登录会话并校验订单归属，不能拿到 orderNo 就改任意订单；真实支付网关回调应改为验签 + 金额/商户号校验（演示接口不适用该模式）。
- **密码散列**：新增 `PasswordUtil`，新密码使用 PBKDF2-HMAC-SHA256 + 16 字节随机盐（12 万次迭代）；历史 MD5 密码仍可登录，并在登录成功后自动升级为 PBKDF2。
- **LLM 客户端**：HttpClient 改为单例 + 连接池，连接 5s / 读取 30s 超时，避免每次请求新建客户端且无限等待；历史消息去掉刚写入的本轮 user 消息，避免重复。
- **推荐数据质量**：购买行为从「下单时」移到「支付成功后」记录，取消/未支付订单不再污染协同过滤数据。
- **已知限制**：推荐共现分未归一化（热门偏置）；AI 客服默认关闭，商品检索是名称匹配而非向量 RAG；没有自动化集成测试，新增 `PasswordUtilTest` 覆盖密码散列与旧 MD5 兼容。
