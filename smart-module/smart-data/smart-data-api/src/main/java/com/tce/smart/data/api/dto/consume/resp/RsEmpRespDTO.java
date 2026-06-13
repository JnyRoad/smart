package com.tce.smart.data.api.dto.consume.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 人事信息表
 *
 * @author fushiping
 * @date 2020-7-09
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RsEmpRespDTO extends BaseDTO {

    /**
	 * 序列号
	 */
	private static final long serialVersionUID = 1L;

    private String EmpSysID;

    private String EmpNo;

    private String EmpName;
}
