package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RoleDTO implements Serializable {
	private static final long serialVersionUID = -3572813993381961231L;

	private Integer roleId;

	private String roleName;

	private Integer roleType;
}
