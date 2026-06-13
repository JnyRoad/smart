# 功能规格：宿舍 / 门锁 / 宿舍申请模块（7 页）

> 来源：旧仓库 `smart-h5/src`（只读净室分析，仅提取功能事实）。
> 所有接口 module=platform 除特别注明；均需登录授权。

---

## 1. 我的宿舍 `/xuchang/dorm`

- 旧组件：`views-mobile/pages/dorm/index.vue`（page2-title + page2-list 模板页）
- **用途**：宿舍功能入口聚合页。
- **UI 元素**：
  - 页头：标题「我的宿舍」+ 头图背景。
  - 入口列表（2 项，图标 + 标题 + 副标题 + 右箭头）：
    1. 门锁动态码 — 「智能门锁动态码开门」→ `/xuchang/dorm/lock`
    2. 水电扣费明细 — 「查询每月宿舍水电扣费明细」→ `/xuchang/dorm/waterElec`
- **交互**：点击整行跳转，无其他逻辑；纯静态页，无接口。
- **状态**：单一状态。

## 2. 水电扣费明细 `/xuchang/dorm/waterElec`

- 旧组件：`views-mobile/pages/dorm/water-elec.vue`
- **用途**：按月查询本人宿舍水电扣费账单。
- **UI 元素**：
  - 顶部筛选条：左侧「查询全部」链接；右侧月份选择器（年月，默认当月，最大可选当月；选「查询全部」后显示「未选择」）。
  - 账单卡片列表，每卡字段：
    - `姓名-工号`（staffName-staffBadge）+ 账单日期（statementDate）
    - 抄表月份（meterMonth）
    - 房间{类别}费：cateInfos 逐项；**前端规则：过滤掉「热水」类别，「冷水」改名为「水」**（即展示「房间水费」「房间电费」）
    - 总计（totalFee）
  - 空态：tce-empty（暂无数据插画 + 文案）。
- **交互与规则**：
  - 选择月份 → 以 `statementMonth=YYYY-MM` 重查；「查询全部」→ statementMonth 传空、date 显示「未选择」。
  - 下拉刷新（重置第 1 页）、上拉分页加载（size=10，current<pages 时加载下一页）。
  - 接口失败 toast res.message。
- **接口**：GET `/dormitory/staff/statementdetail/record`（current, size=10, statementMonth）
- **状态**：有数据 / 空态 / 加载中。

## 3. 门锁动态码 `/xuchang/dorm/lock`

- 旧组件：`views-mobile/pages/lock/index.vue` + `lock/edit-code.vue`（弹窗）
- **用途**：查看本人智能门锁 6 位动态码并修改。
- **UI 元素**：
  - 标题「你的门锁动态码」。
  - 大号动态码数字（接口返回密文，前端解密显示；无值时显示 `******`）。
  - 按钮「修改动态码」→ 弹出修改弹窗。
  - 修改弹窗：头部「修改动态码」+「确定」；表单项「动态码」（placeholder 请输入6位数字动态码，必填）。
- **校验规则**：
  - 必须 6 位纯数字（`/^[0-9]{6}$/`），否则 toast「请输入6位数字动态码」。
  - 不得与当前动态码相同，否则 toast「请输入跟当前动态码不一样的新的动态码」。
  - 修改失败：alert 弹窗（标题「错误」+ 后端 msg）；成功：关闭弹窗并重新拉取动态码。
- **页面状态**：
  - 已入住：显示动态码。
  - 未入住（getPwd 返回空）：alert「您暂未入住智能宿舍，请联系宿管入住！」，确认后跳回 `/xuchang/dorm`。
- **接口**：
  - GET `/dormitory/staff/get/pwd`（badge）
  - POST `/dormitory/staff/update/lock/pwd`（badge, newPwd）
- **跳转**：代码中存在 `Refresh()` → `/xuchang/dorm/getCode`（刷新动态码页），但旧模板未放出按钮（见不确定点）。

## 4. 获取门锁动态码（人脸刷新）`/xuchang/checkIn/getCode`（旧路由 `/xuchang/dorm/getCode`）

- 旧组件：`views-mobile/pages/lock/get-code.vue` + `components/camera`
- **用途**：通过人脸识别校验身份后刷新（重新生成）门锁动态码。
- **UI 元素**：
  - 文案「刷新动态码」「需完成人脸识别」。
  - 取景框拍照区（camera 组件，含人像取景参考图）。
  - 拍照前：提示「请拍照」。
  - 拍照且人脸比对通过后：提示「人脸对比成功」+ 按钮「生成动态码」。
- **交互与规则**：
  - 拍照后图片上传人脸比对接口，成功得到 base64 照片。
  - 点「生成动态码」：提交 badge + facePic；成功 alert「刷新动态码成功！」→ 确认跳 `/xuchang/dorm/lock`；失败 alert（标题「错误」+ 后端 msg）。
- **接口**：
  - POST `/wechat/visit/checkFace`（module=app，人脸比对，body: visitorPhoto=base64）
  - POST `/dormitory/staff/update/pwd`（badge, facePic）
- **状态**：待拍照 / 比对成功 / 刷新成功 / 刷新失败。

## 5. 宿舍申请-发起提交 `/xuchang/checkIn`

- 旧组件：`views-mobile/pages/check-in/index.vue`（page3-tab + tce-form + page3-bottom）
- **用途**：员工发起宿舍入住申请，可系统分配或自选床位。
- **UI 元素**：
  - 顶部双 Tab：「发起提交」（当前）/「查看数据」→ `/xuchang/checkIn/detail`。
  - 表单：
    - 分配方式（必填）：按钮组「自选房间」/「系统分配」，默认自选房间。
    - 楼栋信息（必填，picker，选项来自接口 dormitoryName）。
    - 房间类型（必填，picker，选项依赖所选楼栋；**切换楼栋后房间类型清空重选**）。
    - 房间号（仅自选房间模式显示，点击 → 跳选择房间页）。
    - 床位（仅自选房间模式显示，点击 → 跳选择房间页）。
  - 底部按钮「申请」（圆角主按钮；获取用户信息失败时整个按钮隐藏）。
