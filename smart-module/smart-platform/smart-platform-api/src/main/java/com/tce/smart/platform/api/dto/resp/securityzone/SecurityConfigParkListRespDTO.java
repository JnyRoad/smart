package com.tce.smart.platform.api.dto.resp.securityzone;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@Data
public class SecurityConfigParkListRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

    private Integer parkId;

	private String parkName;

	private LocalDateTime createTime;
}
