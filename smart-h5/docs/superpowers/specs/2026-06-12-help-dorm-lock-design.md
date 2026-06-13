# 第 1 批模块设计：help 帮助中心 + dorm 我的宿舍 + lock 门锁动态码（6 页）+ AES 加密层

日期：2026-06-12
状态：设计已经旅途批准（AES 采用方案 A：加解密双向落地；不附带 /backlog 占位页）；本文档为落档 spec，待旅途审阅。
功能事实来源：docs/prototype/specs/home-mine-help.md §6-7（help）、dorm-lock-checkin.md §1-4（dorm/lock）；
加密算法事实：旧仓库 `src/util/encryption.js` 逐行核对（2026-06-12）。

## 0. 范围与既定决策

- 本批 = 实施顺序第 1 项（旅途逐批验收模式）：help 2 页、dorm 2 页、lock 2 页，单分支 `feat/help-dorm-lock`。
- AES 层双向实现（方案 A）：解密（本批门锁使用）+ 加密（后续模块备用），各做与旧实现的密文比对单测（硬门禁）。
- 不做：check-in（第 2 批）、/backlog 占位页（旅途未选用，等第 4 批闭合）、动态码功能增强。
- 全部页面需登录态（`useRequireAuth`）；工号 badge 取 `GET app:/employee/baseinfo` 的 `employeeBadge`。

## 1. 路由与页面行为

### `/help`（help/index）
- 头部横幅「帮助中心」+ 插画位；问题列表（`questionTitle` 单行省略 + 右箭头）→ `/help/{questionId}`。
- 分页：size=10，上拉加载（current<pages），下拉刷新（重置第 1 页，toast「更新成功」）；空态 ErrorBlock；失败 toast message。

### `/help/[id]`（help/detail）
- 标题条「问题详情」；答案富文本 `answerContent`（DOMPurify + 点击图片全屏预览，复用公告详情 RichTextBody 模式）；加载中/失败态。

### `/dorm`（dorm/index）
- 纯静态入口聚合页：标题「我的宿舍」+ 两行入口（图标+标题+副标题+箭头）：
  门锁动态码「智能门锁动态码开门」→ `/dorm/lock`；水电扣费明细「查询每月宿舍水电扣费明细」→ `/dorm/water-elec`。

### `/dorm/water-elec`（water-elec）
- 筛选条：「查询全部」链接（statementMonth 传空、月份显示「未选择」）+ 月份选择器（年月，默认当月，最大当月）。
- 账单卡：`{staffName}-{staffBadge}` + statementDate、抄表月份 meterMonth、cateInfos 逐项费用、总计 totalFee（按「元」展示）。
- **前端展示规则（旧版逻辑复刻）**：cateInfos 过滤掉「热水」类别；「冷水」改名「水」（即「房间水费」「房间电费」）。纯函数实现 + 单测。
- 分页 size=10 上拉加载 + 下拉刷新；空态；失败 toast。

### `/dorm/lock`（lock/index）
- 标题「你的门锁动态码」+ 大号码（`GET platform:/dormitory/staff/get/pwd?badge=` 返回 **hex 密文**，前端 `decryptFromHex` 解密展示；无值显示 `******`）。
- getPwd 返回空（未入住）：Dialog.alert「您暂未入住智能宿舍，请联系宿管入住！」→ 确认回 `/dorm`。
- 「修改动态码」弹窗：输入框 placeholder「请输入6位数字动态码」；校验 `/^[0-9]{6}$/`（否则 toast「请输入6位数字动态码」）、不得与当前码相同（toast「请输入跟当前动态码不一样的新的动态码」）；提交 `POST platform:/dormitory/staff/update/lock/pwd {badge, newPwd}`（**明文**，旧版如此，已核对 edit-code.vue:50）；失败 alert（标题「错误」+ 后端 msg）；成功关弹窗重拉。
- 「刷新动态码（人脸）」入口 → `/dorm/get-code`（按已批准原型保留，旧版代码有跳转无按钮属历史遗留）。

