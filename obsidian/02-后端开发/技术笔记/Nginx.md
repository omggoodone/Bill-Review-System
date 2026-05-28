---
tags: [tech-note, backend, ops, nginx]
aliases: [Nginx, 反向代理, 静态文件服务器, Web服务器]
created: "2026-05-28"
---

# Nginx 详解

## 核心概念

### Nginx 是什么

**Nginx**（发音 "Engine-X"）是一个轻量级、高性能的 **HTTP Server + 反向代理服务器**。

由俄罗斯程序员 Igor Sysoev 于 2004 年发布，最初为解决 C10K 问题（单机同时处理 10000 个连接）而生。

### 它能做什么

| 功能 | 说明 |
|------|------|
| **静态文件服务** | 直接返回 HTML/CSS/JS/图片，性能远超 Tomcat |
| **反向代理** | 接收外部请求，转发给内部服务（后端、其他服务器） |
| **负载均衡** | 将请求分发到多台后端服务器 |
| **SSL/HTTPS 终端** | 证书配在 Nginx，后端不用关心加密 |
| **缓存** | 缓存后端响应，减少后端压力 |
| **限流** | 限制请求频率，防 DDoS |
| **Gzip 压缩** | 压缩响应体，节省带宽 |

## 为什么 Nginx 这么快

### 架构对比

```
Apache（传统模型）:
  每个请求 → 创建一个线程/进程 → 处理完销毁
  问题：1000并发 = 1000线程，内存爆炸

Nginx（事件驱动模型）:
  一个 Master 进程 → 几个 Worker 进程 → 每个 Worker 异步非阻塞处理几千个连接
  Worker 数量 = CPU 核数，不会因连接数增加而爆内存
```

### 关键设计

- **异步非阻塞 I/O**: 一个 Worker 同时处理 N 个请求，不会因为某个请求慢而阻塞其他请求
- **事件驱动**: 基于 Linux `epoll`（或 BSD `kqueue`），高效监控大量连接
- **Master-Worker 模式**: Master 管理配置/热重载，Worker 干活。Worker 挂了 Master 自动拉起

```
Master 进程 (PID 1000)
  ├─ Worker 1  → epoll 监听 → 处理 2000 个连接
  ├─ Worker 2  → epoll 监听 → 处理 2000 个连接
  ├─ Worker 3  → epoll 监听 → 处理 2000 个连接
  └─ Worker 4  → epoll 监听 → 处理 2000 个连接
```

## 核心概念：正向代理 vs 反向代理

### 正向代理（Forward Proxy）

```
你 → 代理服务器 → 目标网站
     (帮你翻墙)
```

代理的是**客户端**。服务端不知道真正的客户端是谁。

用途：科学上网、公司内网访问外部资源。

### 反向代理（Reverse Proxy）

```
用户 → Nginx → 后端服务器
       (帮后端挡请求)
```

代理的是**服务端**。客户端不知道真正的后端是谁。

用途：负载均衡、安全隔离、SSL 终端。

## 配置语法基础

### 配置文件位置

```bash
/etc/nginx/
├── nginx.conf              # 主配置文件
├── conf.d/                 # 子配置目录（include 进来）
│   └── bill.conf           # 本项目配置
└── sites-enabled/          # Debian/Ubuntu 额外的站点配置
    └── default             # 默认站点（会抢占80端口，需移除）
```

### 配置块层级

```nginx
# 全局块 — 影响 Nginx 整体运行
worker_processes auto;        # Worker 数 = CPU 核数
error_log /var/log/nginx/error.log;

# events 块 — 影响网络连接处理
events {
    worker_connections 1024;  # 每个 Worker 最大连接数
}

# http 块 — HTTP 服务器通用配置
http {
    include       mime.types;
    default_type  application/octet-stream;

    # server 块 — 一个虚拟主机（可以配多个，按 server_name 区分）
    server {
        listen 80;
        server_name localhost;

        # location 块 — URL 路径匹配规则
        location / {
            root /var/www/html;
        }

        location /api/ {
            proxy_pass http://backend:8080;
        }
    }
}
```

