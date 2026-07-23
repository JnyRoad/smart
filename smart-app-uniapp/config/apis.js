// 设备注册
export const API_DEVICE_REGISTER = '/app/device/register'

// 引导
export const API_GUIDE_WELECOME = '/app/guide/welcome'
export const API_GUIDE_OPERATION = '/app/guide/operation'
export const API_PREFECT = '/app/perfect/check/ocr'
// 登录
export const API_LOGIN = '/auth/oauth/token' // 账号密码登录 
export const API_REFRESH_TOKEN = '/auth/oauth/token' // 刷新token
export const API_LOGIN_FACE = '/auth/ocr/token/face' // 人脸识别登录
export const API_LOGIN_OUT = '/auth/token/logout' // 退出登录
export const API_EMPLOYEE_GETSALAYTYPE =  '/app/employee/getSalayType' //查询员工薪资计算类型
// 密码找回
export const API_PASSWORD_MOBILE_QUERY = '/app/password/mobile/query' // 通过工号查手机号码
export const API_SMS_SEND = '/app/password/sms/send' // 发送短信验证码
export const API_SMS_VERIFY = '/app/password/verify' // 效验短信验证码
export const API_PASSWORD_VERIFY_FACE = '/app/password/verify/face' // 人脸识别效验修改密码
export const API_PASSWORD_UPDATE = '/app/password/update' // 设置密码

// 完善信息
export const API_PERFECT_CHECK_FACE = '/app/perfect/check/face' // 信息完整信息检测
export const API_OCRR_IDENTIFICATION = '/app/perfect/identification' // 身份证OCR识别
export const API_OCR_FACE = '/app/perfect/face' // 认证对比 暂时废弃
export const API_FACE_UPDATE = '/app/employee/face/update' // 更新头像

// 定位
export const API_LOCATION_AUTO = '/app/park/location/auto' // 经纬度定位
export const API_PARK_LIST = '/app/park/list' // 获取园区列表

// 首页
export const API_HOME_BANNER = '/app/home/banner' // 获取banner列表
export const API_HOME_MENU = '/app/home/menu' //  导航栏菜单列表
export const API_HOME_BBS_LIST = '/app/home/bbs/list' // 获取公告列表
export const API_HOME_BBS_DETAIL = '/app/home/bbs/detail' // 查看公告详情
export const API_HOME_NEWS_LIST = '/app/home/news/list' // 获取新闻列表
export const API_HOME_NEWS_DETAIL = '/app/home/news/detail' // 获取新闻详情
export const API_HOME_PARK_GENERRAL_LIST = '/app/home/park/general/list' // 园区概况列表
export const API_HOME_PARK_GENERRAL_DETAIL = '/app/home/park/general/detail' // 园区概况详情
export const API_HOME_ACTIVITY_LIST = '/app/home/park/activity/list' // 园区活动列表
export const API_HOME_ACTIVITY_DETAIL = '/app/home/park/activity/detail' // 园区活动详情
export const API_HOME_INSTRODUCE_LIST = '/app/home/park/instroduce/list' // 裕同集团简介列表
export const API_HOME_INSTRODUCE_DETAIL = '/app/home/park/instroduce/detail' // 裕同简介详情
export const API_HOME_CULTURE_LIST = '/app/home/park/culture/list' // 裕同文化列表
export const API_HOME_CULTURE_DETAIL = '/app/home/park/culture/detail' // 裕同文化详情 

// 

// 职位管理
export const API_RECRUIT_LIST = '/job/recruit/list' // 获取招聘岗位列表
export const API_RECRUIT_DETAIL = '/job/recruit/detail'// 查看岗位招聘信息详情
export const API_RECRUIT_UPDATE = '/job/recruit/update' // 修改岗位招聘信息

// 招聘管理
export const API_APPLICATION_LIST = '/app/job/application/list' // 获取简历列表
export const API_APPLICATION_DETAIL = '/app/job/application/detail' // 查看简历详情
export const API_APPLICATION_OPERATION = '/app/job/application/operation' // 简历筛选操作
export const API_APPLICATION_RECORD = '/app/job/application/record' // 查询应聘记录
export const API_APPLICATION_FACE_LIST = '/app/job/application/face/list' // 人脸搜索简历
export const API_APPLICATION_OTPTTYPE_LIST = '/app/job/application/otptype/list' // 招聘状态列表



