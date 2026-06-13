package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogisticsAppointmentDTO extends Model<LogisticsAppointmentDTO> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.ID_WORKER)
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    private String vehiclePlate;
    /**
   * 司机名称
   */
//    @NotBlank(message = "司机姓名不能为空")
    private String driverName;
    /**
   * 司机手机号
   */
//    @NotBlank(message = "司机手机号不能为空")
    private String driverPhone;
    /**
   * 供应商名称
   */
//    @NotBlank(message = "供应商名称不能为空")
    private String supplier;
    /**
     * 预约到达时间
     */
    @NotNull(message = "预约到达时间不能为空")
    private Date startTime;
	/**
	 * 预约离开时间
	 */
    private Date endTime;
    /**
     * 实际到达时间
     */
    private Date arrivalTime;
    /**
     * 实际离开时间
     */
    private Date leaveTime;
    /**
   * 排程编号
   */
    @NotBlank(message = "排程编号不能为空")
    private String planCode;
    /**
   * 预约状态：1-已预约；2-已到达；3-已离开；4-已超时；5-取消预约
   */
    private Integer status;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

	private String companyId;

}
