# smart-print-renderer

这是模板打印链路的受控 PDF 渲染模块，提供进程内调用及仅供平台服务访问的私有 HTTP 入口。接收已授权单面快照，分别生成正反面并按需要合并；不查询业务库、不从外部地址拉取资源、不持久化业务数据，也不连接打印机。

## 接口和页面规则

```js
import { renderPrintTemplates, renderSinglePageTemplate } from './src/render.mjs';

const frontPdf = await renderSinglePageTemplate({
  printType: 'STAFF_CARD',
  template: frontTemplate,
  input: { name: '张三' },
  fontBytes: { NotoSansSC: fontBytes },
});

const cardPdf = await renderPrintTemplates({
  printType: 'STAFF_CARD',
  printMode: 'MANUAL_DUPLEX', // 或 AUTO_DUPLEX
  front: { templateVersionId: 'front-v7', template: frontTemplate, input: { name: '张三' } },
  back: { templateVersionId: 'back-v12', template: backTemplate, input: { name: '研发部' } },
  fontBytes: { NotoSansSC: fontBytes },
});

const visitorPdf = await renderPrintTemplates({
  printType: 'VISITOR_SLIP',
  printMode: 'SINGLE',
  front: { templateVersionId: 'visitor-v3', template: visitorTemplate, input: { name: '李四' } },
  fontBytes: { NotoSansSC: fontBytes },
});
```

`renderSinglePageTemplate` 对 `STAFF_CARD` 和 `VISITOR_SLIP` 都强制 `template.schemas` 恰好一页。员工厂牌的 `renderPrintTemplates` 要求非空的正面、背面版本标识和两个一页模板，只接受 `MANUAL_DUPLEX`、`AUTO_DUPLEX`，独立渲染后按 FRONT/BACK 合并为两页；访客凭条只接受 `SINGLE` 和正面，禁止背面。正反面尺寸和方向声明必须兼容；两面可以使用同名字段，因为输入在独立渲染后才合并，静态文案也不会相互覆盖。本模块会复制模板、输入和版本快照，渲染过程不会改动调用方对象。

渲染器只允许 `text`、`image`、`rectangle`、`line`、`ellipse`、`qrcode` 和 `code128` 组件；图片必须是内联 PNG/JPEG Base64 data URI。`pageSpecJson` 可声明 `widthMm`、`heightMm`、`orientation` 和 `maxPageCount`，模块会与 pdfme `basePdf` 及生成结果复核。

## 私有服务

使用 Node 24 与 pnpm 11.3.0。由运行环境注入至少 32 字符的 `PRINT_RENDERER_TOKEN` 后执行 `pnpm start`；默认仅监听 `127.0.0.1:18764`，可用 `PRINT_RENDERER_HOST` / `PRINT_RENDERER_PORT` 显式配置。凭据不放入源码或前端；跨主机部署需由私有网络和 TLS 保护。

`POST /internal/print-renderer/v1/render` 使用 `Authorization: Bearer <服务凭据>`，请求/响应见[内部契约](../specs/009-print-template-designer/contracts/print-api.md)。每份模板通过 `fieldSchemaJson.fields` 将业务 `key` 对应到 `schemaName`，两面独立解析。图片由平台先完成授权，再传 `resourceManifest` 中的对象标识、媒体类型、hash 和 `contentBase64` 字节；Node 不访问文件服务，不接受未登记的内联图片。平台文件/照片适配器仍需独立验收。

HTTP 请求体上限 52 MiB、同时处理上限 2；单面布局最多 2 MiB、字段快照最多 1 MiB。单资源最多 20 MiB、每面资源总计最多 32 MiB、每个输出 PDF 最多 32 MiB。每次渲染使用独立工作线程，超过 30 秒或调用方断开则终止；上传/请求头分别受 15 秒 / 10 秒限制。固定字体在启动时校验 SHA-256，字形和文字高度按同一 pdfme 字体/换行算法检查。当前仅支持固定普通字号，旋转、富文本、自动缩字等未校准能力会明确拒绝。

平台仍须校验园区、操作、人员、版本和资源权限，并重新读取 PDF 核对页数/尺寸/hash 后保存制品。服务返回 READY 只代表生成 PDF，不代表保存成功或设备出卡。

## 验证

在本目录执行：

```bash
pnpm install --frozen-lockfile --ignore-scripts \
  --store-dir /private/tmp/smart-pdfme-pnpm-store \
  --registry https://registry.npmjs.org
pnpm test
pnpm sample
```

测试使用真实 pdfme 生成中文单面和正反面 PDF，再由 `clawpdf` 提取并断言中文姓名、静态文案和部门字段，同时核对页数、页序和页面尺寸。`pnpm sample` 生成的合成样本位于 `/private/tmp/smart-print-renderer-sample.pdf`。

## 固定组件和字体

- `@pdfme/common`、`@pdfme/generator`、`@pdfme/pdf-lib`、`@pdfme/schemas` 均固定为 `6.1.12`，包元数据标注 MIT。来源：[pdfme LICENSE.md](https://github.com/pdfme/pdfme/blob/main/LICENSE.md)。
- `fontkit` 固定为 `2.0.4`，随包 `package.json` 标注 MIT，用于与 pdfme 一致的字形覆盖检查，分发保留其 LICENSE。
- `clawpdf` 固定为 `0.3.1`，包元数据标注 MIT；它携带 PDFium WASM，分发时须同时保留其 [LICENSE](https://github.com/openclaw/clawpdf/blob/main/LICENSE) 和 [第三方声明](https://github.com/openclaw/clawpdf/blob/main/THIRD_PARTY_NOTICES.md)。
- CJK 字体为 `assets/fonts/NotoSansCJKsc-Regular.otf`，Noto Sans CJK Sans2.004，采用 [OFL 许可证](https://raw.githubusercontent.com/notofonts/noto-cjk/Sans2.004/Sans/LICENSE)。字体 SHA-256 为 `2c76254f6fc379fddfce0a7e84fb5385bb135d3e399294f6eeb6680d0365b74b`，对应许可文件为 `assets/fonts/LICENSE`。

这些模块测试只证明 Node 侧合成 PDF 的兼容性；Vue2 画布与管理端构建证据见[规格验收记录](../specs/009-print-template-designer/quickstart.md)。打印用 PDF 栅格化、Brother QL-800 和现场 CS220/CS-220e 驱动尚未验证。

平台客户端仅对同机loopback地址允许HTTP；跨主机的私有地址必须使用HTTPS，否则在发送请求前拒绝配置。Node可位于同机TLS代理后，代理后端只绑定loopback，不对局域网暴露原始HTTP令牌通道。

## 当前人员照片

设计器的 image 组件可选择“当前人员照片”，该绑定必须必填。模板仅保存 `personPhoto` 绑定及合成占位，不保存某位人员的照片；打印和实际人员预览均由已授权业务来源冻结照片字节、归属及 hash。固定图片继续使用私有上传资源。Node 使用 `pngjs 7.0.0`（MIT）及 `jpeg-js 0.4.4`（BSD-3-Clause）进行完整图片解码，分发时保留依赖包中的完整 LICENSE。图片缺失、损坏、越界或来源不符会拒绝，不能用空白人像代替真实打印。
