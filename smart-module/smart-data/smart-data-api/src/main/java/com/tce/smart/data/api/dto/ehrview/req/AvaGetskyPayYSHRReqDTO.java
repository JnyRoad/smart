package com.tce.smart.data.api.dto.ehrview.req;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class AvaGetskyPayYSHRReqDTO extends BaseVO {

    private List<String> badge;

    private LocalDateTime startTime;
}
