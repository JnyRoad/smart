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
public class OvwYsdepRespDTO extends BaseVO {

    private static final long serialVersionUID = 4284927141330598588L;

    private Integer depid;

    private String depname;

    private String depAbbr;

    private Integer compId;

    private String director;

    private String direcName;

    private String depGrade;

    private Integer adminId;

    private String depCost;

}
