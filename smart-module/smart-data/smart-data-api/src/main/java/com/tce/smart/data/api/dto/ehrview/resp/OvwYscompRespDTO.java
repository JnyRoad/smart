package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class OvwYscompRespDTO extends BaseVO {

    private Integer compid;

    private String title;

    private String compAbbr;

    private Integer compGrade;

    private Integer adminId;

    private Integer ezid;

}