### location 匹配优先级

```nginx
# 1. 精确匹配（最高优先级）
location = /exact { }

# 2. 前缀匹配（^~ 前缀，匹配后不再检查正则）
location ^~ /static/ { }

# 3. 正则匹配（~ 区分大小写，~* 不区分）
location ~ \.(gif|jpg|png)$ { }
location ~* \.(gif|jpg|png)$ { }

# 4. 普通前缀匹配（最低优先级）
location / { }
```

实际匹配顺序: `=` > `^~` > `~`/`~*` > 普通前缀

## 本项目 Nginx 配置逐行解读

```nginx
server {
    listen 80;                           # 监听 80 端口（HTTP 默认端口）
    server_name localhost;               # 服务名（生产换成域名）
    root /opt/bill-review/dist;          # 根目录（文件系统的起点）
    index index.html;                    # 默认首页文件

    charset utf-8;                       # 响应字符编码

    # ---- 前端路由支持 ----
    location / {
        try_files $uri $uri/ /index.html;
        # 依次检查：文件是否存在 → 目录是否存在 → 兜底返回 index.html
        # 必要性：Vue SPA 使用 History 模式，URL 如 /login 在文件系统中
        #         并不存在，需要 Vue Router 在前端接管路由
    }

    # ---- API 代理 ----
    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
        # 末尾的 / 会剥离 /prod-api 前缀
        # /prod-api/login → 后端收到 /login

        proxy_set_header Host              $host;
        # 传递原始域名，后端用于生成链接/重定向

        proxy_set_header X-Real-IP         $remote_addr;
        # 客户端真实 IP（后端日志/审计用）

        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        # 完整代理链：客户端, 代理1, 代理2, ...

        proxy_set_header X-Forwarded-Proto $scheme;
        # 原始请求协议 http/https

        proxy_connect_timeout 60s;         # 连接后端超时
        proxy_read_timeout    60s;         # 读取后端响应超时
    }

    # ---- 静态资源强缓存 ----
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
        # immutable: 告诉浏览器文件不会变，30天内不发起协商缓存请求
        # 前提：文件名带 content hash（Vite 默认这样做）
    }

    # ---- 安全防护 ----
    location ~ /\. {
        deny all;
        # 禁止访问 .git, .env, .htpasswd 等隐藏文件/目录
    }

    # ---- Gzip 压缩 ----
    gzip on;
    gzip_types text/plain text/css application/json
               application/javascript text/xml text/javascript;
    gzip_min_length 1k;      # 小于 1KB 不压缩（压了反而变大）
    gzip_comp_level 6;       # 压缩级别 1-9，6 是性价比最佳
}
```

## try_files 详解

```nginx
try_files $uri $uri/ /index.html;
```

这是 Vue/React SPA 的关键配置。逐项解释：

| 变量 | 含义 |
|------|------|
| `$uri` | 当前请求的 URI（如 `/login`，`/user/profile`） |
| `$uri/` | 把 URI 当目录处理 |
| `/index.html` | 兜底文件 |

**完整流程**（用户访问 `/login`）:

```
1. try_files /login      → 检查 /opt/bill-review/dist/login 文件？不存在
2. try_files /login/     → 检查 /opt/bill-review/dist/login/ 目录？不存在
3. 返回 /index.html      → 浏览器加载 Vue 应用 → Vue Router 看到 URL 是 /login → 渲染登录组件
```

没有 `try_files` 的后果：访问 `/login` 时刷新页面 → Nginx 找不到 `login` 文件 → 返回 404。

## proxy_pass 末尾斜杠问题

这是一个经典陷阱：

