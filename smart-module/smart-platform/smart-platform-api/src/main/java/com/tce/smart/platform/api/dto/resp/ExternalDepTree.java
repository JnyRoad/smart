package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import java.util.List;

@Data
public class ExternalDepTree extends BaseVO {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long value;

	private String label;

	private Integer type;

	private List<ExternalDepTree> children;
}
