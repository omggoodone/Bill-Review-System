# Git 协作指南（入门版）

适用于 ZSC 票据审核系统小组开发。照着敲就行，不懂的地方往下翻。

---

## 零、Git 是什么

Git 就是给你的代码**拍快照**的工具。每次 `commit` 就是拍一张照片，有了这张照片随时可以翻回去看"当时代码长什么样"。

**三个位置，记住就行：**

```
你的电脑              暂存区              GitHub
(随便改)  ──add──→  (购物车)  ──commit──→  (拍快照)  ──push──→  (云端)
```

- `git add` = 把文件放进购物车
- `git commit` = 结账，拍一张快照
- `git push` = 把快照传到 GitHub（让组员看到）
- `git pull` = 把组员的快照拉到本地

**分支是什么？** 就是一个指向某张快照的标签。你基于 main 创建自己的分支，写完代码后合并回去。分支极其轻量，用完就删。

---

## 一、第一次用 Git 要做的事

### 1. 安装 Git

下载：https://git-scm.com/download/win

安装时一路点"下一步"（不用改任何选项）。

装完后打开 CMD 或 PowerShell，输入以下验证：
```bash
git --version
```
显示版本号就是装好了。

### 2. 告诉 Git 你是谁

```bash
git config --global user.name "你的姓名拼音"
git config --global user.email "你的GitHub邮箱@xxx.com"
```

这两条设置只做一次。以后每次 commit 会自动带上你的名字。

### 3. 配 SSH 密钥（连 GitHub 用）

```bash
# 生成密钥（一路回车，不用设密码）
ssh-keygen -t ed25519 -C "你的GitHub邮箱@xxx.com"

# 查看公钥
cat ~/.ssh/id_ed25519.pub
```

把屏幕上输出的一大串字符（以 `ssh-ed25519` 开头）全部复制。

然后：
1. 打开浏览器，登录 GitHub
2. 右上角头像 → Settings
3. 左侧菜单 → SSH and GPG keys
4. 点绿色按钮 "New SSH key"
5. Title 随便填（比如"我的笔记本"），Key 里粘贴刚才复制的那串
6. 点 "Add SSH key"

验证是否配通：
```bash
ssh -T git@github.com
```
看到 `Hi 你的用户名!` 就是成功了。

> **如果 SSH 怎么都配不通**，可以改用 HTTPS：
> ```bash
> git clone https://github.com/cupcoff1/Bill-Review-System.git
> ```
> 缺点：每次 push 要输用户名密码（GitHub 现在用的是 Personal Access Token）。

---

## 二、克隆项目到本地

```bash
# 在你想要放项目的文件夹里（比如桌面），右键 → Git Bash Here，然后输入：
git clone git@github.com:cupcoff1/Bill-Review-System.git

# 进入项目目录
cd Bill-Review-System
```

`git clone` 是把 GitHub 上的整个仓库（包括所有历史快照）复制到你的电脑。只做一次。

---

## 三、日常开发流程（每天照这个来）

### 核心原则

```
永远不要在 main 分支上直接改代码。
每次做新功能都新建一个分支，写完合并回去。
```

### 完整步骤

**第 1 步：更新本地 main**

```bash
git checkout main           # 切换到 main 分支
git pull origin main        # 拉取组员推的最新代码
```

`checkout` = 切换分支（就像切换到另一条时间线）  
`pull` = 把 GitHub 上别人更新的代码下载到本地

**第 2 步：创建你的功能分支**

```bash
git checkout -b feat/功能名
```

分支名规则：`类型/简短描述`  
类型：`feat`=新功能，`fix`=修 bug，`docs`=文档

举例：
```bash
git checkout -b feat/bill-backend     # 做票据后端
git checkout -b feat/bill-frontend    # 做票据前端
git checkout -b docs/readme           # 改文档
```

`checkout -b` = 创建并切换到新分支（新建一条时间线 + 跳过去）

**第 3 步：写代码**

正常写你的代码，改完之后看下改了什么：
```bash
git status          # 列出哪些文件被改了
git diff            # 具体显示每一行改了什么
```

**第 4 步：暂存 + 提交**

```bash
git add .                    # 把所有修改放进暂存区（购物车）
git commit -m "你的提交信息"  # 拍快照，信息写清楚改了什么
```

提交信息用中文，言简意赅：
```
feat: 添加票据 Controller（7个接口）
fix: 修复注册用户角色分配错误
docs: 更新 Git 协作指南
```

**第 5 步：推送到 GitHub**

```bash
git push origin feat/bill-backend
```

第一次 push 这个分支时加 `-u`，后面就不用加了：
```bash
git push -u origin feat/bill-backend
```

**第 6 步：在 GitHub 上创建 Pull Request**

1. 打开 GitHub 仓库页面
2. 你会看到一个黄色横幅 "feat/bill-backend had recent pushes" → 点 "Compare & pull request"
3. 标题自动填好了，下方可以写备注（说一下改了啥）
4. 点 "Create pull request"
5. 让组员看一眼（Code Review）
6. 点 "Merge pull request" → "Confirm merge"

**第 7 步：合并后清理**

