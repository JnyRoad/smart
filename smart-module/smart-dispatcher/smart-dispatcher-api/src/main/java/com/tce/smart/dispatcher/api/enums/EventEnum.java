package com.tce.smart.dispatcher.api.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-platform
 * @ClassName: DeleteEnum
 * @Author jinbo
 * @Date 2019/6/21
 */
@Getter
@AllArgsConstructor
public enum EventEnum {
	/**
	 * 服务网关调用枚举类型
	 */
	DEVICE_ADD(1, "/devicemanager/v1/device/add", "添加设备", OperationEnum.POST.getCode()),

	DEVICE_DELETE(2, "/devicemanager/v1/device/delete", "删除设备", OperationEnum.POST.getCode()),

	DEVICE_UPDATE(3, "/devicemanager/v1/device/update", "修改设备", OperationEnum.POST.getCode()),

	DEVICE_CHECK(4, "/devicemanager/v1/device/check", "设备在线检测", OperationEnum.POST.getCode()),

	DEVICE_CONTROL(8, "/devicemanager/v1/acs/door/control", "闸机/门禁门手动控制", OperationEnum.POST.getCode()),

	DEVICE_STATUS_LIST_SEARCH(11, "/devicemanager/v1/device/status/query", "查询设备状态列表", OperationEnum.POST.getCode()),

	PARKING_ADD(12, "/devicemanager/v1/parking/add", "添加停车场", OperationEnum.POST.getCode()),

	PARKING_DELETE(13, "/devicemanager/v1/parking/delete", "删除停车场", OperationEnum.POST.getCode()),

	PARKING_SPACE_SEARCH(14, "/devicemanager/v1/parking/parkingspace/query", "停车场查询车位信息", OperationEnum.POST.getCode()),

	PARKING_SPACE_UPDATE(15, "/devicemanager/v1/parking/parkingspace/update", "停车场更新车位信息", OperationEnum.POST.getCode()),

	PARKING_ENTRANCE_DEVICE_BIND(16, "/devicemanager/v1/parking/device/bind", "停车场绑定出入口设备", OperationEnum.POST.getCode()),

	PARKING_ENTRANCE_DEVICE_UNBIND(17, "/devicemanager/v1/parking/device/unbind", "停车场解绑出入口设备", OperationEnum.POST.getCode()),

	PARKING_ENTRANCE_AUTH_ADD(18, "/devicemanager/v1/parking/vehicle/access/add", "停车场添加车辆出入权限", OperationEnum.POST.getCode()),

	PARKING_ENTRANCE_AUTH_DELETE(19, "/devicemanager/v1/parking/vehicle/access/delete", "删除车辆出入权限", OperationEnum.POST.getCode()),

	PARKING_ENTRANCE_DEVICE_CONTROL(21, "/devicemanager/v1/parking/barrier/door/control", "道闸门手动控制", OperationEnum.POST.getCode()),

	PARKING_LED_SET(22, "/devicemanager/v1/veec/led/displayparam/set", "停车场车辆出入口LED屏设置", OperationEnum.POST.getCode()),

	PARKING_LED_GET(23, "/devicemanager/v1/veec/led/displayparam/get", "获取停车场车辆出入口LED屏显示信息", OperationEnum.POST.getCode()),

	FACE_FEATURE_EXTRACT(28, "/facerecognition/v1/face/feature/extract", "提取人脸特征值", OperationEnum.POST.getCode()),

	FACE_IMAGE_COMPARE(29, "/facerecognition/v1/face/image/compare", "人脸图片比对", OperationEnum.POST.getCode()),

	FACE_IMAGE_CUT(30, "/facerecognition/v1/face/image/cut", "人脸图片裁剪", OperationEnum.POST.getCode()),

	FACE_IMAGE_THERMAL(31, "/devicemanager/v1/acs/faceparam/set", "人脸验证", OperationEnum.POST.getCode()),

	METER_CHECK_ONLINE(32, "/bridge/checkOnline", "检测水电表(外置阀门)集中器在线状态", OperationEnum.POST.getCode()),

