# orchestrator-java · 隐私计算编排层（Spring Boot）

接收编排请求 → 多租户边界守卫（跨租户须显式授权）→ 调用计算引擎 `engine-py` 执行 PSI。
引擎契约 `privacy-psi-v1` 统一在主仓 `contracts/openapi/privacy-psi-v1.yaml`（单一事实源，本仓不留副本）。

> **只 clone 本仓即可 `mvn package`**——前提是能访问制品仓（见下「制品仓访问」）。

## Java 基座：io.hashmatrix 公共依赖

经 **Maven 坐标**引用主仓 libs-java（`io.hashmatrix`，非 submodule 路径）：

- `<parent>` = `io.hashmatrix:hashmatrix-platform-parent:0.1.0`（Java17 / Spring Boot 3.3.5 / 编译·质量门）
- `import` `io.hashmatrix:hashmatrix-bom:0.1.0`（开发框架版本**唯一来源**，子仓不写版本号）
- `io.hashmatrix:hashmatrix-starter-tenant`（多租户上下文 `TenantContextHolder`）
- `io.hashmatrix:hashmatrix-starter-test`（JUnit5+AssertJ+Mockito+Testcontainers + 脱敏 fixtures，test 域）

> 升级公共依赖 = 改 `<version>` 一行。详见主仓 `libs-java/README.md`、`docs/00-主仓初始化-spec.md` §3。

## 制品仓访问（GitHub Packages）

公共制品发布在 GitHub Packages，**读取需鉴权**。本机 `~/.m2/settings.xml` 加：

```xml
<settings>
  <servers>
    <server>
      <id>github-hashmatrix</id>
      <username>${env.GITHUB_ACTOR}</username>   <!-- 你的 GitHub 用户名 -->
      <password>${env.GITHUB_TOKEN}</password>   <!-- 具 read:packages 的 PAT -->
    </server>
  </servers>
  <profiles>
    <profile>
      <id>github-hashmatrix</id>
      <repositories>
        <repository>
          <id>github-hashmatrix</id>
          <url>https://maven.pkg.github.com/HashMatrixData/hashmatrix</url>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles><activeProfile>github-hashmatrix</activeProfile></activeProfiles>
</settings>
```

> 内网/信创交付：制品镜像同步到内网 Nexus/Artifactory，settings.xml 指向私服（见主仓 spec §3 caveat）。

## 构建与运行

```bash
cd orchestrator-java
./mvnw -q package           # 构建 + 测试（统一测试栈来自 starter-test）
./mvnw spring-boot:run      # 起服务（应用 :8086 / 管理 :9086；引擎地址 engine.base-url 默认 localhost:8087）
```

> Docker：运行镜像消费宿主预构建的 fat-jar（避免容器内注入制品仓凭据），故先 `./mvnw package` 再 `docker build`。

## 多租户

`X-Tenant-Id` 头（网关从 Keycloak JWT 注入）→ starter-tenant `TenantContextFilter` → `TenantContextHolder`。
编排层 `TenantContextHolder.requireTenantId()` 取发起方租户；缺头返回 `400`。
跨租户联合计算须显式 `crossTenantAuthorized=true`，否则 `403 CROSS_TENANT_NOT_AUTHORIZED`（默认不串）。

## 最小 PSI 样例（经编排层）

```bash
curl -s localhost:8086/api/v1/psi/intersect \
  -H 'content-type: application/json' -H 'X-Tenant-Id: tenant-demo' -d '{
  "jobId": "job-0001",
  "parties": [
    {"partyId": "party-a", "tenant": "tenant-demo", "elements": ["alice@example.com","bob@example.com"]},
    {"partyId": "party-b", "tenant": "tenant-demo", "elements": ["bob@example.com","carol@example.com"]}
  ],
  "crossTenantAuthorized": false
}'
# → {"jobId":"job-0001","status":"SUCCEEDED","intersectionSize":1,"intersection":["bob@example.com"],"message":"ok"}
```