```bash
git checkout main            # 切回 main
git pull origin main         # 拉取最新（包含你刚合并的代码）
git branch -d feat/bill-backend   # 删掉本地已完成的分支
```

---

## 四、Tag 用法（标记里程碑）

当你完成一个重要阶段（比如文档写完），可以打个 tag 标记一下：

```bash
git tag v0.1-docs                       # 在当前 commit 上打 tag
git tag v0.1-docs -m "需求+设计文档完成"  # 带备注的 tag
git push origin v0.1-docs               # 推送 tag 到 GitHub
```

查看所有 tag：
```bash
git tag
```

tag 的作用：以后无论代码改了多少版，`git checkout v0.1-docs` 就能回到打 tag 那一刻的状态。

---

## 五、分工建议（5 人）

每个人负责自己擅长的区域，文件改动范围不重叠，减少冲突：

- **A（后端-实体层）**: Entity + DTO + VO + Mapper
- **B（后端-业务层）**: Service + Controller
- **C（前端-页面）**: views/biz/bill/ 下所有 Vue 组件
- **D（前端-对接）**: api/biz/bill.js + 菜单权限 SQL
- **E（整合测试）**: 系统配置 + 联调 + 测试验收

**并行开发顺序**: A 和 C 可以同时开始。A 完成实体/DTO 后 B 接手写 Service。B 完成接口后 D 写 API 封装。全部完成后 E 联调。

---

## 六、常见问题速查

### Q: push 时报错 "Updates were rejected"

别人先你一步推了代码。解决：
```bash
git pull origin main          # 拉最新代码
# 如果有冲突，解决冲突（见下面第七条）
git push origin 你的分支名     # 再推
```

### Q: 改错了文件想撤销

```bash
# 放弃某个文件的所有修改（回到上次 commit 的状态）
git checkout -- 文件名

# 放弃所有修改（谨慎！）
git checkout .
```

### Q: commit 信息写错了

```bash
git commit --amend -m "新的提交信息"
```

### Q: 不知道自己在哪个分支

```bash
git branch          # 列出所有分支，前面带 * 的就是当前分支
git status          # 显示当前分支 + 文件状态
```

### Q: 文件删错了想恢复

如果文件被 Git 追踪过（commit 过），可以恢复：
```bash
git checkout -- 被删的文件名
```

### Q: npm install 报错

`git clone` 下来的项目没有 `node_modules/`。需要手动装：
```bash
cd zsc_frontend
npm install
```

如果安装很慢，换淘宝镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q: 分支太多分不清

```bash
git branch -a        # 查看所有分支（本地 + 远程）
```

---

## 七、冲突怎么解决

当两个人改了同一个文件的同一行，Git 不知道该用谁的，就会产生冲突。

你会看到这种标记：
```
<<<<<<< HEAD
你的代码
=======
别人的代码
>>>>>>> feat/other-branch
```

解决步骤：
1. 找到带标记的文件
2. 和组员商量保留谁的版本（或者两个都保留、改写）
3. 手动删掉 `<<<<<<<`、`=======`、`>>>>>>>` 这三行标记
4. 修改代码，保留最终想要的版本
5. 保存文件
6. `git add .` → `git commit -m "解决冲突"`

**冲突不可怕，它是在保护代码不被悄悄覆盖。**

---

## 八、一日流水的完整示例

```bash
# === 早上来了 ===
git checkout main
git pull origin main

# === 开始写新功能 ===
git checkout -b feat/bill-service

# ... 写代码 ...

# === 写完了 ===
git status                    # 看看改了哪些文件
git add .
git commit -m "feat: 实现票据 Service 层（状态流转+编号生成）"

# === 推送到 GitHub ===
git push origin feat/bill-service

# === 去 GitHub 网页上点 Merge ===
# ... 打开浏览器操作 ...

# === 合并完了，回来清理 ===
git checkout main
git pull origin main
git branch -d feat/bill-service
```

---

## 九、速查卡片

```
克隆项目        git clone git@github.com:cupcoff1/Bill-Review-System.git
拉最新代码      git pull origin main
新建分支        git checkout -b feat/分支名
看改了啥        git status
暂存            git add .
提交            git commit -m "做了什么"
推送            git push origin 分支名
切回 main       git checkout main
删分支          git branch -d 分支名
打标签          git tag v0.1-docs
推送标签        git push origin v0.1-docs
```

---

## 十、术语对照表

- **仓库 (repo)**: 存代码的文件夹 + 全部历史记录
- **分支 (branch)**: 一条独立的工作线，基于 main 分叉出来
- **main**: 主分支，存放正式代码
- **commit**: 一次快照，记录此刻所有文件的状态
- **add**: 把修改加到暂存区（放购物车）
- **push**: 把本地 commit 上传到 GitHub
- **pull**: 把 GitHub 上的更新下载到本地
- **merge**: 把两个分支的代码合成一个
- **PR (Pull Request)**: 请求别人把你的分支合并到 main
- **clone**: 把远程仓库完整复制到本地电脑
- **tag**: 给某个 commit 贴个标签，标记重要时刻
- **冲突 (conflict)**: 两个人改了同一行，Git 让你二选一
