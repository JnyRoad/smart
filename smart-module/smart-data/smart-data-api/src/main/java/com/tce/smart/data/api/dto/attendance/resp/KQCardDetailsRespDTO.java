package com.tce.smart.data.api.dto.attendance.resp;

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
public class KQCardDetailsRespDTO extends BaseVO {

    private String empNo;

    private String empname;

    private String kqdate;

    private String kqtime;
}
