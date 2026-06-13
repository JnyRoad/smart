package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 许昌车辆信息添加
 *
 */
@Data
public class UpdateXCVehicleDTO extends SaveXCVehicleDTO{

    /**
   * 主键
   */
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
}
