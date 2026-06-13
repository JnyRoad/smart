package com.tce.smart.platform.api.dto.resp.securityzone;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *OA同步区域与权限关联表
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@Data
public class OaAreaAuthListRespDTO implements Serializable {

private static final long serialVersionUID = 1L;
    /**
   * 园区id
   */
    private Integer parkId;
	/**
	 * 权限策略名
	 */
	private String authName;
	/**
	 * 权限策略id
	 */
	private Integer authId;
	/**
	 * 权限类型
	 */
	private Integer authType;
}