// 新员工须知
export const API_INFORMATION_FILE_LIST = '/app/employee/new/note/list' // 获取资料列表
export const API_INFORMATION_FILE_DETAIL = '/app/employee/new/note/detail' //资料详情

// 审批公用接口
export const API_PROCESS_PREVIEW = '/app/process/preview' // 预览审批流程
export const API_APPROVE_RECORD = '/app/approve/record' // 待审批离职请求列表
export const API_APPLICATION_APPROVE_NORMAL = '/app/application/approve/normal' // 普通审批操作




//服务
//5.7号新增 获取模块菜单信息
export const API_APPSERVICE_MODULE_LIST = '/app/service/module/list'

// 推送消息
export const API_PLUS_NEWS_LIST = '/app/message/push/list'
export const API_PLUS_NEWS_CHANGE = '/app/message/update/read'
export const API_PLUS_NEW_COUNT = '/app/message/count/app'
export const API_PLUS_ALL_READ = '/app/message/update/all/read'
export const API_PLUS_ALL_DETELE = '/app/message/delete/all'
// 离职
export const API_APPLICATION_DIMISSION = '/app/application/dimission' // 发起离职申请
export const API_APPLICATION_DIMISSION_REASON = '/app/application/dimission/reason' // 获取离职原因列表
export const API_APPLICATION_DIMISSION_TYPE = '/app/application/dimission/type' // 获取离职类型列表
export const API_PROCESS_DIMISSION_GET = '/app/process/dimission/get' // 获取离职流程信息
export const API_PROCESS_DIMISSION_RECORD_LIST = '/app/process/dimission/record/list' // 获取员工离职记录
export const API_PROCESS_DIMISSION_RECORD_DETAIL = '/app/process/dimission/record/detail' // 查看员工离职记录详情
export const API_PROCESS_DIMISSION_RECORD_WORKHAND = '/app/process/dimission/record/workhand' // 查看工作交接
export const API_APPLICATION_APPROVE_WORKHAND = '/app/application/approve/workHand/get'
export const API_APPLICATION_APPROVE_DORM = '/app/application/approve/dorm' // 宿管离职审批
export const API_APPLICATION_APPROVE_INSURANCE = '/app/application/approve/insurance' // 社保员离职审批
export const API_APPLICATION_APPROVE_ATTENDANCE = '/app/application/approve/attendance' // 考勤员离职审批
export const API_APPLICATION_APPROVE_START = '/app/application/approve/start' // 开始交接工作
export const API_APPLICATION_APPROVE_COMMIT = '/app/application/approve/commit' // 提交工作交接
export const API_APPLICATION_APPROVE_WORKHAND_SUBMIT = '/app/application/approve/workHand/submit' // 提交工作交接
export const API_APPREST_BALANCE_ANNUAL_GET = '/app/rest/balance/annual/get' // 查询年假天数

// 请假
export const API_APPLICATION_VACATE = '/app/application/vacate' //  发起请假申请
export const API_APPLICATION_VACATE_TYPE = '/app/application/vacate/type' // 获取休假类型列表
export const API_REST_GET = '/app/rest/get' // 获取可调休的天数
export const API_PROCESS_VACATE_RECORD_LIST = '/app/process/vacate/record/list' // 获取请假记录
export const API_PROCESS_VACATE_RECORD_DETAIL = '/app/process/vacate/record/detail' // 查看请假记录详情
export const API_APPLICATION_CLASS_QUERY = '/app/application/classes/query' // 查看班次信息
export const API_APPLICATION_VACATE_UNIT = '/app/application/vacate/unit' // 获取请假时长单位

