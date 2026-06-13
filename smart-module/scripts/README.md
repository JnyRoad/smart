# smart-module/scripts

`smart-module/scripts/` 保存业务微服务聚合工程内部脚本。

## 目录结构

```text
smart-module/scripts/
├── README.md
└── verify-maven-versioning.rb   # Maven 版本/CI-friendly revision 校验
```

## 使用范围

- 只处理 `smart-module/` 内部 Maven 结构。
- 项目级发布 Jar 汇总脚本在 [../../scripts/](../../scripts/)。
- 新增脚本时要写清输入、输出、是否修改文件，以及失败时的退出码语义。
