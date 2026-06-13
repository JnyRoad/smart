package com.tce.smart.platform.api.dto.resp;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * 查询app访客记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchAppSmtVisitorRespDTO extends BaseVO{

	private static final long serialVersionUID = 5189461219326964029L;

    private Long visitorId;
    /**
   *
   */
    private String visitorName;
    /**
   *
   */
    private String visitorPhotoId;
    private String visitorPhoto;
    /**
   * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
   */
    private Integer status;
    private String statusDesc;
    /**
   *
   */
    private Integer cause;
    private String causeDesc;
    /**
   *
   */
    private Date startTime;
    /**
   *
   */
    private Date endTime;

    /**
     * 审批节点名称
     */
    private String processNodeName;

    private String parkName;

    private Integer parkId;
}
