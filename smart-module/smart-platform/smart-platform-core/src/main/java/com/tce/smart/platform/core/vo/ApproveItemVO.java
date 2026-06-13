package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApproveItemVO extends Model<ApproveItemVO> {
    private static final long serialVersionUID = 1L;

    /**
     * 审批项目名称
     */
    private String itemName;

    /**
     * 审批项目值
     */
    private String itemValue;
}
