# scripts

`scripts/` 保存项目级自动化脚本，当前重点是后端可部署 Spring Boot Jar 的构建、校验和发布目录汇总。

## 目录结构

```text
scripts/
├── README.md
├── build-release-jars.sh          # 构建后端模块并收集可部署 Jar
├── release-jars.manifest          # 发布 Jar 白名单：service|jar path
└── test-build-release-jars.sh     # build-release-jars.sh 的脚本级回归测试
```

## 主要脚本

`build-release-jars.sh` 会：

- 构建 `smart/`、`smart-module/` 和 `smart-module/FileReceiver/`。
- 只收集 `release-jars.manifest` 中列出的 Spring Boot 可执行 Jar。
- 校验 Jar 里是否包含 Spring Boot loader 入口，避免把 API/Core 普通库误当部署包。
- 输出 `smart-jar/`、`manifest.csv`、`sha256sums.txt` 和 `build-info.txt` 到 `release-artifacts/backend/<timestamp>/`。

常用命令：

```bash
scripts/build-release-jars.sh
scripts/build-release-jars.sh --skip-build
scripts/test-build-release-jars.sh
```

## 维护规则

- 新增可部署后端服务时，先确认它会产出 Spring Boot executable jar，再加入 `release-jars.manifest`。
- 不要把 `release-artifacts/`、`smart-jar/` 或脚本测试临时目录提交。
