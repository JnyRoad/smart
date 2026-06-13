package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitJcheLimitDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer parkId;

    private String parkName;

    private LocalDateTime createTime;

}
