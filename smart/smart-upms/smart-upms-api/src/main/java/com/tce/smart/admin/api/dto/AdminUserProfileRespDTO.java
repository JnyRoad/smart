package com.tce.smart.admin.api.dto;

import com.tce.smart.admin.api.dto.SmtParkDTO;
import com.tce.smart.admin.api.entity.SysRole;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 管理端用户资料投影，排除密码、salt、openid 等认证秘密。 */
@Data
public class AdminUserProfileRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private String fullName;
    private String phone;
    private String avatar;
    private Integer deptId;
    private String deptName;
    private String lockFlag;
    private List<SysRole> roleList;
    private List<SmtParkDTO> parkList;
}
