package com.tce.smart.platform.api.dto.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2021-07-20 17:44:48
 */
@Data
public class FaceImgTaskDetailsRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("id")
	private Integer id;
	/**
	 * 图片名
	 */
	@ApiModelProperty("图片名")
	@Excel(name = "图片名", isImportField = "imgName")
	private String imgName;
	/**
	 * 图片code
	 */
	@ApiModelProperty("图片code")
	private String imgCode;
	/**
	 * 下发状态 0 失败 1成功
	 */
	@ApiModelProperty("下发状态")
	private Integer status;

	@ApiModelProperty("下发状态")
	@Excel(name = "下发状态", isImportField = "statusDesc")
	private String statusDesc;
	/**
	 * 备注
	 */
	@ApiModelProperty("备注")
	@Excel(name = "备注", isImportField = "remark")
	private String remark;

	/**
	 * 员工id
	 */
	@ApiModelProperty("员工id")
	private Long staffId;
	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	@Excel(name = "创建时间", isImportField = "createTime")
	private String createTime;
	/**
	 * 任务ID
	 */
	@ApiModelProperty("任务ID")
	private Long taskId;


}
