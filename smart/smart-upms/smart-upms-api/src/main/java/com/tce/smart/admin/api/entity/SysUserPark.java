package com.tce.smart.admin.api.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/***
 * description: 用户园区表 <br>
 * date: 2019/11/20 17:21 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserPark extends Model<SysUserPark> {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Integer userId;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
}
