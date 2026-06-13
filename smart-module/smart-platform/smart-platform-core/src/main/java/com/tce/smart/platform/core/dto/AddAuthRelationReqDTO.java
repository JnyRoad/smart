package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量新增事务与设备关联
 * @author fushiping
 * @date 2020/9/3  11:54
 **/
@Data
public class AddAuthRelationReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 权限关联
	 */
	List<SmtBusinessDeviceAuth> auth;

	/**
	 * 园区id
	 */
	Integer parkId;

	/**
	 * 特殊权限级层
	 */
	List<Integer> jcheIds;

}
