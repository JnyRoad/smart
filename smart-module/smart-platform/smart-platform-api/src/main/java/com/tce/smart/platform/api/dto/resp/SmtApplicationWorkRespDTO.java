package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-19 14:37:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationWorkRespDTO extends BaseVO {
private static final long serialVersionUID = -4612008131352259118L;

    /**
   *
   */
    private Integer id;
    /**
   *
   */
    private String startTime;
    /**
   *
   */
    private String endTime;
    /**
   * 公司名称
   */
    private String company;
    /**
   * 职位
   */
    private String jobName;

    /**
   * 负责人名称
   */
    private String personLiable;
    /**
   * 负责人电话
   */
    private String phone;
    /**
   * 应聘者ID
   */
    private Long applicationId;

}