	VALVE_CHECK_ONLINE(33, "/bridge/checkOnlineValve", "检测外置阀门集中器在线状态", OperationEnum.POST.getCode()),

	WATER_METER_READ(34, "/bridge/watermeter/read", "智能水表读数", OperationEnum.POST.getCode()),

	WATER_METER_IN_VALVE_CONTROL(35, "/bridge/watermeter/in_valve/control", "智能水表内置阀门控制", OperationEnum.POST.getCode()),

	WATER_METER_OUT_VALVE_CONTROL(36, "/bridge/watermeter/out_valve/control", "智能水表外置阀门控制", OperationEnum.POST.getCode()),

	ELE_METER_READ(37, "/bridge/elemeter/read", "智能电表读数", OperationEnum.POST.getCode()),

	ELE_METER_DOWNLOAD_FILE(38, "/bridge/ele/issue/file", "电表集中器下载档案", OperationEnum.POST.getCode()),

	ELE_METER_QUERY_FILE(39, "/bridge/ele/query/file", "电表集中器查询档案", OperationEnum.POST.getCode()),

	WATER_METER_DOWNLOAD_FILE(40, "/bridge/water/issue/file", "水表集中器下载档案", OperationEnum.POST.getCode()),

	WATER_METER_QUERY_FILE(41, "/bridge/water/query/file", "水表集中器查询档案", OperationEnum.POST.getCode()),

	ELE_METER_DEL_FILE(42, "/bridge/ele/del/file", "电表集中器删除档案", OperationEnum.POST.getCode()),

	WATER_METER_DEL_FILE(43, "/bridge/water/del/file", "水表集中器删除档案", OperationEnum.POST.getCode()),

	ELE_METER_BRAKE_CONTROL(44, "/bridge/ele/brake/control", "智能电表闸门控制", OperationEnum.POST.getCode()),

	ELE_METER_BRAKE_READ(45, "/bridge/ele/meter/brake/read", "智能电表闸门读取", OperationEnum.POST.getCode()),

	WATER_METER_OUT_VALVE_REMOTE_CONTROL(46, "/bridge/watermeter/out_valve_remote/control", "智能水表外置阀门远程功能控制", OperationEnum.POST.getCode()),

	/////// Kafka Start //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	DEVICE_ADD_CARD(5, "add_acs_card", "添加闸机/门禁卡片", OperationEnum.KAFKA.getCode()), //module

	DEVICE_DELETE_CARD(6, "del_acs_card", "删除闸机/门禁卡片", OperationEnum.KAFKA.getCode()), //module

	DEVICE_UPDATE_CARD(7, "update_acs_card", "更新闸机/门禁卡片", OperationEnum.KAFKA.getCode()), //module

	PERSON_ACCESS_RECORD(9, "acs_person_record_nty", "人员通行记录通知", OperationEnum.KAFKA.getCode()), //module

	DEVICE_STATUS_CHANGE(10, "device_status_change_nty", "设备状态变更", OperationEnum.KAFKA.getCode()), //module

	PARKING_ENTRANCE_RECORD(20, "brg_vehicle_record_nty", "车辆出入记录通知", OperationEnum.KAFKA.getCode()), //module

	CROSS_BORDER_RECORD(24, "cbc_cross_border_nty", "越界通知", OperationEnum.KAFKA.getCode()), //module

	CAMERA_FACE_SNAP_RECORD(25, "face_snap_data_nty", "摄像头人脸抓拍通知", OperationEnum.KAFKA.getCode()), //module

	PIDC_ID_INFO_NTY(26, "pidc_id_info_nty", "人证比对机身份证信息通知", OperationEnum.KAFKA.getCode()),

	FACE_THERMOMETRY_NTY(27, "face_thermometry_nty", "人脸测温通知", OperationEnum.KAFKA.getCode()),

	WATER_REPEATER_READ(90, "water_repeater_read_nty", "水表读数上报", OperationEnum.KAFKA.getCode()),

	WATER_REPEATER_IN_VALVE_STATE(91, "water_repeater_inValveState_nty", "水表内置阀门状态上报", OperationEnum.KAFKA.getCode()),

