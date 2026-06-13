package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 人员卡片信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class SmbUrlDTO implements Serializable {
    private static final long serialVersionUID = 6688927601908056816L;

    private String url;
    /**
     * 设备编号【必选】
     */
    private String ip;

    /**
     * 卡片编号【必选,纯数字且小于32位】
     */
    private String password;

	/**
	 * 员工号，暂只支持纯数字的员工号，非纯数字的不下发
	 */
	private String user;


}
