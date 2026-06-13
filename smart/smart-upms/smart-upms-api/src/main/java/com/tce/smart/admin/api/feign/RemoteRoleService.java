package com.tce.smart.admin.api.feign;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysLog;
import com.tce.smart.admin.api.entity.SysRole;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 *
 * @date 2020/9/28
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteRoleService {
	/**
	 * 获取角色列表
	 *
	 * @param from   是否内部调用
	 * @return succes、false
	 */
	@GetMapping("/role/list")
    Result<List<SysRole>> listRoles(@RequestHeader(SecurityConstants.FROM) String from);

}
