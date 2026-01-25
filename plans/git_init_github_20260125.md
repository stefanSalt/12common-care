# Git 初始化并推送 GitHub（2026-01-25）

## 目标

- 在 `/home/stefan/projects/base-admin` 初始化 Git 仓库并完成首次推送到 GitHub。

## 风险提示

- 若 GitHub 远端仓库已包含初始提交（例如勾选了 README/.gitignore/license），首次 push 可能被拒绝，需要先 pull/rebase 或改用 force（默认不使用 force）。
- 若仓库为 Public，需确保不包含密钥/凭证等敏感信息。

## 待确认信息

- [x] GitHub 远端仓库 URL：`git@github.com:stefanSalt/base-admin.git`
- [x] 远端仓库是否为空仓库：是
- [x] 分支名：main（默认）

## 执行步骤（等待你回复“开始执行”后再跑）

- [x] `git init -b main`
- [x] `git add -A`
- [x] `git commit -m "chore: initial commit"`
- [x] `git remote add origin <REMOTE_URL>`
- [x] `git push -u origin main`

## 验证

- [x] `git status` 显示 clean
- [x] `git remote -v` 正确
- [ ] GitHub 页面可看到 main 分支与提交记录
