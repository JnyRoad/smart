# smart/scripts

`smart/scripts/` 保存 `smart/` 基础平台工程内部使用的校验脚本。

## 目录结构

```text
smart/scripts/
├── README.md
├── check-admin-search.js          # UPMS 管理端搜索相关静态检查
└── verify-maven-versioning.rb     # Maven 版本/CI-friendly revision 校验
```

## 使用范围

- 只处理 `smart/` 基础平台工程内部规则。
- 项目级发布 Jar 汇总脚本在 [../../scripts/](../../scripts/)。
- 新增脚本时要在本 README 写清楚输入、输出和是否会修改文件。
