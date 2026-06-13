package com.tce.smart.algorithm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName AlgorithmChoose
 * @Description 算法选择表
 * @Author wxjason
 * @Date 2019\8\1 0001 12:01
 * Version 1.0
 **/
@Data
@Builder
@TableName("algorithm_config")
@EqualsAndHashCode(callSuper = true)
public class AlgorithmConfig extends Model<AlgorithmConfig> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 算法类型：对应AlgorithmTypeEnum枚举中的type
	 */
	private String algorithmType;
	/**
	 * 配置内容
	 */
	private String content;

}
