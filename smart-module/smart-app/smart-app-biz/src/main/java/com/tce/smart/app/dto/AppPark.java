package com.tce.smart.app.dto;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import lombok.Data;

import java.util.List;

@Data
public class AppPark {
	private List<SmtParkDTO>  park;
}
