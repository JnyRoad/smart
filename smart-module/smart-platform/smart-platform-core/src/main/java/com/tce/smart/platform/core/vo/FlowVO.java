package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 流程节点
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowVO extends Model<FlowVO> {
private static final long serialVersionUID = 1L;


    private String nodeName;
    private Integer nodeState;
    private String processDesc;
    private Date processDate;
    private String remark;
    /**
     * 审批人
     */
    private String createUser;
}
