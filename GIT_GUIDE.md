# Git 协作指南

适用于 ZSC 图书管理系统小组开发。

## 一、前置准备

### 安装 Git

1. 下载：https://git-scm.com/download/win
2. 安装时一路默认即可
3. 验证：
```bash
git --version
```

### 配置 SSH 密钥（只需一次）

```bash
# 生成密钥（一路回车，不用设密码）
ssh-keygen -t ed25519 -C "你的GitHub邮箱@xxx.com"

# 复制公钥
cat ~/.ssh/id_ed25519.pub
```

1. 打开 GitHub → Settings → SSH and GPG keys → New SSH key
2. 把复制的内容粘贴进去，保存

验证是否配通：
```bash
ssh -T git@github.com
```
看到 `Hi cupcoff1!` 就是成功了。

---

## 二、克隆项目

```bash
git clone git@github.com:cupcoff1/Bill-Review-System.git
cd Bill-Review-System
```

---

## 三、日常开发流程

### 核心原则

```
永远在功能分支上开发 → 推送到 GitHub → 在 GitHub 上提 PR → 合并到 main
```

**绝不要直接在 main 分支上改代码。**

### 每一步怎么操作

**1. 拉取最新代码**

```bash
git checkout main
git pull origin main
```

**2. 从 main 创建你的功能分支**

分支命名规则：`类型/功能描述`

```bash
# 类型：feat=新功能  fix=修bug  docs=文档
git checkout -b feat/book-search    # 要做"图书搜索"功能
git checkout -b fix/login-error     # 修"登录报错"的 bug
```

**3. 写代码**

正常写你的代码，改完之后看下改了什么：
```bash
git status          # 改了哪些文件
git diff            # 具体改了什么内容
```

**4. 暂存并提交**

```bash
git add .           # 暂存所有修改
git commit -m "一句话说清楚你做了什么"
```

提交信息用中文，简单明了：
```
feat: 添加图书搜索接口
fix: 修复验证码不显示的bug
```

**5. 推送你的分支到 GitHub**

```bash
git push origin feat/book-search
```

**6. 去 GitHub 创建 Pull Request**

- GitHub 上会看到 `Compare & pull request` 按钮，点它
- 标题写清楚改了什么
- 点 `Create pull request`
- 让组员 review（看看有没有明显问题）
- 点 `Merge pull request`

**7. 合并后，回到本地更新 main**

```bash
git checkout main
git pull origin main
git branch -d feat/book-search   # 删除本地已完成的分支
```

---

## 四、分工建议

一个功能从 API 到页面一个人搞定，避免两个人同时改同一个文件：

| 成员 | 负责区域 | 文件范围 |
|------|----------|----------|
| A | 后端系统模块 | `zsc_backend/zsc-system/` |
| B | 后端业务模块 | `zsc_backend/zsc-module/` |
| C | 前端页面 | `zsc_frontend/src/views/` |

每个人改各自目录的文件，合并时自然不会有冲突。

---

## 五、冲突怎么办

冲突出现在"两个人改了同一个文件的同一行"时：

```
<<<<<<< HEAD
你的代码
=======
别人的代码
>>>>>>> feat/other-branch
```

解决步骤：
1. 和组员一起确认两个版本中保留哪一个
2. 手动删掉 `<<<<<<<`、`=======`、`>>>>>>>` 这些标记
3. 保留最终想要的代码
4. `git add .` → `git commit -m "解决冲突"`

冲突不危险，它只是在保护你的代码不被静默覆盖。

---

## 六、常见问题速查

### push 时提示 "Updates were rejected"

说明别人在你之前推了新代码到同一个分支：

```bash
git pull origin main          # 先拉最新代码
# 解决冲突（如果有）
git push origin 你的分支名     # 再推
```

### 改错了想放弃

```bash
# 只放弃某几个文件的修改
git checkout -- 文件名

# 放弃所有未提交的修改（谨慎）
git checkout .
```

### commit 信息写错了想改

```bash
git commit --amend -m "新的提交信息"
# 如果已经 push 过，需要加 --force（谨慎）
```

### npm install 报错

没有 `node_modules/` 目录，因为 `git clone` 不会下载依赖。需要自己装：

```bash
cd zsc_frontend
npm install
```

---

## 七、一条流水的完整示例

```bash
# 早上来第一件事
git checkout main
git pull origin main

# 开始做一个新功能
git checkout -b feat/add-borrow-record

# ... 写代码 ...

# 写完了，看看改了啥
git status

# 提交
git add .
git commit -m "feat: 添加借阅记录功能"

# 推送到 GitHub
git push origin feat/add-borrow-record

# 去 GitHub 创建 PR → 合并 → 回来
git checkout main
git pull origin main
```

把这张命令表贴在群里，照着敲就行，一周后就习惯了。