	WATER_REPEATER_OUT_VALVE_STATE(92, "water_repeater_outValveState_nty", "水表外置阀门状态上报", OperationEnum.KAFKA.getCode()),

	ELE_REPEATER_READ(93, "electric_repeater_read_nty", "电表读数上报", OperationEnum.KAFKA.getCode()),

	ELE_REPEATER_BRAKE_STATE(94, "electric_repeater_brakeState_nty", "电表闸门状态上报", OperationEnum.KAFKA.getCode()),

	/////// Kafka End //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// >>>>>>>>>>>>> ISC Start >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	ISC_PERSON_ADD(50, "/api/resource/v2/person/single/add", "添加人员v2", OperationEnum.POST.getCode()),
	ISC_PERSON_BATCH_ADD(51, "/api/resource/v1/person/batch/add", "批量添加人员", OperationEnum.POST.getCode()),
	ISC_PERSON_UPDATE(52, "/api/resource/v1/person/single/update", "修改人员", OperationEnum.POST.getCode()),
	ISC_PERSON_BATCH_DEL(53, "/api/resource/v1/person/batch/delete", "批量删除人员", OperationEnum.POST.getCode()),
	ISC_PERSON_GET(54, "/api/resource/v1/person/condition/personInfo", "根据人员唯一字段获取人员详细信息", OperationEnum.POST.getCode()),

	ISC_FACE_ADD(55, "/api/resource/v1/face/single/add", "添加人脸", OperationEnum.POST.getCode()),
	ISC_FACE_UPDATE(56, "/api/resource/v1/face/single/update", "修改人脸", OperationEnum.POST.getCode()),
	ISC_FACE_DEL(57, "/api/resource/v1/face/single/delete", "删除人脸", OperationEnum.POST.getCode()),
	ISC_FACE_IMAGE_GET(58, "/api/acs/v1/event/pictures", "获取门禁事件的图片", OperationEnum.POST.getCode()),

	ISC_DEVICE_GET(60, "/api/irds/v2/deviceResource/resources", "获取资源列表v2", OperationEnum.POST.getCode()),
	ISC_DEVICE_DETAIL_GET(61, "/api/resource/v1/resource/indexCodes/search", "根据编号查询资源详细信息", OperationEnum.POST.getCode()),
	ISC_ACCESS_DEVICE_STATUS_GET(62, "/api/nms/v1/online/acs_device/get", "获取门禁设备在线状态", OperationEnum.POST.getCode()),
	ISC_ACCESS_DEVICE_GET(63, "/api/resource/v2/acsDevice/search", "查询门禁设备列表v2", OperationEnum.POST.getCode()),

	ISC_TASK_SIMPLE_DOWN(65, "/api/acps/v1/authDownload/task/simpleDownload", "简单同步权限下载_根据人员与设备通道指定下载", OperationEnum.POST.getCode()),
	ISC_CREATE_TASK(66, "/api/acps/v1/authDownload/task/addition", "创建下载任务_根据人员与设备通道指定下载", OperationEnum.POST.getCode()),
	ISC_ADD_DATA_TO_TASK(67, "/api/acps/v1/authDownload/data/addition", "下载任务中添加数据_根据人员与设备通道指定下载", OperationEnum.POST.getCode()),
	ISC_TASK_START(68, "/api/acps/v1/authDownload/task/start", "开始下载任务", OperationEnum.POST.getCode()),
	ISC_TASK_PROCESS_GET(69, "/api/acps/v1/authDownload/task/progress", "查询下载任务进度", OperationEnum.POST.getCode()),
	ISC_TASK_RECORD_GET(70, "/api/acps/v1/download_record/channel/list/search", "查询设备通道权限下载记录列表", OperationEnum.POST.getCode()),
	ISC_TASK_RECORD_DETAIL_GET(71, "/api/acps/v2/download_record/person/detail/search", "查询设备通道的人员权限下载详情v2", OperationEnum.POST.getCode()),
	ISC_AUTH_CONFIG_ADD(72, "/api/acps/v1/auth_config/add", "添加权限配置", OperationEnum.POST.getCode()),
	ISC_AUTH_CONFIG_DEL(73, "/api/acps/v1/auth_config/delete", "删除权限配置", OperationEnum.POST.getCode()),
	ISC_AUTH_CONFIG_DOWN(74, "/api/acps/v1/authDownload/configuration/shortcut", "根据出入权限配置快捷下载", OperationEnum.POST.getCode()),
	ISC_AUTH_CONFIG_PROCESS_GET(75, "/api/acps/v1/auth_config/rate/search", "查询权限配置单进度", OperationEnum.POST.getCode()),
	ISC_AUTH_ITEM_LIST_GET(82, "/api/acps/v1/auth_item/list/search", "查询权限条目列表", OperationEnum.POST.getCode()),
	ISC_CARD_ADD(83, "/api/cis/v1/card/bindings", "批量开卡", OperationEnum.POST.getCode()),
	ISC_CARD_DELETE(84, "/api/cis/v1/card/deletion", "卡片退卡", OperationEnum.POST.getCode()),
	ISC_CARD_LIST_GET(85, "/api/irds/v1/card/advance/cardList", "查询人员卡片列表", OperationEnum.POST.getCode()),

