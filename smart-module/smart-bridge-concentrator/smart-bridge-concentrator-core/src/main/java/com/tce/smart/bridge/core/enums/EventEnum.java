package com.tce.smart.bridge.core.enums;

import cn.hutool.core.util.StrUtil;
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
	/////// Kafka Start //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	WATER_REPEATER_READ(38, "water_repeater_read_nty", "水表读数上报", OperationEnum.KAFKA.getCode()),

	WATER_REPEATER_IN_VALVE_STATE(39, "water_repeater_inValveState_nty", "水表内置阀门状态上报", OperationEnum.KAFKA.getCode()),

	WATER_REPEATER_OUT_VALVE_STATE(40, "water_repeater_outValveState_nty", "水表外置阀门状态上报", OperationEnum.KAFKA.getCode()),

	ELE_REPEATER_READ(41, "electric_repeater_read_nty", "电表读数上报", OperationEnum.KAFKA.getCode()),

	ELE_REPEATER_BRAKE_STATE(42, "electric_repeater_brakeState_nty", "电表闸门状态上报", OperationEnum.KAFKA.getCode()),

	/////// Kafka End //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		if (StrUtil.isNotEmpty(desc)) {
			for (EventEnum eventEnum : EventEnum.values()) {
				if (eventEnum.getDesc().equals(desc)) {
					return eventEnum.getCode();
				}
			}
		}
		return null;
	}

	public static EventEnum key(String key) {
		if (StrUtil.isNotEmpty(key)) {
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

	public static List<EventEnum> kafka(){
		return Arrays.stream(EventEnum.values()).filter(EventEnum::isKafka).collect(Collectors.toList());
	}
}
