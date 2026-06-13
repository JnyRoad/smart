package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddSnapVehicleDTO extends SmtSnapVehicle {

	private static final long serialVersionUID = 1L;

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



	public AddSnapVehicleDTO() {
		super();
	}


	@Builder
	public AddSnapVehicleDTO(Integer totalCount, Integer freeCount) {
		this.totalCount = totalCount;
		this.freeCount = freeCount;
	}

}
