package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @description: 公摊水电表关联房间表
 * @date: 2020-09-29 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_COMMON_SD_ROOM")
@EqualsAndHashCode(callSuper = true)
public class SmtCommonSDRoom extends Model<SmtCommonSDRoom> {

	/**
	 * 公摊表记录Id
	 */
	private Long commonId;

	/**
     * 房间Id
	 */
	private Integer roomId;
}
