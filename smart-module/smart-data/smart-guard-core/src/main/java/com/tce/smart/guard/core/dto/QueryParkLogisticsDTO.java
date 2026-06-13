package com.tce.smart.guard.core.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
@Data
public class QueryParkLogisticsDTO implements Serializable {
private static final long serialVersionUID = 9065298496271683283L;

    /**
   * 主键ID
   */
    private Integer id;
    /**
   * 园区编号
   */
    private Integer parkId;
    /**
   * 物流中心编号
   */
    private String companyId;
	/**
	 * 物流中心名称
	 */
	@TableField(exist = false)
	private String companyName;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
