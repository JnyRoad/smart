package com.tce.smart.data.api.dto.consume.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公司账户刷卡消费记录
 *
 * @author mkwu
 * @date 2019-08-02
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class ConsumePubRecordRespDTO extends ConsumeRecordRespDTO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8090681676037061504L;

}
