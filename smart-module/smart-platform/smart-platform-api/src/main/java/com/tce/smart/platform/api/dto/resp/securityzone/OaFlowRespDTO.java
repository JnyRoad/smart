package com.tce.smart.platform.api.dto.resp.securityzone;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程节点
 *
 * @author FUSHIPING
 * @date 2019-04-13 18:19:00
 */
@Data
public class OaFlowRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("节点名称")
    private String nodeName;

	@ApiModelProperty("节点状态")
    private Integer nodeState;

	@ApiModelProperty("审批备注")
    private String processDesc;

	@ApiModelProperty("审批日期")
    private Date processDate;

	@ApiModelProperty("备注")
    private String remark;

	@ApiModelProperty("审批人")
    private String createUser;
}
