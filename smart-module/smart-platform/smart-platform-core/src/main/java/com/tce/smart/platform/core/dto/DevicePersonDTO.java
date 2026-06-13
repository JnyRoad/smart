package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * 设备绑定人员信息
 * @author Administrator
 *
 */
@Data
public class DevicePersonDTO extends Model<DevicePersonDTO> {

    private static final long serialVersionUID = 1L;

    private String cardNo;

    private String personName;

    private String imageId;

    private Integer action;

    private Date createTime;

	private String serialNo;
}
