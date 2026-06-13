package com.tce.smart.xcc6.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表
 *
 * @author wuling
 * @date 2021-01-19
 */
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("RS_Dpt")
public class RsDpt extends Model<RsDpt> {

    /**
	 * 序列号
	 */
	private static final long serialVersionUID = -7852766247744894274L;

	@TableField("DptSysID")
    private String DptSysID;

    @TableField("DptNo")
    private String DptNo;
}
