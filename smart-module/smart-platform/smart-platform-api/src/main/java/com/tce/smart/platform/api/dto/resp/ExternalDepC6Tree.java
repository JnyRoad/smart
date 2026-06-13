package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;

@Data
public class ExternalDepC6Tree extends BaseVO {

	@JsonSerialize(using = ToStringSerializer.class)
	private String value;

	private String label;

	private List<ExternalDepC6Tree> children;
}
