package com.tce.smart.dispatcher.api.enums;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ISCApiErrorCodeClassifier {

	private static final String UNKNOWN_DESCRIPTION = "ISC返回未知错误";
	private static final Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> ERRORS_BY_SECTION;
	private static final Map<String, ErrorDefinition> GENERIC_ERRORS;
	private static final int OFFICIAL_DEFINITION_COUNT;
	private static final String OFFICIAL_ERROR_CODE_RESOURCE = "isc-error-codes/isecure-center-v1.7.0.tsv";

	static {
		Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> errorsBySection = new EnumMap<>(ISCApiErrorCodeSection.class);
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_MANAGEMENT, "0x00000000", "操作成功");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_MANAGEMENT, "0x02f19089", "用户不存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_MANAGEMENT, "0x02f19094", "请求参数含有非法字符");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0", "操作成功");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15400001", "服务错误：服务异常");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15400002", "参数错误：必填字段不能为空");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15400003", "参数错误：字段合法性校验不通过");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15404001", "资源异常：资源不存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15401001", "计划模板错误：计划模板ID不存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15401002", "计划模板错误：假日组ID不存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15401003", "计划模板错误：计划模板名称重复");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15401004", "计划模板错误：假日组名称重复");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405001", "任务操作错误：任务不存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405002", "任务操作错误：任务已开始下载");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405003", "任务操作错误：删除任务失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405004", "任务操作错误：停止任务失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405005", "任务操作错误：暂停任务失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15405006", "任务操作错误：添加待下载数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403007", "下载错误：无可用数据下载");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403008", "下载错误：权限报文下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403009", "下载错误：设备接入服务不在线或连接失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300a", "下载错误：设备不在线或网络不通");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300b", "下载错误：设备正在下载");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300c", "下载错误：查询设备的驱动地址失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300d", "下载错误：不支持卡片权限");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300e", "下载错误：不支持指纹权限");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540300f", "下载错误：不支持人脸权限");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403010", "下载错误：设备容量已满");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403011", "下载错误：人员没有开卡");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403012", "下载错误：人员没有指纹");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403013", "下载错误：人员没有人脸图片");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403014", "下载错误：获取人脸图片地址失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403501", "回调错误：驱动结果响应超时");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403502", "回调错误：下载记录处理异常");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403503", "回调错误：计划模板下载失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403504", "回调错误：清空权限失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403505", "回调错误：设备下载超时");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403506", "回调错误：权限数据非法");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403507", "回调错误：卡号错误");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403508", "回调错误：指纹/人脸已存在");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403509", "回调错误：指纹/人脸质量差");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350a", "回调错误：设备未绑定图片服务器");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350b", "回调错误：下载人脸图片失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350c", "回调错误：人脸建模失败");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350d", "回调错误：人脸眼间距太小");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350e", "回调错误：未下发卡权限");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x1540350f", "回调错误：未知原因,请检查数据后重试");
		register(errorsBySection, ISCApiErrorCodeSection.ACCESS_CONTROL_PERMISSION, "0x15403519", "回调错误：人脸图片不符合要求");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x60500101", "未知错误");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf10003", "license 为空");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf10004", "license过期");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf19090", "参数为空异常");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf19092", "参数范围异常");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf19094", "请求参数含有非法字符");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf19058", "查询登录用户失败");
		register(errorsBySection, ISCApiErrorCodeSection.TEMPERATURE, "0x8bf19091", "没有资源权限");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072001", "参数错误：必填参数为空");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072002", "参数错误：参数范围不正确");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072003", "参数错误：参数格式不正确");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072004", "参数错误：未指定分页大小或者分页过大导致返回报文过长");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00052101", "服务错误：服务性能已达上限");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00052102", "服务错误：服务异常");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00052103", "服务错误：服务响应超时");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00052104", "服务错误：服务不可用");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072201", "资源异常：资源访问未授权");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072202", "资源异常：资源不存在");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072203", "资源异常：License数量受限");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00072204", "资源异常：License未提供该功能");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x00052301", "其他错误：其他未知错误");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00002", "该用户不存在");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00003", "区域查询条件sonOrgIndexCodes 不合法");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00004", "资源查询条件containSubRegion、exactCondition 不合法");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00005", "资源类型resourceType、权限码authCodes错误；");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00015", "当前入参非系统唯一标识");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00016", "Token校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00017", "SecuSID已过期");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00018", "密钥交换失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00019", "加密数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c00020", "解密数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c02301", "批量操作时部分成功、部分失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c02302", "管理相关权限校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c02303", "资源不存在");
		register(errorsBySection, ISCApiErrorCodeSection.RESOURCE_DIRECTORY, "0x14c02304", "请求超时");
		register(errorsBySection, ISCApiErrorCodeSection.EVENT_SUBSCRIPTION, "0x00072001", "The required parameter $$ is blank.");
		register(errorsBySection, ISCApiErrorCodeSection.EVENT_SUBSCRIPTION, "0x00072002", "The value of Parameter $$ is out of range.");
		register(errorsBySection, ISCApiErrorCodeSection.EVENT_SUBSCRIPTION, "0x00072003", "The format of Parameter $$ is not correct.");
		register(errorsBySection, ISCApiErrorCodeSection.EVENT_SUBSCRIPTION, "0x02c0112d", "token check fail");
		register(errorsBySection, ISCApiErrorCodeSection.EVENT_SUBSCRIPTION, "0x02c0112e", "get reg param fail");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f003", "表结构不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511014", "告警信息不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f002", "配置项不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511013", "队列参数超过限制长度");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f001", "文件或文件夹不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511015", "白名单过滤");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f007", "数据库表结构初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511018", "参数解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f006", "数据库错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511017", "开始时间大于结束时间");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f005", "数据库连接失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350f004", "索引不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511019", "数据查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511010", "状态属性status参数非法（0-离线，1-在线，-1-未检测）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511012", "没有告警信息%s");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511011", "参数不合法");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100c", "分页信息pageSize格式错误（正整数）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100b", "分页信息pageNo格式错误（正整数）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100e", "查询参数includeSubNode格式错误（1-包含，0-不包含）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100d", "区域编码regionId不能为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100f", "排序参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351100a", "用户ID不存在(未定义)");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511003", "没有找到此indexCode的记录");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511002", "资源还原失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03505020", "CMDB事件报文错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511005", "资源编码indexCode为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511004", "接口验证失败，token为空或者错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511007", "资源类型不存在（资源类型未定义）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511006", "时间参数格式错误，解析失败(时间参数格式：yyyy-MM-dd)");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511009", "用户id不能为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511008", "资源模型属性不存在（未定义）");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511001", "主键还原失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00b", "远程请求错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00a", "创建请求失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03505019", "告警操作错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00f", "组件配置文件初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03505018", "告警接受错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00e", "Token认证错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00d", "数据库初始化错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e00c", "请求其他组件错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512005", "资源下发失败错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512006", "汉字编码失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512003", "业务日志初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512004", "手动同步错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350501e", "启用的告警规则为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350501d", "告警状态缓存修改失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350501f", "告警导出错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512001", "客户端请求错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03512002", "组件应用容器关闭错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350501c", "告警状态缓存初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03505022", "告警数据处理错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351102a", "告警状态参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03505021", "解析指标报文错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511025", "统计指标值参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511024", "统计指标空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511027", "告警时间为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511026", "资源属性指标不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511029", "该资源类型接口不支持");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511028", "告警指标为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350a002", "注册平台KPI信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350a001", "注册平台指标信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350a004", "保活失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350a003", "录入平台上报数据失败，数据格式不合法");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350a005", "注销平台信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511021", "触发门限参数异常");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511020", "资源类型参数为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511023", "告警id为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03511022", "恢复门限参数异常");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101d", "告警等级参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101c", "告警等级参数为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101f", "告警类型参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101e", "告警类型参数为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506005", "资源编辑失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506004", "资源列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506006", "资源删除失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506001", "资源类型查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506003", "资源详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101b", "网域ID为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506002", "资源管理分页查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351101a", "请求参数为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506021", "资源全量下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03518005", "定时处理tb_subscriber_alarm_info表里过期记录失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03518006", "SecuDK 握手验证失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03518007", "SQL 语句执行错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03518001", "握手失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501006", "查询监控点历史状态失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501005", "监控点列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501004", "监控点资源详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501003", "监控点分辨率情况及总数失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501009", "监控点资源详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506019", "导入历史详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501008", "编码设备在线情况及总数失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501007", "编码设备列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506016", "资源导入模板下载失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506018", "导入历史查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506017", "资源导入失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501002", "监控点在线情况及总数失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501001", "监控点列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350601e", "资源导入，保存资源失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350601b", "资源绑定失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350601a", "确认导入资源");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350601c", "资源解绑失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501017", "视频质量统计情况查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501016", "视频质量统计列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501015", "录像检查列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501014", "资源历史录像情况查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501019", "视频质量统计详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501018", "视频质量统计指标情况查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506026", "资源目录操作失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501013", "资源录像详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506023", "资源增量下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501012", "录像检查结果列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506022", "资源全量下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501011", "编码设备列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506025", "创建巡检计划失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501010", "编码设备历史状态查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03506024", "生成CSV文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03507010", "业务概览大数据搜图错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03507011", "业务概览卡口错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501028", "门禁设备列表查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501027", "门禁设备类型查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501026", "门禁设备在线状态查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff00", "参数校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501025", "组织区域树查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff01", "获取区域失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501029", "门禁设备详情查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501020", "视频质量统计历史诊断详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff06", "查询设备历史状态失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff07", "分页查询设备在线状态失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff08", "查询用户信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501024", "视频预览失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff02", "获取组件地址失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501023", "一键图像重巡失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff03", "查询设备在线率失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501022", "图像重巡监控点列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff04", "获取门禁设备类型失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501021", "视频质量统计列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff05", "导出设备在线状态失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350102f", "解码设备历史查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350102e", "解码设备详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350700e", "业务概览云分析统计错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350700f", "业务概览设备能力集统计错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350700c", "运维概况列表详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350102d", "解码设备列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03513002", "数据转换失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350700d", "运维概况小时归并失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03513003", "定时任务停止失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350102b", "门禁设备历史记录查询");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350102a", "门禁设备导出");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03513001", "数据设置失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502008", "区域运维统计统计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501039", "清理历史数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502009", "区域运维统计图表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501038", "解析Double数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501037", "巡检入库失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501036", "存储设备导出");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502004", "异常点位详情列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502005", "录像情况统计统计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502006", "录像情况统计图表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502007", "录像情况统计统计导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501031", "解码设备在导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502001", "视频质量统计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501030", "解码设备在线率查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502002", "异常点位详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502003", "视频质量统计统计导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501035", "存储设备在线率查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501034", "存储设备历史");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03507012", "业务概览趋势图错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501033", "存储设备详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501032", "存储设备列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514008", "测试连通性失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03508001", "获取一键运维详情失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514007", "实时巡检失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514006", "生成全量数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514005", "删除下级平台失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103f", "录像巡检删除巡检结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514009", "NCG批量保存失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103a", "巡检数据每日归并失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514004", "更新下级平台失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103e", "录像巡检保存巡检结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514003", "创建下级平台失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103d", "录像巡检保存录像计划失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514002", "下级平台详情获取失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103c", "接口内部错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514001", "下级平台列表查询获取失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350103b", "参数校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff20", "组件http通信失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502019", "监控点实时统计详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501049", "录像保存天数巡检保存归并结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350ff21", "查询权限区域列表失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501048", "录像保存天数巡检保存巡检结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501047", "录像巡检更新资源属性失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502015", "取流情况统计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502016", "取流情况统图表计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502017", "取流情况统图表计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502018", "监控点实时统计列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502011", "区域运维统计权重值修改失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501042", "录像巡检消息为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502012", "区域运维统计导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501041", "录像巡检配置信息为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502013", "区域运维统计详情导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501040", "录像巡检消息格式转换失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501046", "录像巡检消息实体转换失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501045", "拷贝录像计划失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501044", "录像巡检查询录像计划失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03502010", "区域运维统计权重值查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501043", "录像巡检保存巡检归并结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350201e", "监控点离线时长统计导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501060", "统计报表云分析单元列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350201a", "监控点实时统计图表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350104a", "录像保存天数巡检消息实体转换失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350201b", "监控点实时统计列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350201c", "监控点实时统计详情导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350201d", "监控点离线时长统计查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350104c", "VQD结果矫正失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501059", "云存储磁盘汇总信息查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501058", "云存储汇总信息查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501053", "云存储节点详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501052", "云存储详情查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501051", "云存储节点列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501050", "云存储列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501057", "云存储列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03508004", "导出点播状态结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501056", "云存储监控点列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501055", "云存储存储卷列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03508002", "导出一键运维结果失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03501054", "云存储磁盘列表查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03508003", "一键运维巡检失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d001", "保存缓存信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d004", "删除缓存信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d005", "REDIS初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d002", "获取缓存信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d003", "批量获取缓存信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105c", "大数据集群列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514022", "检验是否已经存在失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d008", "数据库执行失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105b", "云存储存储卷汇总信息查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514021", "录像CSV解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d009", "热点过滤器初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105a", "云存储监控点汇总信息查询失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514020", "录像CSV解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d006", "事件发布失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d007", "模型获取失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105f", "云分析单元列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105e", "云分析集群列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514024", "录像保存天数CSV解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350105d", "大数据单元列表导出失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514023", "录像保存天数CSV生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401f", "socket创建失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503005", "获取区域数量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503006", "获取区域资源失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503007", "获取用户数量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503008", "获取用户资源失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503001", "获取监控点数量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401a", "文件不存在");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503002", "获取监控点资源失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503003", "获取编码设备数量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503004", "获取编码设备资源失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401e", "HASH生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401d", "文件生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401c", "VQD增量生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351401b", "文件重命名失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514019", "NCG获取远程文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509002", "添加拓扑树失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514018", "NCG读取增量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509001", "查询拓扑树失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514017", "NCG全量失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514016", "录像CSV生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350300e", "获取同步任务失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350300f", "同步数据失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514011", "CSV解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350300b", "初始化License信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514010", "VQD批量入库失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350300c", "增量消息解析异常");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350300d", "更新License异常");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514015", "录像CSV生成失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514014", "IO错误失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514013", "ping命令失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03514012", "socket关闭失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400f", "VQD级联解析失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400e", "获取NCG服务失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503012", "视频监控响应失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400d", "获取服务节点失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400c", "ncg状态入库错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503010", "加解密失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400b", "IO流关闭失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03503011", "同步任务调度异常");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351400a", "游标文件设置失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510002", "mq初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510001", "秘钥初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515019", "生成消防设备资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510004", "mq消息入库失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515018", "生成可视对讲资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510003", "mq密码解码失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509010", "拓扑保存失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515017", "生成可视对讲资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510006", "巡检结果接收mq的目的错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510005", "巡检结果接收mq消息为空");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510008", "秘钥交换握手失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510007", "巡检结果线程池停止失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515012", "生成大数据单元资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515011", "生成大数据单元资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515010", "生成大数据集群资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515016", "生成梯控读卡器资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515015", "生成梯控读卡器资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515014", "生成梯控主机资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515013", "生成梯控主机资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00a", "全量缓存初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500f", "生成大数据集群资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00d", "快照配置失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00e", "版本配置初始化失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00b", "模型校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00c", "CSV文件读写错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500a", "生成存储设备资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03510009", "查询录像配置失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509009", "查询拓扑图详情失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d00f", "版本读写失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509008", "添加拓扑节点失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509007", "添加网元失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509006", "查询未关联的拓扑节点");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500e", "生成云分析单元资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509005", "删除拓扑树失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500d", "生成云分析单元资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509004", "更新拓扑树失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500c", "生成云分析资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03509003", "拓扑图参数错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351500b", "生成云分析资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515009", "生成存储设备资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515008", "生成区域资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515007", "生成区域资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350d010", "采集数据校验失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515006", "生成编码设备资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515001", "生成监控点资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900f", "查询拓扑节点详情失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515005", "生成编码设备资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900e", "删除拓扑关系失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515004", "生成解码设备资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900d", "更新拓扑关系失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515003", "生成解码设备资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900c", "添加拓扑关系失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515002", "生成监控点资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900b", "更新拓扑节点失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350900a", "删除拓扑节点失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03504003", "巡检资源增量下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03504002", "巡检资源同步解锁失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03504001", "巡检资源下发失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e013", "数据库连接失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e012", "本地加密错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e011", "获取配置文件参数失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e010", "获取组件信息失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350400b", "巡检结果ntp入库失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350400a", "消息队列推送状态失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e002", "加载多语言错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e001", "导出excel文件错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515028", "生成云存储节点资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e006", "握手错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e005", "配置文件加载错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e004", "文件解压错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e003", "类加载错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515023", "生成录像计划全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e009", "远程地址不可达");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515022", "生成门禁读卡器资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e008", "脚本执行错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515021", "生成门禁读卡器资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0350e007", "CloaderDHI初始化错误");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515020", "生成门禁点资源资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515027", "生成云存储节点资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515026", "生成云存储资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515025", "生成云存储资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x03515024", "生成录像计划增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351000a", "恢复默认的录像配置信息出错");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501b", "生成消防传感器资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501a", "生成消防设备资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501f", "生成门禁点资源资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501e", "生成门禁设备资源增量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501d", "生成门禁设备资源全量文件失败");
		register(errorsBySection, ISCApiErrorCodeSection.NETWORK_MANAGEMENT, "0x0351501c", "生成消防传感器资源增量文件失败");
		loadOfficialErrorCodeResource(errorsBySection);

		Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> immutableBySection = new EnumMap<>(ISCApiErrorCodeSection.class);
		Map<String, ErrorDefinition> genericErrors = new LinkedHashMap<>();
		int definitionCount = 0;
		for (Map.Entry<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> entry : errorsBySection.entrySet()) {
			Map<String, ErrorDefinition> sectionErrors = Collections.unmodifiableMap(entry.getValue());
			immutableBySection.put(entry.getKey(), sectionErrors);
			definitionCount += sectionErrors.size();
			for (ErrorDefinition definition : sectionErrors.values()) {
				genericErrors.putIfAbsent(definition.getErrorCode(), definition);
			}
		}
		ERRORS_BY_SECTION = Collections.unmodifiableMap(immutableBySection);
		GENERIC_ERRORS = Collections.unmodifiableMap(genericErrors);
		OFFICIAL_DEFINITION_COUNT = definitionCount;
	}

	private ISCApiErrorCodeClassifier() {
	}

	public static String describeForUser(EventEnum eventEnum, String errorCode) {
		return find(eventEnum, errorCode)
				.map(ErrorDefinition::getDescription)
				.orElse(UNKNOWN_DESCRIPTION);
	}

	public static Optional<ErrorDefinition> find(EventEnum eventEnum, String errorCode) {
		String normalizedErrorCode = normalizeErrorCode(errorCode);
		if (normalizedErrorCode.length() == 0) {
			return Optional.empty();
		}
		ISCApiErrorCodeSection section = ISCApiErrorCodeSection.fromEvent(eventEnum);
		if (section != null) {
			ErrorDefinition sectionDefinition = findBySection(section, normalizedErrorCode);
			if (sectionDefinition != null) {
				return Optional.of(sectionDefinition);
			}
		}
		return Optional.ofNullable(GENERIC_ERRORS.get(normalizedErrorCode));
	}

	public static int officialDefinitionCount() {
		return OFFICIAL_DEFINITION_COUNT;
	}

	private static ErrorDefinition findBySection(ISCApiErrorCodeSection section, String normalizedErrorCode) {
		Map<String, ErrorDefinition> sectionErrors = ERRORS_BY_SECTION.get(section);
		return sectionErrors == null ? null : sectionErrors.get(normalizedErrorCode);
	}

	private static void register(Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> errorsBySection,
			ISCApiErrorCodeSection section, String errorCode, String description) {
		register(errorsBySection, section, section.getDescription(), errorCode, description);
	}

	private static void register(Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> errorsBySection,
			ISCApiErrorCodeSection section, String sectionDescription, String errorCode, String description) {
		Map<String, ErrorDefinition> sectionErrors = errorsBySection.computeIfAbsent(section, key -> new LinkedHashMap<>());
		String normalizedErrorCode = normalizeErrorCode(errorCode);
		sectionErrors.put(normalizedErrorCode, new ErrorDefinition(sectionDescription, normalizedErrorCode, description));
	}

	private static void loadOfficialErrorCodeResource(Map<ISCApiErrorCodeSection, Map<String, ErrorDefinition>> errorsBySection) {
		InputStream inputStream = ISCApiErrorCodeClassifier.class.getClassLoader()
				.getResourceAsStream(OFFICIAL_ERROR_CODE_RESOURCE);
		if (inputStream == null) {
			throw new IllegalStateException("ISC官方错误码资源不存在：" + OFFICIAL_ERROR_CODE_RESOURCE);
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.trim().length() == 0 || line.startsWith("#")) {
					continue;
				}
				String[] parts = line.split("\t", -1);
				if (parts.length < 4) {
					throw new IllegalStateException("ISC官方错误码资源格式错误，line=" + lineNumber);
				}
				ISCApiErrorCodeSection section = ISCApiErrorCodeSection.valueOf(parts[0]);
				register(errorsBySection, section, parts[1], parts[2], parts[3]);
			}
		} catch (IOException e) {
			throw new IllegalStateException("加载ISC官方错误码资源失败", e);
		}
	}

	private static String normalizeErrorCode(String errorCode) {
		if (errorCode == null) {
			return "";
		}
		String normalizedErrorCode = errorCode.trim().toLowerCase(Locale.ROOT);
		if (normalizedErrorCode.length() == 0 || !normalizedErrorCode.startsWith("0x")) {
			return normalizedErrorCode;
		}
		try {
			String hexValue = Long.toHexString(Long.parseLong(normalizedErrorCode.substring(2), 16));
			while (hexValue.length() < 8) {
				hexValue = "0" + hexValue;
			}
			return "0x" + hexValue;
		} catch (NumberFormatException e) {
			return normalizedErrorCode;
		}
	}

	public static final class ErrorDefinition {
		private final String section;
		private final String errorCode;
		private final String description;

		private ErrorDefinition(String section, String errorCode, String description) {
			this.section = section;
			this.errorCode = errorCode;
			this.description = description;
		}

		public String getSection() {
			return section;
		}

		public String getErrorCode() {
			return errorCode;
		}

		public String getDescription() {
			return description;
		}
	}

	private enum ISCApiErrorCodeSection {
		PUBLIC("公共返回码"),
		RESOURCE_DIRECTORY("资源目录"),
		ACCESS_CONTROL_MANAGEMENT("门禁管理"),
		ACCESS_CONTROL_PERMISSION("出入控制权限"),
		CARD("卡片"),
		EVENT_SUBSCRIPTION("事件订阅"),
		TEMPERATURE("测温应用"),
		NETWORK_MANAGEMENT("网管"),
		OFFICIAL("官方返回码");

		private final String description;

		ISCApiErrorCodeSection(String description) {
			this.description = description;
		}

		private String getDescription() {
			return description;
		}

		private static ISCApiErrorCodeSection fromEvent(EventEnum eventEnum) {
			if (eventEnum == null) {
				return null;
			}
			switch (eventEnum) {
				case ISC_PERSON_ADD:
				case ISC_PERSON_BATCH_ADD:
				case ISC_PERSON_UPDATE:
				case ISC_PERSON_BATCH_DEL:
				case ISC_PERSON_GET:
				case ISC_FACE_ADD:
				case ISC_FACE_UPDATE:
				case ISC_FACE_DEL:
				case ISC_DEVICE_GET:
				case ISC_DEVICE_DETAIL_GET:
				case ISC_ACCESS_DEVICE_GET:
				case ISC_CARD_LIST_GET:
					return RESOURCE_DIRECTORY;
				case ISC_CARD_ADD:
				case ISC_CARD_DELETE:
					return CARD;
				case ISC_FACE_IMAGE_GET:
					return ACCESS_CONTROL_MANAGEMENT;
				case ISC_TASK_SIMPLE_DOWN:
				case ISC_CREATE_TASK:
				case ISC_ADD_DATA_TO_TASK:
				case ISC_TASK_START:
				case ISC_TASK_PROCESS_GET:
				case ISC_TASK_RECORD_GET:
				case ISC_TASK_RECORD_DETAIL_GET:
				case ISC_AUTH_CONFIG_ADD:
				case ISC_AUTH_CONFIG_DEL:
				case ISC_AUTH_CONFIG_DOWN:
				case ISC_AUTH_CONFIG_PROCESS_GET:
				case ISC_AUTH_ITEM_LIST_GET:
					return ACCESS_CONTROL_PERMISSION;
				case ISC_EVENT_SUBSCRIBE:
				case ISC_EVENT_UNSUBSCRIBE:
					return EVENT_SUBSCRIPTION;
				case ISC_TEMPERATURE_GET:
				case ISC_TEMPERATURE_GROUP_GET:
				case ISC_TEMPERATURE_POINT_GET:
				case ISC_TEMPERATURE_REGISTER_GET:
					return TEMPERATURE;
				case ISC_ACCESS_DEVICE_STATUS_GET:
					return NETWORK_MANAGEMENT;
				default:
					return null;
			}
		}
	}
}