// 调休
export const API_APPLICATION_REST = '/app/application/rest' // 发起调休申请
export const API_APPLICATION_REST_TYPE = '/app/application/rest/type' // 获取休假类型列表
export const API_REST_BALANCE_ADJUST_GET = '/app/rest/balance/adjust/get' // 获取可调休天数
export const API_PROCESS_REST_RECORD_LIST = '/app/process/rest/record/list' // 获取调休记录
export const API_PROCESS_REST_RECORD_DETAIL = '/app/process/rest/record/detail' // 查看调休记录详情

// 加班
export const API_APPLICATION_EXTRAWORK = '/app/application/extrawork' // 发起加班申请
export const API_APPLICATION_EXTRAWORK_CLASS_TYPE = '/app/application/extrawork/class/type' // 加班类型
export const API_PROCESS_EXTRAWORK_RECORD_LIST = '/app/process/extrawork/record/list' // 获取加班记录
export const API_PROCESS_EXTRAWORK_RECORD_DETAIL = '/app/process/extrawork/record/detail' // 查看加班记录详情
export const API_APPLICATION_EXTRAWORK_TYPE = '/app/application/extrawork/type' // 获取加班类型列表

// 出差
export const API_APPLICATION_TRAVEL = '/app/application/travel' // 发起出差申请
export const API_PROCESS_TRAVEL_RECORD_LIST = '/app/travel/process/record/list' // 获取出差记录
export const API_PROCESS_TRAVEL_RECORD_DETAIL = '/app/travel/process/record/detail' // 查看加班记录详情
export const API_PROCESS_TRAVEL_RECORD_INFODAY = '/app/travel/process/record/infoDay' // 查看日程
export const API_PROCESS_TRAVEL_RECORD_INFOREPORT = '/app/travel/process/record/infoReport' // 查看出差报告详情
export const API_PROCESS_TRAVEL_RECORD_INFOFLOW = '/app/travel/process/record/infoFlow' // 查看流程
// 考勤
export const API_APPLICATION_ATTENDANCE_LIST = '/app/application/attendance/list' // 获取考勤列表
export const API_APPLICATION_ATTENDANCE_DETAIL = '/app/application/attendance/detail' // 查看考勤详情
export const API_APPLICATION_ATTENDANCEERROR_DETAIL = '/app/application/attendanceError/detail' // 异常考勤
export const API_APPLICATION_ATTENDANCESUCCESS_DETAIL = '/app/application/attendanceSuccess/detail'
export const API_APPLICATION_ATTENDANCE_PATCH_QUERY = '/app/application/attendance/patch/query' // 获取补卡详情
export const API_APPLICATION_ATTENDANCE_PATCH_REASON = '/app/application/attendance/patch/reason' // 获取补卡原因列表
export const API_APPLICATION_ATTENDANCE_PATCH = '/app/application/attendance/patch' // 发起补卡申请
export const API_PROCESS_ATTENDANCE_PATCH_RECORD_LIST = '/app/process/attendance/patch/record/list' // 获取补卡记录
export const API_APPLICATION_ATTENDANCE_PATCH_PATCHCOUNT = '/app/application/attendance/patch/patchCount' // 当月补卡次数
export const API_APPLICATION_ATTENDANCE_RECORD_DETAIL = '/app/process/attendance/patch/record/detail' // 获取补卡详情
export const API_APPLICATION_ATTENDANCE_GETSKYPAY = '/app/application/attendance/getSkyPay' // 获取考勤汇总
export const API_APPLICATION_ATTENDANCE_MONTH_LIST = '/app/application/attendance/month/list' //当月考情明细
// 薪资查询
export const API_SALARY_LIST = '/app/salary/list' // 获取工资条列表
export const API_SALARY_DETAIL = '/app/salary/detail' // 查看工资条详情
export const API_WAGE_SIGN_SAVE = '/app/wage/sign/save' // 工资签收

// 访客预约
export const API_VISIT_LIST = '/app/visit/list' // 获取来访预约列表
export const API_VISIT_REASON_LIST = '/app/visit/reason/type' // 事由列表
export const API_VISIT_DETAIL = '/app/visit/detail' // 查看来访预约详情
export const API_VISIT_ADD = '/app/visit/add' // 添加访客预约
export const API_VISIT_MEMBER_ADD = '/app/visit/member/add' // 添加随行人员
export const API_VISIT_MEMBER_LIST = '/app/visit/member/list' // 查看随行人员列表
export const API_VISIT_APPROVE = '/app/visit/approve' // 预约审批
export const APT_VISTI_APPROVALCOUNT = '/app/visit/approve/getToApprovalCount' // 带我审批的数量