### `/dorm/get-code`（lock/get-code）
- 文案「刷新动态码 / 需完成人脸识别」+ 拍照区（复用 FaceUpload mode='face' 的 checkFace 链路；比对成功提示「人脸对比成功」）。
- 「生成动态码」：`POST platform:/dormitory/staff/update/pwd {badge, facePic}`；**facePic = checkFace 响应的 `resultData.base64`**（已核对旧 get-code.vue:75 与 services/upload.js:9-19——同一 checkFace 接口，访客模块消费 `data`(照片 id)，本页消费 `resultData.base64`）。FaceUpload 组件增加可选 `onUploaded(raw)` 回调暴露原始响应供本页取用。成功 alert「刷新动态码成功！」→ 确认跳 `/dorm/lock`；失败 alert「错误」+ msg。

## 2. AES 加密层 `src/lib/crypto/aes.ts`（TDD 硬门禁）

旧实现两段逻辑（互不对称，逐行核对自 encryption.js）：

| 方向 | 算法 | key 解析 | IV | padding | 输入/输出 |
|---|---|---|---|---|---|
| 加密 encryptFields | AES-**CBC** | Latin1 | =key | **ZeroPadding** | 明文 → base64 |
| 解密 decryptFromHex | AES-**ECB** | Utf8 | — | Pkcs7 | **hex** → base64 → utf8 |

- API：`decryptFromHex(hexStr: string): string`；`encryptFields<T>(data: T, fields: (keyof T)[]): T`（另含 Base64 模式：`btoa`）。
- key 来源链：`window.__SMART_CONFIG__.securityEncodeKey` → `NEXT_PUBLIC_SECURITY_ENCODE_KEY`，缺失抛错（快速失败）。**真实 key 不进仓库**：config.js 留空占位，部署期注入。
- **比对单测**：测试文件内用 crypto-js 逐字复刻旧 encryption.js 两段逻辑作为 oracle；固定 16 字节测试 key + 多组样本（6 位数字、中文、长文本、空串边界）断言：新 encrypt 密文 === oracle 密文；oracle 加密(ECB/Pkcs7 方式构造)的 hex 经新 decrypt 还原明文。
- 依赖：`crypto-js`（与旧版同库，规避模式/padding 实现差异）。

## 3. 接口清单（全部 bearer）

| 接口 | Method/模块 | 用途 |
|---|---|---|
| `app:/guide/help/question/list` | GET | 帮助分页（current,size=10；**分页参数按 query 传**——旧版误放 body 属已记录缺陷，契约按 query） |
| `app:/guide/help/question/answer/{id}` | GET | 帮助详情 |
| `platform:/dormitory/staff/statementdetail/record` | GET | 水电账单（current,size=10,statementMonth 可空） |
| `platform:/dormitory/staff/get/pwd` | GET | 动态码密文（badge） |
| `platform:/dormitory/staff/update/lock/pwd` | POST | 修改动态码（badge,newPwd 明文） |
| `platform:/dormitory/staff/update/pwd` | POST | 人脸刷新动态码（badge,facePic） |
| `app:/wechat/visit/checkFace` | POST | 人脸比对（既有，复用） |

响应结构按规格字段 + 旧组件命名定义宽松类型，最终以真实抓包为准（架构 §3）。

## 4. 测试策略

- 单测：AES 双向密文比对（§2）；水电 cateInfos 过滤/改名纯函数；key 缺失抛错。
- E2E（网络层 mock）：
  1. help：列表分页 → 详情富文本；空态。
  2. 水电：默认当月查询参数断言 → 切月份重查 → 查询全部（statementMonth 空）→ 空态。
  3. 门锁：mock 返回「用测试 key 按旧算法加密的 hex 密文」+ 注入测试 key 配置 → 断言解密后明文展示；修改弹窗三连（非 6 位 / 与当前相同 / 成功提交体断言）；未入住 alert 回跳 /dorm。
  4. get-code：人脸上传（mock checkFace）→ 生成动态码提交体断言 → 成功回 /dorm/lock。
  5. mine 页「我的宿舍」、help 入口点击落到真实页面（死链闭合回归）。

## 5. 分支与门禁

单分支 `feat/help-dorm-lock`：`pnpm check/test/e2e/build` 全绿 → 独立子 agent 评审修复至无明确问题 → PR → 合并 → 向旅途汇报验收（本批完成后停下，等指令再进第 2 批）。

## 6. 边界与防御性决策

1. ~~get-code facePic 取值~~ 已核实并写入 §1（resultData.base64）。
2. 水电金额单位旧接口未注明，按「元」展示（原型同）。
3. 动态码解密失败（密文损坏/key 不符）的兜底：显示 `******` + toast「动态码解析失败」，不崩页（旧版无此分支，新版补防御）。
