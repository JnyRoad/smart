package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/***
 * description: 合同签约单位 <br>
 * date: 2019/11/27 11:36 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@TableName("ovw_YsConComany")
public class OvwYsConComany extends Model<OvwYsConComany> {

    private static final long serialVersionUID = -7431740358399132535L;

	@TableField("CompID")
    private Integer compId;

	@TableField("CompAbbr")
    private String compAbbr;

    @TableField("Title")
    private String title;

}