	ISC_EVENT_SUBSCRIBE(76, "/api/eventService/v1/eventSubscriptionByEventTypes", "按事件类型订阅事件", OperationEnum.POST.getCode()),
	ISC_EVENT_UNSUBSCRIBE(77, "/api/eventService/v1/eventUnSubscriptionByEventTypes", "按事件类型取消订阅", OperationEnum.POST.getCode()),

	ISC_TEMPERATURE_GET(78, "/api/gptas/v1/alarmInfo/search", "查询测温记录", OperationEnum.POST.getCode()),
	ISC_TEMPERATURE_GROUP_GET(79, "/api/gptas/v1/group/search", "查询测温分组", OperationEnum.POST.getCode()),
	ISC_TEMPERATURE_POINT_GET(80, "/api/gptas/v1/thermodetector/search", "查询测温点", OperationEnum.POST.getCode()),
	ISC_TEMPERATURE_REGISTER_GET(81, "/api/gptas/v1/register/search", "查询测温登记记录", OperationEnum.POST.getCode()),

	// >>>>>>>>>>>>> ISC End >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	UNKNOWN(-1, "", "未知", OperationEnum.UNKNOWN.getCode());

	private Integer code;
	private String key;
	private String desc;
	private Integer operation;

	public static EventEnum eventEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (EventEnum eventEnum : EventEnum.values()) {
				if (eventEnum.getCode().equals(code)) {
					return eventEnum;
				}
			}
		}
		return EventEnum.UNKNOWN;
	}

	public static String desc(Integer code) {
		return eventEnum(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (EventEnum eventEnum : EventEnum.values()) {
				if (eventEnum.getDesc().equals(desc)) {
					return eventEnum.getCode();
				}
			}
		}
		return null;
	}

	public static EventEnum key(String key) {
		if (StringUtils.isNotEmpty(key)) {
			for (EventEnum eventEnum : kafka()) {
				if (eventEnum.getKey().equals(key)) {
					return eventEnum;
				}
			}
		}
		return EventEnum.UNKNOWN;
	}

	public static boolean isKafka(EventEnum eventEnum){
		return eventEnum.getOperation().equals(OperationEnum.KAFKA.getCode());
	}

	public static boolean isPost(EventEnum eventEnum){
		return eventEnum.getOperation().equals(OperationEnum.POST.getCode());
	}

	public static boolean isGet(EventEnum eventEnum){
		return eventEnum.getOperation().equals(OperationEnum.GET.getCode());
	}

	public static boolean isISC(EventEnum eventEnum) {
		return eventEnum.name().startsWith("ISC_");
	}

	public static List<EventEnum> kafka(){
		return Arrays.stream(EventEnum.values()).filter(EventEnum::isKafka).collect(Collectors.toList());
	}
}
