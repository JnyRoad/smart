package com.tce.smart.platform.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class XcVehicleList extends BaseVO {
	/**
	*
	*/
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;
    /**
   * 车牌号
   */
    private String vehiclePlate;


	/**
	 * 联系人
	 */
	private String contactsUser;

	/**
	 * 联系电话
	 */
	private String contactsPhone;

	/**
	 * 车牌类型
	 */
	private Integer ctId;

	/**
	 * 车牌状态
	 */
	private Integer cardState;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 有效开始日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	/**
	 * 有效结束日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	/**
	 * 操作人
	 */
	private String optUser;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;
}
