# Git 克隆指南

## 仓库信息

- **仓库地址**: `git@github.com:lishouchao/NextERP.git`
- **当前开发分支**: `feature/spring-modulith-migration`
- **主分支**: `main`

## 克隆方法

### 方法1: 克隆时直接指定分支

```bash
git clone -b feature/spring-modulith-migration git@github.com:lishouchao/NextERP.git
```

### 方法2: 先克隆仓库，再切换分支

```bash
git clone git@github.com:lishouchao/NextERP.git
cd NextERP
git checkout feature/spring-modulith-migration
```

### 方法3: 已有仓库，拉取最新代码

```bash
cd NextERP
git fetch origin
git checkout feature/spring-modulith-migration
git pull
```

## 验证

```bash
# 查看当前分支
git branch

# 查看最近提交
git log --oneline -3
```

预期输出:
```
* feature/spring-modulith-migration
  main

3fb0ffe docs: 添加 SAP 数据库研究项目和 NextERP 数据库设计
762b93a feat: 完成 HR 模块开发 (对标 SAP S/4HANA HCM)
b622002 refactor: 重构项目结构，移除旧版微服务架构
```

## SSH Key 配置

如果克隆失败，检查 SSH Key 配置：

```bash
# 1. 检查是否已有 SSH Key
ls ~/.ssh/id_ed25519.pub

# 2. 如果没有，生成新的 SSH Key
ssh-keygen -t ed25519 -C "your_email@example.com"

# 3. 启动 ssh-agent
eval "$(ssh-agent -s)"

# 4. 添加 SSH Key 到 agent
ssh-add ~/.ssh/id_ed25519

# 5. 复制公钥内容
cat ~/.ssh/id_ed25519.pub

# 6. 添加到 GitHub
#    GitHub -> Settings -> SSH and GPG keys -> New SSH key
#    粘贴公钥内容并保存

# 7. 测试连接
ssh -T git@github.com
```

## 常见问题

### Q: Permission denied (publickey)

**原因**: SSH Key 未配置或未添加到 GitHub

**解决**:
1. 检查 SSH Key 是否存在: `ls ~/.ssh/`
2. 生成新的 SSH Key (如上)
3. 将公钥添加到 GitHub

### Q: Repository not found

**原因**: 仓库地址错误或无访问权限

**解决**:
1. 检查仓库地址拼写
2. 确认有仓库访问权限
3. 确认使用 SSH 地址而非 HTTPS

### Q: 分支不存在

**原因**: 分支名拼写错误或未推送

**解决**:
1. 检查分支名: `git branch -r | grep spring`
2. 确保分支已推送到远程

## 项目目录结构

```
NextERP/
├── backend-modulith/          # Spring Modulith 后端
├── frontend/                  # Next.js 前端
├── docs/                      # 文档
│   └── HR/                    # HR 模块文档
├── research/                  # 研究文档
│   ├── sap-database/          # SAP 数据库研究
│   └── nexterp-database/      # NextERP 数据库设计
└── README.md
```

## 相关链接

- GitHub 仓库: https://github.com/lishouchao/NextERP
- HR 模块文档: [docs/HR/README.md](./HR/README.md)
- 数据库设计: [research/nexterp-database/README.md](../research/nexterp-database/README.md)
