package com.tce.smart.data.api.dto.businesstrip.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 出差日程数据
 *
 * @author mkwu
 * @date 2019-06-24
 */
@Data
public class CcdFormtableMainDt1RespDTO extends BaseVO {

    private static final long serialVersionUID = 1459794959179504263L;

	private Integer mainId;

    private String departureTime;

    private String arrivalTime;

    private String departureCity;

    private String arrivalCity;

    private Integer transportLargeClass;

    private Integer transportSubClass;

    private Double averageTicketPrice;

    private Double actualTicketPrize;
}
