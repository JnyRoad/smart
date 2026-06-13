# 裕同 smart-ui 全量页面功能清单(Next.js 重写依据)

> 逆向自 Vue2.7 + ElementUI + Avue 旧系统。按业务模块组织,区分「页面」与「子组件」。

## 目录

1. [系统管理与代码生成](#1-sys-admin)
2. [框架与登录(布局/登录/锁屏/标签/搜索)](#2-sys-frame)
3. [平台公共业务组件](#3-platform-components)
4. [宿舍管理-房间/床位/门锁](#4-dormitory-room)
5. [宿舍管理-入住/退宿/授权/报修/住宿员工](#5-dormitory-checkin)
6. [宿舍管理-水电抄表/计算/统计/告警](#6-dormitory-utility)
7. [设备管理](#7-device)
8. [基础信息(人员/组织/供应商/薪资签收/社保/导入)](#8-basic)
9. [保密区域管理](#9-security_area)
10. [业务设置](#10-business)
11. [可视化大屏](#11-panel)
12. [区域管理/停车场/权限策略](#12-area)
13. [访客管理](#13-visitor)
14. [业务监控（请假/加班/补卡/调休/外宿补贴/离职/入职/退宿审批记录）](#14-work)
15. [外包人员管理](#15-outsourcing)
16. [车辆管理](#16-vehicle)
17. [人资行政管理](#17-personnel_manage)
18. [招聘 / 简历登记 / 预约访客](#18-recruit-resume)
19. [门禁卡/通行记录(ISC)](#19-records)
20. [杂项(信息发布/出入口/园区服务/停车/报警/物流车/返厂/物品放行/首页/水电看板)](#20-misc)
21. [全局组件与错误页(补充)](#21-global)

---

## 汇总统计

- 模块分组: 21
- 页面合计: 284
- 子组件合计: 169
- 文件覆盖: 453(实测全量 .vue,经 find 覆盖校验 100%,零虚构、零遗漏)

| 模块 | 页面 | 子组件 |
|---|---|---|
| 系统管理与代码生成 | 12 | 0 |
| 框架与登录(布局/登录/锁屏/标签/搜索) | 5 | 14 |
| 平台公共业务组件 | 0 | 19 |
| 宿舍管理-房间/床位/门锁 | 12 | 23 |
| 宿舍管理-入住/退宿/授权/报修/住宿员工 | 9 | 13 |
| 宿舍管理-水电抄表/计算/统计/告警 | 14 | 14 |
| 设备管理 | 30 | 11 |
| 基础信息(人员/组织/供应商/薪资签收/社保/导入) | 23 | 7 |
| 保密区域管理 | 8 | 21 |
| 业务设置 | 20 | 4 |
| 可视化大屏 | 6 | 20 |
| 区域管理/停车场/权限策略 | 16 | 6 |
| 访客管理 | 14 | 3 |
| 业务监控（请假/加班/补卡/调休/外宿补贴/离职/入职/退宿审批记录） | 16 | 0 |
| 外包人员管理 | 12 | 3 |
| 车辆管理 | 14 | 0 |
| 人资行政管理 | 11 | 3 |
| 招聘 / 简历登记 / 预约访客 | 17 | 2 |
| 门禁卡/通行记录(ISC) | 9 | 0 |
| 杂项(信息发布/出入口/园区服务/停车/报警/物流车/返厂/物品放行/首页/水电看板) | 32 | 0 |
| 全局组件与错误页(补充) | 4 | 6 |

---

## 1-sys-admin · 系统管理与代码生成

本模块是 Saber/BladeX(pig 体系)中后台的系统底座,负责账号与权限治理(用户、角色、部门、菜单、字典)、安全运维(登录日志、在线令牌、第三方登录配置、客户端/终端密钥、网关路由)以及个人账户自服务(资料/密码)。权限按钮通过 permissions['xxx'] 控制显示。核心业务流:管理员维护菜单树->给角色分配菜单权限与数据范围->给用户绑定角色与园区。另含一个开发工具页(代码生成器),从数据库表一键生成 CRUD 代码包并下载。

**页面 12 个 / 子组件 0 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 用户管理 | 列表/CRUD | ✓ | /admin/user/index | avue-crud 用户列表分页/搜索(sys_user_add/edit/del 按钮权限控制)、添加用户:用户名、姓名、密码、手机号、所属部门、多选下拉绑定角色(deptRoleList 取角色字典,多选 tag 展示 roleList)、多选下拉关联园区(allPark 取园区字典,多选 tag 展示 parkList)、密码字段新增必填、编辑留空则不改密(明文/密文切换 showPassword)、编辑时回填已选角色与园区 id(handleOpenBefore)、锁定状态(lockFlag 有效/锁定)以 tag 展示、删除用户带用户名二次确认(delObj userId) | 用户username、姓名、密码password、所属部门deptId、手机号phone、关联园区parkList、角色roleList、锁定状态lockFlag、创建时间 | src/views/admin/user/index.vue |
| 个人信息/修改密码 | 表单 |  | /admin/user/info | Tab 切换信息管理/密码管理(switchTab,弱密码时仅显示密码 Tab)、信息管理:展示只读用户名、修改手机号(/admin/user/edit 提交)、密码管理:原密码+新密码+确认新密码三段校验、新密码强度规则校验(validatePwd:不少于8位、含数字大小写特殊符、禁连续/重复、不可与原密码相同)、确认密码一致性校验(vldPassword2)、改密成功强制登出跳登录页(logOutHandle)、弱密码场景(isStrongPwd=false)展示星空背景独立改密页与安全提示、重置表单(resetFields) | 用户名username、手机号phone、原密码password、新密码newpassword1、确认密码newpassword2、头像avatar、isStrongPwd弱密码标记 | src/views/admin/user/info.vue |
| 角色管理 | 列表/CRUD | ✓ | /admin/role/index | avue-crud 角色列表分页/搜索(sys_role_add/edit/del/perm 按钮权限)、新增/编辑角色:角色名称、角色标识、角色描述、数据权限 dsType 选择(全部/自定义/本级及子级/本级)、自定义数据范围时弹部门树多选(dsScope,fetchTree 加载部门,getCheckedKeys 拼接)、权限按钮打开'分配权限'弹窗:加载菜单树(fetchMenuTree)并回显已选(fetchRoleTree)、菜单树勾选解析叶子节点(resolveAllEunuchNodeId)并提交(permissionUpd 含半选 HalfCheckedKeys)、删除角色带名称二次确认(delObj roleId) | 角色名称roleName、角色标识roleCode、角色描述、数据权限类型dsType、数据范围dsScope、菜单权限menuIds、创建时间 | src/views/admin/role/index.vue |
| 部门管理 | 列表/CRUD | ✓ | /admin/dept/index | 左侧部门树展示(fetchTree)支持按名称过滤(filterNode)、默认全展开、点击树节点加载详情(getObj)到右侧表单、顶部按钮组添加/编辑/删除(sys_dept_add/edit/del 权限控制)、表单维护:父级节点 parentId、节点编号 deptId、部门名称 name、排序 sort、新增/更新带必填校验(create/update validate)、删除当前节点带二次确认(delObj currentId)、取消编辑还原表单(onCancel/resetForm) | 部门名称name、父级节点parentId、节点编号deptId、排序sort | src/views/admin/dept/index.vue |
| 菜单管理 | 列表/CRUD | ✓ | /admin/menu/index | 左侧菜单树展示(fetchMenuTree)支持过滤与展开状态记忆(nodeExpand/nodeCollapse 维护 aExpandedKeys)、点击节点加载详情(getObj)到右侧表单、顶部添加/编辑/删除按钮(sys_menu_add/edit/del 权限)、表单字段:父级节点、节点ID、标题、权限标识 permission、图标(avue-crud-icon-select 图标选择器)、类型(菜单/按钮)、排序、前端组件 component、前端地址 path(iframe)、路由缓存 keepAlive 开关、类型过滤器 0菜单/1按钮、排序非空校验后再提交 create/update、删除节点二次确认(delObj currentId) | 菜单标题name、权限标识permission、父级parentId、节点ID menuId、图标icon、类型type、排序sort、前端组件component、前端地址path、路由缓存keepAlive | src/views/admin/menu/index.vue |
| 字典管理 | 列表/CRUD | ✓ | /admin/dict/index | avue-crud 字典列表分页/搜索(searchChange)、添加字典:数据值、标签名、类型、描述、排序、备注、行内编辑(rowEdit/handleUpdate putObj)受 sys_dict_edit 控制、行内删除带'标签名+类型'二次确认(sys_dict_del)、分页 size/current 切换 | 数据值value、标签名label、类型type、描述、排序sort、备注remark | src/views/admin/dict/index.vue |
| 操作日志 | 列表/CRUD | ✓ | /admin/log/index | avue-crud 日志列表分页(默认 descs=create_time 倒序)、搜索过滤(searchChange)、查看类型、标题、IP地址、请求方式、客户端、请求时间、创建时间、删除单条日志带 ID 二次确认(sys_log_del 权限,delObj id) | 日志类型type、标题title、IP地址、请求方式method、客户端、请求时间、创建时间 | src/views/admin/log/index.vue |
| 终端管理(OAuth客户端) | 列表/CRUD | ✓ | /admin/client/index | avue-crud 客户端列表分页、添加终端:编号 clientId、密钥 clientSecret、域 scope、授权模式、回调地址、权限、自动放行、令牌时效、刷新时效、扩展信息、资源ID、行内编辑(sys_client_edit,putObj)、行内删除带 clientId 二次确认(sys_client_del,delObj)、刷新回调 refreshChange | 客户端编号clientId、密钥clientSecret、域scope、授权模式authorizedGrantTypes、回调地址webServerRedirectUri、权限authorities、自动放行autoapprove、令牌时效accessTokenValidity、刷新时效refreshTokenValidity、扩展信息、资源ID | src/views/admin/client/index.vue |
| 在线令牌/踢人 | 列表/CRUD | ✓ | /admin/token/index | avue-crud 在线令牌列表分页、展示用户ID、用户名、令牌、类型、过期时间、踢人:按 access_token 强制用户下线带二次确认(delObj access_token,sys_client_del 权限)、刷新回调 refreshChange | 用户ID、用户名username、令牌access_token、类型、过期时间expires_in | src/views/admin/token/index.vue |
| 第三方登录配置 | 列表/CRUD | ✓ | /admin/social/index | avue-crud 第三方登录配置列表分页/搜索、新增配置:类型、描述、appId、appSecret、回调地址(generator_syssocialdetails_add 权限)、行内编辑(generator_syssocialdetails_edit,putObj)、行内删除带 ID 二次确认(generator_syssocialdetails_del,delObj)、刷新与搜索回调 | 类型type、描述、appId、appSecret、回调地址redirectUrl、创建时间 | src/views/admin/social/index.vue |
| 网关路由配置 | 特殊/公开页 | ✓ | /admin/route/index | 顶部警告提示'非工程师不建议操作'(el-alert)、vue-json-editor 可视化编辑路由 JSON、加载时将 predicates/filters 字符串解析为 JSON(fetchList)、更新按钮提交整份路由配置(putObj)并通知成功 | 路由routeId、断言predicates、过滤器filters、动态路由JSON | src/views/admin/route/index.vue |
| 代码生成器 | 特殊/公开页 | ✓ | /gen/index | avue-crud 数据库表列表分页/搜索(/gen/generator/page,展示表名称、表注释、索引引擎、创建时间)、点击'生成'按钮按表名打开'生成配置'弹窗(avue-form)、配置生成参数:表名称、包名 packageName、作者 author、模块 moduleName、表前缀 tablePrefix、注释 comments、提交生成并下载代码(handleDown -> /gen/generator/code) | 数据库表tableName、表注释tableComment、索引引擎engine、包名packageName、作者author、模块moduleName、表前缀tablePrefix、注释comments | src/views/gen/index.vue |


## 2-sys-frame · 框架与登录(布局/登录/锁屏/标签/搜索)

本模块是整个智慧园区中后台的外壳与入口体系,基于 Avue(Saber/BladeX)布局封装。核心业务流:用户在登录页(支持普通登录与平板 pad 登录、记住密码、弱密码校验、社交账号登录回调)鉴权后进入主框架;主框架由顶部导航栏(用户下拉、退出、可视化面板、打印访客条入口)、左侧后端动态下发的多级菜单、顶部多标签页(tags)、主体 router-view 四部分构成,并附带锁屏、菜单搜索、主题切换、界面设置、错误日志、token 定时刷新等系统级能力。Next.js 重写时这部分对应 App Shell / Layout、登录鉴权、菜单与标签状态管理。

**页面 5 个 / 子组件 14 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 主框架布局页 | 特殊/公开页 |  | / | 组合 top 顶部栏 / sidebar 左侧菜单 / tags 标签页 / router-view 主体四区布局、keep-alive 缓存带 $keepAlive 标记的路由页面、每 10 秒轮询检测 access_token,expires_in 低于阈值时自动 dispatch RefreshToken 刷新令牌(带 refreshLock 防重入)、window.onresize 监听屏幕尺寸提交 SET_SCREEN 实现响应式折叠、点击遮罩层 SET_COLLAPSE 折叠/展开侧边栏、预留(已注释)基于 stompjs/SockJS 的 WebSocket 协同任务提醒 | userInfo、access_token、expires_in、isLock、isCollapse、website | src/page/index/index.vue |
| 锁屏页 | 特殊/公开页 |  | /lock | 展示当前用户名,输入解锁密码(与 lockPasswd 比对)、密码错误抖动动画+提示,正确则 CLEAR_LOCK 解锁并跳回锁屏前的标签页、退出按钮二次确认后 dispatch LogOut 跳转 /login、回车键提交解锁 | userInfo.username、lockPasswd、tag | src/page/lock/index.vue |
| 登录页 | 特殊/公开页 |  | /login | 品牌区(裕同 Logo/slogan)+登录卡片布局,标题 TCE-云视智慧园区、内嵌 userLogin 账号密码登录组件、监听路由 query 中 state/code,自动 dispatch LoginBySocial 完成社交账号登录并跳转欢迎页、底部展示公众号二维码、官网/企业地址/电话/备案版权信息、平板模式(/pad/login,meta.isPad)隐藏底部信息栏、每秒刷新当前时间 | website、tagWel、socialForm(state/code)、isPad | src/page/login/index.vue |
| 社交登录授权回调页 | 特殊/公开页 |  | /authredirect | 解析 query 中 state/code/type、type=BIND 时调用 /admin/social/bind 绑定社交账号并提示成功后关闭窗口、非绑定场景关闭当前窗口并将 state/code 回传给 opener 登录页完成登录 | state、code、type、tagWel | src/page/login/authredirect.vue |
| 前端错误日志页 | 列表/CRUD |  |  | avue-crud 展示本地捕获的错误日志列表、上传服务器:确认后 dispatch SendLogs 上报、清空本地日志:确认后 CLEAR_LOGS、展开行查看错误堆栈 stack,日志类型 tag 标记、由 top-logs 全屏弹窗内嵌调用 | logsList、stack、type | src/page/logs/index.vue |

<details><summary>子组件清单(14)</summary>

- `layout.vue` — 仅含 router-view 的占位布局,供多级嵌套子路由挂载使用(loadPageLayout)
- `logo.vue` — 左上角品牌 Logo,根据菜单折叠状态在完整标题与缩略标识间淡入淡出切换
- `tags.vue` — 已访问页面的多标签导航,支持点击切换、关闭单个/其他/全部标签及右键菜单
- `index.vue` — 左侧主导航容器,创建时拉取后端动态菜单并格式化为路由,渲染纵向折叠菜单
- `sidebarItem.vue` — 递归渲染多级菜单项/子菜单,处理点击跳转、外链新窗口打开与角色权限过滤
- `index.vue` — 顶部工具栏:折叠按钮、可视化面板入口、打印访客条入口、用户下拉(个人信息/退出)
- `top-color.vue` — 通过 color mixin 动态切换 ElementUI 主题色的拾色器(当前页面无可见 UI,登录页隐藏引用)
- `top-lock.vue` — 顶部锁屏图标,首次锁屏弹窗设置锁屏密码,之后置锁并跳转锁屏页
- `top-logs.vue` — 顶部 bug 图标带未读错误日志数角标,点击全屏弹窗展示本地错误日志列表
- `top-menu.vue` — 顶部横向一级菜单,点击按 parentId 拉取下级菜单并跳转到首个可用页面
- `top-search.vue` — 顶部菜单自动补全搜索,扁平化全部菜单后按名称前缀匹配并跳转目标页(当前顶部栏已注释隐藏)
- `top-setting.vue` — 右侧滑出设置面板,展示版权信息并通过 avue-form 切换各项界面显隐开关
- `top-theme.vue` — 弹窗选择特色主题(当前仅保留"裕同"主题),切换后 setTheme 应用并持久化
- `userlogin.vue` — 用户名/密码登录表单,含记住密码、弱密码校验、平板专用跳转及验证码逻辑

</details>


## 3-platform-components · 平台公共业务组件

本模块是平台侧各业务页面共享的一组可复用表单控件,不含任何菜单/路由可达页面(router 中无任何引用)。核心是一批"远程数据下拉/级联选择器":每个组件封装 el-select 或 el-cascader,通过 v-model(value/input)双向绑定,mounted 或父级传入的上游 id(parkId/compId/depId/dormitoryId/floorId 等)变化时按需调后端接口拉取选项,并通过 doChange/getItem/defaultHandle 等自定义事件把选中项回吐给父组件,实现园区-楼栋-楼层-房间、公司-部门-岗位等级联联动。另有一个图片上传压缩组件。这些组件被招工、宿舍、车辆、设备权限等业务表单内嵌使用。

**页面 0 个 / 子组件 19 个**

<details><summary>子组件清单(19)</summary>

- `index.vue` — 图片卡片式上传,前端用 lrz 压缩并统一转 JPEG Base64 后通过 input/complete 事件回传父组件
- `index.vue` — 封装 el-cascader,加载 /platform/device/area/tree 设备区域树供级联选择
- `index.vue` — 按 parkId 加载车辆类权限列表(authority/list/3)的单选下拉,选中后回传整条 item
- `index.vue` — 按 parkId 加载人员类权限列表(authority/list/1)的多选下拉,collapse-tags 折叠展示
- `index.vue` — 按 parkId 加载人员类权限列表(authority/list/1)的单选下拉,选中后回传整条 item
- `index.vue` — 加载招工模块全部公司列表(recruitment/getComp)的下拉,支持单选/多选切换
- `index.vue` — 按 parkId 加载该园区下公司/BU 列表(parkbu/getByPark)的单选下拉
- `index.vue` — 封装 el-cascader,通过 getCompTree 加载公司-部门树供级联选择
- `index.vue` — 按 compId 加载该公司下部门列表(recruitment/getDep)的单选下拉
- `index.vue` — 按 parkId 查询宿舍楼列表(dormitory/queryDormitory)的多选下拉
- `index.vue` — 按 parkId 查询宿舍楼列表(dormitory/queryDormitory)的单选下拉,支持默认选中首项
- `index.vue` — 按 parkId+dormitoryId 查询楼层列表(dormitory/floor/queryFloor)的多选下拉
- `index.vue` — 按 parkId+dormitoryId 查询楼层列表(dormitory/floor/queryFloor)的单选下拉
- `index.vue` — 加载招工模块进出厂类别字典(recruitment/getJche)的单选下拉
- `index.vue` — 按 depId 加载该部门下岗位列表(recruitment/getJob)的单选下拉
- `index.vue` — 加载全部园区列表(park/all)的单选下拉,支持默认选中首项、可控清空
- `index.vue` — 纯前端静态枚举下拉(男/女/夫妻家属/其他),无后端接口
- `index.vue` — 按 parkId+dormitoryId+floorId 查询房间列表(dormitory/room/queryRoomList)的单选下拉
- `index.vue` — 按 parkId 加载该园区宿舍房型列表(dormitory/type/by/park)的单选下拉,支持默认选中首项

</details>


## 4-dormitory-room · 宿舍管理-房间/床位/门锁

本模块覆盖智慧园区宿舍管理的核心线下资产与人员入住闭环:房间/楼栋/楼层维护、宿舍分类(可住职层/床位数)、床位入住与退换宿、公摊及房间水电抄表、智能门锁绑定房间与开门记录审计、床位入住率监控统计。核心业务流为:园区-楼栋-楼层-房间-床位的层级树 → 配置房间类型与水电模板 → 员工/非员工办理入住(含外宿补贴/多套宿舍检查)→ 入住期间换宿/退宿/家属管理/补打凭条 → 床位监控与水电抄表统计 → 门锁绑定房间并记录开门方式。多数页面以园区为顶层筛选,左侧楼栋楼层树+右侧房间/床位卡片的双栏布局为主。

**页面 12 个 / 子组件 23 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 房间管理(可视化卡片) | 看板/大屏 | ✓ | /platform/dormitory/new_room | 顶部双图表展示园区总入住统计(chart1)与各楼栋统计(chart2)、按园区/楼栋/楼层/房间类型/入住状态(未满/已满/空房)/可住性别/仅异常房间多条件筛选查询、房间卡片以床位色块区分在职(绿)/离职未退宿(红)/未入职(蓝)/空床位四种状态,显示可住性别与实住/总床位、公摊抄表(跳转sd_public页)、批量抄电/批量抄热水/批量抄冷水(dlg_water_batch)、单房间水电抄表(dlg_water_dorm)、退换宿舍(dlg_change_check_out_dorm)与更换宿舍(dlg_dorm_change)、分页(8/16/24/32/40)、Pad端适配(隐藏图表、双列卡片) | 房间、床位、入住状态、园区统计、楼栋统计、水电抄表 | src/views/platform/dormitory/new_room/index.vue |
| 公摊水电表 | 列表/CRUD |  | /platform/dormitory/new_room/sd_public | 左栏列出所有公摊水电表卡片(表名/楼栋/公摊房间列表)、新增/编辑/删除公摊水电表(dlg_ammeter)、右栏avue-crud展示选中表的抄表记录(上月读数/当月读数/月度用量,修正读数标红)、新增抄表/编辑抄表/删除抄表读数(dlg_water_dorm_public)、按设备类型(热水/冷水/电)分类显示、分页 | 公摊水电表、抄表读数、设备类型、月度用量 | src/views/platform/dormitory/new_room/sd_public.vue |
| 床位管理 | 列表/CRUD | ✓ | /platform/dormitory/bed_mng | 左侧园区-楼栋-楼层-房间检索树(可过滤),按节点层级查询对应床位、按工号/姓名/BU/派遣公司/状态/是否空床位/入住时间区间搜索、空床位点击入住(_check_in),在住人员显示工号与人员tooltip(户籍/民族/手机/部门/职务)、换宿(dlg_dorm_change)、退宿(_check_out)、补打凭条(dlg_note_pre)、床位锁定/解锁开关(isLock,空床可锁定不参与分配)、非员工入住信息编辑(工号/姓名/性别/职务)、修改入住时间、修改床位编号、修改简易备注、添加标记列表(_remark_list)、夫妻/家属房添加与查看家属(dlg_family)、已离职人员快捷筛选+离职人数统计、先入住未报道(status=-1)筛选与批量移除、入住信息Excel导入(import向导)、导出床位明细表格、导出家属列表、查询条件随路由query回填(从可视化/列表跳入保留状态) | 床位、入住人员、员工/非员工、家属、标记备注、退宿换宿、离职状态 | src/views/platform/dormitory/bed_mng/index.vue |
| 床位入住-选择员工 | 表单 |  | /platform/dormitory/bed_mng/check_in/:id | 按工号/姓名/园区/BU/部门搜索员工(性别按房间属性过滤)、确认添加触发入住检查(callOwanceDetails:是否申请过外宿补贴/是否已拥有多套宿舍)、选择入住日期并确认入住(addDormitoryStaff)、返回床位管理或可视化页(按fromNew来源) | 空床位、员工、外宿补贴检查、多套宿舍检查、入住日期 | src/views/platform/dormitory/bed_mng/check_in.vue |
| 床位监控统计 | 看板/大屏 | ✓ | /platform/dormitory/bed_control | 按园区+多选楼栋筛选、床位统计卡片(按职层/福利等级显示总数及男/女/其他入住数与进度条)、切换按楼层统计/按房间类型统计/按宿舍空位统计三种表格(应住/实住/剩余)、三种维度分别导出Excel | 床位统计、楼层统计、房间类型统计、空位统计、入住率 | src/views/platform/dormitory/bed_control/index.vue |
| 门锁绑定 | 列表/CRUD | ✓ | /platform/dormitory/lock_bind | 按门锁名称搜索/清空、avue-crud展示门锁(名称/连接状态/已绑定房间)、点击绑定房间打开选择弹窗(bind_room)、分页 | 门锁设备、绑定房间、连接状态 | src/views/platform/dormitory/lock_bind/index.vue |
| 门锁绑定-选择房间 | 弹窗页 |  |  | 园区-楼栋-楼层树(可过滤,仅楼层节点触发房间查询)、房间卡片展示(房号/可住性别/空床位数/总床位)并单选、二次确认后绑定门锁到房间(deviceArea修改为楼栋/房间) | 门锁、房间、楼层树、绑定区域 | src/views/platform/dormitory/lock_bind/bind_room.vue |
| 门锁列表(电量监控) | 列表/CRUD | ✓ | /platform/dormitory/lock_list | 按园区/设备名称/剩余电量/设备状态(断开/正常)搜索、门锁卡片按状态显示三种图标(正常/低电量/断开)及剩余电量百分比、显示当前园区电量阈值,低于阈值标记低电量、图例(正常/断开/低电量)、分页(混入tce.mixins.list) | 门锁设备、剩余电量、电量阈值、连接状态 | src/views/platform/dormitory/lock_list/index.vue |
| 开门记录 | 列表/CRUD | ✓ | /platform/dormitory/door_open_record | 按园区/门锁名称/姓名/工号/开门方式(密码/指纹/卡片/远程开门)搜索、avue-crud展示开门记录(门锁名称/绑定房间/人员编号/姓名/开门方式/开门时间)、导出开门记录统计Excel、分页 | 开门记录、门锁、开门方式、开门时间、人员 | src/views/platform/dormitory/door_open_record/index.vue |
| 房间管理(列表) | 列表/CRUD |  | /platform/dormitory/room | 左侧楼栋楼层树:新增楼栋/楼层、编辑楼栋(名称)/楼层(房间数量)、删除楼栋/楼层(添加楼层支持起始编号自动读取、楼层数量上限14)、按是否参与分配/是否参与计算/宿舍分类/房间属性筛选、房间块按性别(男/女/夫妻混住)着色,不参与分配标记锁角标、编辑单房间(是否参与分配/计算、宿舍分类、床位数、房间属性、水电分摊模板、离职结算模板)、全选+批量设置房间类型(分配/计算/分类/属性)、批量设置房间水电分摊模板、删除房间、导出房间列表Excel | 房间、楼栋、楼层、宿舍分类、水电模板、房间属性、参与分配/计算 | src/views/platform/dormitory/room/list.vue |
| 房间管理可视化 | 看板/大屏 | ✓ | /platform/dormitory/room/visual | 选择园区显示入住率环形图(echarts)与楼栋入住进度、选择楼栋后加载楼层,支持楼层多选/全选、按楼层渲染整栋楼房间网格(未满/已满/空房状态色+男/女/夫妻/其他性别点)、房间下拉:详情(跳床位管理按roomId)、编辑房间(分配/分类/床位数/性别)、删除房间、切换到房间列表页(goListPage) | 房间、楼栋、楼层、入住率、房间状态 | src/views/platform/dormitory/room/visual.vue |
| 宿舍分类 | 列表/CRUD | ✓ | /platform/dormitory/dorm_type | 按所属园区搜索/清空、avue-crud新增/编辑/删除宿舍分类(行内表单)、分类关联多个职层(avue-crud-select多选,typeCode)、选择所属园区、分页 | 宿舍分类、职层、园区、床位数 | src/views/platform/dormitory/dorm_type/index.vue |

<details><summary>子组件清单(23)</summary>

- `dlg_ammeter_batch.vue` — 房间管理页批量抄电对话框组件(代码中已注释停用)
- `dlg_family.vue` — 查看/管理某入住人员家属的对话框组件,内嵌家属列表与新增家属
- `chart-item.vue` — 基于echarts的单个占比饼图渲染子组件
- `chart2.vue` — 房间管理页各楼栋入住统计图表组件(无数据占位)
- `dlg_change_check_out_dorm.vue` — 对房间内入住人员进行退宿或换宿操作的对话框组件
- `chart1.vue` — 房间管理页园区总入住统计图表组件(无数据占位)
- `dlg_water_batch.vue` — 房间管理页批量抄热水/冷水/电的对话框组件(按itemType区分)
- `dlg_ammeter.vue` — 公摊水电页新增/编辑公摊水电表(选房间/设备类型)的对话框组件
- `dlg_family_list.vue` — 以avue-crud展示家属列表(关系等)的内嵌子组件
- `dlg_check_out_dorm.vue` — 单人退宿操作的对话框组件
- `dlg_water_dorm.vue` — 对单个房间进行水电抄表录入的对话框组件
- `dlg_add_family.vue` — 为入住人员新增家属信息的对话框组件
- `dlg_water_dorm_public.vue` — 公摊水电表新增/编辑单次抄表读数的对话框组件
- `_check_in_staff.vue` — 床位入住弹窗内按工号检索员工详情办理入住的表单子组件
- `_check_in_non_staff.vue` — 床位入住弹窗内手填工号/姓名等办理非员工入住的表单子组件
- `_check_out.vue` — 床位管理页对在住人员办理退宿的对话框子组件
- `_remark_list.vue` — 查看人员标记历史并入口新增标记的对话框子组件
- `_add_remark.vue` — 为入住人员添加标记备注的对话框子组件
- `_check_in.vue` — 床位入住主对话框,切换员工/非员工入住表单的容器子组件
- `dlg_note_pre.vue` — 入住凭条打印预览(补打凭条)的对话框子组件
- `step2.vue` — 入住信息批量导入向导第二步(批量导入执行)子组件
- `index.vue` — 床位管理页入住信息导入弹窗内的分步向导容器子组件
- `step1.vue` — 入住信息批量导入向导第一步(导入Excel数据)子组件

</details>


## 5-dormitory-checkin · 宿舍管理-入住/退宿/授权/报修/住宿员工

该模块覆盖智慧园区宿舍的全生命周期业务:快速入住(读身份证自动/手动分配床位并打印凭条)、员工入住申请的审批分配与退回、门锁授权(给员工授权门锁、设定有效期、取消/重新授权)、离职退宿水电结算记录、宿舍/园区报修工单回复审批,以及按楼栋树查询的住宿员工入住/退宿台账。核心业务流:员工提交入住申请→宿管自动/手动分配床位或现场快速入住→生成入住记录并打印凭条→入住期间可换宿/门锁授权/报修→离职或退房时退宿并生成水电结算记录。各 index.vue 为后端动态下发菜单的主入口页,报修详情为显式路由子页。

**页面 9 个 / 子组件 13 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 快速入住 | 特殊/公开页 | ✓ | /platform/dormitory/ks_checkIn/index | 两步流程:第一步基础设置(分配规则/园区/楼栋/房间类型,手动时选房间号+床位号),第二步录入入住信息、分配规则切换:自动分配(基于预设房源关联匹配)/手动分配(自定义选具体房间床位)、园区/楼栋(楼栋-楼层级联cascader)/房间类型联动选择,本地localStorage记忆上次园区房型信息、WebSocket(ws://127.0.0.1:33666)连接读卡控件读取二代身份证,自动回填姓名/性别/民族/身份证号/住址/签发机关/出生日期/有效期并自动入住、手动分配前比对入住人员性别与房间性别(男/女房才校验,夫妻/其他不校验)、入住前调用checkDormInInfo做重复入住校验,确认后autoallot分配床位、床位信息表展示分配结果,高亮当前分配行(isFlag),姓名列tooltip展示工号/性别/部门/职级、补打凭条(凭条预览弹窗)、换宿(更换床位弹窗)、退宿(复用bed_mng退宿组件)、分配成功自动触发打印入住凭条(裕同科技凭条48mm小票打印)、手动分配成功后自动取下一个可用空床位 | 入住人员(姓名/性别/民族/身份证号/住址/签发机关/出生日期/有效期)、园区、楼栋、楼层、房间类型、房间、床位、入住记录、入住凭条 | src/views/platform/dormitory/ks_checkIn/index.vue |
| 入住申请 | 列表/CRUD | ✓ | /platform/dormitory/check_in_apply/index | 按工号/姓名/申请时间/状态(申请中/申请通过/已退回/已撤销)/园区检索、列表展示园区/工号/姓名/BU(tooltip部门职层)/房间喜好/备注/状态/申请时间、自动分配:调recommend推荐床位后弹窗确认applyManual分配、手动分配:弹窗按宿舍树选房选床applyManual分配、退回:弹窗选退回原因(床位已满/已申请外宿/其他自填)failBack退回、状态为申请中(status=1)时三个操作才可用、分页(size/current change) | 入住申请单、申请人(工号/姓名/BU/部门/职层)、房间喜好、申请状态、床位 | src/views/platform/dormitory/check_in_apply/index.vue |
| 门锁授权管理 | 列表/CRUD | ✓ | /platform/dormitory/grant_auth/index | 按园区/工号/姓名/手机号/门锁名称/状态(待授权/已授权/授权失败/已失效)/授权开始时间区间检索、列表展示门锁名称/工号/授权人员/手机号/授权时间(含长期有效)/状态/钥匙类型备注(keyList)、人员授权(新增):查询人员勾选+穿梭框选门锁设备+设定有效期(长期/指定时间)批量授权addObjBatch、取消授权(已授权状态,二次确认后cancelAuth移除设备授权)、重新授权(非已授权状态,弹窗重设有效期reAuth)、编辑授权有效期、删除授权记录(delObj)、导出授权记录excel(门锁/人员编号/授权人员/授权时间/状态/备注)、isAvailable=1时操作按钮禁用 | 授权记录、授权人员(工号/姓名/手机号)、门锁设备、钥匙(卡号/指纹/密码)、授权有效期、授权状态 | src/views/platform/dormitory/grant_auth/index.vue |
| 离职退宿生成记录 | 列表/CRUD | ✓ | /platform/dormitory/resignation_record/index | 按园区/BU/工号/姓名/离职日期/结算时间检索(离职日期与结算时间拆成起止时间区间)、列表展示园区/工号/姓名/BU/部门/个人扣款/离职日期/状态/生成时间、详情弹窗(离职天数/退宿日期/上月抄表时间/最终离职天数/生成记录+对外接口调用日志)、日志存储策略设置(只保留最近N天调用日志,ruleSet配置businessType=4)、导出离职水电生成记录excel | 离职退宿记录、离职员工(工号/姓名/BU/部门)、个人扣款(水电费)、离职天数、抄表时间、接口调用日志 | src/views/platform/dormitory/resignation_record/index.vue |
| 住宿员工(入住/退宿台账) | 特殊/公开页 | ✓ | /platform/dormitory/staff/index | 左侧园区/楼栋/楼层/房间四级树检索(allList)并按节点层级过滤、关键字过滤树节点、tab切换入住记录(check_in)与退宿记录(check_out)、选中树节点联动子表按园区/楼栋/楼层/房间查询 | 园区、楼栋、楼层、房间、住宿员工 | src/views/platform/dormitory/staff/index.vue |
| 入住记录(子表) | 列表/CRUD |  |  | 按工号/姓名/BU/性别/职层(jcheSelect)/入住时间区间检索、列表展示园区/楼栋/房间/床位/工号/姓名/性别/BU/组织/岗位/职层/入住时间/操作人、点击入住时间弹窗修改(updateDormitoryStaff,不可选未来日期)、导出入住信息excel(性别/组织等回退dor字段) | 入住记录、住宿员工(工号/姓名/性别/BU/组织/岗位/职层)、床位、入住时间 | src/views/platform/dormitory/staff/check_in.vue |
| 退宿记录(子表) | 列表/CRUD |  |  | 按工号/姓名/BU/性别/职层/退宿类型(换宿/外宿/离职/退房/自离)/入住时间/退宿时间区间检索、列表展示园区/楼栋/房间/床位/工号/姓名/性别/BU/组织/岗位/职层/退宿类型/入住退宿时间,离职自离类型红色标记、点击退宿时间弹窗修改(updateCheckOutTime)、删除退宿记录(delObj二次确认)、导出退宿信息excel | 退宿记录、退宿类型(换宿/外宿/离职/退房/自离)、住宿员工、床位、退宿时间 | src/views/platform/dormitory/staff/check_out.vue |
| 宿舍/园区报修 | 列表/CRUD | ✓ | /platform/dormitory/repairs/index | 按维修区域(宿舍/办公室/车间/园区周边)/维修类别(灯/插座/水龙头/空调/床等)/申请时间区间/状态检索、列表展示维修区域/类别/所在楼栋/房间/故障描述/申请时间/申请人/状态、状态机:待审批/待确认/已安排维修/维修成功/已关闭/已通过/已拒绝、查看跳转报修详情页(带queryPage/queryForm参数)、待确认(status=1)回复:已安排人员维修/无法维修关闭单;已安排(status=2)回复:维修完毕/无法维修关闭单(replyRepair带回复内容)、导出园区报修信息excel、详情返回带参数恢复列表查询条件 | 报修工单、维修区域、维修类别、故障描述、报修状态、回复记录 | src/views/platform/dormitory/repairs/index.vue |
| 宿舍报修详情 | 详情 |  | /platform/dormitory/repairs/detail/:id | 展示状态/姓名/工号/BU/部门/维修区域类别/维修位置(楼栋#房间)/园区/故障描述、故障照片viewer图片查看、审批信息时间轴(提交节点+各审批节点,result待审批/通过/拒绝/关闭/等待多色标记+备注)、回复记录列表(回复人/时间/状态/描述)、返回报修列表(携带原查询条件) | 报修工单、故障照片、审批流程节点、审批人、回复记录 | src/views/platform/dormitory/repairs/detail.vue |

<details><summary>子组件清单(13)</summary>

- `dlg_dorm_change.vue` — 快速入住页换宿弹窗:左侧房间树+右侧床位可选/不可选/禁用状态,选新床位确认换宿(changeBed)
- `dlg_dorm_select.vue` — 手动分配选房弹窗:按空余床位数(1-8)和房间属性(男/女/夫妻家属/其他)筛选楼层房间,点房间再选床位
- `dlg_note_pre.vue` — 入住凭条预览弹窗:展示裕同科技欢迎凭条(姓名/性别/房间室床/楼栋/时间),调用vue-print-nb打印48mm小票
- `dlg_dorm_beds.vue` — 通用床位选择弹窗:按roomId拉取床位列表,空床可选/已住不可选/禁用三态,选中后回传床位对象
- `index.vue` — 房间类型联动下拉组件:按园区+楼栋请求/platform/dormitory/type/by/park-and-dormitory返回房型列表
- `dlg_auto_check_in.vue` — 入住申请自动分配确认弹窗:展示系统推荐的楼栋/楼层/房间号/床位/房型,确认后applyManual完成分配
- `dlg_send_back.vue` — 入住申请退回弹窗:选择退回原因(预设三选+其他自填)调用failBack退回申请
- `dlg_dorm_manual.vue` — 入住申请手动分配弹窗:按applyId拉宿舍树,选房间后展示床位选床,applyManual确认入住
- `AddDialog.vue` — 新增/编辑授权弹窗:按姓名工号查人员勾选(无卡号指纹密码者不可选)、穿梭框选门锁、设有效期(长期/指定时间)批量授权
- `AuthRoomsDialog.vue` — 授权房间查看弹窗:以方块形式展示该授权关联的房间号(X室)列表,只读关闭
- `ReAuthDialog.vue` — 重新授权弹窗:对失效/失败的授权重设有效期(长期有效/指定时间区间)调用reAuth重新下发
- `settlement_rule.vue` — 日志存储策略弹窗:设置离职水电对外接口调用日志只保留最近N天,超期自动删除(ruleSet)
- `detail.vue` — 离职退宿记录详情弹窗:tab展示离职明细(园区/工号/离职退宿日期/抄表/离职天数)与对外接口调用日志(请求/响应报文)

</details>


## 6-dormitory-utility · 宿舍管理-水电抄表/计算/统计/告警

该模块覆盖宿舍/厂区水电业务全链路:先在「离职扣费规则配置(dhr_set)」「水电分摊模板(sd_templates)」里维护计费/分摊规则,再通过「水电抄表(sd_meterread)」按普通表与公摊(热水/冷水/电)分类录入上/本月底数,「房间水电计算(water_room_calculate)」批量计算并生成不可修改的结算明细;明细按房间(room_sdstatement)和个人(staff_sdstatement/water_inStaff)出结算/扣款报表并支持导出与EHR同步;另有日计算表(water_room_day/daily_statistics)、用量排行与设备用量统计(water_statistics/water_usage_statistics/_new)、告警记录(water_warning_record)及楼层床位统计(floor_statistics)等查询/导出页。核心业务流为:规则配置→抄表→计算生成明细→结算/扣款报表→统计与告警。

**页面 14 个 / 子组件 14 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 离职水电扣费规则配置 | 列表/CRUD | ✓ | /platform/dormitory/dhr_set/index | 按园区(parkSelect)筛选规则模板列表(avue-crud分页)、新增/编辑规则模板(模板名称必填,弹框表单)、删除规则模板(二次确认)、行操作:水电扣费金额(打开deduction_set弹框)、行操作:BU使用范围(打开setBu弹框)、行操作:使用宿舍(打开bind_room弹框)、行操作:结算规则(打开settlement_rule弹框) | 园区park、扣费规则模板(templateName/parkId)、tempId | src/views/platform/dormitory/dhr_set/index.vue |
| 水电抄表 | 列表/CRUD | ✓ | /platform/dormitory/sd_meterread/index | 四个分类Tab切换(普通水电表/公摊热水表/公摊冷水表/公摊电表)、按园区/楼栋/楼层/房间号/抄表月份/抄表状态/结算状态多条件筛选、生成水电明细(二次确认+全屏loading,generateSDStatementDetail,生成后不可改)、查看抄表数据(各类目上/本月底数,getRoomMeterReadDetail)、抄表录入(取上月底数自动带入,getPerMonthDetail)、修改入住人天(打开dlg_edit_avg,仅未结算且已抄完)、查看人天修改记录(dlg_record_day_modify)、查看上月止度修改记录(dlg_record,isRevise)、抄表状态/结算状态文案渲染,行内新增房间水电记录弹框 | 房间水电记录mrId、抄表类目categoryId(1热水/2冷水/3电)、上月底数preMonthNum/本月底数curMonthNum、抄表状态status/结算状态statementStatus、抄表月份meterMonth | src/views/platform/dormitory/sd_meterread/index.vue |
| 房间水电计算 | 特殊/公开页 | ✓ | /platform/dormitory/water_room_calculate/index | 左侧楼栋/楼层/房间树检索(关键字过滤,选楼栋/楼层/房间加载)、按抄表月份查询房间用电/冷水/热水实用-标准-超标及单价、超标金额、住宿总天数、日平均金额(自定义多级表头表格)、导入抄表数据(打开importMeterData弹框)、导出抄表数据(打开exportMeterData弹框)、计算水电(按楼栋,二次确认+loading,generateSDStatementDetail,生成后不可改)、批量保存水电(按楼栋/楼层/房间,resetSdDetail)、行内修改水电抄表(打开dlg_water_dorm)、点击住宿总天数修改入住人天(复用dlg_edit_avg)、有变更行高亮(黄色) | 房间用量明细(elePreMonthNum/eleCurMonthNum/eleUse/eleQty/eleOverUse/coldUse...)、超标金额totalAmount/住宿总天数inDays/日平均金额avgAmount、抄表月份meterMonth | src/views/platform/dormitory/water_room_calculate/index.vue |
| 员工个人水电抄表 | 列表/CRUD | ✓ | /platform/dormitory/water_inStaff/index | 左侧楼栋/楼层/房间树检索过滤、按抄表月份查询个人水电记录(avue-crud分页)、导出表格(个人水电,打开exportMeterData)、部门分摊水电表导出(打开exportShareData)、在住状态(staffStatusInit)及创建/入住/离开/抄表时间格式化展示 | 员工个人水电记录(status/inTime/outTime/meterMonth)、楼栋树treedata | src/views/platform/dormitory/water_inStaff/index.vue |
| 水电分摊模板 | 列表/CRUD | ✓ | /platform/dormitory/sd_templates/index | 按园区/级层/分摊模板名称筛选列表(avue-crud分页)、新增分摊模板(园区+模板名称+员工层级,addTemplate)、编辑分摊模板名称(updateDormitorySDTemplate)、删除模板(二次确认,delTemplate)、配置分摊模板(打开setTemp配置弹框)、加载员工级层下拉(queryJobJchenList) | 分摊模板(templateName/parkId/jchenid/jchenname)、员工级层jobJchenList | src/views/platform/dormitory/sd_templates/index.vue |
| 水电用量排行统计 | 列表/CRUD | ✓ | /platform/dormitory/water_statistics/index | 按园区/楼栋筛选、统计时间按月/按日切换(联动日期选择器类型)、起止日期/月份选择(自动纠正顺序)、统计类型用水/用电切换、导出Excel(exportExcel,占位)、用量排行列表(fetchStatementList,avue-crud分页) | 水电结算/用量记录、统计时间区间、统计类型(用水/用电) | src/views/platform/dormitory/water_statistics/index.vue |
| 水电用量统计(年度) | 列表/CRUD |  | /platform/dormitory/water_usage_statistics/index | 按年份查询(禁选未来年份)、区域类型(厂区/宿舍)与水电类型(热水/冷水/电)文案渲染、展示年用量及1-12月分月用量(avue-crud分页)、导出Excel(自定义表头,export_json_to_excel) | 用量统计(placeType区域类型/areaName/sdType水电类型/deviceName/yearUse/month1Use..month12Use) | src/views/platform/dormitory/water_usage_statistics/index.vue |
| 水电用量统计(设备明细) | 列表/CRUD |  | /platform/dormitory/water_usage_statistics_new/index | 时间快捷选择(今日/本月/上月/今年/去年)+自定义日期区间、类型筛选(全部/冷水/热水/电表)、按区域级联(areaCascader)与设备标签(deviceTagSelect)筛选、展示设备起数/止数/累计用量、通讯地址、关联集中器(avue-crud分页)、导出Excel(查询段起止数与累计用量) | 设备用量(areaName/deviceName/sdType/deviceTag/commAddress/concentratorName/startNum/endNum/sumNum)、时间区间startDate/endDate | src/views/platform/dormitory/water_usage_statistics_new/index.vue |
| 宿舍水电日计算表 | 列表/CRUD | ✓ | /platform/dormitory/water_room_day/index | 按房间号/结算月份筛选、水电日计算结算列表(fetchStatementList,avue-crud分页) | 房间日计算结算记录(roomName/meterMonth) | src/views/platform/dormitory/water_room_day/index.vue |
| 宿舍水电日统计表 | 特殊/公开页 |  | /platform/dormitory/daily_statistics/index | 左侧楼栋/楼层/房间树检索过滤(必须选楼栋或楼层)、按结算时间区间(datetimerange,默认昨天全天)查询、展示各房间电/冷水/热水昨日-今日读数及用电/用水实用量、抄表日期(自定义多级表头表格) | 房间日用量(preEleNum/curEleNum/actEleNum/preColdNum/curColdNum/preHotNum/curHotNum/actWaterNum/meterMonth) | src/views/platform/dormitory/daily_statistics/index.vue |
| 水电告警记录 | 列表/CRUD | ✓ | /platform/dormitory/water_warning_record/index | 按警告时间/警告类型/处理状态(未处理/已处理/已忽略)/警告内容筛选、查看告警详情弹框(告警类型/内容/来源/时间)、批量忽略报警/批量已处理(基于多选)、详情弹框内忽略/已处理、告警列表(avue-crud分页,多选) | 告警记录(告警类型/内容/来源/时间/处理状态) | src/views/platform/dormitory/water_warning_record/index.vue |
| 房间水电结算明细 | 列表/CRUD | ✓ | /platform/dormitory/room_sdstatement/index | 多条件筛选(园区/楼栋/楼层/房间号/抄表月份)、结算状态文案(已生成/已同步)渲染、查看明细弹框:房间结算(普通/公摊各类目上下月底数、实际用量、总配额、超出用量/单价、费用小计、分摊房间数与均摊费用)、查看明细弹框:个人结算(工号/姓名/入住日期/入住总天数/修正天数/结算天数/日均摊费用/个人结算费用)、弹框内锚点导航(房间结算/个人结算)、结算列表(fetchStatementList,avue-crud分页) | 房间结算categoryDataList(meterType/categoryId/preMonthNum/curMonthNum/actualQty/totalQty/overQty/overFee/totalFee/roomNum/roomAvgFee)、个人结算staffStatmentDataList(staffBadge/staffName/inTime/inTotalDays/reviseDays/statementDays/avgFee/statementFee/fee) | src/views/platform/dormitory/room_sdstatement/index.vue |
| 个人水电扣款明细 | 列表/CRUD | ✓ | /platform/dormitory/staff_sdstatement/index | 园区/BU/部门级联筛选(el-cascader,getCompTree)、按工号/姓名/扣款月份筛选、个人扣款明细列表(住宿天/日平均金额/个人扣款/扣款月份等,avue-crud分页)、导出表格(工号/姓名/BU/部门/入住日期/房号/住宿天/日平均金额/个人扣款/扣款月份/扣款来源园区)、入住日期格式化展示 | 个人扣款明细(badge/name/compName/depName/inTime/roomName/inDays/avgFee/fee/meterMonth/parkName) | src/views/platform/dormitory/staff_sdstatement/index.vue |
| 楼层床位统计报表 | 特殊/公开页 | ✓ | /platform/dormitory/floor_statistics/index | 按园区/多楼栋/房间职层/房间属性(性别)筛选、自定义统计表:可用房间/标配人数/标配床位/锁定床位/可住人数/已住人数/空床位/单独空房间、按性别分组小计(fl_sum过滤器汇总)、导出楼层统计报表(exportApi,js-file-download保存.xls)、园区默认选中后自动加载(defaultHandle) | 楼层床位统计(dormitoryDesc/roomTypeDesc/roomNum/typeBedTotal/standardBedNum/lockBedNum/roomBedTotal/alreadyUse/freeBedNum/freeRoomNum/roomSexDesc) | src/views/platform/dormitory/floor_statistics/index.vue |

<details><summary>子组件清单(14)</summary>

- `settlement_rule.vue` — 设置离职当天是否计算水电及规则模板(集团石岩规则)的弹框,提交businessType=4/configType=5规则配置
- `deduction_set.vue` — 按职员层维护离职水电扣费项目列表,可新增/编辑/删除扣费项
- `deduction_add.vue` — 为选定职层配置1-12月每日平均水/电计算标准的扣费录入弹框,支持按月批量填充
- `bind_room.vue` — 通过房间树勾选规则模板适用的宿舍房间,带关键字过滤与回显并提交绑定
- `setBu.vue` — 用穿梭框为规则模板配置适用的BU(分子公司)使用范围并提交
- `dlg_edit_avg.vue` — 结算前修改房间内各人员入住天数及修改原因,影响个人日均摊费用
- `dlg_record.vue` — 以表格展示房间各类目上月止度被重置/修改的记录(无则显示'无')
- `dlg_record_day_modify.vue` — 以表格展示房间内人员入住天数的修改历史记录
- `dlg_water_dorm.vue` — 修改单个房间的电/冷水/热水上下月表读数(支持换表前后),实时计算实用/标准/超标用量
- `index.vue` — 按园区/楼栋/月份下载抄表Excel模板,填写后上传导入水电抄表数据
- `index.vue` — 按园区与多楼栋导出当月房间抄表数据(exportData)
- `index.vue` — 按园区与多楼栋导出员工个人抄表数据(exportData)
- `exportShareData.vue` — 按园区与多楼栋导出部门分摊水电表(exportShareData)
- `setTemp.vue` — 为水电分摊模板配置各类目(热水/冷水/电)1-12月的标准用量与超标单价,支持按月批量修改

</details>


## 7-device · 设备管理

智慧园区/厂区的设备接入与控制中枢，覆盖人脸/刷卡类通行设备(门禁机、闸机、考勤机、门禁、道闸)、能源计量设备(水表、电表、阀门及其集中器)、门锁与摄像头。核心业务流：录入设备并绑定园区区域/房间→设备在线状态监控→对人员/车辆授权并向设备下发(含重新下发、一键清空、删除授权)→采集运行数据(抄表读数、操作记录、下发记录、设备更换记录)。门禁/闸机/道闸侧以"设备-权限-通关名单"为主线；水电表侧以"集中器-表计-阀门控制-抄表"为主线。设备标签贯穿全模块用于分组批量操作。

**页面 30 个 / 子组件 11 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 门禁机管理 | 列表/CRUD | ✓ | /platform/device/entrance_guard | 按设备名称/所在区域/接通状态(未连线/离线/在线)/设备标签搜索与清空、卡片网格展示在线状态、设备名称、所属区域、设备标签、卡机类型(刷脸/刷卡/刷脸+刷卡)、添加门禁机(名称/厂家海康大华/卡机类型/账号/序列号/密码/IP/端口/区域/进出类型/体温检测开关与阈值/标签)、编辑门禁机(ISC同步设备字段只读)、删除门禁机、设备设置弹窗:按园区批量配置各设备体温检测开关与阈值、勾选设备批量设置标签、一键清空设备授权(加入清空队列)、重新下发所有人脸记录(全屏loading)、跳转更新设备权限(gate_limit,deviceType=4)、跳转通关人员名单(person_list)、分页 | 门禁机设备(deviceName/deviceCode/deviceIp/devicePort/deviceVendor/deviceCapability/eventType/thermalEnable/thermalThreshold/isSync)、区域/园区、设备标签 | src/views/platform/device/entrance_guard.vue |
| 闸机管理 | 列表/CRUD | ✓ | /platform/device/gate | 按设备名称/区域/接通状态/设备标签搜索清空、卡片展示在线状态、名称、区域、标签、卡机类型、添加闸机(厂家/卡机类型/账号/序列号/密码/IP/区域/进出类型/端口/体温检测/标签/设备标识考勤机或门禁)、编辑/删除闸机、设备设置弹窗按园区批量配置体温检测、批量设置标签、一键清空授权、重新下发人脸记录、跳转更新设备权限(gate_limit,deviceType=1)、跳转通关人员(person_list,deviceType=1)、分页 | 闸机设备、区域/园区、设备标签、deviceTag(考勤机/门禁) | src/views/platform/device/gate.vue |
| 考勤机管理 | 列表/CRUD | ✓ | /platform/device/attendance | 按设备名称/区域/接通状态/标签搜索清空、卡片展示在线状态、名称、区域、标签、卡机类型、添加考勤机(厂家/卡机类型/账号/序列号/密码/IP/区域/进出类型/端口/体温检测/标签)、编辑/删除考勤机(ISC同步设备只读)、设备设置弹窗按园区批量配置体温检测、批量设置标签、一键清空授权、重新下发人脸记录、跳转更新设备权限(gate_limit,deviceType=3)、跳转通关人员(person_list,deviceType=3)、分页 | 考勤机设备、区域/园区、设备标签 | src/views/platform/device/attendance.vue |
| 门禁管理 | 列表/CRUD | ✓ | /platform/device/xc_guard | 按所在区域/接通状态/启用状态搜索清空、卡片展示在线状态、名称、区域、标签、启停开关、添加门禁(名称/厂家/账号/序列号/密码/IP/区域/端口/进出类型/标签)、编辑/删除门禁、批量设置标签、一键清空授权、重新下发人脸记录、跳转通关人员(person_list,deviceType=2)、分页 | 门禁设备、区域/园区、设备标签、enableStatus启停 | src/views/platform/device/xc_guard/index.vue |
| 道闸管理 | 列表/CRUD | ✓ | /platform/device/automatic | 按所在区域/接通状态搜索清空、卡片展示在线状态、名称、区域、标签、添加道闸(名称/厂家海康大华臻识道尔/账号/序列号/密码/IP/区域/端口/子类型/进出类型/LED显示屏IP与端口/标签)、编辑/删除道闸、一键清空授权、跳转编辑LED显示屏信息(ledInfo,仅海康)、跳转通关车辆(vehicle_list)、跳转更新设备权限(automatic_limit)、批量设置标签、分页 | 道闸设备(deviceSubtype/ledScreenIp/ledScreen)、区域/园区、车辆通行子类型parking、设备标签 | src/views/platform/device/automatic.vue |
| 摄像头管理 | 列表/CRUD | ✓ | /platform/device/camera | 按园区级联/接通状态/启用状态搜索清空、卡片展示在线状态、通道名称、区域、启停开关(切换即更新)、添加摄像头(通道号/通道名称/厂家/通道管理号/IP/管理端口/协议类型/账号/密码/所属区域)、编辑摄像头、删除摄像头、区域级联树加载、分页 | 摄像头通道(channelNo/channelManager/protocolType/deviceType=4)、区域树 | src/views/platform/device/camera.vue |
| 闸机/门禁更新设备权限 | 特殊/公开页 |  | /platform/device/gate_limit | 按姓名搜索人员列表(带人脸头像单选)、选中人员后加载其拥有的可删除设备权限树(树形勾选,仅授权过的可选)、勾选要删除的设备并批量删除授权、删除后按deviceType(1闸机/3考勤/4门禁)跳回对应设备页、取消返回 | 人员(name/cardNo/faceImage)、设备权限树、deviceType | src/views/platform/device/gate_limit.vue |
| 道闸更新设备权限 | 特殊/公开页 |  | /platform/device/automatic_limit | 按车牌号搜索车辆列表(自动转大写,单选)、选中车辆后加载其拥有的道闸设备权限树、勾选并批量删除设备授权、删除后返回道闸管理页、取消返回 | 车辆(plate/cardNo)、设备权限树 | src/views/platform/device/automatic_limit.vue |
| 道闸LED显示屏信息编辑 | 表单 |  | /platform/device/ledInfo/:id | 按显示场景切换(正常/有权限过车/无权限过车)加载配置、每排文字设置文字颜色(7色)、文字运动(静止/上下左右移动/闪烁)、内容类型(文本/时间)、时间类型选择日期格式模板(YYYY-MM-DD等)、文本类型插入余位{ps}/车牌号{pn}占位符、每排可勾选本次是否设置,四排独立、语音文字插入欢迎光临/停车检查/等待确认模板、保存校验至少设置一排并下发、取消返回道闸页 | LED屏配置(displayScene/ledAreaList每行areaColor/areaAction/areaType/areaContent/soundText)、deviceCode | src/views/platform/device/ledInfo.vue |
| 通关人员名单 | 列表/CRUD |  | /platform/device/person_list/:id | 按姓名/工号/创建时间范围搜索清空、avue表格展示人脸头像(viewer可放大)、姓名、工号等、删除单个通关人员授权(加入删除队列,删除中状态)、按deviceType返回对应设备页(携带原查询条件)、分页 | 通关人员(name/badge/cardNo/faceImage/status)、deviceId/serialNo/deviceType | src/views/platform/device/person_list.vue |
| 通关车辆名单 | 列表/CRUD |  | /platform/device/vehicle_list/:id | 按车牌号/车主姓名搜索清空、avue表格展示车辆信息、删除单个通关车辆授权(加入删除队列,删除中状态)、返回道闸管理页(携带原查询条件)、分页 | 通关车辆(plate/name/cardNo/status)、deviceId/serialNo | src/views/platform/device/vehicle_list.vue |
| 设备标签管理 | 列表/CRUD | ✓ | /platform/device/xc_device_tag | 按标签名称搜索清空、avue表格展示标签名称、添加标签(弹窗)、编辑标签、删除标签、分页 | 设备标签(tagName) | src/views/platform/device/xc_device_tag/index.vue |
| 授权人员 | 列表/CRUD | ✓ | /platform/device/xc_auth_person | 按设备名称/工号/设备类型(闸机/门禁/道闸)/所在区域搜索清空、avue表格展示设备名称、设备类型、区域、工号、授权人员、创建时间、删除单条授权(按cardNo+deviceCode,加入删除队列)、导出Excel(授权人员导出.xls)、分页 | 授权人员记录(deviceName/deviceType/areaName/badge/staffName/cardNo/deviceCode) | src/views/platform/device/xc_auth_person/index.vue |
| 授权车辆 | 列表/CRUD | ✓ | /platform/device/xc_auth_car | 按设备名称/车牌号/所在区域搜索清空、avue表格展示设备名称、区域、车牌号、创建时间、删除单条车辆授权(按cardNo+deviceCode,加入删除队列)、导出Excel(授权车辆导出.xls)、分页 | 授权车辆记录(deviceName/areaName/plate/cardNo/deviceCode) | src/views/platform/device/xc_auth_car/index.vue |
| 门锁管理 | 列表/CRUD | ✓ | /platform/device/xc_lock_door | 按园区级联/接通状态(接通/掉线)/电量值搜索清空、卡片展示在线状态、设备名称、绑定房间、启停状态(只读开关)、电量进度条与百分比、跳转门锁下发记录(lock_door/record)、分页 | 门锁设备(deviceName/deviceArea/isAvailable/devicePower) | src/views/platform/device/xc_lock_door/index.vue |
| 门锁下发记录 | 列表/CRUD |  | /platform/device/lock_door/record/:id | 按下发时间范围搜索清空、avue表格展示姓名、下发时间、导出Excel(门锁下发记录)、分页 | 门锁下发记录(personName/createTime)、deviceId | src/views/platform/device/xc_lock_door/components/record.vue |
| 电表管理 | 列表/CRUD | ✓ | /platform/device/electric_manage | 按园区/设备位置/状态/通信地址/关联集中器/标签搜索清空、卡片展示状态、名称、通信地址、绑定房间、所在区域(宿舍/厂区)、关联集中器、标签、当前读数、闸门控制开关(开启中/关闭中/未关联闸门状态)、添加电表(名称/序号/倍率/通信地址/通信端口/集中器/区域类型宿舍或厂区/绑定宿舍房间/绑定区域/标签)、编辑电表、更换设备(沿用同一接入位置换表)、删除电表、单表开关闸门(确认弹窗)、操作记录跳转、历史度数跳转、导入电表(下载模板+上传Excel,带进度轮询与错误名单回传)、导出电表Excel、全选、批量删除/批量手动抄表/批量下载档案/批量开闸/批量关闸/批量设置标签、分页 | 电表(name/seq/ratio/address/port/concentratorId/placeType/roomId/areaId/valveStatus/currentReading/tagIds)、集中器、设备标签 | src/views/platform/device/electric_manage/index.vue |
| 电表历史度数 | 列表/CRUD |  | /platform/device/electricManage/record/:id | 顶部显示当前设备名称、按采集时间范围搜索清空、avue表格展示历史读数记录、返回电表管理页、分页 | 电表读数记录(eleMeterId/采集时间/读数) | src/views/platform/device/electric_manage/components/record.vue |
| 电表操作记录 | 列表/CRUD |  | /platform/device/electricManage/log/:id | 顶部显示当前设备名称、avue表格展示操作记录(按targetId+code查询)、返回电表管理页 | 电表操作日志(targetId) | src/views/platform/device/electric_manage/components/logs.vue |
| 电表集中器管理 | 列表/CRUD | ✓ | /platform/device/electric_meter | 按园区/集中器名称/通信地址/状态搜索清空、avue表格展示园区、集中器名称、IP、端口、通信地址、状态、备注、新增集中器(园区/IP/端口/名称/通信地址/备注)、编辑集中器、删除集中器、下载档案、查看设备(跳转电表管理并按集中器过滤)、分页 | 电表集中器(parkId/ip/port/name/address/remark/status) | src/views/platform/device/electric_meter/index.vue |
| 水表管理 | 列表/CRUD | ✓ | /platform/device/water_manage | 按园区/设备位置/状态/通信地址/关联集中器/标签搜索清空、卡片按用户大类显示不同图标,展示状态、名称、通信地址、用户大类、绑定房间、所在区域、关联集中器、标签、当前读数、添加水表(名称/集中器/下行通道M-BUS/用户大类冷水热水直饮水中水大口径/序号/通信地址/区域类型/绑定宿舍或区域/标签)、编辑水表、更换设备、删除水表、操作记录跳转、历史度数跳转、导入水表(下载模板+上传Excel,进度轮询与错误名单回传)、导出水表Excel、全选、批量删除/批量手动抄表/批量下载档案/批量设置标签、分页 | 水表(name/concentratorId/port/largeClass/seq/address/placeType/roomId/areaId/currentReading/tagIds)、集中器、设备标签 | src/views/platform/device/water_manage/index.vue |
| 水表历史度数 | 列表/CRUD |  | /platform/device/waterManage/record/:id | 顶部显示当前设备名称、按采集时间范围搜索清空、avue表格展示历史读数记录、返回水表管理页、分页 | 水表读数记录(waterMeterId/采集时间/读数) | src/views/platform/device/water_manage/components/record.vue |
| 水表操作记录 | 列表/CRUD |  | /platform/device/waterManage/log/:id | 顶部显示当前设备名称、avue表格展示操作记录(按targetId+code查询)、返回水表管理页 | 水表操作日志(targetId) | src/views/platform/device/water_manage/components/logs.vue |
| 水表集中器管理 | 列表/CRUD | ✓ | /platform/device/water_meter | 按集中器名称/通信地址/状态搜索清空、avue表格展示园区、集中器名称、IP、通信地址、状态、备注、新增集中器(园区/IP/端口/名称/通信地址/备注)、编辑集中器、删除集中器、查看设备(跳转水表管理并按集中器过滤)、分页 | 水表集中器(parkId/ip/port/name/address/remark/status) | src/views/platform/device/water_meter/index.vue |
| 阀门管理 | 列表/CRUD | ✓ | /platform/device/valve_manage | 按设备名称/本地远程状态/开关状态搜索清空、avue表格展示设备名称、集中器名称、标签、备注、本地/远程开关、开关状态开关、本地/远程状态切换(确认弹窗,调changeRemoteStatus)、阀门开关切换(确认弹窗,调putValve)、新增水表阀门(名称/集中器/序号/标签/备注)、编辑水表阀门、操作记录跳转、分页 | 水表阀门(name/concentratorId/seq/tagIds/remark/remoteStatus/isOpen)、集中器、设备标签 | src/views/platform/device/valve_manage/index.vue |
| 阀门操作记录 | 列表/CRUD |  | /platform/device/valveManage/log/:id | 顶部显示当前设备名称、avue表格展示操作记录(按targetId+code查询)、返回阀门管理页 | 阀门操作日志(valveMeterId) | src/views/platform/device/valve_manage/logs.vue |
| 阀门集中器管理 | 列表/CRUD | ✓ | /platform/device/valve_meter | 按园区/集中器名称/状态(在线/离线)搜索清空、avue表格展示园区、集中器名称、IP、状态、备注、新增集中器(园区/IP/端口/名称/备注)、编辑集中器、删除集中器、分页 | 阀门集中器(parkId/ip/port/name/remark/status) | src/views/platform/device/valve_meter/index.vue |
| 设备更换记录 | 列表/CRUD | ✓ | /platform/device/change_record | 页签切换水表记录/电表记录、按页签懒加载对应子表格组件 | 更换记录(水表/电表) | src/views/platform/device/change_record/index.vue |
| 车辆下发记录 | 列表/CRUD | ✓ | /platform/device/carDistributionRecord/index | 按设备名称/设备类型/所在区域/下发时间搜索重置、avue表格展示下发记录、重新下发(行操作)、删除(行操作)、分页 | 车辆下发记录(roomName/meterMonth/status) | src/views/platform/device/carDistributionRecord/index.vue |
| 人脸下发记录 | 列表/CRUD | ✓ | /platform/device/faceDistributionRecord/index | 按设备名称/设备类型/所在区域/下发时间搜索重置、avue表格展示下发记录、重新下发(行操作)、删除(行操作)、分页 | 人脸下发记录(roomName/meterMonth/status) | src/views/platform/device/faceDistributionRecord/index.vue |

<details><summary>子组件清单(11)</summary>

- `water_record.vue` — 设备更换记录页内嵌的水表更换记录表格,支持按更换前后通信地址与更换时间筛选
- `electric_record.vue` — 设备更换记录页内嵌的电表更换记录表格,支持按更换前后通信地址与更换时间筛选
- `setTag.vue` — 门禁类设备列表批量设置标签的弹窗组件,接收勾选deviceIds并提交标签
- `setTag.vue` — 电表列表批量设置标签的弹窗组件
- `setTag.vue` — 水表列表批量设置标签的弹窗组件
- `bind_room.vue` — 电表新增时选择楼栋楼层与房间进行绑定的弹窗组件
- `bind_room.vue` — 水表新增时选择楼栋楼层与房间进行绑定的弹窗组件
- `AddDialog.vue` — 设备标签管理页的新增/编辑表单弹窗
- `device-tag-select.vue` — 拉取设备标签列表的多选下拉组件,供各设备表单/搜索复用
- `device-type-select.vue` — 设备类型(闸机/门禁/道闸)固定枚举下拉组件
- `device-capability-select.vue` — 卡机类型(刷脸机/刷卡机/刷脸+刷卡)固定枚举下拉组件

</details>


## 8-basic · 基础信息(人员/组织/供应商/薪资签收/社保/导入)

智慧园区/厂区中后台的"基础信息"域,围绕"人(员工/供应商人员)—组织(企业/部门)—权限(通关/APP)—卡片(ISC实体卡)—福利(社保/工资签收)—台账(短信/上传/批量导入记录)"展开。核心业务流:HR 侧维护组织与在职/离职人员台账并支持 Excel 批量入职;员工信息页负责导入人脸照片、分配门禁通关权限与 APP 权限并下发到设备(ISC),失败可在记录页追溯;同时维护派遣/外包供应商及其服务人员、ISC 实体卡、APP 权限策略,以及工资签收凭证、社保公积金宣传位等。多数 index.vue 为后端动态菜单可直达的主入口页,detail/add/edit/auth 等为路由跳转子页,弹窗/select/import 等为内嵌组件。

**页面 23 个 / 子组件 7 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 员工信息列表 | 列表/CRUD | ✓ | /platform/basic/staff_info | 按工号(支持批量粘贴工号)、姓名、手机号、园区/BU/部门级联、中心、岗位、职层、员工状态、是否有照片、入职时间区间多条件搜索/清空、导出员工信息为Excel(工号/姓名/BU/部门/职层/岗位/入职日期/状态/园区/是否有照片/通关权限/APP权限)、批量导入员工人脸照片(以工号命名、校验jpg格式与20-200KB大小、单次<2000张、查工号存在性、可逐条移除、上传后弹下发进度)、勾选员工后批量分配门禁通关权限(双栏穿梭选择策略、追加/覆盖两种模式,无人脸照片拦截)、勾选员工后批量分配APP权限策略(多选)、勾选员工重新下发权限到设备(无人脸照片拦截、二次确认)、跳转员工详情、查看通关权限(deviceAuth存在时可点)、下发后弹出IssueAuth进度弹窗轮询待下发/已下发/失败数量 | 员工(badge/name/phone/compName/depName/jcheName/jobName/status/parkName/facePicId)、通关权限策略deviceAuth、APP权限策略appAuth、下发任务taskRecord | src/views/platform/basic/staff_info/index.vue |
| 员工信息详情 | 详情 |  | /platform/basic/staff_info/detail/:id | 展示人脸照片、姓名/性别/工号/手机/BU/中心/部门/岗位/职层/园区、民族/身份证地址/上级/状态/离职类型与日期/入职日期/福利层级/工作邮箱/紧急联系人等、手机号在线编辑(isMobile校验)并保存、ISC卡片子表:新增/编辑/删除实体卡(选已启用ISC同步的园区、8-20位数字或大写字母、禁999开头虚拟卡)、展示卡片同步状态标签(待同步/已同步/同步失败/本地取消)、返回列表并回填查询条件 | 员工档案smtStaff、紧急联系人smtStaffEmergency、ISC实体卡(cardNo/parkId/syncStatus)、ISC园区配置iscParkConfig | src/views/platform/basic/staff_info/detail.vue |
| 员工通关权限详情 | 详情 |  | /platform/basic/staff_info/auth/:id | 下拉切换该员工的多条通关权限策略、展示策略类型、备注(只读)、以树形(check-strictly只读)展示策略覆盖的设备/区域、返回员工列表并回填查询条件 | 通关权限策略(authName/typeName/remark)、设备树children | src/views/platform/basic/staff_info/auth.vue |
| 人员管理(在职) | 列表/CRUD | ✓ | /platform/basic/personnel_manage | 左侧部门树搜索过滤、节点点击按部门过滤、节点上新增/编辑/删除部门、按工号、姓名、是否上传人脸照片搜索/清空、新增/编辑员工(姓名/性别/手机/身份证/人脸/工号/在职状态/职层/部门/上级(按部门自动带出)/岗位/入职日期/派遣单位,工号6-12位字母数字、身份证8-20位非汉字、手机号校验)、弹窗导入员工(内嵌两步Excel导入向导)、批量导入员工人脸照片(校验jpg/<=200KB/<2000张、工号存在性、可移除)、导出在职人员为Excel、批量离职(粘贴工号查询员工、勾选移除、确认离职)、编辑部门(上级部门/部门名称/部门主管远程搜索/关联C6部门分组,仅deptType=2显示)、删除单个员工(二次确认) | 员工(badge/name/sex/phone/certno/faceImg/status/jcheId/depId/jobName/entryTime/dispatch)、部门(deptName/parentDept/director/c6DeptNo)、职层字典recruitment、C6部门 | src/views/platform/basic/personnel_manage/index.vue |
| 人员管理(离职) | 列表/CRUD | ✓ | /platform/basic/personnel_manage/leave | 左侧部门树过滤、按部门/工号/姓名(status=0已离职)搜索、批量恢复在职(粘贴工号查询、勾选移除/清空、确认恢复)、导出离职人员为Excel、查看离职人员详情(只读表单)、删除离职人员(二次确认) | 离职员工(badge/name/status=0/depName/jcheName/jobName/entryTime)、部门树 | src/views/platform/basic/personnel_manage/leave.vue |
| 批量导入员工-步骤1上传 | 向导 |  |  | 下载人员导入模板(staff-temp.xlsx)、上传xls/xlsx(校验后缀、单工作表、非空)、用XLSX解析并清洗(姓名/岗位/工号/手机/职层/身份证/部门/入职日期必填、手机号与身份证校验、Excel日期数值转字符串)、校验通过进入下一步 | Excel员工行(name/post/jobNumber/phone/rank/identity/department/entryTime/dispatch) | src/views/platform/basic/personnel_manage/import/step1.vue |
| 批量导入员工-步骤2确认导入 | 向导 |  |  | 表格预览解析后的员工数据、逐行删除、二次确认后批量提交(postImportStaff)、返回上一步 | 待导入员工fromList | src/views/platform/basic/personnel_manage/import/step2.vue |
| 组织管理 | 列表/CRUD | ✓ | /platform/basic/organization_manage | 按组织名称搜索/清空、新增/编辑企业组织(选园区、组织名称、管理员用户名(编辑禁改)、管理员密码(编辑留空不改、强度校验)、用户角色固定企业管理员、企业类型外包单位/派遣工)、删除组织(二次确认)、列表展示企业类型(外包单位/派遣工) | 组织/企业(compName/parkId/userName/compType)、管理员账号、园区 | src/views/platform/basic/organization_manage/index.vue |
| 供应商管理 | 列表/CRUD | ✓ | /platform/basic/supplier | 按所在园区(parkSelect)、公司名称搜索/清空、添加/编辑服务单位(公司名称1-30字校验、所在园区、法人校验、税务识别号、备注)、删除供应商(自定义确认弹窗) | 供应商/服务单位(name/parkId/legalPerson/numbers/remark) | src/views/platform/basic/supplier/index.vue |
| 供应商人员管理 | 列表/CRUD | ✓ | /platform/basic/supplier_staff | 按所在园区、公司名称(随园区联动加载)、服务人员姓名搜索/清空、添加/编辑服务人员(园区、公司、姓名1-30字校验、联系电话手机号校验、备注)、删除服务人员(自定义确认弹窗) | 供应商服务人员(name/supplierId/parkId/phone/remark)、供应商列表 | src/views/platform/basic/supplier_staff/index.vue |
| 工资签收管理列表 | 列表/CRUD | ✓ | /platform/basic/salary_sign | 按工号、姓名、园区/BU/部门级联、工资月份、签收日期区间搜索/清空、跳转查看单条签收详情 | 工资签收记录(badge/name/wageDate/parkName/compName/depName/createTime) | src/views/platform/basic/salary_sign/index.vue |
| 工资签收详情 | 详情 |  | /platform/basic/salary_sign/detail/:id | 展示薪资期间/工号/姓名/园区/BU/部门/签收日期、展示本人电子签名(base64图片)、用html2canvas把签收信息区域生成图片并下载(命名为工号_姓名_工资签收凭证) | 工资签收(wageDate/badge/name/signImg) | src/views/platform/basic/salary_sign/detail.vue |
| 社保公积金 | 列表/CRUD | ✓ | /platform/basic/social_security | 按标题搜索/清空、添加/编辑社保条目(标题名称2-30字校验、配图上传(EXIF方向校正+压缩+canvas转base64)、标题链接)、删除社保条目(自定义确认弹窗) | 社保条目(title/image/url) | src/views/platform/basic/social_security/index.vue |
| 短信发送记录 | 列表/CRUD | ✓ | /platform/basic/note_record | 按手机号、短信模板、发送状态、发送时间区间搜索/清空、查看短信详情弹窗(模板/手机号/发送时间/内容/状态/备注) | 短信记录(phoneNo/tempId/msgState/msgContent)、短信模板、发送状态字典 | src/views/platform/basic/note_record/index.vue |
| 授权管理(解除授权) | 列表/CRUD | ✓ | /platform/basic/grant_auth | 按园区、姓名、工号搜索/清空、列表展示授权平台/园区/工号/姓名/授权ID(openId)/创建时间、解除某员工的授权(自定义确认弹窗) | 开门授权(from/parkName/badge/staffName/openId) | src/views/platform/basic/grant_auth/index.vue |
| ISC卡片快速维护 | 特殊/公开页 | ✓ | /platform/basic/isc_card_fast_add | 选已启用ISC同步的园区(校验同步开关与dispatcherParkId)、按工号(精确)或姓名搜索定位员工、展示员工卡片信息与候选列表、读卡器/手动输入卡号(8-20位数字或大写字母、禁999虚拟卡、回车加入队列),支持保存后继续/仅加入队列模式、批量粘贴(每行工号+卡号,空格/Tab/逗号分隔,最多200行,前端逐行校验、按工号批量查员工映射)、待提交队列(去重、校验离职拦截、逐条移除/清成功行/清空)、批量提交(逐条saveIscStaffCard并自动创建ISC同步任务、统计成功/失败)、删除员工已有ISC卡片、最近录卡结果/同步任务追踪表格(刷新)、跳转ISC卡片同步任务页与人员详情 | ISC实体卡(staffId/parkId/cardNo/syncStatus)、ISC园区配置(cardSyncEnabled/dispatcherParkId)、员工、ISC卡片同步任务 | src/views/platform/basic/isc_card_fast_add/index.vue |
| APP权限策略列表 | 列表/CRUD | ✓ | /platform/basic/app_permis | 按所属园区、权限名称搜索/清空、跳转新增/编辑APP权限策略、删除策略(固定策略isFix禁删、自定义确认弹窗) | APP权限策略(authName/parkId/isFix) | src/views/platform/basic/app_permis/index.vue |
| APP权限策略新增 | 表单 |  | /platform/basic/app_permis/add | 选所属园区(切换时取该园区是否园区前端通用菜单标记initFlag)、填权限名称(<=10字)、备注(<=50字)、勾选允许查看APP的职层(职层字典)、园区非通用菜单时可设置是否为园区前端通用菜单开关、双树勾选招聘HR权限与服务模块(全选/清空)、保存校验必填(园区/名称/至少一个职层/至少一个模块) | APP权限策略(parkId/authName/authDesc/jcheId/hrAuthId/moduleId/initFlag)、HR招聘权限树、服务模块树、职层字典 | src/views/platform/basic/app_permis/add.vue |
| APP权限策略编辑 | 表单 |  | /platform/basic/app_permis/edit/:id | 加载策略详情回填表单(园区/名称/备注/职层/已选HR权限树/已选模块树/initFlag)、切换园区更新园区通用菜单标记、双树勾选HR权限与服务模块(全选/清空)、保存校验必填后提交修改 | APP权限策略(parkId/authName/authDesc/jcheId/hrAuthId/moduleId/initFlag)、HR权限树、服务模块树 | src/views/platform/basic/app_permis/edit.vue |
| 上传员工记录列表 | 列表/CRUD | ✓ | /platform/basic/upload_record | 按工号、姓名、导入状态(失败/成功)搜索/清空、跳转查看上传记录详情并回填查询条件 | 上传记录(badge/name/status) | src/views/platform/basic/upload_record/index.vue |
| 上传员工记录详情 | 详情 |  | /platform/basic/upload_record/detail/:id | 展示人脸照片、工号/姓名/BU/部门/园区/导入时间/导入状态(成功/失败)/操作人、返回列表并回填查询条件 | 上传记录(facePicUrl/badge/name/compName/depName/parkName/status/createUser) | src/views/platform/basic/upload_record/detail.vue |
| 批量导入照片任务列表 | 列表/CRUD | ✓ | /platform/basic/xc_batchImportImg | 按所属园区、任务名称搜索/清空、添加导入任务(弹出批量导入照片组件)、列表展示园区/任务名称/导入数量/下发成功数/下发失败数(红色)/创建时间、查看任务明细、删除任务(二次确认) | 导入照片任务(parkName/taskName/totalNum/successNum/failNum) | src/views/platform/basic/xc_batch_import_staff_img/index.vue |
| 批量导入照片任务详情 | 详情 |  | /platform/basic/xc_batchImportImg/detail | 按下发状态(成功/失败/下发中)搜索/清空、展示所属园区/任务名称,列出照片名称/状态/描述/下发时间、导出明细为Excel(照片导入.xls)、返回任务列表 | 照片下发明细(imgName/statusDesc/remark/createTime) | src/views/platform/basic/xc_batch_import_staff_img/detail.vue |

<details><summary>子组件清单(7)</summary>

- `doPasteBadge.vue` — 员工列表搜索用的批量粘贴工号对话框,校验格式与行数
- `issueAuth.vue` — 权限下发后轮询展示各员工各设备的下发结果
- `_import_img.vue` — 仅含照片读取/上传方法、模板为空div的遗留辅助组件
- `_upload.vue` — 人员表单内的人脸照片上传、压缩与人脸裁剪组件
- `index-single.vue` — 按工号远程搜索员工的单选popover下拉组件
- `index.vue` — 人员导入两步向导的容器,串联step1上传与step2确认
- `import.vue` — 新建批量导入照片任务的对话框,选园区/填任务名/选照片并提交

</details>


## 9-security_area · 保密区域管理

该模块负责厂区保密区域的门禁与权限全链路管理,覆盖五条业务主线:(1)保密区域字典维护(新老工厂区域编码);(2)保密门禁OA申请(选申请人/区域/批量人员并设置门禁权限,提交OA审批后向门禁设备下发人脸权限,支持手动下发与失败照片重传);(3)保密项目维护(建项目、绑定园区门禁权限策略、为项目授权员工);(4)保密协议签署管理(查询员工签署状态并批量为员工设置保密项目);(5)保密区供应商及供应商授权人员管理(供应商树维护、协议附件、授权项目、人员导入导出、协议到期邮件通知)。核心业务流是:维护区域与供应商基础数据 → 发起门禁申请并设权限 → OA审批通过后下发到门禁设备 → 跟踪下发结果。

**页面 8 个 / 子组件 21 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 保密区域维护 | 列表/CRUD | ✓ | /platform/security_area/area_mng/index | 按关键字(编号或名称)+所属工厂(新工厂/老工厂)搜索区域、新增区域弹窗:录入所属工厂、编号、字段名、名称、编辑区域(编号/字段名/工厂在编辑态禁改,仅可改名称)、删除单条区域(确认弹窗)、avue-crud 分页表格展示,工厂类型本地映射为新工厂/老工厂文案 | 保密区域(security-area)、编号code、字段名type、名称desc、所属工厂factoryType | src/views/platform/security_area/area_mng/index.vue |
| 保密区门禁申请列表 | 列表/CRUD | ✓ | /platform/security_area/securityGuardApply | 按OA单号、所属园区/BU/部门(deptCascader级联)、申请时间区间、下发状态(待下发/已下发)搜索、跳转新增保密门禁申请页、查看申请详情(带回查询条件)、手动下发:仅OA已通过的申请可点,调用下发接口将权限推送门禁设备、表格展示申请人工号/姓名、园区/BU/部门、OA状态、下发状态、下发总数/成功数/失败数 | 门禁申请(security/auth/apply)、OA状态oaStatus、下发状态deviceStatus、园区/BU/部门、下发统计totalNum/successNum/failNum | src/views/platform/security_area/xc_guard_apply/index.vue |
| 新增保密门禁申请 | 表单 |  | /platform/security_area/securityGuardApply/add | 选择申请进入园区(parkSelect)、输入申请人工号回车自动查询员工信息(姓名/BU/部门),失焦兜底查询、选择申请进入区域(新工厂/老工厂/二者)联动展示可进区域复选(zoneSelect+新老厂区清单)、勾选新老工厂可进区域列表(按区域code多选)、内嵌人员名单组件:粘贴工号/按部门筛选批量加人,逐人或批量设置门禁权限、顶部状态条统计已加入/已选/需处理人数、提交前校验申请人信息、人员权限非空、无失败备注、提交申请 / 提交并继续添加(go(0)刷新)两种提交 | 门禁申请addform、申请人applyBadge/staffInfo、进入区域permitFactoryType、区域类型areaType、授权人员personList(含applyAuths) | src/views/platform/security_area/xc_guard_apply/add.vue |
| 保密门禁申请详情 | 详情 |  | /platform/security_area/securityGuardApply/detail/:id | 展示工单信息:OA单号、申请时间、OA状态、申请区域(新老工厂区域名)、下发状态与下发总数/成功/失败数、右侧OA审批流时间轴(节点/审批人/退回/当前审批人状态着色)、人员名单表:工号/姓名/进入区域/下发权限/下发状态/失败原因、对下发失败(status=2)的人员更换照片(UploadImg弹窗重传人脸)、导出下发失败人员excel(仅含失败记录) | 申请详情detailInfo、OA审批流oaFlow、下发人员明细(task/details)、下发状态status、人脸照片 | src/views/platform/security_area/xc_guard_apply/detail.vue |
| 保密项目维护列表 | 列表/CRUD | ✓ | /platform/security_area/securityProjectMng | 按项目名称、所属园区(parkSelect)搜索、添加/修改保密项目(AddDialog:园区+项目名+项目代码+勾选项目门禁授权策略)、查看权限(ckAuth弹窗展示项目绑定的门禁权限策略树)、跳转授权员工页(staff)、批量删除选中项目、表格展示园区/项目名/项目代码/项目门禁授权列表/创建时间 | 保密项目(security/zone)、项目名securityName、项目代码securityCode、门禁授权authIds/authNameList、园区parkId | src/views/platform/security_area/xc_project_mng/index.vue |
| 保密项目授权员工 | 列表/CRUD |  | /platform/security_area/securityProjectMng/staff/:id | 按工号、姓名、园区/BU/部门(deptCascader)搜索项目下授权员工、添加员工(AddStaffDialog:粘贴工号/按部门筛选批量加入)、导入excel批量授权员工(DlgImportPerson)、批量删除员工(支持按筛选条件或勾选删除,勾选优先)、返回项目列表、表格展示工号/姓名/BU/部门/岗位/入职时间 | 项目授权员工(security/zone person)、securityId项目ID、园区/BU/部门、工号staffBadge | src/views/platform/security_area/xc_project_mng/staff.vue |
| 保密协议签署管理 | 列表/CRUD | ✓ | /platform/security_area/xc_sign_mng/index | 按工号(多个空格分隔)、姓名、园区/BU/部门、签署状态(已/未签署)、入职时间区间搜索、批量设置保密项目(选中员工后BatchProject弹窗勾选项目)、查看员工已关联保密项目(CheckProject弹窗,可删除项目)、表格展示工号/姓名/BU/部门/岗位/入职时间/签署状态 | 员工签署(security/person)、签署状态signStatus、保密项目、园区/BU/部门 | src/views/platform/security_area/xc_sign_mng/index.vue |
| 保密区供应商人员 | 列表/CRUD | ✓ | /platform/security_area/supplier_person/index | 左侧供应商树(按园区/A类/非A类分层),点击供应商联动右表人员、导入供应商(选园区+分类,下载模板,前端XLSX解析校验后批量导入)、导出供应商(按园区导出xls)、导入人员(dlgSupplierPerson:excel导入或从访客记录勾选导入)、导出人员(前端导出供应商名称/授权人/身份证号excel)、更新授权项目(dlgAuthProject:多选供应商批量设置授权项目号)、到期设置(dlgNoticeSet:协议到期前N天邮件通知)、编辑/删除单条供应商人员,批量删除人员、按授权人员姓名模糊搜索 | 供应商人员(supplier_person)、供应商supplier、园区parkId、身份证idCard/手机phone、授权项目authorList | src/views/platform/security_area/supplier_person/index.vue |

<details><summary>子组件清单(21)</summary>

- `_supplier.vue` — 供应商左侧树组件,含新增/编辑/删除供应商弹窗(协议日期、授权项目/区域/人数、保密协议图片或PDF上传、携带物品)。
- `_supplier_select.vue` — 带复选框的供应商树组件,供更新授权项目弹窗选择多个供应商并返回选中id。
- `dlg_authProject.vue` — 批量为勾选供应商设置授权项目号(以'/'分割)的弹窗。
- `dlg_noticeSet.vue` — 按园区配置供应商协议到期前N天的邮件通知(模板+变量+收件邮箱名单)。
- `dlg_supplierPerson.vue` — 为供应商导入授权人员:excel导入或从访客记录勾选(校验证件号/重复/手机号)导入。
- `dlg_importPerson.vue` — excel导入人员的分步弹窗(下载模板/上传/校验手机号/确认)。
- `authPersonList.vue` — 门禁申请页内嵌人员名单:批量加人、逐人/批量设权限、照片与权限校验、统计汇总。
- `auth-person-multi-select.vue` — 门禁权限多选下拉组件(collapse-tags),供人员行内选择权限。
- `authSelectMulti.vue` — 本次申请门禁权限批量勾选弹窗,支持按名称/编号搜索并校验照片/保密区关联规则。
- `zone-select.vue` — 申请进入区域(新工厂/老工厂/二者)选择下拉组件。
- `upload_img.vue` — 对下发失败人员重新上传人脸照片(base64)并重传下发的弹窗。
- `AddDialog.vue` — 添加/编辑保密项目弹窗:园区+项目名+代码+勾选园区门禁权限策略。
- `AddStaffDialog.vue` — 为保密项目添加授权员工弹窗:粘贴工号/按部门筛选/批量移除后保存。
- `authes.vue` — 只读展示门禁权限策略明细树(check-strictly,默认勾选)的弹窗。
- `ckAuth.vue` — 查看某保密项目已绑定门禁权限策略列表并可查看每条策略详情。
- `dlg_importPerson.vue` — excel导入授权员工到保密项目的弹窗(下载专用模板、上传校验、保存)。
- `doDept.vue` — 懒加载部门树+复选,按部门批量筛选员工返回上层的弹窗。
- `doPaste.vue` — 粘贴员工工号(每行一个)批量导入人员的弹窗。
- `project_name.vue` — 项目名称多选下拉组件(collapse-tags,远程加载)。
- `batchProject.vue` — 为选中员工批量勾选并设置保密项目的弹窗。
- `checkProject.vue` — 查看某员工已关联保密项目(项目代码/名称/授权门禁标签)并可删除。

</details>


## 10-business · 业务设置

业务设置模块是智慧园区后台的"业务规则配置中心",按园区(parkId)维度为各业务子系统下发运行规则,本身不产生业务数据。覆盖考勤汇总/工资签收提醒、厂牌补领扣费、宿舍管理员与水电结算、访客预约规则与记录邮件推送、入厂申请区域授权、保密区OA同步与权限自动回收、物品放行/园区报修/退宿申请的多节点审批流编排、水电告警阈值、消息推送模板、ISC(海康)平台园区绑定与卡片初始化导入等。多数页面以"园区选择 + avue-crud 列表 + 弹窗/详情表单"或"单页表单直接保存"两种形态实现,审批类(放行/报修/退宿)走可视化的条件+审批节点编排详情页。

**页面 20 个 / 子组件 4 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 考勤汇总提醒设置 | 列表/CRUD | ✓ | /platform/business/attendance/index | 搜索(按所属园区)、清空搜索、添加考勤汇总规则配置(弹窗)、编辑配置(弹窗)、设置每月考勤确认提醒日(1-28日下拉)、设置考勤确认截止至N天后系统自动确认签收、提醒频次固定仅提醒一次、是否启用开关(switch,setType=2)、分页 | 考勤汇总配置(parkId/deadline提醒日/delayLine截止天数/isMessage启用/setType=2) | src/views/platform/business/attendance/index.vue |
| 厂牌补领设置 | 列表/CRUD | ✓ | /platform/business/badge/index | 搜索(按所属园区)、清空搜索、添加厂牌补领设置(弹窗)、编辑厂牌补领设置(弹窗)、设置每张厂牌扣取费用(元)、删除(rowDel,模板中已注释隐藏)、分页 | 厂牌补领配置(parkId/price每张费用) | src/views/platform/business/badge/index.vue |
| 工资签收设置 | 列表/CRUD | ✓ | /platform/business/salary/index | 搜索(按所属园区)、清空搜索、添加工资签收设置(弹窗)、编辑工资签收设置(弹窗)、设置每月工资签收提醒日(1-28日下拉)、设置签收确认截止至N天后系统自动确认签收、提醒频次固定仅提醒一次、是否启用开关(switch,setType=1)、分页 | 工资签收配置(parkId/deadline提醒日/delayLine截止天数/isMessage启用/setType=1) | src/views/platform/business/salary/index.vue |
| 宿舍管理员设置 | 列表/CRUD | ✓ | /platform/business/dormitory/index | 搜索(按所属园区)、清空搜索、添加宿舍管理员(弹窗,管理员1-4各填工号)、编辑宿舍管理员(弹窗)、分页 | 宿舍管理员配置(parkId/badgeOne/badgeTwo/badgeThree/badgeFour工号) | src/views/platform/business/dormitory/index.vue |
| 宿舍水电结算设置 | 表单 | ✓ | /platform/business/dormitory_set/index | 选择所属园区(切换自动回填已有配置)、结算规则选择:按自然月(1号到月底)、结算规则选择:动态日期(上月N日为结算日,N=2-28)、保存配置(saveData,返回true弹成功通知)、(智能抄表区块已在模板中注释) | 宿舍水电结算配置(parkId/type结算方式/countDate结算日) | src/views/platform/business/dormitory_set/index.vue |
| 宿舍水电告警设置 | 表单 | ✓ | /platform/business/dormitory_alarm/index | 选择所属园区、结算规则:固定日期(每月N日为结算日,1-28)、结算规则:动态日期(每月倒数第N天为结算日)、启用宿舍水电智能抄表开关、设置每隔N小时采集一次水电数据、设置以N点后最近一次数据为当天抄表数据、保存按钮(saveInfo方法体为空,功能未实现) | 宿舍水电告警/抄表配置(parkId/radio1结算方式/waterNum/switch抄表开关) | src/views/platform/business/dormitory_alarm/index.vue |
| 水电告警阈值设置 | 表单 | ✓ | /platform/business/hydropower_alarm/index | 选择所属园区、设置水表告警:连续N次采集都超过用量阈值则生成水表超量告警、设置电表告警:连续N次采集都超过用量阈值则生成电表超量告警、维护告警通知接收人列表(账号名称+姓名,增/删行)、保存按钮(saveInfo/addLine/delLine方法体为空,功能未实现) | 水电告警配置(parkId/waterNum/waterThreshold/electricNum/electricThreshold/alarmList接收人) | src/views/platform/business/hydropower_alarm/index.vue |
| 消息推送接收人设置 | 表单 | ✓ | /platform/business/xc_msg_set/index | 填写模板名称、维护接收人列表(显示已添加数量)、输入工号回车调用_publicService查询并回填姓名、点击+添加接收人(校验工号不重复)、删除接收人行、保存(saveData,返回true弹成功并刷新)、进入时加载已有模板回填 | 消息推送模板(name模板名/personList接收人[badge工号/name姓名]) | src/views/platform/business/xc_msg_set/index.vue |
| ISC平台绑定配置 | 列表/CRUD | ✓ | /platform/business/isc_park_config/index | 搜索(按所属园区)、清空搜索、添加ISC平台绑定(业务园区+ISC调度园区+卡片同步开关+备注)、编辑绑定、删除绑定(二次确认)、预检(按人员范围全部/在职/离职生成预检明细,dryRun,不写本地卡表;仅卡片同步启用可点)、初始化导入(按人员范围从海康ISC导入实体卡,离职卡作废生成退卡任务;仅卡片同步启用可点)、选择人员范围弹窗(ALL/ACTIVE/RESIGNED含提示文案)、同步记录(跳转/platform/records/isc_card_import并带parkId/batchId)、分页 | ISC平台绑定(parkId业务园区/dispatcherParkId调度园区/cardSyncEnabled卡片同步/remark)、卡片导入批次(staffScope人员范围/batchId) | src/views/platform/business/isc_park_config/index.vue |
| 访客设置列表 | 列表/CRUD | ✓ | /platform/business/visitor/index | 添加访客设置(跳转detail/0新建)、编辑(跳转detail/:parkId)、列表展示园区/创建时间、分页 | 访客设置记录(id/parkId/园区/创建时间) | src/views/platform/business/visitor/index.vue |
| 访客设置详情 | 详情 |  | /platform/business/visitor/detail/:parkId | 选择所属园区(切换重新加载各项配置)、被访限制规则:勾选职层(选中职层员工不可被设为被访人)、访客记录推送设置:选统计周期(每天/每周/每月)、维护记录推送人列表(姓名+邮箱,校验后增/删行)、访客邀约:是否需要上级领导审批开关、疫情信息管控(限特定园区):行程码/健康码是否显示勾选、访客提示:开关+wangeditor富文本编辑提示内容、保存(并行保存邮件/职层/批量配置/疫情配置后返回) | 访客邮件推送(emails[receiver/email]/type周期)、被访职层限制(jcheList)、批量配置(needApproval邀约审批/isNeedNotice提示/content)、疫情管控(isTripCode/isHealthCode) | src/views/platform/business/visitor/detail.vue |
| 入厂申请设置 | 表单 | ✓ | /platform/business/incoming/index | 入厂申请区域设置:按区域类别/区域展示通关权限,编辑弹窗关联多个通关权限、手动实时刷新(doSyncTask从源同步区域)、被访限制规则:勾选职层、H5常用区域展示配置:设置展示数量、按新/老工厂勾选常用区域及排序、前端提示配置:开关+wangeditor富文本(许昌访客页显示)、保存(并行保存区域授权/职层/批量配置含configType=2提示与7常用区域) | 入厂区域授权(areaTypeId/parkId/authLists通关权限)、被访职层限制(jcheList)、H5常用区域配置(inlineAreaLimit/factories/areas)、前端提示(isNeedNotice/content) | src/views/platform/business/incoming/index.vue |
| 物品放行设置列表 | 列表/CRUD | ✓ | /platform/business/release/index | 添加(选园区,eventCode=3创建放行配置)、编辑(跳转detail带id/parkId/parkName/isUploadImg)、列表展示园区、分页 | 物品放行审批配置(parkId/parkName/eventCode=3/isUploadImg) | src/views/platform/business/release/index.vue |
| 物品放行审批流配置 | 详情 |  | /platform/business/release/detail | 保安放行是否需上传照片开关、添加/删除审批节点(含审批顺序、节点名称)、添加/删除触发条件(物品类型/楼栋名称,等于/不等于,且/或)、审批人设置(无指定/指定审批人/同室友)、指定审批人按工号回车查姓名并增删、审批人通过规则(全部通过/任一通过)、通知方式(APP PUSH/短信选模板/许昌园区微信推送)、保存审批流配置/返回 | 审批节点(name/sort/passRule/isExistApprover/isAppPush/isMsg/msgTemplate)、触发条件(conditionType物品类型/楼栋/comparator/compareValue/connector)、审批人(approverBadge/approverName) | src/views/platform/business/release/detail.vue |
| 园区报修设置列表 | 列表/CRUD | ✓ | /platform/business/repair/index | 添加(选园区,eventCode=5创建报修配置)、编辑(跳转detail带id/parkId/parkName)、列表展示园区、分页、复用release的_service接口 | 园区报修处理配置(parkId/parkName/eventCode=5) | src/views/platform/business/repair/index.vue |
| 园区报修处理流配置 | 详情 |  | /platform/business/repair/detail | 添加/删除处理条件节点、添加/删除触发条件(维修区域/维修类型/楼栋名称,等于/不等于,且/或)、处理人设置:按工号回车查姓名、通知方式(APP PUSH/短信选模板/许昌园区微信推送)、保存/返回、复用release的_service接口 | 处理节点(approvalPersons/isAppPush/isMsg/msgTemplate)、触发条件(conditionType维修区域/维修类型/楼栋/comparator/compareValue/connector)、处理人(approverBadge/approverName) | src/views/platform/business/repair/detail.vue |
| 退宿申请设置列表 | 列表/CRUD | ✓ | /platform/business/dorm_exit/index | 添加(选园区,eventCode=6创建退宿配置)、编辑(跳转detail带id/parkId/parkName)、列表展示园区、分页 | 退宿申请审批配置(parkId/parkName/eventCode=6) | src/views/platform/business/dorm_exit/index.vue |
| 退宿申请审批流配置 | 详情 |  | /platform/business/dorm_exit/detail | 添加/删除审批节点(审批顺序、节点名称)、添加/删除触发条件(退宿原因/楼栋名称,等于/不等于,且/或)、审批人设置(无指定/指定审批人/同室友/上级领导)、指定审批人按工号回车查姓名并增删、审批人通过规则(全部通过/任一通过或拒绝)、通知方式(APP PUSH/短信选模板/许昌园区微信推送)、保存/返回 | 审批节点(name/sort/passRule/isExistApprover)、触发条件(conditionType退宿原因/楼栋/comparator/compareValue/connector)、审批人(approverBadge/approverName) | src/views/platform/business/dorm_exit/detail.vue |
| 保密区设置列表 | 列表/CRUD | ✓ | /platform/business/security_area/index | 添加设置(选园区,初始化白名单配置editWhiteListObj)、编辑(跳转edit/:parkId)、列表展示园区/创建时间、分页 | 保密区配置(parkId/园区/whiteList白名单/创建时间) | src/views/platform/business/security_area/index.vue |
| 保密区设置编辑 | 详情 |  | /platform/business/security_area/edit/:parkId | 选择所属园区(切换重新加载)、保密区OA审批信息设置:展示申请区域类别/OA申请区域/已设置状态/关联平台区域权限、手动实时刷新(doSyncTask同步OA区域字典)、为每个OA区域编辑关联平台区域权限(弹窗多选)、权限自动删除配置:N天无进出记录则删除员工权限、过滤场景勾选(节假日/出差/请假/调休)、启用白名单开关(白名单人员不受检测)、白名单维护(工号回车查姓名,增/删行)、保存(并行保存区域权限映射+白名单配置)/返回 | OA区域映射(oaAreaId/oaAreaName/factoryType工厂类型/authList关联权限)、权限自动删除(deleteDay/isHoliday/isBusiness/isLeave/isCompensatory)、白名单(isWhiteList/whiteList[staffBadge/staffName]) | src/views/platform/business/security_area/edit.vue |

<details><summary>子组件清单(4)</summary>

- `batchAuth.vue` — 入厂区域穿梭框选择并保存关联通关权限的弹窗子组件
- `delAuth.vue` — 勾选删除入厂区域已关联通关权限的弹窗子组件
- `batchAuth.vue` — 保密区OA区域穿梭框选择并保存关联平台区域权限的弹窗子组件
- `delAuth.vue` — 勾选删除保密区OA区域已关联平台权限的弹窗子组件

</details>


## 11-panel · 可视化大屏

智慧园区运营大数据可视化大屏模块,含旧版(panel)与新版(panel_new)两套全屏看板。整体业务流:进入大屏后按园区维度拉取统计接口,定时(60s)轮询刷新,通过 ECharts/Canvas/进度条/抓拍图渲染园区概况(占地、厂房、宿舍、组织构成、员工分布、车位、宿舍入住率)与人员出入(设备人脸抓拍、访客分析、访客实时进出厂、车辆进出时段)两大屏,顶部可切换两屏并选择/跳转园区。新版相对旧版增加了顶部园区下拉选择、按 parkId 维度查询、组件化拆分。该模块为只读展示型,无增删改,大量子组件被主屏内嵌。

**页面 6 个 / 子组件 20 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 可视化大屏外壳(旧版) | 特殊/公开页 | ✓ | /platform/panel | 顶部双 Tab 切换:园区概况(跳 /platform/panel/bigdata)与人员出入(跳 /platform/panel/accessto)、每秒刷新顶部实时时钟(setInterval 1s)、点击退出图标二次确认后调用 LogOut 退出登录并跳转 /login、根据当前路由高亮对应 Tab | 当前时间、园区/人员屏标识 | src/views/platform/panel/index.vue |
| 园区概况大屏(旧版) | 看板/大屏 |  | /platform/panel/bigdata | 拉取园区列表取第一个园区 id,调 /park/statistics 获取统计、园区描述卡片:占地面积/生产厂房/宿舍楼数/餐厅楼数、园区员工卡片:入驻BU数/部门数/岗位数/员工总数、车辆动态卡片:员工车辆/外来车辆/车位总数/当前空余(/vehicle/count、/parking/correction/count)、宿舍动态:房间数/床位数,半圆环 ECharts 显示入住人数与床位占用率,并按极高/较高/正常/较低分档高亮、组织构成饼图(chartPie)与员工分布柱状图(chartBar)、右上 Canvas 贝塞尔曲线+流动虚线动画绘制园区点位地图特效、全屏 Loading 遮罩,60s 定时轮询刷新数据 | 园区统计(parkArea/workshopToal/dormitoryToTal/diningRoomTotal)、员工统计(compTotal/deptToal/jobTotal/staffTotal)、车位(totalCount/freeCount)、车辆动态(innerTotal/foreignTotal)、宿舍(roomTotal/bedTotal/bedStaffTotal)、组织构成 compStatistics | src/views/platform/panel/bigdata.vue |
| 人员出入大屏(旧版) | 看板/大屏 |  | /platform/panel/accessto | 4 个设备区块展示现场抓拍照与人脸识别照,含姓名/身份/出入类型/出入时间/单位名称(图片加载失败回退占位图)、访客分析:按来访事由(业务会谈/拜访/面试/其他)环形饼图占比(/visitor/searchVisitorAnalysisToday)、访客实时:今日累计进厂/出厂人数与各闸口设备进出场计数(searchVisitorInToday/OutToday/DeviceToday)、车辆进入时段分析:按0-4/5-8/...时段进门、出门双柱状图(/snap/vehicle/count)、设备抓拍数据来自 searchVisitorDeviceAnalysisToday、60s 定时轮询刷新 | 设备抓拍(deviceName/snapPhotoUrl/personUrl/personName/personTypeDesc/eventTypeDesc/snapTime/company)、访客分析(causeDesc/causeCount)、访客实时(进厂数/出厂数/设备进出计数)、车辆时段(indoorNums/outdoorNums) | src/views/platform/panel/accessto.vue |
| 可视化大屏-园区数据屏(新版) | 看板/大屏 |  | /platform/panel/park | 顶部 top 切换园区概况/人员出入并选择园区、左侧通过 require.context 动态加载 park-component2 下园区数据/APP动态/设备安装/员工总数四卡片(当前为静态占位数据)、右侧内嵌宿舍动态(dorm-item)、车位动态(car-item)组件与地图特效(map)、纯布局组合页,自身无接口请求 | 园区数据、APP动态、设备安装、员工总数分布 | src/views/platform/panel_new/park.vue |
| 园区概况大屏(新版) | 看板/大屏 |  | /platform/panel/view | 顶部 top 选择园区,默认/切换时按 parkId 调 /park/statistics 等接口、园区描述(v1-1)、园区员工(v1-2)卡片渲染统计、组织构成与员工分布(v1-3)环形图+滚动进度条列表、宿舍动态(dorm-item) /bigdata/park/dormitory,车位动态(car-item) /bigdata/park/parking、地图特效组件,60s 定时轮询刷新车位与宿舍 | 园区描述、园区员工、组织构成 compStatistics、车位 parking、宿舍 dormitory | src/views/platform/panel_new/view.vue |
| 人员出入大屏(新版) | 看板/大屏 |  | /platform/panel/person | 顶部 top 选择园区,Promise.all 并发拉取访客分析/车辆计数/访客实时/设备抓拍、左侧4个设备抓拍组件(ps1-1),按区域设备显示人脸抓拍、访客分析(ps1-3)、访客实时(ps1-4)、车辆进入时段分析(ps1-5)、接口:areadevice/snap、searchVisitorAnalysisToday、snap/vehicle/count、bigdata/park/visitor、60s 定时轮询刷新 | 设备抓拍 snapDataList、访客分析、访客实时 inOutRecords、车辆时段 indoorNums/outdoorNums | src/views/platform/panel_new/person.vue |

<details><summary>子组件清单(20)</summary>

- `top.vue` — 新版大屏公共顶部:园区概况/人员出入切换、园区下拉选择、实时时钟、跳首页
- `map.vue` — Canvas 贝塞尔曲线+流动虚线绘制园区点位连线动画特效组件
- `index.vue` — 车位动态卡片:可用/总车位进度条与车辆进出记录滚动表
- `index.vue` — 宿舍动态卡片:房间/床位 Tab 切换,饼图展示空置与已使用占比
- `chart-item.vue` — 宿舍空置/已使用占比 ECharts 饼图子组件
- `index.vue` — 单/双设备抓拍卡片:现场抓拍+人脸识别照与人员信息,双设备可切换
- `index.vue` — 访客来访事由占比环形图与图例百分比列表
- `chart-item.vue` — 访客分析占比 ECharts 环形饼图子组件
- `index.vue` — 今日累计进出厂人数与各区域进出园区计数表
- `index.vue` — 各时段车辆进入/离开园区双柱状图卡片
- `chart-item.vue` — 车辆进出园区时段 ECharts 双柱状图子组件
- `index.vue` — 园区描述卡片:占地面积/生产厂房/宿舍楼/餐厅楼
- `index.vue` — 园区员工卡片:入驻BU/部门/岗位/员工总数
- `index.vue` — 组织构成环形图(中心员工总数)+各BU员工分布滚动进度条列表
- `chart-item.vue` — 组织构成 ECharts 环形图子组件,中心显示员工总数
- `index.vue` — 接入园区/涉及数据统计卡片(当前为静态占位数据)
- `index.vue` — APP安装/登录/业务统计卡片(当前为静态占位数据)
- `index.vue` — 人行闸机/车辆道闸数量统计卡片(当前为静态占位数据)
- `index.vue` — 各园区员工总数环形图+进度条分布卡片(当前为静态占位数据)
- `chart-item.vue` — 员工分布 ECharts 环形图子组件(静态占位数据)

</details>


## 12-area · 区域管理/停车场/权限策略

该模块是智慧园区门禁/通行权限的配置中枢，由三大块组成：1) 园区(park)——园区档案的增删改查及每个园区的多页签配置(组织关系BU/访客通知开关/招聘签约单位/各类人车通行权限默认值/BU级权限关联)；2) 区域(area)——以园区为顶层的三级区域树(经纬度定位)，以及只读的通关人员名单；3) 权限策略(limit)——按人员/车辆、门禁/考勤设备维度创建"通关权限策略"并勾选关联设备树，再把员工(支持批量粘贴工号)或内部车辆关联到策略上。核心业务流：建园区→配区域树→建通关权限策略并选设备→给策略关联人/车→在园区配置页把策略指派为访客/员工/物流/公司等场景的默认通行权限。

**页面 16 个 / 子组件 6 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 区域管理(区域树) | 特殊/公开页 | ✓ | /platform/area/area/index | 左侧区域树检索过滤(关键字 filterNode)，默认全部展开、按树节点层级动态展示添加/编辑/删除按钮(顶级园区不可编辑删除，level1-2可添加子区域)、弹窗新增/编辑区域：区域名称、上级区域(只读)、经度、纬度、备注，含名称/经纬度正则校验、删除区域(二次确认弹框)、点击树节点联动右侧表格分页查询该区域下子区域(按 parkId/pid 过滤)、表格展示所属园区、区域名称、上级区域名称(空显示'顶级')、经纬度、备注，分页 | 园区Park、区域Area(areaName/pid/parkId/areaLongitude/areaLatitude/remark) | src/views/platform/area/area/index.vue |
| 通关人员 | 列表/CRUD |  | /platform/area/area/person/:id | avue-crud 表格展示通关人员：工号、姓名、BU、部门、职层、岗位、支持分页、列筛选、搜索、刷新按钮(getList 接口当前被注释，为占位/待接后端的只读页) | 通关人员(workid/staff_name/bu/department/layer/position) | src/views/platform/area/area/person.vue |
| 权限策略 | 列表/CRUD | ✓ | /platform/area/limit/index | 搜索区：所属园区(parkSelect)、通关权限名称、类型(公共区域/保密区域)，搜索+清空、新建跳转添加通关权限页、表格列：权限策略名称、类型(人员/车辆)、权限性质(公共/保密)、备注、所属园区、行操作：编辑、删除(二次确认)、按 type 条件展示：type=1 显示'关联员工'、type=3 显示'关联内部车辆'，跳详情页(带 backPageTag=limit)、分页 | 通关权限策略Authority(authorityName/type/areaType/parkId/remark) | src/views/platform/area/limit/index.vue |
| 门禁设备通关权限策略 | 列表/CRUD | ✓ | /platform/area/limit/indexAccess | 列表固定按 deviceUseType=1(门禁) 过滤查询、搜索：所属园区、通关权限名称、公共/保密区域、新增/编辑跳转时携带 deviceUseType=1，设备类型被锁定为门禁、编辑、删除(二次确认)、type=1 关联员工(backPageTag=limitAccess)、type=3 关联内部车辆、分页 | 通关权限策略Authority(deviceUseType=1门禁) | src/views/platform/area/limit/indexAccess.vue |
| 考勤设备通关权限策略 | 列表/CRUD | ✓ | /platform/area/limit/indexAttendance | 列表固定按 deviceUseType=2(考勤) 过滤查询、搜索：所属园区、通关权限名称、公共/保密区域、新增/编辑跳转时携带 deviceUseType=2(考勤)、编辑、删除(二次确认)、type=1 关联员工(backPageTag=limitAttendance)、type=3 关联内部车辆、分页 | 通关权限策略Authority(deviceUseType=2考勤) | src/views/platform/area/limit/indexAttendance.vue |
| 添加权限策略 | 表单 |  | /platform/area/limit/add | 表单字段：权限名称、所属园区(parkSelect)、权限类型(人员/车辆)、权限性质(公共/保密)、备注(≤60字)、设备类型(门禁/考勤)、权限类型为车辆(type=3)时权限性质锁定公共区域并禁用、从路由 query.deviceUseType 带入并锁定设备类型(isAlone)、按 类型+园区(+区域性质)动态加载设备树：车辆调 getTree、人员调 getTreePersonNew、设备树多选(check-strictly 非级联)，提交时取勾选 keys 作为 checkedlimits、保存(校验后 addObj)返回上一页、取消返回 | 通关权限策略Authority、设备/区域树Device(checkedlimits) | src/views/platform/area/limit/add.vue |
| 编辑权限策略 | 表单 |  | /platform/area/limit/edit/:id | getObj 回显策略详情，权限类型只读不可改、字段：权限名称、所属园区、权限性质(车辆类型时禁用)、备注、设备类型(query带deviceUseType时锁定)、设备树回显默认勾选(default-checked-keys=checkedlimits)，可重新勾选、按 类型+园区 动态加载设备树(车辆 getTree / 人员 getTreePersonNew)、保存(putObj，带loading)返回、取消返回 | 通关权限策略Authority、设备/区域树Device | src/views/platform/area/limit/edit.vue |
| 权限关联员工 | 列表/CRUD |  | /platform/area/limit/personDetail/:id/:type | 顶部显示当前操作权限名称，返回按钮按 backPageTag 回到对应策略列表、搜索：工号(点击弹出批量粘贴工号框，标签展示已选工号数)、姓名、表格列：工号、姓名、员工状态(staffStatusInit过滤器)、创建时间，多选、批量删除选中员工(batchDel，二次确认)、清空该策略全部员工权限(clearAll，二次确认)、批量粘贴：弹窗粘贴工号文本→batchAdd 批量关联员工，返回不符合条件工号提示、分页 | 策略-员工关联(badge/personName/staffStatus)、通关权限策略authId | src/views/platform/area/limit/personDetail.vue |
| 权限关联车辆 | 列表/CRUD |  | /platform/area/limit/vechileDetail/:id/:type | 搜索：车牌号、车主姓名、表格列：车牌号、车主、创建时间，多选、批量删除选中车辆(batchDel，二次确认)、清空该策略全部车辆权限(clearAll，二次确认)、单行删除(rowDel)、分页 | 策略-车辆关联(vehiclePlate/personName)、通关权限策略authId | src/views/platform/area/limit/vechileDetail.vue |
| 园区管理 | 列表/CRUD | ✓ | /platform/area/park/index | 新增/编辑园区弹窗：园区名称、地址、转发服务url(bridgeUrl)、园区定位范围(米/radius)、经度、纬度、咨询电话、占地面积(亩)、厂房个数、食堂楼栋个数，含名称/经纬度/电话/数字校验、表格列：园区名称、地址、转发服务url、定位范围、经纬度、咨询电话、删除园区(二次确认)、行操作'配置信息'跳转园区设置页(带 parkId)、分页 | 园区Park(parkName/parkAddress/bridgeUrl/radius/经纬度/parkPhone/area/workShopNum/diningRoomNum) | src/views/platform/area/park/index.vue |
| 园区配置信息(Tab容器) | 特殊/公开页 |  | /platform/area/park/setting | el-tabs 五个页签：组织关系(setBu)、访客设置(setVisitor)、招聘设置(setRecruit)、通行权限(setDeviceAuth)、BU权限配置(setBuAuth)、从路由 query.parkId 透传给各子配置组件 | 园区parkId | src/views/platform/area/park/setting.vue |
| 园区配置-组织关系 | 表单 |  | /platform/area/park/setting/setbu | 穿梭框(el-transfer)选择园区关联BU(可过滤)、填写物流中心ID(logisticId)、复选职层：勾选不允许车辆入园申请的职层(getJchesObj 取职层字典)、回显已配置(viewOrgInfo)，保存(saveOrgInfo)后返回园区列表 | 园区组织关系(workCompList/logisticId/jcheList)、BU、职层字典 | src/views/platform/area/park/setBu.vue |
| 园区配置-访客设置 | 表单 |  | /platform/area/park/setting/setvisitor | 动态渲染开关列表(parkSwitchArray)，每项 el-switch 启用(1)/停用(0)、访客到访(visitor_arrive)项支持设置到访前N分钟通知、getSwitcheList 回显，saveSwitch 保存后返回园区列表 | 园区开关(switchName/switchCode/isOn/beforeTime) | src/views/platform/area/park/setVisitor.vue |
| 园区配置-招聘设置 | 表单 |  | /platform/area/park/setting/setrecruit | 联动下拉：BU(getCompList)、签约单位ID(getConComanyList)、工作地点(getWorkBaseList)、新增一行合同签约单位(BU/签约单位/工作地点)，三项必选校验、表格展示已添加的签约单位并支持删除行、回显(getRecruitSetInfo)，保存(saveRecruitSetInfo)后返回园区列表 | 招聘签约单位(workComp/workOrg/workBase)、compOrgList | src/views/platform/area/park/setRecruit.vue |
| 园区配置-通行权限 | 表单 |  | /platform/area/park/setting/setDeviceAuth | 员工区：员工刷脸通行权限、员工车辆通行权限、特殊职层车辆通行权限(选职层)及对应车辆权限、访客区：访客人员通行权限、访客车辆通行权限、更多区：物流车辆、公司车辆、非员工车辆通行权限、各项用 personAuth/vehicleAuth 下拉选已建策略，按 businessCode(1-10) 组装 auth 数组，均必填、fetchAuthList 拉人/车策略候选(type=1/3)，fetchAuthId 回显，editAuth 保存后返回园区列表 | 园区通行权限映射(auth[businessCode].authId/jcheIds)、人车通关策略 | src/views/platform/area/park/setDeviceAuth.vue |
| 园区配置-BU权限配置 | 表单 |  | /platform/area/park/setting/setBUAuth | 列表展示各BU(compName)、设置状态(已设置/未设置)、已配权限标签(securityId，超出显示+N)、每个BU点击编辑打开穿梭框弹窗(batchAuth)多选权限策略、getBuList 回显，saveBuEdit 批量保存(compId/parkId/securityId)，成功提示并刷新 | BU-权限关联(compId/parkId/securityId)、BU列表 | src/views/platform/area/park/setBuAuth.vue |

<details><summary>子组件清单(6)</summary>

- `doPaste.vue` — 粘贴员工工号文本框，按换行/逗号拆分后 batchAdd 批量给策略关联员工
- `doPasteBadge.vue` — 搜索区用的批量粘贴工号框，最多200个，回传给父页作为查询条件
- `batchAuth.vue` — 穿梭框多选某BU可关联的权限策略，回写到 setBuAuth 行数据
- `jcheList.vue` — 封装职层(getJche)多选下拉，供通行权限页选特殊职层
- `personAuth.vue` — 封装人员类通关权限策略单选下拉(数据由父组件传入)
- `vehicleAuth.vue` — 封装车辆类通关权限策略单选下拉(数据由父组件传入)

</details>


## 13-visitor · 访客管理

访客管理模块覆盖园区/厂区的访客与货车入厂全流程：访客在线预约 → 多级审批（含代理审批人、审批白名单/岗位免二级审批）→ 预约码/身份证核验 → 自助凭条打印（Brother 标签打印插件 bpac）→ 现场抓拍与当天到访展示 → 黑名单管控。包含访客预约记录、入厂申请记录(访客/货车两类)、当天来访、黑名单(园区+EHR)、白名单、审批权限岗位、代理审批人、邮件定时推送等子功能。核心业务流是预约-审批-核验下发门禁权限-凭条打印-到访抓拍。

**页面 14 个 / 子组件 3 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 访客预约记录 | 列表/CRUD | ✓ | /platform/visitor/visitor_record/index | 多条件搜索(访客姓名/手机号/是否有车/车牌号/所属单位/来访事由/被访人姓名/园区BU部门级联/来访状态/预约来访时间段)、清空搜索条件并重查、卡片列表展示访客照片/姓名/手机/车牌/单位/园区及来访-离开时间，按状态着色徽标(已通过/已拒绝/待审批/已到达/超时未到/已离开/预约超时)、重新下发门禁权限reSend(仅审批通过0/超时未到4状态可用)、删除通行权限delAuth(hasAuth=1时可用)、导出Excel表格(最多1000条，含健康码/行程码等15列，状态文本化)、新窗口打开访客核实页(/#/visitor/qrCode)、点击卡片跳转预约记录详情，携带分页与搜索条件回填、分页(8/16/24/32/40) | 访客预约记录visitorRecord、访客visitorName/visitorPhone/visitorPhoto/company、车牌vehiclePlate、来访事由cause、来访状态status、被访人receptionist、园区/BU/部门、健康码/行程码 | src/views/platform/visitor/visitor_record/index.vue |
| 访客预约记录详情 | 详情 |  | /platform/visitor/visitor_record/detail/:id | 展示访客信息(照片/姓名/证件类型号码/手机/车牌/来访单位/预约来访离开时间/来访事由/园区/创建时间/拒绝理由)、疫情管控信息展示(行程码/健康码图片，viewer大图预览)、园区驻厂/家属住宿(cause=5/7)展示驻厂说明与身份证正反面照片、随行人员swiper轮播展示(照片+姓名)、被访人信息表(姓名/工号/BU/部门/岗位/职层/电话)、已到达(status=3)展示现场抓拍照片列表与抓拍时间、审批记录竖向步骤条(审批人工号-姓名-职层、审批结果与时间)、加入黑名单(取证件号/姓名/园区，二次确认后提交)、返回列表并回填分页与搜索条件 | 访客详情visitorInfo、证件certType/certNo、随行人员fellowVisitorList、被访人receptionist、抓拍snapVisitorList、审批流程processList、黑名单addBlackObj | src/views/platform/visitor/visitor_record/detail.vue |
| 入厂申请记录(访客) | 列表/CRUD | ✓ | /platform/visitor/incoming_record/index | 多条件搜索(访客姓名/证件号/手机号/是否有车/车牌/所属单位/来访事由(枚举接口)/被访人姓名/园区BU部门级联/来访状态/预约时间段)、身份证读卡器WebSocket(ws://127.0.0.1:33666)读取证件号自动查询、卡片展示设备下发状态(待下发/下发成功/下发失败/下发中/已下发)、重新下发门禁权限(审批通过0/已到达3/超时未到4/已离开5可用)、删除通行权限(二次确认弹窗)、导出Excel(最多1000条，状态文本化)、新窗口打开打印访客条页(/#/qrCodeNew)、点击卡片跳转入厂申请详情、来访事由枚举动态加载、分页 | 入厂申请记录incomingRecord(applyType=1)、访客信息、设备下发状态deviceStatus、来访事由枚举、证件号certNo、门禁权限hasAuth | src/views/platform/visitor/incoming_record/index.vue |
| 入厂申请记录(货车) | 列表/CRUD | ✓ | /platform/visitor/incoming_record/truck | 多条件搜索(同访客记录)、身份证读卡器WebSocket读取证件号查询、货车/车辆占位图区分展示(cause在13-16区间显示车辆图)、删除通行权限(二次确认)、导出Excel(最多1000条)、新窗口打开打印访客条页(/#/qrCodeNew)、点击卡片跳转入厂申请详情、来访事由枚举动态加载、分页 | 入厂申请记录incomingRecord(applyType=2 货车)、车辆vehiclePlate、司机/来访事由、门禁权限 | src/views/platform/visitor/incoming_record/truck.vue |
| 入厂申请记录详情 | 详情 |  | /platform/visitor/incoming_record/detail/:id | 展示预约码smsCode、申请基本信息表(申请人/申请部门/来访单位/申请时间/来访事由/来访类别/是否拍照/携带物品/来访种类/区域接待人/状态)、授权进入区域类别展示，按flag区分新工厂/老工厂区域(getAreaType映射code→desc)、短期来访随行人员表(姓名/性别/证件号/进入起止时间段/访客照片)、车辆通行证办理表(司机姓名/籍贯/驾驶证号/紧急联络人及方式/车牌/车型/颜色/车辆类型/证件类型/相关证件图)、已到达展示抓拍信息列表、审批记录竖向步骤条(创建人-节点名、审批描述与时间)、返回上一页 | 入厂申请详情、授权区域areaType/新老工厂、随行人员fellowVisitorList、车辆通行证vehicleList、抓拍snapVisitorList、审批流程processList、预约码smsCode | src/views/platform/visitor/incoming_record/detail.vue |
| 当天来访(实时监控) | 看板/大屏 | ✓ | /platform/visitor/visitor_intraday | 定时300秒轮询最新抓拍访客(searchNewSnapVisitor)与今日到访列表、展示来访信息(人脸底图/人脸车辆抓拍图、访客姓名/身份证/手机/车牌/来访单位/到访时间/来访事由/来访区域/园区)、随行人员人脸底图与抓拍图对比展示、被访人信息(姓名/电话/部门)、今日到访avue表格(访客姓名/来访园区/所属单位/被访部门/预约来访离开时间)，新抓拍自动置顶去重、viewer图片大图预览、分页 | 当天来访snapVisitor、人脸底图visitorPhoto/抓拍图snapPhoto、随行人员snapTodayFellowVisitorList、被访人、到访时间snapTime/来访区域areaName | src/views/platform/visitor/visitor_intraday/index.vue |
| 黑名单管理 | 列表/CRUD | ✓ | /platform/visitor/blacklist/index | 园区黑名单/EHR黑名单单选切换(EHR只读)、搜索(姓名/身份证号/园区)、清空搜索、新增黑名单(姓名/身份证号(带校验)/所属园区/原因，弹窗表单)、删除黑名单(二次确认)、批量导入黑名单(选择园区+上传Excel，前端XLSX解析校验姓名/身份证号/原因列，下载模板/resource/blacklist.xlsx)、导入失败按身份证号+原因明细提示、avue表格展示(园区/姓名/身份证号/创建人/原因/创建时间)、分页 | 园区黑名单blacklist、EHR黑名单blacklistHr、身份证号cardNo、园区parkId、拉黑原因reason、批量导入Excel | src/views/platform/visitor/blacklist/index.vue |
| 审批白名单 | 列表/CRUD | ✓ | /platform/visitor/whiteList/index | 信息提示：列表内人员不进行二级审批、搜索(工号/姓名/园区BU部门级联/岗位)、清空搜索、添加白名单(选园区+输工号查询员工信息回填姓名/BU/部门/岗位，校验该员工是否有所选园区权限)、删除白名单(二次确认)、批量删除(勾选行)、avue表格(园区/工号/姓名/BU/部门/岗位/创建时间，支持selection)、分页 | 审批白名单whiteList、员工staffBadge/staffName、园区/BU/部门/岗位、免二级审批 | src/views/platform/visitor/whiteList/index.vue |
| 访客审批权限(岗位) | 列表/CRUD | ✓ | /platform/visitor/approveAuthority/index | 信息提示：列表内岗位不进行二级审批、搜索(岗位/园区)、清空搜索、添加岗位(园区→BU→部门→岗位级联联动选择，层级变更自动清空下级)、删除岗位权限(二次确认)、批量删除(勾选行)、avue表格展示岗位权限列表、分页 | 访客审批权限approveAuthority、园区/BU/部门/岗位级联、免二级审批岗位 | src/views/platform/visitor/approveAuthority/index.vue |
| 代理审批人 | 列表/CRUD | ✓ | /platform/visitor/agent/index | 信息提示：经理以上可设代理人，审批同时发送给代理审批人、搜索(被访人工号/姓名/代理审批人工号/姓名)、清空搜索、添加代理人(输被访人工号查询回填姓名+取park，输代理审批人工号查询回填姓名，弹窗表单)、删除代理人(二次确认)、avue表格(园区/被访人工号姓名BU部门/代理审批人工号姓名BU部门/创建时间)、分页 | 代理审批人proxy、被访人interViewee、代理审批人proxyBadge/proxyName、园区parkId | src/views/platform/visitor/agent/index.vue |
| 访客记录邮件推送设置 | 表单 | ✓ | /platform/visitor/email_push/index | 选择所属园区(切换后加载该园区已有推送配置)、设置记录统计周期(每天/每周/每月)、维护推送人列表(姓名+邮箱，输入后点'+'添加，带姓名/邮箱格式校验)、删除推送人行、保存推送配置(有数据走editObj更新)、说明邮件命名规则：{园区名称}-访客记录报表-统计起止时间 | 邮件推送配置emailPush、园区parkId、统计周期type(0天/1周/2月)、推送人receiver/email | src/views/platform/visitor/email_push/index.vue |
| 访客凭条打印 | 特殊/公开页 |  | /platform/visitor/qrCode/index 与 /qrCode、/qrCodeNew | 6位预约码输入(自动聚焦+扫码器/虚拟数字键盘)，满6位自动查询、凭条打印提示弹窗(预约打印4步说明)、查询访客信息并校验访客码有效性(delFlag失效提示)、Brother bpac插件检测，未安装提示并引导安装Chrome扩展、按是否VIP选择标签模板(visitor-VIP.lbx/visitor.lbx)、填充模板字段(访客姓名/园区/公司/被访人/来访事由/起止时间)并打印主访客及随行人员、打印后删除预约码(delSmsCode)并清空键盘 | 访客凭条打印、预约码keyCods/smsCode、访客信息visitorData、随行人员memberList、bpac标签模板lbx、VIP模板 | src/views/platform/visitor/qrCode/index.vue |
| 访客凭条打印(新版) | 特殊/公开页 |  | /platform/visitor/qrCode_new/index 与 /qrCodeNew | 核验方式切换：扫码(预约码6位)/识别身份证(证件号18位)、对应输入框格位与提示文案动态切换、扫码/键盘/读卡器输入满位自动查询(getInfoApi/getInfoApiCard)、授权进入区域类别新老工厂映射(getAreaType flag=0/1)、凭条打印提示弹窗、Brother bpac插件打印访客标签(主访客+随行人员)、打印完成清理 | 访客凭条打印新版、预约码keyCods、身份证号cardCods、核验方式checkType、授权区域areaType、bpac标签模板 | src/views/platform/visitor/qrCode_new/index.vue |
| 访客核实 | 特殊/公开页 |  | /visitor/qrCode | 读卡器WebSocket初始化，自动读取二维码/身份证、6位预约码或18位身份证号输入(扫码/虚拟键盘)，满位自动查询、查询成功后router跳转到访客预约记录详情/platform/visitor/visitor_record/detail/:id、凭条打印提示弹窗、错误提示 | 访客核实、预约码keyCods、身份证号cardCods、读卡器socket、跳转预约详情 | src/views/platform/visitor/visitor_record/qrCode/index.vue |

<details><summary>子组件清单(3)</summary>

- `keys.vue` — 凭条打印页内嵌的0-9数字虚拟键盘子组件，含删除键，最多6位，通过doKeyPress事件回传码值
- `keys.vue` — 新版凭条打印页内嵌的数字虚拟键盘子组件，功能同qrCode/keys，供扫码核验输入使用
- `keys.vue` — 访客核实页内嵌的数字虚拟键盘子组件，功能同其他keys，输入预约码/证件号

</details>


## 14-work · 业务监控（请假/加班/补卡/调休/外宿补贴/离职/入职/退宿审批记录）

业务监控模块是对各类员工自助/审批类业务单据的只读监控台，统一布局为「列表页 + 详情页」两层结构。8 个子模块（请假 askLeave、加班 overTime、补卡 attendance、调休 breakOff、离职申请 leaveApplication、入职记录 toStaff、外宿补贴 outDormitory、退宿 checkedOut）各自从对应业务系统接口分页拉取单据列表，支持按工号/姓名/时间范围（退宿额外按园区）检索，点击「查看详情」跳转到只读详情页展示工单/班次/审批等完整信息。整个模块没有任何新增、编辑、删除、审批操作，仅做查询与详情查看（API 层只有 fetchList + getById 两个 GET 接口），属于监控/留痕性质。列表页为后端动态下发菜单的主入口，详情页通过 router/platform/index.js 显式声明、按 recordId 路由可达。

**页面 16 个 / 子组件 0 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 请假记录列表 | 列表/CRUD | ✓ | /platform/work/askLeave/index | 按工号(staffBadge)精确检索、按姓名(staffName)检索、按创建时间区间(datetimerange)检索，默认00:00:00-23:59:59、搜索/清空(重置表单)按钮、avue-crud 分页列表(默认20条/页，按 create_time 倒序)、列表列：工号/姓名/BU/部门/岗位/开始时间/结束时间/请假类型/请假时长/请假原因/流程编号/创建时间、开始/结束时间用 dateFormat2 过滤器格式化、行内「查看详情」跳转详情页(携带 queryForm/queryPage 以便返回时回填检索条件)、无新增/编辑/删除按钮(纯监控只读) | 请假申请记录(staffBadge,staffName,compName,depName,jobName,startDate,endDate,typeDesc,vacateCount,cause,processId,recordDate,recordId) | src/views/platform/work/askLeave/index.vue |
| 请假详情 | 详情 |  | /platform/work/askLeave/detail/:id | 按路由 id 调用 getById 拉取详情(取 data.employee)、工单信息区：流程编号、创建时间、班次信息区：班次名称、二入/二出/四入/四出/五入/五出 打卡点、请假信息区：工号/姓名/BU/部门/岗位/开始时间/结束时间/请假类型/请假时长(含单位)/请假原因、附件图片预览(viewer 组件，加载失败显示默认人像)、返回按钮(回列表并回填原检索条件) | 请假详情(processId,createDate,className,secondEnter/Out,fourthEnter/Out,fifthEnter/Out,employeeBadge,employeeName,buName,deptName,jobName,startDate,endDate,vacateTypeDesc,vacateCount,unit,vacateDesc,photo) | src/views/platform/work/askLeave/detail.vue |
| 加班记录列表 | 列表/CRUD | ✓ | /platform/work/overTime/index | 按工号/姓名/创建时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页，create_time 倒序)、列表列：工号/姓名/BU/部门/岗位/加班时间/班级(workClassCodeDesc)/加班类型/加班时长/加班原因/流程编号/创建时间、status 列自定义插槽展示状态、行内「查看详情」跳转详情(携带检索条件)、纯监控只读，无增删改 | 加班申请记录(staffBadge,staffName,compName,depName,jobName,extraworkDate,workClassCodeDesc,extraworkTypeName,extraworkCount,cause,processId,recordDate,recordId,status) | src/views/platform/work/overTime/index.vue |
| 加班详情 | 详情 |  | /platform/work/overTime/detail/:id | 按路由 id 调用 getById 拉取详情(取 data.employee)、工单信息区：流程编号、创建时间、班次信息区：班次名称、二/四/五入出打卡点、加班信息区：工号/姓名/BU/部门/岗位/加班时间/班别/加班类型、分段加班起止时间展示：第二段/第四段/第五段 开始与结束时间(空值显示-)、加班时长(小时)、加班原因、返回按钮(回列表并回填检索条件) | 加班详情(processId,createDate,classDesc,extraworkDate,extraworkClassDesc,extraworkTypeDesc,startDate2/4/5,endDate2/4/5,extraworkCount,extraworkDesc,班次打卡点) | src/views/platform/work/overTime/detail.vue |
| 补卡记录列表 | 列表/CRUD | ✓ | /platform/work/attendance/index | 按工号/姓名/创建时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页，create_time 倒序)、列表列：工号/姓名/BU/部门/岗位/考勤月份/补卡开始时间/补卡原因/流程编号/创建时间、行内「查看详情」跳转详情(携带检索条件)、纯监控只读 | 补卡申请记录(staffBadge,staffName,compName,depName,jobName,workMonth,patchDate,patchReasonDesc,processId,recordDate,recordId) | src/views/platform/work/attendance/index.vue |
| 补卡详情 | 详情 |  | /platform/work/attendance/detail/:id | 按路由 id 调用 getById 拉取详情(直接取 data)、工单信息区：流程编号、创建时间、补卡信息区：工号/姓名/BU/部门/岗位/考勤月份/补卡开始时间/补卡原因、二/四/五段进出打卡时间及各段是否跨天(secondOutCover等，1=是)、备注、附件图片预览(viewer)、返回按钮(回列表并回填检索条件) | 补卡详情(processId,createTime,employeeBadge,employeeName,buName,depName,jobName,workMonth,startTime,causeDesc,secondEnter/Out,fourthEnter/Out,fifthEnter/Out,*Cover跨天标记,remark,photoUrl) | src/views/platform/work/attendance/detail.vue |
| 调休记录列表 | 列表/CRUD | ✓ | /platform/work/breakOff/index | 按工号/姓名/创建时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页，create_time 倒序)、列表列：工号/姓名/BU/部门/岗位/出勤时间/调休时间/调休原因/流程编号/创建时间、行内「查看详情」跳转详情(携带检索条件)、纯监控只读 | 调休申请记录(staffBadge,staffName,compName,depName,jobName,workDate,restDate,restDesc,processId,recordDate,recordId) | src/views/platform/work/breakOff/index.vue |
| 调休详情 | 详情 |  | /platform/work/breakOff/detail/:id | 按路由 id 调用 getById 拉取详情(取 data.employee)、工单信息区：流程编号、创建时间、调休信息区：工号/姓名/BU/部门/岗位/出勤时间/调休时间、要调休天数(restCount)与可调休天数(restAbleCount)对比展示、调休原因、返回按钮(回列表并回填检索条件) | 调休详情(processId,createTime,staffBadge,staffName,buName,depName,jobName,workDate,restDate,restCount,restAbleCount,restDesc) | src/views/platform/work/breakOff/detail.vue |
| 离职申请列表 | 列表/CRUD | ✓ | /platform/work/leaveApplication/index | 按工号(badge)/姓名(name)/离职时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页)、列表列：工号/姓名/BU/部门/岗位/离职日期/离职原因/离职类型/流程编号/创建时间、离职日期 datetime 类型格式化为 yyyy-MM-dd、行内「查看详情」跳转详情(携带检索条件)、纯监控只读 | 离职申请记录(badge,name,compName,depName,jobName,leaveTime,leaveReasonDesc,leaveTypeDesc,processId,createTime) | src/views/platform/work/leaveApplication/index.vue |
| 离职申请详情 | 详情 |  | /platform/work/leaveApplication/detail/:id | 按路由 id 调用 getById 拉取详情(取 data 与 data.items)、离职主信息区(双列)：工号/姓名/BU/部门/岗位/入职日期/离职日期/离职类型/离职原因/创建时间、离职交接明细表(el-table)：责任部门/交接项目/交接人工号/交接人姓名/金额/交接说明、返回按钮(回列表并回填检索条件) | 离职详情(badge,name,compName,depName,jobName,joinTime,leaveTime,leaveTypeDesc,leaveReasonDesc,createTime)、离职交接项(zrdepName,jjItem,jjr,jjrName,je,jjRemark) | src/views/platform/work/leaveApplication/detail.vue |
| 入职记录列表 | 列表/CRUD | ✓ | /platform/work/toStaff/index | 按工号/姓名/创建时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页)、列表列：工号/姓名/BU/部门/岗位名称/关联ID(seqId)/创建时间、行内「查看详情」跳转详情(携带检索条件)、纯监控只读 | 入职记录(badge,name,compName,depName,jobName,seqId,createTime) | src/views/platform/work/toStaff/index.vue |
| 入职记录详情 | 详情 |  | /platform/work/toStaff/detail/:id | 按路由 id 调用 getById 拉取档案(data.smtStaff/education/work/family/relation)、右侧锚点导航按钮：入职主信息/教育经历/工作经验/家庭成员/任职关系(页内 #id 跳转)、入职主信息：头像图片预览(viewer)+工号/姓名/公司/部门/岗位、教育经历列表：起止时间/学校/专业/学历(含是否最高学历)/学位(含是否最高学位)、工作经验列表：起止时间/服务单位/岗位/负责人/联系方式、家庭成员列表：姓名/关系/性别/生日/电话/单位/职务、任职关系列表：亲属工号/姓名/任职关系/详细关系/公司/部门/岗位 | 入职档案(smtStaff:badge/name/compName/depName/jobName/facePic)、教育经历、工作经验、家庭成员、任职关系 | src/views/platform/work/toStaff/detail.vue |
| 外宿补贴列表 | 列表/CRUD | ✓ | /platform/work/outDormitory/index | 按工号(staffBadge)/姓名(name)/创建时间区间检索、搜索/清空按钮、avue-crud 分页列表(20条/页)、列表列：工号/姓名/BU/部门/岗位/补贴开始时间/补贴类型/补贴金额/流程编号/创建时间、行内「查看详情」跳转详情(携带检索条件)、纯监控只读 | 外宿补贴记录(staffBadge,name,compName,depName,jobName,startTime,allowanceType,amount,processId,createTime) | src/views/platform/work/outDormitory/index.vue |
| 外宿补贴详情 | 详情 |  | /platform/work/outDormitory/detail/:id | 按路由 id 调用 getById 拉取详情(直接取 data)、工单信息区：流程编号、创建时间、外宿补贴信息区：工号/姓名/BU/部门/岗位/外宿地址/补贴开始时间/补贴结束时间/补贴类型/计算规则/补贴说明/补贴金额/备注、返回按钮(回列表并回填检索条件) | 外宿补贴详情(processId,createTime,staffBadge,name,compName,depName,jobName,outAddress,startTime,endTime,allowanceType,computaionRule,explain,amount,remark) | src/views/platform/work/outDormitory/detail.vue |
| 退宿记录列表 | 列表/CRUD | ✓ | /platform/work/checkedOut/index | 园区下拉选择(parkSelect 组件，带默认值回调 defaultHandle)、按申请人工号(badge)/申请人姓名(name)/申请时间区间检索、搜索/清空按钮、avue-crud 分页列表、列表列：园区/宿舍信息/退宿原因/预计离开时间/备注/申请人工号/申请人姓名/申请时间/审批节点(statusDesc)/是否处理(isHandle)、行内「查看详情」跳转详情(携带检索条件)、代码中保留(注释)楼栋多选与是否处理筛选，当前未启用、纯监控只读 | 退宿申请记录(parkName,dormitoryName,quitReasonDesc,applyLeaveTime,remark,badge,name,createTime,statusDesc,isHandle,parkId) | src/views/platform/work/checkedOut/index.vue |
| 退宿详情 | 详情 |  | /platform/work/checkedOut/detail/:id | 按路由 id 调用 getById 拉取详情(直接取 data)、解析 dorDetailStr 拼出园区名与宿舍信息、退宿信息区：园区/宿舍信息/退宿原因/预计离开时间/备注/申请人工号/姓名/申请时间、附件图片多图预览(viewer，路径拼 platform/image/view/)、放行信息区(仅 status>3 显示)：放行人/放行时间/状态、审批信息时间线(processRecord)：区分提交节点/审批节点，按 result 状态(0待审批/1通过/2拒绝/3关闭/4等待)着色，展示审批人/结果/备注/时间、返回按钮(回列表并回填检索条件) | 退宿详情(parkName,dormitoryName,quitReasonDesc,applyLeaveTime,remark,badge,name,createTime,dorDetailStr,imgs,status,securityStaff,leaveTime,statusDesc)、审批记录processRecord(recordNode,statusName,staffInfos:staffName/result/resultDesc/remark/recordDate) | src/views/platform/work/checkedOut/detail.vue |


## 15-outsourcing · 外包人员管理

该模块面向智慧园区/厂区的外包及派遣劳务人员全生命周期管理。核心业务流为:外包单位(企业)由平台建档并配置门禁权限→外包单位通过 Excel 提交批量入职申请单→园区方对申请单逐单审批(通过/拒绝)→审批通过后人员进入"在职人员"台账(支持新增/编辑/导入/照片导入/导出/批量离职)→离职人员单独归档查询。后端接口集中在 /platform/out/src/apply/*(申请审批)与复用的 /platform/basic/personnel_manage、organization_manage、staff_info(在职人员、单位、照片)。

**页面 12 个 / 子组件 3 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 外包人员申请单列表 | 列表/CRUD | ✓ | /platform/outsourcing/apply/index | 按申请时间区间(daterange)筛选、按状态筛选(待审批0/已通过1/已拒绝2)、搜索/重置查询条件、弹窗式两步导入向导发起 Excel 申请(componentImport)、列表展示申请单号/申请人数/申请时间/状态(avue-crud)、点击查看跳转申请详情(携带 applyId、queryPage、queryForm、isApprove=false 返回参数)、分页(size-change/current-change)、接口:GET /platform/out/src/apply/page | 申请单(applyId)、申请人数applyNum、申请时间applyTime、状态status/statusDesc | src/views/platform/outsourcing/apply/index.vue |
| 外包人员申请审批列表 | 列表/CRUD | ✓ | /platform/outsourcing/approve/index | 按单位名称compName模糊搜索、按申请时间区间、状态(待审批/已通过/已拒绝)筛选、搜索/重置、对待审批单点击'通过'(二次确认,POST passOrRefuse status=1)、对待审批单点击'拒绝',弹窗填写拒绝原因(必填、限100字)后提交(status=2)、点击查看跳转申请详情(isApprove=true)、列表含申请单号/单位名称/申请人数/申请时间/状态,分页、接口:GET /platform/out/src/apply/page、POST /platform/out/src/apply/passOrRefuse | 申请单applyId、单位名称compName、申请人数、申请状态statusDesc、拒绝原因reason | src/views/platform/outsourcing/approve/index.vue |
| 外包人员申请详情 | 详情 |  | /platform/outsourcing/detail/:id | 展示申请信息:申请单号/申请时间/申请状态+拒绝原因/审批人/审批时间、分页展示申请名单(部门/工号/姓名/手机号/身份证号/岗位/职层/入职日期/派遣渠道)、返回按钮按 isApprove 参数分别回到审批列表或申请列表、接口:POST /platform/out/src/apply/detail/{applyId}、GET /platform/out/src/apply/detail/page/{applyId} | 申请单applyId、审批人approver/审批时间、申请名单(部门/工号/姓名/身份证/岗位/职层/入职日期/派遣渠道) | src/views/platform/outsourcing/detail/detail.vue |
| 外包申请-Excel导入向导(容器) | 向导 |  |  | STP1->STP2 两步切换控制(step 状态)、向父组件透传 complete 完成事件、内嵌于申请列表'导入excel申请'弹窗 | 导入数据列表dataList | src/views/platform/outsourcing/apply/import/index.vue |
| 外包申请导入-步骤1上传Excel | 向导 |  |  | 下载导入模板(/resource/staff-temp.xlsx)、上传 xls/xlsx,用 XLSX 库解析(仅允许单工作表)、字段清洗与校验:姓名/岗位/工号/手机号/职层/身份证号/部门/入职日期非空校验、手机号格式校验(isMobile)、证件号校验(不含汉字、8~20位)、入职日期数字/斜杠格式归一化为 yyyy-MM-dd、解析成功进入下一步,数据 v-model 上抛、字段含'派遣渠道dispatch' | 导入人员行(name/post/jobNumber/phone/rank/identity/department/entryTime/dispatch) | src/views/platform/outsourcing/apply/import/step1.vue |
| 外包申请导入-步骤2确认并批量提交 | 向导 |  |  | 表格预览待导入名单(姓名/岗位/工号/手机号/职层/身份证号/部门/入职日期/派遣渠道)、逐行删除(二次确认)、返回上一步、批量导入提交(二次确认),字段映射为 badge/certno/depName/jcheName/jobName 等、接口:POST /platform/out/src/apply/excel/import | 待导入名单fromList、字段映射(badge/certno/depName/jcheName/jobName/name/phone/entryTime/dispatch) | src/views/platform/outsourcing/apply/import/step2.vue |
| 外包在职人员管理 | 列表/CRUD | ✓ | /platform/outsourcing/onwork/index | 左侧部门树:搜索过滤、节点新增/编辑/删除部门、编辑部门弹窗:上级部门/部门名称/部门主管(工号远程搜索)/关联c6部门、按工号、姓名、是否上传人脸照片筛选在职人员(status=1)、新增员工弹窗(基本信息+组织信息,含人脸上传componentUpload、手机/工号/身份证校验)、编辑员工(工号不可改,联动上级负责人getDirector)、Excel 两步导入员工(componentImport)、批量导入员工照片(以工号命名、jpg≤200KB、≤2000张,先校验工号存在性再上传base64)、导出在职人员 Excel(Export2Excel,含工号/姓名/证件号/部门/职层/岗位/入职日期/状态)、批量离职:输入多工号查询->勾选->批量移除->确认离职、人脸照片列表预览(viewer)、分页、接口:personnel_manage(getStaffPage/getStaff/postAddStaff/getDeptTree/getDeptList/delDept/getDeptDetails/postDeptSave/getDirector/getC6DeptList/getStaffByBadgeBatch/delStaffBatch)、staff_info(getStaffImgInfo/importImgs) | 在职员工(badge/name/sex/phone/certno/faceImg/jcheId/depId/jobName/entryTime/dispatch/status)、部门dept、部门主管director、c6部门、员工照片facePic | src/views/platform/outsourcing/onwork/index.vue |
| 外包离职人员管理 | 列表/CRUD | ✓ | /platform/outsourcing/onwork/leave | 左侧部门树过滤(只读,按部门筛选)、按工号、姓名筛选离职人员(status=0)、查看详情弹窗(全字段只读:基本信息+组织信息+照片)、删除离职人员(二次确认 postDelStaff)、导出离职人员信息 Excel、人脸照片预览、分页、接口:personnel_manage(getStaffPage/getStaff/postDelStaff/getDeptTree/getDeptList) | 离职员工(badge/name/certno/depName/jcheName/jobName/entryTime/status)、部门 | src/views/platform/outsourcing/onwork/leave.vue |
| 外包员工Excel导入向导(容器) | 向导 |  |  | STP1->STP2 两步切换、complete 完成事件上抛、内嵌于在职人员'导入员工'弹窗 | 导入数据列表dataList | src/views/platform/outsourcing/onwork/import/index.vue |
| 外包员工导入-步骤1上传Excel | 向导 |  |  | 下载模板(/resource/staff-temp.xlsx)、XLSX 解析 xls/xlsx(仅单工作表)、非空校验+工号校验(字母数字、6~12位)+手机号校验+身份证校验(不含空格/汉字、8~20位)、入职日期格式归一化、解析成功进入下一步,字段含'派遣单位dispatch'(与申请模块的'派遣渠道'略有差异) | 导入员工行(name/post/jobNumber/phone/rank/identity/department/entryTime/dispatch) | src/views/platform/outsourcing/onwork/import/step1.vue |
| 外包员工导入-步骤2确认批量导入 | 向导 |  |  | 表格预览待导入员工名单、逐行删除(二次确认)、返回上一步、批量导入(二次确认)、接口:postImportStaff(personnel_manage) | 待导入员工名单fromList | src/views/platform/outsourcing/onwork/import/step2.vue |
| 外包单位管理 | 列表/CRUD | ✓ | /platform/outsourcing/unit/index | 按组织名称buName搜索/清空、新增企业弹窗(园区选择/组织名称/管理员用户名密码/用户角色固定企业管理员/企业类型(外包单位1/派遣工2)/门禁权限多选)、园区切换联动加载门禁权限列表(deviceAuthList)、密码强度校验(validatePwd),编辑时密码留空不修改、编辑企业(用户名不可改、回填门禁权限)、删除企业(二次确认)、列表企业类型渲染(外包单位/派遣工),分页、接口:organization_manage(getDataList/postSaveOrgXuCh/delOrg/getDetails/getUserParkAll)、staff_info(deviceAuthList) | 外包单位/企业(compName/userName/password/compType/parkId)、管理员用户、门禁权限deviceAuthId、园区park | src/views/platform/outsourcing/unit/index.vue |

<details><summary>子组件清单(3)</summary>

- `_upload.vue` — 员工人脸相片上传组件:压缩、转JPG、调用算法服务人脸检测裁剪后回传base64
- `_import_img.vue` — 员工照片批量导入的无UI逻辑组件(读取文件、校验工号、上传),实际页面逻辑已内联到 onwork/index.vue
- `index-single.vue` — 基于 popover 的单选人员搜索下拉,按工号远程检索并回选单个人员

</details>


## 16-vehicle · 车辆管理

车辆管理模块负责智慧园区/厂区内各类车辆的台账与通行权限管理,覆盖员工车辆、公司车辆(公车)、非员工(临时)车辆三类车辆的录入、查询、导出与详情维护;同时提供入园审批(车主自助申请入园由管理员通过/拒绝)、车辆黑名单(禁入车辆维护)与车辆维护(车牌有效期/车辆状态台账)。核心业务流:录入车辆时绑定所属园区、车牌、车辆权限(authority)及对接员工(按工号查询)或临时联系人,并上传行驶证/驾驶证照片;通过车辆权限关联授权设备(道闸/限制区),配合道闸放行;员工自助申请入园进入审批流,审批通过后获得入园权限。所有列表均支持按园区/车牌/权限等条件检索并导出 Excel。

**页面 14 个 / 子组件 0 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 员工车辆列表 | 列表/CRUD | ✓ | /platform/vehicle/staff_vehicle/index | 按车主姓名/车牌号/车辆类型(大型车·小型车·其他车)检索、按所属园区/BU/部门级联(el-cascader)筛选、按车辆权限(authCarSelect 组件,随园区联动)筛选、按职层(福利等级,getWelfare 动态拉取)筛选、按员工状态(enumStaffStatus)筛选、按车辆状态(已添加/已删除 isDelete)筛选、清空搜索条件并重置分页、新增车辆:跳转 staff_vehicle/add、查看详情:跳转 staff_vehicle/detail/:id 并携带 pageType=1、当前分页与查询条件、删除车辆(确认弹框,delObj),仅 isDelete==0 显示删除按钮、导出车辆信息为 Excel(Export2Excel,size=10000 全量拉取,字段:车主/车牌号/车辆类型/园区/部门/职层/员工状态/车辆状态/手机号,含状态中文转译)、分页(size-change/current-change),固定 vehicleAscription=1 区分员工车辆、返回详情时回填查询条件与分页(queryForm/queryPage) | 车辆(vehicle)、车主/员工、车牌号、车辆类型、车辆权限(authority)、所属园区/BU/部门、职层(福利等级)、员工状态、车辆状态(isDelete) | src/views/platform/vehicle/staff_vehicle/index.vue |
| 添加员工车辆 | 表单 |  | /platform/vehicle/staff_vehicle/add | 上传行驶证照片(EXIF 方向校正+canvas 压缩为 base64,>2MB 缩放 1/5)、上传驾驶证照片(同上压缩逻辑)、选择所属园区(allPark),园区切换后联动加载该园区车辆权限(authorityList)、录入车牌号:正则校验中国车牌格式并自动转大写,调用 plate 接口校验是否已绑定防重复、选择车类型(大/小/其他)、车品牌、车颜色(15 种颜色枚举)、选择车辆权限(必填)、按工号查询员工(getStaffDetail),回填姓名/所属BU/部门/身份证号/性别/手机(只读),性别 0/1 转男/女、校验员工已查询(staffId 非空)后提交 addObj,固定 vehicleAscription=1、提交成功返回上一页;取消返回 | 车辆、行驶证、驾驶证、员工(工号/姓名/BU/部门/身份证/性别/手机)、车牌号、车辆权限、所属园区 | src/views/platform/vehicle/staff_vehicle/add.vue |
| 员工车辆详情/编辑 | 详情 |  | /platform/vehicle/staff_vehicle/detail/:id | pageType 区分:1员工车辆、2公司车辆、3临时人员,共用同一详情页(临时人员隐藏人员信息区,显示对口人/电话/备注)、查看车辆信息(行驶证/驾驶证图片可 viewer 放大)、人员信息表格、编辑模式切换:可改园区、车类型、车品牌、车颜色、车辆权限,重新上传证件照,车牌号只读、车牌号正则校验+自动转大写、切换所属园区联动:在已有权限集合 auths 中匹配当前园区权限(selectAuthByParkId)、车辆权限切换联动获取权限对应 type 及授权设备树(getTypeByAuthId/getDevices)、查看授权设备弹框:el-tree 树形展示该权限授权的道闸/限制区设备,仅展示禁止编辑(disabledDevice 全部 disabled)、编辑时按工号重新查询员工信息回填(selectStaffDetail)、保存(putObj)提交园区+权限+车辆信息;取消恢复查看态、返回按 pageType 跳回对应列表并回填查询条件 | 车辆、行驶证、驾驶证、员工/对口人员、车辆权限、授权设备(道闸/限制区)、所属园区 | src/views/platform/vehicle/staff_vehicle/detail.vue |
| 公司车辆列表 | 列表/CRUD | ✓ | /platform/vehicle/company_vehicle/index | 按对口人员/车牌号/车辆类型检索、按所属园区/BU/部门级联筛选、按车辆权限(authCarSelect)筛选、清空搜索并重置、添加公车:跳转 company_vehicle/add、查看详情:复用 staff_vehicle/detail/:id 并携带 pageType=2、删除公车(确认弹框 delObj),仅 isDelete==0 显示、导出公司车辆信息 Excel(size=100000,字段:对口人员/车牌号/车辆类型/园区/部门/车辆状态/手机号)、分页;固定 vehicleAscription=0 区分公司车辆、路由变化与返回时刷新/回填查询 | 公司车辆、对口人员、车牌号、车辆类型、车辆权限、所属园区/部门、车辆状态 | src/views/platform/vehicle/company_vehicle/index.vue |
| 添加公司车辆 | 表单 |  | /platform/vehicle/company_vehicle/add | 上传行驶证/驾驶证照片(EXIF 方向校正+canvas 压缩 base64)、选择所属园区,园区联动加载车辆权限(authorityList)、车牌号正则校验+转大写+plate 防重复校验(company_vehicle_detail.plate)、选择车类型、车品牌、车颜色、车辆权限(必填)、按工号查询对口员工(getStaffDetail)回填姓名/BU/部门/身份证/性别/手机(只读)、校验 staffId 后 addObj 提交,固定 vehicleAscription=0、提交返回;取消返回 | 公司车辆、行驶证、驾驶证、对口员工、车牌号、车辆权限、所属园区 | src/views/platform/vehicle/company_vehicle/add.vue |
| 非员工车辆列表 | 列表/CRUD | ✓ | /platform/vehicle/non_staff_vehicle/index | 按车牌号检索、按所属园区(parkSelect 组件)筛选、按车辆权限(authCarSelect,随园区联动)筛选、清空搜索并重置、添加车辆:跳转 non_staff_vehicle/add、查看详情:跳转 non_staff_vehicle/detail/:id 并携带 pageType=3、删除车辆(确认弹框 delObj)、备注列空值显示占位符 '-'、导出临时车辆信息 Excel(字段:对口人员/车牌号/电话/园区/备注)、分页;路由变化刷新 | 非员工/临时车辆、对口人员、车牌号、电话、所属园区、备注 | src/views/platform/vehicle/non_staff_vehicle/index.vue |
| 添加非员工车辆 | 表单 |  | /platform/vehicle/non_staff_vehicle/add | 上传行驶证/驾驶证照片(EXIF 校正+canvas 压缩 base64)、选择所属园区,园区联动加载车辆权限(authorityList)、车牌号正则校验+转大写+plate 防重复校验(non_staff_vehicle.plate)、选择车类型、车品牌、车颜色、车辆权限(必填)、手填对口人员姓名(必填)与电话(必填,isMobile 手机号校验)、填写备注(textarea)、addObj 提交,vehicleAscription=0;提交返回,取消返回 | 临时车辆、行驶证、驾驶证、对口人员、电话、车牌号、车辆权限、所属园区、备注 | src/views/platform/vehicle/non_staff_vehicle/add.vue |
| 非员工车辆详情/编辑 | 详情 |  | /platform/vehicle/non_staff_vehicle/detail/:id | 查看车辆信息(行驶证/驾驶证 viewer 放大),车类型/车颜色经本地 filter 转中文、编辑模式:改园区、车类型、车品牌、车颜色、对口人员、电话、车辆权限、备注,车牌号只读、车牌号正则校验+转大写;电话 isMobile 校验、切换园区在 auths 中匹配当前园区权限(selectAuthByParkId)、车辆权限切换联动获取授权设备树(getTypeByAuthId/getDevices)、查看授权设备弹框:el-tree 展示该权限授权设备(只读 disabled)、保存 putObj 提交;取消恢复查看态;返回回填查询条件 | 临时车辆、行驶证、驾驶证、对口人员、电话、车辆权限、授权设备、所属园区、备注 | src/views/platform/vehicle/non_staff_vehicle/detail.vue |
| 入园审批列表 | 列表/CRUD | ✓ | /platform/vehicle/entry_examine/index | 按车牌号检索、按申请园区(parkSelect)筛选、按申请时间区间(daterange,startTime/endTime)筛选、按申请状态(审批中0/已审批1/已拒绝2)筛选、清空搜索并重置、申请状态用彩色状态点展示(entryExamineFormat/entryExamineClassFormat 过滤器)、查看详情:跳转 entry_examine/detail/:id、通过审批:确认弹框后 putObj(status=1),仅 applyStatus==0 显示、拒绝审批:弹出拒绝原因弹框(reason 必填),putObj(status=2),仅 applyStatus==0 显示、分页;返回回填查询条件与时间区间 | 入园申请、车牌号、车主、所属部门、手机号、福利层级、申请园区、申请时间、申请状态、拒绝原因 | src/views/platform/vehicle/entry_examine/index.vue |
| 入园审批详情 | 详情 |  | /platform/vehicle/entry_examine/detail/:id | 展示申请信息:申请入园园区、申请时间、申请状态(过滤器转中文)、描述、审批人、审批时间、展示车辆信息:行驶证/驾驶证图片(viewer 放大)、车牌号、车类型、车品牌、车颜色、展示人员信息:头像、车主姓名、身份证号、性别(genderInit)、手机号、所属部门、返回列表并回填查询条件(fetchDetail 拉取) | 入园申请、车辆(行驶证/驾驶证/车牌/类型/品牌/颜色)、车主(姓名/身份证/性别/手机/部门)、审批信息(状态/审批人/时间/原因) | src/views/platform/vehicle/entry_examine/detail.vue |
| 车辆黑名单 | 列表/CRUD | ✓ | /platform/vehicle/vehicle_black/index | 按所属园区(parkSelect)检索、清空搜索并重置、添加车辆弹框:选园区、填车牌号(正则校验+转大写+plate 防重复)、填原因,saveObj 提交、编辑车辆弹框:getObj 回填,改园区/车牌/原因,putObj 提交(车牌同样校验防重复)、单条删除(rowDel 确认弹框 delObj),按返回 data 提示成功/失败、批量删除:勾选多行(selection)收集 ids,batch 接口批量删除,未选时提示、分页(size-change/current-change)、(注:文件首行注释误写为'园区管理',实为车辆黑名单,api 走 /platform/vehicle/black/*) | 黑名单车辆、所属园区、车牌号、创建时间、禁入原因(remark) | src/views/platform/vehicle/vehicle_black/index.vue |
| 车辆维护列表 | 列表/CRUD | ✓ | /platform/vehicle/vehicle_maintain/index | 按车牌号检索、按所属园区(parkSelect)筛选、按车辆状态(挂失0/过期1,cardState)筛选、按创建时间区间(daterange rangTimeIn)筛选、清空搜索并重置、添加车辆:跳转 vehicle_maintain/add、删除车辆(确认弹框 delObj),仅 isDelete==0 显示、查看详情按钮存在但 handleDetail 方法体被注释(detail.vue 为空文件,功能未实现)、导出按钮与 export2Excel 已整体注释停用、分页;api 走 /platform/vehicle/xc-*(本地 _service.js),列含联系人/联系方式/车牌/类型/状态/起止时间/园区/创建信息 | 维护车辆、联系人、联系方式、车牌号、车牌类型、车牌状态(挂失/过期)、有效期(起止时间)、所属园区、创建时间/创建人 | src/views/platform/vehicle/vehicle_maintain/index.vue |
| 添加车辆维护 | 表单 |  | /platform/vehicle/vehicle_maintain/add | 选择所属园区(allPark)、车牌号正则校验+转大写+plate 防重复(_service.plate,走 /platform/vehicle/plate)、选择车牌类型(临时/月租/充值/贵宾/免费/收费月租车牌,ctId 必填)、选择车类型(大/小/其他,必填)、车品牌(必填)、车颜色(白/黑/红,值为颜色码)、备注、按工号查询员工(getStaffDetail)回填联系人姓名/联系方式(只读)、选择车辆状态(挂失/过期 cardState)、选择有效期区间(daterange,提交时拆为 startDate/endDate)、校验联系人后 addObj 提交(xc-save);取消返回(注:提交成功后 router.go(-1) 被注释,不自动跳转) | 维护车辆、联系人(按工号)、联系方式、车牌号、车牌类型(ctId)、车类型、车品牌、车颜色、车辆状态、有效期(起止)、所属园区、备注 | src/views/platform/vehicle/vehicle_maintain/add.vue |
| 车辆维护详情(空) | 详情 |  | /platform/vehicle/vehicle_maintain/detail/:id | 空文件,无任何模板/逻辑;列表 handleDetail 已注释,实际不可达 |  | src/views/platform/vehicle/vehicle_maintain/detail.vue |


## 17-personnel_manage · 人资行政管理

人资行政管理模块覆盖考勤、工牌(厂牌)、员工餐补充值、工资/考勤签收确认与员工申诉反馈等行政事务。核心业务流:1)考勤侧——按园区/BU/部门/工号/月份筛选员工的"工资签收"与"考勤汇总确认"记录,可批量发送短信催办、导出 Excel、查看个人电子签名凭证并下载图片;另有考勤补卡异常统计。2)工牌侧——厂牌补领申请的同意/拒绝/确认领取审批流,以及厂牌挂失记录查询导出。3)充值侧——按新入职/在职两类生成员工餐补充值名单,支持特殊名单充值、修改应发餐补、一键清理、批量删除并同步到 C6 系统。4)申诉侧——接收员工反馈,人工回复或转交他人处理。所有列表页均为后端菜单动态下发的主入口,详情/审批走弹窗或独立详情路由。

**页面 11 个 / 子组件 3 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 考勤异常统计 | 列表/CRUD | ✓ | /platform/personnel_manage/abnormal_attendance/index | 园区/BU/部门三级级联筛选、补卡日期筛选、搜索/清空、分页表格展示补卡日期、园区、BU、部门、补卡原因、补卡人数、点击查看打开统计记录弹窗(只读详情) | 补卡日期startTime、园区parkName、BU compName、部门depName、补卡原因cause、补卡人数statistics | src/views/platform/personnel_manage/abnormal_attendance/index.vue |
| 厂牌挂失记录 | 列表/CRUD | ✓ | /platform/personnel_manage/badge_loss/index | 园区/BU/部门级联筛选、按工号筛选、按挂失时间区间(datetimerange)筛选、搜索/清空、导出 Excel(全量拉取后前端导出,文件名含时间)、点击查看打开挂失详情弹窗 | 员工工号badge、员工姓名name、所属园区parkName、BU compName、部门depName、挂失时间createTime | src/views/platform/personnel_manage/badge_loss/index.vue |
| 厂牌补领记录 | 列表/CRUD | ✓ | /platform/personnel_manage/badge_apply/index | 园区/BU/部门级联、工号、办理状态(枚举从后端getOperaStatus获取)、挂失时间区间筛选、搜索/清空、导出 Excel(含厂牌价格)、同意申请(state=1时):填写领取地址后提交editObj(state=2)、拒绝申请(state=1时):填写拒绝原因后提交editObj(state=4)、确认领取(state=2时):二次确认弹窗后editObj(state=3)、查看详情弹窗:申请信息+厂牌费用+办理进度时间轴(operaList) | 员工工号badge、员工姓名name、BU/部门/园区、申请原因reason、厂牌费用price、办理状态state/stateDesc、领取地址address、办理进度operaList | src/views/platform/personnel_manage/badge_apply/index.vue |
| 工资签收管理 | 列表/CRUD | ✓ | /platform/personnel_manage/salary_sign/index | 园区/BU/部门级联、工号、姓名、工资月份(默认上月)、签收状态(待签收/已签收)筛选、顶部展示已签收/未签收小计、发送短信提醒:先调sendMsgNum取人数→二次确认弹窗(含5s倒计时)→sendMsg按当前筛选群发、导出 Excel(长耗时二次确认,全量拉取)、搜索/清空/分页、点击查看跳转 /salary_sign/detail/:id | 工号badge、姓名name、BU/部门/园区、工资月份wageDate、签收状态signStatus/signStatusDesc、签收时间createTime | src/views/platform/personnel_manage/salary_sign/index.vue |
| 工资签收详情 | 详情 |  | /platform/personnel_manage/salary_sign/detail/:id | 按路由 id 调 getById 加载工资签收信息、展示薪资期间、签收状态、通知状态、工号/姓名/园区/BU/部门、base64 渲染本人电子签名图片(viewer 可放大)、html2Canvas 截图生成并下载'工资签收凭证'图片 | 薪资期间wageDate、签收状态signStatusDesc、通知状态noticeStatus、电子签名signImg(base64)、工号/姓名/园区/BU/部门 | src/views/platform/personnel_manage/salary_sign/detail.vue |
| 考勤汇总确认管理 | 列表/CRUD | ✓ | /platform/personnel_manage/attendance_sign/index | 园区/BU/部门级联、工号、姓名、考勤月份(默认上月)、确认状态(未确认/已确认)筛选、顶部展示已确认/未确认小计、发送短信提醒:sendMsgNum取人数→二次确认弹窗(5s倒计时)→sendMsg群发提醒考勤确认、导出 Excel(长耗时二次确认,全量拉取)、搜索/清空/分页、点击查看跳转 /attendance_sign/detail/:id | 工号badge、姓名name、BU/部门/园区、考勤月份checkDate、确认状态signStatus/signStatusDesc | src/views/platform/personnel_manage/attendance_sign/index.vue |
| 考勤汇总确认详情 | 详情 |  | /platform/personnel_manage/attendance_sign/detail/:id | 按路由 id 调 getById 加载考勤明细、左栏签收信息+本人电子签名(base64,viewer 放大)、右栏考勤信息:应/实出勤、平时/周末/法定加班、事假病假待料工伤婚丧产假陪产产检放行年休假、迟到早退累计与次数、夜班次数(avaGetskyPayYSHRDTO 各字段a6~a36)、html2Canvas 下载'考勤汇总确认签收凭证'图片 | 考勤期间checkDate、确认状态/通知状态、电子签名signImg、出勤/加班/假期/迟到早退/夜班等考勤项 a6-a36 | src/views/platform/personnel_manage/attendance_sign/detail.vue |
| 员工充值名单-新入职 | 列表/CRUD | ✓ | /platform/personnel_manage/new_recharge/index | 未同步/已同步单选切换(syncStatus)、园区(parkSelect)、BU(多选 selectBU)、工号(空格分隔多工号)、姓名、生成状态(已/未生成)、考勤月份(默认本月)、入职日期区间筛选、开启勾选开关切换表格 selection,勾选或全条件批量删除(deleteRecharge)、同步充值名单到C6(toC6,二次确认显示条数,返回提示弹窗)——仅未同步时、导出 Excel(动态标题exportTitle,长耗时二次确认)、查看充值详情弹窗(含餐补标准、应/实出勤、餐补结算、同步状态)、搜索/清空/分页 | 工号badge、姓名name、所属园区parkNames、BU/部门、福利层次welfareLevel、餐补标准standard、考勤月份checkMonth、应出勤shouldOn、实际应出勤actualOn、应发餐补account、同步状态syncStatus、rechargeType=1(新入职) | src/views/platform/personnel_manage/new_recharge/index.vue |
| 员工充值名单-在职 | 列表/CRUD | ✓ | /platform/personnel_manage/senior_recharge/index | 未同步/已同步单选切换、园区/BU(多选)/工号(空格分隔)/姓名/生成状态/考勤月份(默认上月)/入职日期区间筛选、生成充值名单(getSeniorInfo,长耗时二次确认)——仅未同步时、同步充值名单到C6(toC6,二次确认显示条数)、特殊名单充值(打开 dlg_specialRecharge 弹窗)、应发餐补可点击编辑(打开 dlg_account 弹窗,仅未同步)、一键清理(按月份+类型+同步状态全清 deleteRecharge)、批量删除(勾选或全条件)、导出 Excel(动态标题)、查看充值详情弹窗、搜索/清空/分页 | 工号badge、姓名name、园区parkNames、BU/部门、福利层次welfareLevel、餐补标准standard、考勤月份checkMonth、应/实出勤、应发餐补account/备注blank、同步状态syncStatus、rechargeType=2(在职) | src/views/platform/personnel_manage/senior_recharge/index.vue |
| 员工申诉(反馈)管理 | 列表/CRUD | ✓ | /platform/personnel_manage/staff_appeal/index | 按反馈问题类型(人事服务/宿舍服务/车间管理)、状态(已申诉/已回复)、反馈时间区间筛选、搜索/重置/分页、表格自定义列显示是否已转交(ischange)、状态为已申诉(1)显示'回复'、已回复(2)显示'查看',均跳转 staff_appeal/detail/:id 并带筛选与分页参数 | 工号badge、反馈人staffName、BU/部门、反馈人电话staffPhone、反馈类型appealType/appealTypeDesc、状态status/statusDesc、是否已转交ischange、反馈时间createTime | src/views/platform/personnel_manage/staff_appeal/index.vue |
| 员工反馈详情(回复/转交) | 详情 |  | /platform/personnel_manage/staff_appeal/detail/:id | 按 id 调 getDetails 加载反馈信息(含申诉图片 viewer 预览)、回复内容文本域(100字限制,实时字数统计;已回复时只读、已转交时禁用)、确定回复 saveReply 提交后返回列表、转交Ta人:弹窗输入工号 queryStaffList 查询员工→单选→changeStaff 转交、返回上一页(携带原查询条件与分页) | 反馈人/手机号/BU/部门/工号、反馈类型appealTypeDesc、反馈描述appealDesc、申诉图片appealImgs、状态status/isChange、回复内容replyDesc/回复人replyName/回复时间replyTime、转交工号changeBadge | src/views/platform/personnel_manage/staff_appeal/detail.vue |

<details><summary>子组件清单(3)</summary>

- `dlg_account.vue` — 在职充值名单中修改单条记录的应发餐补金额与备注(updateRecharge)
- `dlg_specialRecharge.vue` — 粘贴工号(空格分隔)+备注,对特殊名单批量发起餐补充值(singleRecharge)
- `select_bu.vue` — 依据园区 parkId 拉取并多选 BU 的下拉组件,供充值名单筛选复用

</details>


## 18-recruit-resume · 招聘 / 简历登记 / 预约访客

该模块覆盖企业招聘全链路与外部人员登记入口三块业务。招聘端(recruit)面向 HR:发布/管理招聘岗位、生成对外岗位投递链接、接收并按"已投递→已邀请→待复试→待入职→已入职/已入库/已拒绝"七态流转应聘者、批量 Excel 入职、维护面试/入职邮件与短信通知模板。简历登记端(resume)是从岗位链接(/gangwei/:id)进入的对外三步式入职简历采集流程(身份证 OCR→人脸+基础信息→紧急联系人/教育/工作/家庭/任职关系),最终投递到对应招聘岗位。预约访客端(appointment)是从裕同官网跳转进来的访客自助预约表单。其中 resume 与 appointment 的页面 isAuth=false,为对外免登录页面。

**页面 17 个 / 子组件 2 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 招聘岗位列表 | 列表/CRUD | ✓ | /platform/recruit/recruitment/index | 按园区(parkSelect)/岗位名称/职层(jcheSelect)/状态(招聘中/招聘结束/招聘暂停)组合搜索、清空、新增招聘岗位(跳转 add 页)、查看岗位详情(跳转 detail 页,带回查询条件)、删除岗位(二次确认弹框 delObj)、刷新/取消刷新岗位(refreshJob,isUp 1/0 切换置顶刷新)、生成岗位投递链接(弹框展示 https://tech.szyuto.com/#/gangwei/{id} 并一键复制,提示可制作二维码扫码登记)、状态彩色圆点标记(recruitStatusClassFormat 过滤器)、分页(size/current change) | 招聘岗位(jobName/parkName/compName/depName/jcheName/recruitNum/status/isUp) | src/views/platform/recruit/recruitment/index.vue |
| 新增招聘岗位 | 表单 |  | /platform/recruit/recruitment/add | 园区/BU/地区/部门/岗位/职层级联选择(park→bu→dept→job,切换上级清空下级)、按职层自动带出福利层次(jcheChange 映射 A~H)、工资类型(具体数字含最低/最高校验/薪资面议)、专业 tag 自由增删、语言要求/电脑要求 单选(中英日法 / 精通熟练一般)、学历要求下拉、工作经验年限、岗位职责 wangEditor 富文本编辑、联系方式文本域、招聘人数/招聘状态/有效期(daterange,带一/两/三月/半年快捷与禁用过去)、表单校验后 addObj 保存、取消返回 | 招聘岗位(parkId/compId/depId/jobId/jcheId/welfareLevel/salaryType/salaryStart/salaryEnd/major/reqLanguage/compRequire/education/workYear/jobCotent/recruitNum/status/startTime/endTime/local) | src/views/platform/recruit/recruitment/add.vue |
| 招聘岗位详情/编辑 | 详情 |  | /platform/recruit/recruitment/detail/:id | getById 回显岗位全部字段、查看态/编辑态切换(编辑按钮→表单解禁,保存 putObj)、园区/BU/部门/岗位/职层级联编辑、福利层次随职层联动、工资类型、专业 tag、语言/电脑/学历/工作经验、岗位职责富文本编辑、招聘人数/状态/有效期编辑、返回上一页 | 招聘岗位(同 add 字段集) | src/views/platform/recruit/recruitment/detail.vue |
| 应聘管理列表 | 列表/CRUD | ✓ | /platform/recruit/applicant/index | 状态分栏切换(已投递0/已邀请2/待复试4/待入职3/已入职5/已拒绝1/已入库6)、按园区/BU/部门级联、姓名、年龄段、手机号、岗位、投递时间段搜索、清空、批量选择(selection)后按当前状态显示对应操作按钮、面试邀请(选时间 putObj status=2)、复试邀请(putObj status=4)、录取/邀请入职(putObj status=3)、入职确认(putObj status=5)、加入人才库(putObj status=6)、拒绝(按阶段给出不同应聘者原因/公司原因勾选+其他原因文本,拼接 refuseReason)、已入职者重新同步到员工库(putObjToStaff)、查看应聘记录(getProcess,竖向步骤条展示状态流转+操作人)、应聘详情跳转、导出 Excel(按状态命名文件)、去批量入职(handleEntry 跳 entry) | 应聘记录application(name/sex/age/phone/jobName/jcheName/applyDate/status)、应聘流程process、园区BU部门树compTree | src/views/platform/recruit/applicant/index.vue |
| 应聘详情 | 详情 |  | /platform/recruit/applicant/detail/:id | getById 拉取应聘岗位信息+个人信息(人脸照/身份证照 base64)+教育/工作经历、待入职/已入职态额外展示紧急联系人、家庭成员、任职关系、证件号/有效期(支持长期)、婚姻状况等过滤器展示、面试邀请/复试/录取/入职/拒绝/加入人才库(同列表的 putObj 流转,带时间选择与拒绝原因弹框)、拒绝原因展示、返回带回列表查询条件 | application、recruitment、applicationWork/Education/Emergency/Relation/FamilyMember | src/views/platform/recruit/applicant/detail.vue |
| 邮件通知模板 | 特殊/公开页 | ✓ | /platform/recruit/emailmodel | Tab 切换(面试通知 msinform / 入职准备通知 rzzbinform / 入职通知 rzinform)、router-view 承载各子模板编辑页、默认重定向到面试通知 | 邮件模板分类tab | src/views/platform/recruit/emailmodel/index.vue |
| 面试通知模板 | 表单 |  | /platform/recruit/emailmodel/model/msinform | getDetail 按 tempCode 回显模板标题/内容、邮件标题输入、邮件内容 wangEditor 富文本编辑、可插入占位标签(姓名/岗位/面试时间/面试地址)、editModel 保存模板 | 消息模板msgTemplate(tempCode/tempName/tempContent) | src/views/platform/recruit/emailmodel/msinform.vue |
| 入职准备通知模板 | 表单 |  | /platform/recruit/emailmodel/model/rzzbinform | getDetail 回显模板,标题+富文本正文编辑、保存模板按钮受 sys_recruit_model_edit 权限控制(仅管理员可编辑)、邮件接收人表格(园区/姓名/手机/邮箱)新增、删除、新增接收人弹框(园区选择+姓名/手机/邮箱,手机邮箱格式校验)、editPerson 保存接收人名单 | 消息模板msgTemplate、邮件接收人(parkName/name/phone/email) | src/views/platform/recruit/emailmodel/rzzbinform.vue |
| 入职通知模板 | 表单 |  | /platform/recruit/emailmodel/model/rzinform | getDetail 按 tempCode 6001 回显模板、邮件标题输入、邮件内容 wangEditor 富文本编辑、editModel 保存模板 | 消息模板msgTemplate(tempCode/tempName/tempContent) | src/views/platform/recruit/emailmodel/rzinform.vue |
| 短信通知模板 | 特殊/公开页 | ✓ | /platform/recruit/msnmodel/index | fetchList 拉取全部短信模板、短信标题下拉选择、选中后只读展示该模板短信内容(消息类型固定为短信) | 短信模板template(tempName/tempContent) | src/views/platform/recruit/msnmodel/index.vue |
| 批量入职-导入Excel | 向导 | ✓ | /platform/recruit/entry/index | 下载《新员工登记信息》Excel 模板、上传 xlsx/xls(XLSX 解析,限单工作表)、逐列校验(姓名/性别/民族/出生日期/证件号/户籍地址/签发机关/证件有效期起止)、性别男女转码、证件号去空格转大写、查重、校验通过存 localStorage(entryFileInfo)并进入第二步 | 新员工导入(name/sex/nation/birth/certno/homeAddress/police/validDateFm/validDate) | src/views/platform/recruit/entry/index.vue |
| 批量入职-入职登记 | 向导 |  | /platform/recruit/entry/step2 | 读取上一步 localStorage 名单渲染 avue-crud 表格、园区/BU/部门/岗位级联选择(统一归属)、逐行删除待入职人员、importStaff 批量提交入职登记(/platform/staff/register) | 待入职名单、组织岗位(parkId/compId/depId/jobId) | src/views/platform/recruit/entry/step2.vue |
| 简历登记-身份证识别 | 向导 |  | /gangwei/:id | 由岗位投递链接 /gangwei/:id 进入、上传身份证正面照、反面照(imgUpload 组件)、OCR 识别身份证信息(ocrRead)回填姓名/性别/民族/生日/证件号/地址/签发机关/有效期、saveIdentification 保存身份信息,下一步进入人脸采集 | 身份信息(cardA/cardB/certno/name/sex/nation/birth/homeAddress/police/validDate) | src/views/platform/resume/index.vue |
| 简历登记-人脸与基础信息 | 向导 |  | /platform/face | 上传人脸正面照并做人脸检测/裁剪(moduleUpload 组件,成功/失败状态标记)、addFace 提交人脸、手机号填写、发送验证码(sendMsg)、绑定手机(bindMobile)、基础信息表单(婚姻状况、邮箱、学历等)校验后进入下一步 | 人脸照ocrPhote、手机绑定(phone/验证码)、应聘者基础信息 | src/views/platform/resume/face.vue |
| 简历登记-详细经历 | 向导 |  | /platform/info | 紧急联系人 增/改/删(单条,addEmergency/editEmergency/delEmergency)、家庭成员 增删(姓名/关系/性别/出生/单位/职务/手机,addFamily)、教育经历 增删(起止/学校/专业/学历学位/是否最高,addEducation)、工作经验 增删(起止/单位/岗位/负责人/联系方式,addWork)、任职关系 增删(按工号查员工 selectStaffInfo、关系类型,addRelation)、下拉数据来源 getRelations/getDegrees/getEducations 字典、deliver 最终投递简历到对应岗位,跳转完成页 | 紧急联系人、家庭成员、教育经历、工作经验、任职关系 | src/views/platform/resume/info.vue |
| 简历登记-完成 | 特殊/公开页 |  | /platform/resumeFinish | 登记完成结果展示(静态提示页) |  | src/views/platform/resume/resume_finish.vue |
| 预约访客 | 表单 |  | /platform/appointment | 来访者信息填写(姓名/手机号/来访离开时间/来访事由/车牌号/所属单位)、手机验证码获取与校验、上传访客人脸正面照、被访者信息填写(姓名/手机号)、确认预约提交 | 访客预约(name/tel/verification/time1/time2/reason/carid/firm/photo/interviewee_name/interviewee_tel) | src/views/platform/appointment/index.vue |

<details><summary>子组件清单(2)</summary>

- `preview_resume.vue` — 应聘列表内嵌的简历预览组件(人脸/身份证照与基础字段展示),当前在 applicant/index 中已注释停用
- `_upload.vue` — 人脸照片上传组件:本地压缩(lrz)+调用人脸检测裁剪算法(/algorithm/out/face/cut),供 face.vue 使用

</details>


## 19-records · 门禁卡/通行记录(ISC)

该模块是智慧园区门禁/通行体系的"下发任务与同步日志"中心,聚合三类数据源:1) 园区设备(闸机/门禁/道闸)对人员、车辆通行权限的下发任务记录及其执行状态;2) 与第三方门禁平台 ISC 之间的 IC 卡同步(卡片同步任务、初始化批量导入、离职/访客权限残留清理);3) 与 EHR/C6 系统的员工数据与照片同步日志及同步频率配置。各页面均为后端动态菜单下发的查询型列表(只读为主,addBtn/editBtn/delBtn 全部关闭),核心业务动作集中在多条件检索、任务状态查看、失败重发、生成删除任务、明细下钻和同步参数设置。所有页面路由约定为 /platform/records/&lt;目录&gt;/index,其中 isc_card_import、isc_access_cleanup 在 router/platform/index.js 显式注册,其余靠后端菜单动态挂载。

**页面 9 个 / 子组件 0 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 门禁人员下发任务记录 | 列表/CRUD | ✓ | /platform/records/person/index | 按姓名/员工号模糊检索(general、badge)、按业务类型筛选(变更员工权限/APP信息完善/访客预约/员工扫码登记/招聘入职/入厂申请/导入员工照片)、按任务状态筛选(待处理/已处理/失败/处理中)、按任务类型筛选(下发/删除/修改/延迟下发/延迟删除/延迟修改)、按设备类型筛选(deviceTypeSelect 组件:闸机/门禁/道闸)、按所在区域级联筛选(areaCascader,取末级 areaId)、按下发时间区间筛选(daterange,拆 startTime/endTime)、清空搜索条件(resetFields)、列表分页(current/size,默认按 create_time 倒序)、deviceType 自定义插槽渲染中文(1闸机/2门禁/3道闸)、失败任务(taskType===2)行内"再次下发"按钮,调 updateTaskStatus 重发;非失败态按钮置灰disabled、展示下发结果 remark(overHidden 溢出隐藏) | 下发任务(deviceTask)、员工(badge/general)、设备(deviceName/deviceType)、区域(areaName/areaId)、业务类型serviceType、任务状态taskType、任务动作action/actionDesc | src/views/platform/records/person/index.vue |
| ISC门禁人员下发任务记录 | 列表/CRUD | ✓ | /platform/records/person_isc/index | 按姓名/员工号模糊检索、按业务类型筛选(同 person:7类)、按状态筛选(ISC专属:初始化/成功/失败/处理中/已取消/权限已过期/设备离线)、按任务类型筛选(下发/删除/修改/延迟下发/延迟删除/延迟修改)、按设备名称模糊检索(deviceName)、按设备类型筛选(deviceTypeSelect)、按所在区域级联筛选(areaCascader)、按下发时间区间筛选,created 时默认填入最近7天(setStartTime)、清空时重置回最近7天默认范围、查询前剔除 downTime/areaIdArray 等非接口字段(JSON深拷贝后 delete)、调 fetchISCList(/platform/device/task/isc/person/page)分页、deviceType 插槽中文渲染、多一列"操作人"(optUser);menu 列关闭、无行内操作 | ISC下发任务、员工(badge/general)、设备(deviceName/deviceType)、区域areaName、业务类型serviceType、ISC任务状态taskType、操作人optUser | src/views/platform/records/person_isc/index.vue |
| 门禁车辆下发任务记录 | 列表/CRUD | ✓ | /platform/records/vehicle/index | 按车牌号(general)/车主姓名(personName)/员工号(badge)模糊检索、按业务类型筛选(员工车辆/公司车辆/非员工车辆/访客预约/物流车预约/入厂申请)、按任务状态筛选(待处理/已处理/失败/处理中)、按下发时间区间筛选、按设备类型筛选(deviceTypeSelect)、按所在区域筛选(el-cascader,created 时 getTree(3) 拉取区域树,取二级节点作为 deviceCode)、清空搜索、列表分页(create_time 倒序)、deviceType 插槽中文渲染;展示卡片号 cardNo、menu 列关闭、无行内操作 | 车辆下发任务、车辆(cardNo卡片号/general车牌/personName车主)、员工badge、设备(deviceName/deviceType)、区域、业务类型serviceType、任务状态taskType | src/views/platform/records/vehicle/index.vue |
| EHR员工同步记录 | 列表/CRUD | ✓ | /platform/records/staff/index | 按工号(badge)/姓名(name)模糊检索、按创建时间区间筛选(datetimerange,默认时间 00:00:00-23:59:59)、清空搜索、列表分页(create_time 倒序),timeRange 数组 join 成字符串以兼容后端、展示工号/姓名/岗位名称jobName/BU(compName)/部门depName/创建时间、行内"查看详情":通过隐藏 viewer 容器把 base64 照片(photo 字段)以图片查看器放大预览,加载失败走 errorImgPeaple 兜底图 | 员工同步记录、员工(badge/name/jobName/compName/depName)、员工照片photo(base64) | src/views/platform/records/staff/index.vue |
| EHR员工同步设置 | 表单 | ✓ | /platform/records/staff_setting/index | 选择需要同步的 BU 集合(buAllSelect 多选组件,compIds 必填)、设置同步频率值 time(校验:必须为正整数 checkTime)、选择频率单位 timeUnit(时/分/日/周/月)、保存配置(onSubmit→addList,带表单校验,成功弹 notify)、取消(saveCancel:重新拉取已存配置并清除校验)、进入时回显当前已配置的 BU、频率、单位(fetchList 回填,compId 转 Number) | EHR同步配置、同步BU(compIds)、同步频率(time/timeUnit) | src/views/platform/records/staff_setting/index.vue |
| C6照片同步记录 | 列表/CRUD | ✓ | /platform/records/toC6/index | 按工号(empNo)/姓名(name)模糊检索、按创建时间区间筛选(datetimerange,默认 00:00:00-23:59:59)、清空搜索、列表分页(create_time 倒序),timeRange join 处理、展示记录流水号 id/工号 empNo/是否已同步过 isDispose(未同步0/已同步1)/创建时间、行内"查看照片":隐藏 viewer 放大预览 base64 照片(photo),失败走 errorImgPeaple 兜底 | C6照片同步记录、员工(empNo)、同步状态isDispose、照片photo(base64) | src/views/platform/records/toC6/index.vue |
| IC卡片同步任务 | 列表/CRUD | ✓ | /platform/records/isc_card_task/index | 按园区筛选(parkSelect 可清空)、按工号(badge)/姓名(name)/卡ID(cardNo)模糊检索、按任务动作筛选(1新增卡片/2删除卡片)、按创建时间区间筛选(daterange,拆 startTime/endTime,查询前 delete timeRange)、清空搜索(重置为空表单)、列表分页、行数据规范化:actionText 把 action 数值映射成"新增卡片/删除卡片/未知动作",remark 缺省补"-"、展示园区/工号/姓名/卡ID/任务动作/执行结果remark/创建时间/更新时间/操作人optUser | IC卡同步任务、园区parkName、员工(badge/name)、卡片cardNo、任务动作action、执行结果remark、操作人optUser | src/views/platform/records/isc_card_task/index.vue |
| IC卡片初始化导入记录 | 列表/CRUD | ✓ | /platform/records/isc_card_import/index | 批次区:按园区(parkSelect)/模式(预检DRY_RUN、初始化导入IMPORT)/状态(初始化/执行中/完成/失败)/创建时间区间检索、批次区清空、分页;批次行"明细"按钮点击下钻填充 batchId 并加载下方明细、批次行规范化展示:模式modeDesc、人员范围staffScopeDesc(全部/在职/离职)、状态statusDesc,以及总数/成功/跳过/冲突/失败计数、耗时consume(ms)、起止时间、操作人、备注、明细区:按工号/姓名/ISC卡号/结果(可导入/已导入/已清理/已一致/冲突/本地多出/ISC无卡/ISC无人员/无效卡/失败)检索、明细区独立"搜索明细"与独立分页、明细行规范化:resultText 把结果码映射中文,展示工号/姓名/ISC人员ID/ISC卡号/本地卡号/结果/原因reason/创建时间、支持从路由 query.parkId、query.batchId 进入并自动加载对应批次明细 | 导入批次(batchId)、导入明细(detail)、园区parkId/parkName、导入模式mode、人员范围staffScope、批次状态status、结果resultCode、卡号(iscCardNo/localCardNo)、ISC人员personId | src/views/platform/records/isc_card_import/index.vue |
| ISC权限残留清理 | 列表/CRUD | ✓ | /platform/records/isc_access_cleanup/index | 按园区(parkSelect)/人员类型(访客VISITOR/离职人员STAFF)/处理状态(待处理EXECUTABLE/保留PROTECTED)检索、按关键词(姓名/证件/工号/人员ID)、设备编码、结束时间区间检索;默认处理状态为 EXECUTABLE、清空搜索(重置默认表单)、顶部汇总卡片实时统计:总数/待处理/保留/访客/离职人员/未生成删除任务/待重试(fetchSummary)、列表多选(selection),展示园区/人员类型/姓名/证件工号/本地人员ID/ISC人员ID/设备/设备编码/权限来源/起止时间/处理状态/删除任务状态/原因、行内"生成任务":对单条 EXECUTABLE 记录生成删除任务(executeRow),非可执行态按钮禁用、顶部"生成删除任务":勾选行则按 downRecordIds 批量生成,未勾选则按当前筛选条件整体生成(executeSelected→executeCleanup)、执行后弹出已创建/已刷新/跳过数量提示并刷新列表与汇总 | 权限残留记录(downRecordId)、园区parkName、人员(personName/badge/personType)、本地人员ID cardNo、ISC人员personId、设备(deviceName/deviceCode)、权限来源serviceType、处理状态cleanupStatus、删除任务状态deleteTaskStatus、汇总summary | src/views/platform/records/isc_access_cleanup/index.vue |


## 20-misc · 杂项(信息发布/出入口/园区服务/停车/报警/物流车/返厂/物品放行/首页/水电看板)

这是智慧园区中后台里跨域的杂项功能集合,覆盖10个业务子域:信息发布(终端多媒体下发/WebSocket实时播放)、出入口记录(人/车/物品出入抓拍与导出)、园区服务(满意度调查表+员工反馈处理)、停车场管理(车位实时统计/停车场CRUD/进出记录)、安防报警(短信推送配置+报警记录)、物流车预约通行、返厂确认、OA物品放行查询、登录首页(在线人数)、水电数据看板(DataEase iframe)。各菜单主入口多为 index.vue 列表页,通过 _service/api 拉取分页数据,点击行进入 detail/add/edit 子页;部分页面带 Excel 导出、级联区域筛选、园区/BU/部门级联和审批状态流转。

**页面 32 个 / 子组件 0 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 信息资源管理(列表) | 列表/CRUD | ✓ | /platform/info_delivery/info_mng/index | 按资源名称/发布类型/发布状态/创建时间区间搜索、清空搜索条件、跳转新增信息资源页、跳转编辑资源页(带id)、删除资源(二次确认弹窗)、发布类型下拉远程取值(getSelectType)、发布状态(已发布/未发布)筛选、分页查询、列表展示资源名称/发布类型/发布状态/发布终端/创建时间/创建人 | 信息资源infoName、发布类型type/typeDesc、发布状态status、发布终端terminalName、创建人creator | src/views/platform/info_delivery/info_mng/index.vue |
| 新增信息资源 | 表单 |  | /platform/info_delivery/info_mng/add | 填写资源名称、Tab切换资源类型(视频/图片/PDF/文本/网页)、上传mp4视频(限50M,格式校验)、上传jpg/png图片(限500kb)并设置播放顺序、增删多图、上传PDF文件(限20M)、文本内容录入+左右/上下轮播方向+背景色/字体色取色器、网页URL录入(建议1366*768)、图片转base64上传(importImgs)、视频/PDF走FormData文件上传接口、表单校验后保存、返回列表 | 资源名称infoName、资源类型activeName、图片imageReqDTOS(imageId/url/sort)、文本content/textMoveType/textStyle、文件content(fileUrl) | src/views/platform/info_delivery/info_mng/add.vue |
| 编辑信息资源 | 表单 |  | /platform/info_delivery/info_mng/edit/:id | 回显并修改资源名称、Tab切换五种资源类型、重新上传视频/图片/PDF、图片多图增删与播放顺序调整、文本内容/轮播方向/颜色编辑、网页URL编辑、保存更新、返回列表 | 信息资源id、资源名称、资源类型、媒体内容 | src/views/platform/info_delivery/info_mng/edit.vue |
| 信息发布终端实时展示(WebSocket播放页) | 特殊/公开页 |  | /video | 按路由ip参数建立WebSocket连接(/websocket/:ip)、视频自动循环播放、图片卡片式轮播(el-carousel)、PDF逐页浏览(上一页/下一页/键盘左右键翻页)、文本无缝滚动(左右/上下方向,自定义背景/字体色)、网页iframe嵌入展示、按推送type动态切换展示形态 | 终端ip、推送type(0视频/1图片/2PDF/3文本/4网页)、fileUrl、images、文本textStyle/textMoveType | src/views/platform/info_delivery/data_show/video.vue |
| PDF流预览测试页 | 特殊/公开页 |  | /pdf | 按页码跳转浏览、顺/逆时针旋转90度、调用打印、加载进度条展示、总页数显示、PDF文件流地址加载(news/file/stream) | PDF流地址src、页码page、旋转角度rotate | src/views/platform/info_delivery/data_show/pdf.vue |
| 发布终端管理 | 列表/CRUD | ✓ | /platform/info_delivery/info_mng/index | 按终端名称搜索、弹窗新增/编辑终端(名称/IP/备注)、绑定发布资源(可搜索下拉infoSelect)、实时发布/定时发布切换(定时需选发布时间)、删除终端(二次确认)、生成并复制终端播放链接(/#/video?ip=,clipboard复制)、分页查询、列表展示终端名称/发布类型/资源名称/IP/备注/创建时间/创建人 | 终端name、IP地址ip、绑定资源infoId/infoName、发布时效timeType、发布时间startTime、备注remark | src/views/platform/info_delivery/device_mng/index.vue |
| 车辆出入记录(列表) | 列表/CRUD | ✓ | /platform/entrance/vehicle/index | 内部车辆/外部车辆Tab切换(不同列配置)、按车主/车牌号/出入类型(进门/出门)/出入时间搜索、地点级联筛选(area tree)、内部车辆按园区/BU/部门级联筛选、外部车辆按手机号搜索、清空搜索、导出Excel(内/外部分别表头,含放行方式/权限翻译)、查看车辆出入详情(带返回参数)、分页查询 | 车主driverName、车牌号vehiclePlate、出入地点areaName、出入类型eventType、出入时间snapTime、园区/BU/部门、vehicleAscription(2内部/3外部) | src/views/platform/entrance/vehicle/index.vue |
| 车辆出入详情 | 详情 |  | /platform/entrance/vehicle/detail/:id | 展示车牌号/车品牌/车颜色、展示出入园区/地点/类型/时间、展示车主信息(内部含工号/BU/部门;外部含联系方式)、返回列表(回带检索条件) | 车辆信息carInfo、出入信息、车主driverInfo、driverType(1内部/外部) | src/views/platform/entrance/vehicle/detail.vue |
| 人员出入记录(列表) | 列表/CRUD | ✓ | /platform/entrance/face/index | 内部人员/外部人员Tab切换、按姓名/工号/出入类型搜索、地点级联筛选、内部按园区/BU/部门级联筛选、设备名称远程搜索下拉(remoteSearchDevice)、体温正常/异常筛选、出入时间区间搜索、导出Excel、查看人员出入详情、分页查询 | 姓名personName、工号badge、出入类型eventType、设备deviceId、体温isNormal、出入时间snapTime、园区/BU/部门 | src/views/platform/entrance/face/index.vue |
| 人员出入详情 | 详情 |  | /platform/entrance/face/detail/:id | 展示抓拍人像(viewer可放大)、展示人员信息(内部含工号/BU/部门/岗位;外部含身份信息)、展示出入园区/地点/类型/时间/体温、返回列表 | 人员照片photo、人员信息personInfo、personType(1内部)、出入信息 | src/views/platform/entrance/face/detail.vue |
| 物品放行记录(出入口-列表) | 列表/CRUD | ✓ | /platform/entrance/article/index | 按园区/BU/部门级联+申请人+车牌号搜索、按状态(待审批/通过/拒绝/已出厂)筛选、按申请时间区间搜索、查看放行详情、对通过状态记录执行'确认出厂'(updateStatus)、清空搜索、分页查询 | 申请人name、车牌号licensePlate、放行状态status、申请时间、园区/BU/部门 | src/views/platform/entrance/article/index.vue |
| 物品放行详情(出入口) | 详情 |  | /platform/entrance/article/detail/:id | 展示所属园区/BU/部门/工号/申请人、展示携带人/车牌号/物品类型、宿舍物品类型(articlesType=3)显示房间信息、返回列表 | 申请信息articleInfo、物品类型articlesType、携带人carrier、车牌licensePlate | src/views/platform/entrance/article/detail.vue |
| 满意度调查表管理(列表) | 列表/CRUD | ✓ | /platform/park_service/paper/index | 按所属园区/调查表名称/状态(未开始/进行中/已结束)搜索、新增调查表、查看(编辑)调查表、删除调查表(确认)、进入调查结果统计页、状态过滤器渲染、分页查询 | 调查表title、所属园区parkId、状态status(0未开始/1进行中/2已结束) | src/views/platform/park_service/paper/index.vue |
| 新增/编辑调查表 | 表单 |  | /platform/park_service/paper/add | 选择所属园区(联动加载BU)、填写调查表名称、设置有效期时间区间、多选发布范围(BU)、动态增删问题(题目+题型)、按题型配置选项、表单校验、编辑模式园区禁用(isEdit)、保存提交 | 调查表title/parkId/timeRange/compIds、问题questions(title/type/selects) | src/views/platform/park_service/paper/add.vue |
| 调查结果统计 | 详情 |  | /platform/park_service/paper/detail/:id | 展示调查表名称与已提交人数、按问题渲染ECharts统计图(逐题选项占比)、导出Excel报表(exportApi+js-file-download) | 统计结果statisticsData、问题questions、选项selects(answer/num)、提交人数totalCount | src/views/platform/park_service/paper/detail.vue |
| 员工反馈(列表) | 列表/CRUD | ✓ | /platform/park_service/feed_back/index | 按反馈问题搜索、按处理状态(未处理/已处理)筛选、按反馈时间区间搜索、查看反馈详情、清空搜索、分页查询 | 反馈问题question、处理状态status、反馈时间timeRange | src/views/platform/park_service/feed_back/index.vue |
| 员工反馈详情(处理) | 详情 |  | /platform/park_service/feed_back/detail/:id | 展示反馈人/手机号/BU/部门/员工号/反馈时间/问题标签、填写处理回复(限100字)、已处理回显处理人/状态/处理时间、保存处理结果(putObj)、返回列表 | 反馈信息editForm、处理回复reply、处理状态status、处理人operator | src/views/platform/park_service/feed_back/detail.vue |
| 停车场当前车辆(列表) | 列表/CRUD | ✓ | /platform/parking/present_parking/index | 剩余/已用/总车位环形进度统计、切换停车场下拉(depLots)、车位校对(handelCheck)、按园区/车牌号/车主搜索、查看在场车辆详情、清空搜索、分页查询 | 车位统计curDepotInfo(free/use/total)、停车场parkingId、车牌号vehiclePlate、车主driverName | src/views/platform/parking/present_parking/index.vue |
| 当前车辆详情 | 详情 |  | /platform/parking/present_parking/detail/:id | 展示车牌号/车品牌/车颜色、展示车主姓名/电话/所属园区、返回列表 | 车辆信息vehicleInfo、车主信息 | src/views/platform/parking/present_parking/detail.vue |
| 停车场管理(列表) | 列表/CRUD | ✓ | /platform/parking/parking_lot/index | 按所属园区搜索、弹窗新增停车场(园区/名称/总车位)、弹窗编辑停车场(总车位编辑模式禁用)、删除停车场(rowDel)、车位数正整数校验、清空搜索 | 停车场name、所属园区parkId、总车位totalCount | src/views/platform/parking/parking_lot/index.vue |
| 停车记录(列表) | 列表/CRUD | ✓ | /platform/parking/parking_record/index | 按所属园区/车牌号/车主搜索、按出入类型(进/出)筛选、按出入时间区间搜索、查看停车记录详情、清空搜索、分页查询 | 车牌号vehiclePlate、车主driverName、出入类型eventType、出入时间、所属园区parkId | src/views/platform/parking/parking_record/index.vue |
| 警报设置 | 表单 | ✓ | /platform/alarm/set | 推送方式(发送短信,默认勾选)、选择推送模板、动态增加推送人(信息+手机号)、动态删除推送人、保存报警设置 | 推送方式type、推送模板template、推送人person(info/tel) | src/views/platform/alarm/set.vue |
| 警报记录(列表) | 列表/CRUD | ✓ | /platform/alarm/record/index | 按地点级联搜索(area tree)、按时间区间搜索、卡片墙展示报警抓拍图/名称/时间/地点、图片加载失败占位、无数据提示、分页(prev/pager/next/jumper/sizes) | 报警抓拍snapId、报警名称alarmName、报警时间alarmTime、报警地点areaName | src/views/platform/alarm/record/index.vue |
| 警报记录详情 | 详情 |  | /platform/alarm/record/detail/:id | 展示报警人像、展示姓名/身份/报警名称/报警时间/报警地点 | 报警信息alarmInfo(vs_name/alarm_name/alarm_time/alarm_site)、抓拍图img | src/views/platform/alarm/record/detail.vue |
| 物流车预约通行记录(列表) | 列表/CRUD | ✓ | /platform/logistics_vehicle/reserve_record/index | 预约状态单选切换(已预约/已到达/已离开)、按所属园区/供应商/车牌号/司机姓名搜索、查看通行详情、清空搜索、分页查询、(注释保留手动进厂/离厂/返回预约/取消预约等扩展动作) | 供应商supplier、车牌号vehiclePlate、司机driverName、预约状态status、所属园区parkId | src/views/platform/logistics_vehicle/reserve_record/index.vue |
| 物流车通行详情 | 详情 |  | /platform/logistics_vehicle/reserve_record/detail/:id | 展示车牌号、展示出入园区/地点/类型/时间、返回(由列表查询返回) | 车辆信息carInfo、出入信息 | src/views/platform/logistics_vehicle/reserve_record/detail.vue |
| 返厂确认记录(列表) | 列表/CRUD | ✓ | /platform/in_out/return_factory/index | 按OA单号/携带人姓名搜索、查看返厂详情、对未确认记录执行'确认返厂'(apply)、清空搜索、分页查询 | OA单号processId、携带人name、返厂状态backStatus | src/views/platform/in_out/return_factory/index.vue |
| 返厂详情 | 详情 |  | /platform/in_out/return_factory/detail | 展示申请时间/确认状态、展示申请人/部门/放行去处/是否返厂、展示出发地点/到达地点/放行人级别/放行事项、加载中提示、返回 | 申请主表applyMain(sqr/sqbm/fxqc/sffc/fxdd/dddd/fxsx)、确认状态statusName | src/views/platform/in_out/return_factory/detail.vue |
| 物品放行查询(业务管理-列表) | 列表/CRUD | ✓ | /platform/business_manage/article/index | 按申请人/工号搜索、按创建时间区间搜索、查看放行详情、重置搜索、分页查询 | 申请人name、工号badge、创建时间startTime/endTime | src/views/platform/business_manage/article/index.vue |
| 物品放行详情(业务管理) | 详情 |  | /platform/business_manage/article/detail | 展示申请时间/确认状态、展示申请人/部门/放行去处/是否返厂、展示出发/到达地点/放行人级别/放行事项、加载中提示、返回 | 申请主表applyMain、确认状态statusName | src/views/platform/business_manage/article/detail.vue |
| 平台首页 | 看板/大屏 | ✓ | /platform/home/index | 展示欢迎语(当前登录用户名)、实时时钟(每秒刷新)、拉取并展示当前在线人数(loggedCount,不足三位补零)、首页背景图 | 登录用户userInfo、在线人数onLine、时间time | src/views/platform/home/index.vue |
| 水电数据看板 | 看板/大屏 | ✓ | /platform/equipment/hydropower/index | iframe嵌入DataEase看板(window.origin+/link/qxlu57rV)、全屏自适应展示水电统计报表 | DataEase面板panelUrl | src/views/platform/equipment/hydropower/index.vue |




## 21-global · 全局组件与错误页(补充)

覆盖校验补回的 10 个文件,位于 `src/components/` 全局组件库与根组件,不属于 `views/` 业务页面分组,故工作流首轮未纳入。其中 4 个是路由可达页面(错误页/iframe 容器),6 个是全站复用基础组件。Next.js 重写时:错误页对应 `app/error.tsx`、`not-found.tsx` 等;基础组件对应共享 UI 组件库。

**页面 4 个 / 子组件 6 个**

| 页面 | 类型 | 菜单入口 | 路由 | 功能要点 | 数据实体 | 源文件 |
|---|---|---|---|---|---|---|
| 403 无权限页 | 特殊/公开页 |  | /403 | 展示 403 无权限插画与提示「You don't have permission」、返回首页、返回上一页 | — | src/components/error-page/403.vue |
| 404 页面不存在 | 特殊/公开页 |  | /404 | 通配路由 `*` 兜底重定向至此、展示 404 插画「YOU LOOK LOST」、返回首页、返回上一页 | — | src/components/error-page/404.vue |
| 500 服务器错误 | 特殊/公开页 |  | /500 | 展示 500 服务异常插画「the server is wrong」、返回首页、返回上一页 | — | src/components/error-page/500.vue |
| iframe 外链容器页 | 特殊/公开页 |  | /myiframe/:routerPath | 用 basic-container 包裹 iframe,按 query.src 或 routerPath 加载外部 URL、监听路由变化重载、自适应高度、NProgress 进度条 | 外链地址 src/urlPath | src/components/iframe/main.vue |

<details><summary>子组件清单(6)</summary>

- `App.vue` — 应用根组件,仅挂载 `<router-view/>`,全局壳
- `basic-container/main.vue` — Avue 基础卡片容器,el-card 包裹内容,支持 block 撑满高度,全站列表/表单页统一外壳
- `empty/empty.vue` — 表格/列表「暂无数据」空状态占位组件(内置 SVG 插画)
- `tce-img/index.vue` — 图片上传组件,集成 lrz 前端压缩,限 png/jpeg,单图替换式上传,自定义 http-request
- `tce-label-justify/index.vue` — 文本两端对齐展示组件(按字符撑开 + 填充占位),用于表单 label 对齐
- `tce-search-bar/index.vue` — 可折叠查询条件容器,「展开/收起查询条件」切换,包裹搜索表单 slot

</details>

---

## 说明与重写提示

- **数据来源**:多 agent 逐文件逆向,交叉比对 `src/const/crud/**`(avue 列/表单/按钮配置)、`src/api/**`(后端接口)、`src/router/platform/index.js` 中文注释。
- **覆盖校验(可复现)**:工作流首轮覆盖 443 个文件;以 `find src -name "*.vue"` 实测全量 453 个,用 `comm` 双向比对——揪出 10 个漏读(均为 `src/components/` 全局组件 + `App.vue`,见第 21 章)并补全;反向比对 `comm -13` 命中 0 行(零虚构)。**443 + 10 = 453,最终 453/453 全覆盖**。
- **routePath 是推断辅助字段,不是铁证**:业务页 `/platform/<目录>/index` 前缀与静态子路由(`router/platform/index.js`)一致、可信度高;但真实路径最终由后端动态菜单 `path` 字段决定。`admin`/`gen` 系统模块前缀为 `/admin`、`/gen`(全代码库 `grep` 零处 `platform/admin`,工作流误加的 `/platform` 前缀已据此修正)。**唯一确凿锚点是「源文件」列**——重写时以文件路径核对,routePath 仅供参考。
- **「菜单入口✓」**:后端动态菜单可直达的主入口页;无标记者为详情/新增/编辑/弹窗等二级页面或公开页(扫码、打印、大屏、登记)。
- **页面 vs 子组件**:子组件(`*-select`/`*-cascader`/`components/`/`import/` 等)被页面内嵌复用,单独折叠列出,重写时归入共享组件库而非路由页面。
- **重写关键决策点**:旧系统重度依赖 Avue `avue-crud`(单份 option 配置生成整张增删改查表)与后端动态菜单。React/Next.js 无等价物,需先自建「schema 驱动的表格/表单」基建 + 菜单/权限驱动的路由,否则 284 个页面会退化为 284 份手写 CRUD。这是原型设计阶段必须先定调的架构基石。
