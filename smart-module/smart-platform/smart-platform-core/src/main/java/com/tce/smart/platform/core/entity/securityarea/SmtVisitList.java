package com.tce.smart.platform.core.entity.securityarea;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.io.Serializable;

/**
 * @description: 保密区预约来访名单
 * @date: 2020-07-30 8:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_VISIT_LIST")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitList extends Model<SmtVisitList> {

	private static final long serialVersionUID = 8213014904037779448L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
     * 预约记录标识
	 */
	private Long orderId;

	/**
     * 姓名
	 */
	private String visitName;

	/**
     * 手机号
	 */
	private String phone;
}