- **交互与规则**：
  - 进入页面：拉取楼栋列表、用户实名信息（badge → 姓名/性别/民族/证件号/生日/住址/证件有效期），并从 localStorage `roomInfo` 回填已选房间（从选择房间页返回的场景）。
  - 跳选择房间页前先校验必填项，携带 parkId/dormitoryId/roomType。
  - 申请提交：必填校验；自选模式下未选房间号 toast「请选择房间号」；提交体含用户实名信息 + dormitoryId + roomType + badge + parkId（自选再加 floorId/roomId/bedId）。
  - 成功 → 跳「查看数据」页；失败 toast res.message；获取用户信息失败 toast「获取用户信息失败！」。
- **接口**：
  - POST `/dormitory/queryDormitory`（parkId, isAccount=true）
  - GET `/dormitory/type/by/park-and-dormitory`（parkId, dormitoryId）
  - GET `/staff/define/badge`（badge）
  - POST `/dormitory/room/autoallot`（申请提交，自动/手动共用）
- **状态**：自选房间 / 系统分配 / 用户信息获取失败（隐藏申请按钮）。

## 6. 手动选择房间 `/xuchang/checkIn/selectRoom`

- 旧组件：`views-mobile/pages/check-in/select-room.vue`
- **用途**：按楼层浏览可选房间并挑选床位。
- **UI 元素**：
  - 左侧楼层竖列（「X层」，可选中高亮，默认第 1 层）。
  - 右侧「房间」网格：每格「X房」+「{男|女} 空床数/总床数」（roomSex 0=男 1=女），选中高亮。
  - 床位选择器（底部 picker，标题「选择床位」，选项「X床」，已占用 staffBadge≠null 或 delFlag=1 的床位剔除）。
- **交互与规则**：
  - 进入页拉楼层树（parkId/dormitoryId/roomType 来自路由 query）；无数据 toast「无房间信息！」/「获取房间信息失败！」并退回 `/xuchang/checkIn`。
  - 切楼层 → 重新拉该层房间、清空房间选中。
  - 点房间：freeBedNum>0 才拉床位，否则 toast「该房间无可选用的床位！」；床位过滤后为空 toast「无可选用的床位！」。
  - 选定床位 → 把 floorId/房间/床位写入 localStorage `roomInfo` → 返回 `/xuchang/checkIn` 回填。
- **接口**：
  - GET `/park/tree/condition`（楼层树）
  - GET `/dormitory/room/search/condition`（parkId, dormitoryId, roomType, floorId）
  - POST `/dormitory/room/bedDetail/{roomId}`
- **状态**：正常选择 / 无房间 / 房间满员 / 无可选床位。

## 7. 宿舍申请详情（查看数据）`/xuchang/checkIn/detail`

- 旧组件：`views-mobile/pages/check-in/list.vue`
- **用途**：查看本人入住分配结果与门锁身份特征（指纹/动态码）录入状态。
- **UI 元素**：
  - 顶部双 Tab：「发起提交」→ `/xuchang/checkIn` /「查看数据」（当前）。
  - 卡片（每条分配记录）：
    - 宿舍楼栋（dormitoryName）
    - 房间号（roomName + 「房间」）
    - 床位（bedNumber + 「号床」）
    - 「身份特征检查」框：
      - 指纹：状态图标（code 1/3=成功图标，0/2/4=失败图标）+ fingerprintDesc
      - 动态码：状态图标（同规则）+ 文案（dynamicCode===3 显示「已录入」，否则 dynamicDesc）
    - 卡底：fingerprintCode≠0 且取到动态码时，居中展示解密后的动态码数字。
  - 空态：tce-empty。
- **交互**：下拉刷新；接口失败 toast（message 或「网络错误」）。
- **接口**：
  - GET `/dormitory/staff/roomList/{staffBadge}`
  - GET `/dormitory/staff/get/pwd`（badge）
- **状态**：有记录（特征全部成功 / 部分失败）/ 空态。

---

## 不确定点

1. **lock/index.vue 的 `Refresh()`**：代码定义了跳转 `/xuchang/dorm/getCode`，但模板没有对应按钮（疑似历史遗留或被注释的入口）。原型在门锁动态码页保留「刷新动态码（人脸）」入口以保证 get-code 页可达，需产品确认。
2. **get-code.vue 上传接口**：页面 import 自 `services/upload.js` 的 `postImageSave` 实际指向 `/wechat/visit/checkFace`（人脸比对），与同名的 `services/uploadImage.js`（通用 `/file/upload`）重名；按 import 路径取前者。
3. **list.vue 的 getPwd 调用**：`this.decryption` 在 methods 中不存在（定义在 filters），疑似旧代码 bug；原型按「展示解密后动态码」的意图实现。
4. **水电明细的金额单位**：旧接口仅返回数字（fee/totalFee），未注明单位；原型按「元」展示。
5. **water-elec 的上拉分页**：模板绑定了 `@pulling-up` 且实现存在，正常生效；「查询全部」时 statementMonth 传空字符串。
6. **getCode 路由前缀**：任务给的新路由是 `/xuchang/dorm/getCode`，与 lock/index.vue 跳转一致；旧 README 未再核对路由表（页面文件位于 lock/ 目录）。