```nginx
# ✅ 正确 — 有斜杠
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080/;
}
# 请求 /prod-api/user/login → 后端收到 /user/login
# location 匹配的 /prod-api/ 被替换为 proxy_pass 的 /


# ❌ 错误 — 无斜杠
location /prod-api/ {
    proxy_pass http://127.0.0.1:8080;
}
# 请求 /prod-api/user/login → 后端收到 /prod-api/user/login
# 整个路径原封不动传给后端（后端路由没有 /prod-api 前缀 → 404）
```

**规则: 如果 `proxy_pass` 末尾有 `/`，则 `location` 匹配的路径会被剥离。**

## 负载均衡基础

```nginx
# 定义一组后端服务器
upstream backend_servers {
    server 192.168.1.10:8080 weight=3;   # weight=3, 分配 30% 流量
    server 192.168.1.11:8080 weight=7;   # weight=7, 分配 70% 流量
    server 192.168.1.12:8080 backup;     # 备用，只有其他都挂了才用
}

server {
    location / {
        proxy_pass http://backend_servers;
    }
}
```

负载策略：
- **轮询**（默认）: 逐个轮流
- **weight**: 按权重比例分配
- **ip_hash**: 同 IP 始终打到同一台（解决 Session 问题）
- **least_conn**: 发给当前连接数最少的机器

## 常用命令

```bash
# 启停
sudo nginx                       # 启动
sudo nginx -s stop               # 快速停止
sudo nginx -s quit               # 优雅停止（处理完当前请求再停）
sudo nginx -s reload             # 热重载配置（不停服！）
sudo nginx -s reopen             # 重新打开日志文件（日志切割用）

# 检查
sudo nginx -t                    # 测试配置文件语法
sudo nginx -T                    # 测试 + 打印完整配置（调试用）
nginx -v                         # 查看版本
nginx -V                         # 查看版本 + 编译参数

# systemd
systemctl status nginx
systemctl start/stop/restart nginx
```

## 日志

```bash
# Nginx 默认日志位置
/var/log/nginx/access.log        # 访问日志
/var/log/nginx/error.log         # 错误日志

# 自定义日志路径（本项目）
/opt/bill-review/logs/nginx-access.log
/opt/bill-review/logs/nginx-error.log
```

访问日志格式（默认 combined 格式）:

```
127.0.0.1 - - [28/May/2026:14:56:39 +0800] "GET /api/login HTTP/1.1" 200 1234 "-" "Mozilla/5.0..."
客户端IP     - -  时间戳                               请求行              状态码 字节数 引用  User-Agent
```

## 常见问题排查

### 配置不生效

```bash
# 1. 先检查语法
sudo nginx -t

# 2. 确认配置被加载（grep 你的配置）
sudo nginx -T | grep "你的配置关键词"

# 3. 重载
sudo nginx -s reload
```

### 出现默认 Nginx 欢迎页

默认站点 `sites-enabled/default` 抢占了 80 端口，删除即可。

```bash
sudo rm /etc/nginx/sites-enabled/default
sudo nginx -s reload
```

### 502 Bad Gateway

Nginx 能访问到，但后端没响应。后端挂了或端口错了。

```bash
# 检查后端进程
pgrep -f zsc-admin.jar
# 检查后端端口
ss -tlnp | grep 8080
# 检查后端日志
tail -50 /opt/bill-review/logs/app.log
```

### 404 Not Found

可能原因：
- 前端 `dist/` 路径不对（检查 `root` 指令指向的目录是否存在 `index.html`）
- `proxy_pass` 缺少末尾 `/`，导致 `/prod-api` 前缀未被剥离
- `try_files` 缺失，SPA 路由刷新 404

### CORS 跨域错误

如果前后端不在同一端口，浏览器会报跨域。两种解法：

1. **用 Nginx 代理**（本项目做法）：前后端同域名同端口，不存在跨域
2. **后端加 CORS 配置**：`@CrossOrigin` 注解或 CorsFilter

## 相关笔记

- [[../部署说明|部署说明]]
- [[../../00-项目总览/环境配置|环境配置]]