// 我的模块
// 员工
export const API_EMPLOYEE_BASEINFO = 'app/employee/baseinfo' // 获取员工
export const API_EMPLOYEE_FULLINFO = '/app/employee/fullinfo' // 获取员工完整信息
export const API_EMPLOYEE_RELATION_UPDATE = '/app/employee/relation/update' // 紧急联系人修改
export const API_EMPLOYEE_ROOM_DETAIL = '/app/employee/room/detail' // 员工宿舍详情
export const API_EMPLOYEE_QRCODE = '/app/employee/qrcode' // 获取员工个人二维码
export const API_EMPLOYEE_ROOM_APPLY = '/app/employee/room/apply' // 员工申请内宿
export const API_EMPLOYEE_ROOM_OUT = '/app/agreement/room/out' // 外宿协议
export const API_EMPLOYEE_ALLOWANCE = '/app/employee/allowance/get' // 补贴信息
export const API_EMPLOYEE_OUT_ROMM_APPLY = '/app/employee/out/room/apply' // 申请外宿
export const API_EMPLOYEE_OUT_ROOM_DETAIL = 'app/employee/out/room/detail' //
export const API_EMPLOYEE_OUT_ROMM_APPLY_DETAIL = '/app/employee/out/room/apply/detail'
// 车辆
export const API_EMPLOYEE_VEHICLE_BASEINFO = '/app/employee/vehicle/baseinfo' // 获取车辆信息
export const API_EMPLOYEE_VEHICLE_AUTH_DETAIL = '/app/employee/vehicle/auth/detail' // 查看车辆通行权限详情
export const API_EMPLOYEE_VEHICLE_AUTH_PARK = '/app/employee/vehicle/auth/park' // 获取车辆通行权限列表
export const API_EMPLOYEE_VEHICLE_AUTH_APPLY = '/app/employee/vehicle/auth/apply' // 申请车辆通行权限
export const API_EMPLOYEE_VEHICLE_ADD = '/app/employee/vehicle/add' // 添加车辆
export const API_EMPLOYEE_VEHICLE_COLOR_TYPE = '/app/employee/vehicle/color/type' // 获取车辆颜色类型列表
export const API_EMPLOYEE_VEHICLE_TYPE = '/app/employee/vehicle/type' // 获取车辆类型列表
export const API_EMPLOYEE_VEHICLE_DELETE = '/app/employee/vehicle/delete' // 移出我的车辆
// 帮助中心
export const API_GUIDE_HELP_QUESTION_LIST = '/app/guide/help/question/list' // 获取常见的问题列表
export const API_GUIDE_HELP_QUESTION_ANSWER = '/app/guide/help/question/answer' // 查看常见问题解答

// 设置
export const API_SETTING_SUGGEST = '/app/setting/suggest' // 意见反馈
export const API_SETTING_ABOUTUS = '/app/setting/aboutus' // 关于我们
export const API_SETTING_VERSION_CHECK = '/app/setting/version/check' // 版本检查
export const API_SETTING_VERIFY_OLDPHONE = '/app/setting/updatephone/verify/oldmobile/smscode' // 验证原手机号码
export const API_SETTING_NEWPHONE_SENDSMS = '/app/setting/updatephone/send/smscode' // 获取新手机号码的验证码
export const API_SETTING_UPDATAPHONE = '/app/setting/updatephone/update' // 更换手机号码
// 删除消息
export const API_MESSAGE_DELETE = '/app/message/delete' //删除消息
// 消费记录 
export const API_CONSUME_RECORD_COUNT = '/app/consume/record/count' //消费记录总金额 
export const API_CONSUME_RECORD_LIST = '/app/consume/record/list' //消费记录列表
export const APP_AGREEMENT_SERVICE = '/app/agreement/service' //app协议
