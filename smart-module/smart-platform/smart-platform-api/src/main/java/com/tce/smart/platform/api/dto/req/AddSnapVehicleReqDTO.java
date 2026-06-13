package com.tce.smart.platform.api.dto.req;

import com.tce.smart.platform.api.dto.SmtSnapVehicleDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddSnapVehicleReqDTO extends SmtSnapVehicleDTO {

	private static final long serialVersionUID = 7625566940534308196L;

	/**
	 * 车位总数
	 */
	@NotNull(message = "车位总数不可空")
	private Integer totalCount;

	/**
	 * 剩余车位数
	 */
	@NotNull(message = "剩余车位数不可空")
	private Integer freeCount;



	public AddSnapVehicleReqDTO() {
		super();
	}


	@Builder
	public AddSnapVehicleReqDTO(Integer totalCount, Integer freeCount) {
		this.totalCount = totalCount;
		this.freeCount = freeCount;
	}

}
