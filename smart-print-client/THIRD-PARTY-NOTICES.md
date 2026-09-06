# 打印工作站第三方组件

锁定版本来自 `src/packages.lock.json`（2026-09-05核实）。本项目没有修改这些第三方组件。分发工作站产物时须同时保留整个 `licenses/` 目录及本文，不只分发 EXE/DLL。

| 组件 | 固定版本 | 包声明与随包条款 |
| --- | --- | --- |
| PDFtoImage | 5.4.0 | MIT；[原版许可](https://github.com/sungaila/PDFtoImage/blob/v5.4.0/LICENSE)，副本 `licenses/PDFtoImage-MIT.txt` |
| System.Drawing.Common / Microsoft.Win32.SystemEvents | 10.0.8 | MIT；保留 `licenses/dotnet-10.0.8/` 的许可和第三方声明 |
| SkiaSharp / SkiaSharp.NativeAssets.Win32 | 4.150.1 | 包声明 MIT；原生 Skia 及其依赖的独立条款完整保留在 `licenses/skiasharp-4.150.1/` |
| bblanchon.PDFium.Win32 | 152.0.7961 | NuGet 声明 Apache-2.0；构建工具许可 MIT，PDFium 及静态依赖各自条款完整保留在 `licenses/pdfium-7961/`，另附 Apache-2.0 正文 |
| Linux/macOS 的 PDFium、SkiaSharp 原生资产 | 与上表相同版本 | 用于离线测试；Windows 发布按 `win-x64` 选择资产，不把跨平台测试视为 Windows 实机验证 |

PDFium 声明来自上游发布 [chromium/7961 的 Windows x64 原始包](https://github.com/bblanchon/pdfium-binaries/releases/download/chromium/7961/pdfium-win-x64.tgz)。归档 SHA-256 为 `88276459349b291c41f10422dad0210f007c04d919c8fa56472b6b7c6406adf4`；归档中的 `pdfium.dll` 与实际 NuGet 中 Windows x64 DLL 逐字节 hash 一致，SHA-256 为 `d3d9f4b7c9dabe3363f30779c5c3c715c47332749fa590e4b4a2b8b6780cb1c4`。因此这里保留的是所用二进制版本的声明，不能用构建工具的 MIT 标签代替所有原生组件条款。

PDFium/Skia 所包含的 FreeType 按其随包 FreeType License 使用。Portions of this software are copyright © The FreeType Project (www.freetype.org). All rights reserved. 其他组件版权和免责条款见随附原文；未将含可选许可的组件改为仅依据 GPL 使用。

Brother b-PAC、P-touch Editor、Brother/HiTi 驱动不在本仓库或发布包内。管理员从厂商官网取得并按相应许可安装；不把厂商 SDK、安装包或许可申请替用户公开分发。参见 [Brother 官方下载](https://support.brother.com/g/s/es/dev/en/bpac/download/index.html?c=eu_ot&comple=on&lang=en&navi=offall&redirect=on)、[Brother 使用条件](https://support.brother.com/g/s/agreement/English/agree.html)、[HiTi 官方支持](https://www.hiti.com/support/download.aspx?hiti=1&lang=CN)。
