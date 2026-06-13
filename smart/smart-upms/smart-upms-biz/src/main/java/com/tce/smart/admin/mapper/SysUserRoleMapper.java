package com.tce.smart.admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.admin.api.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 *
 * @since 2017-10-29
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
	/**
	 * 根据用户Id删除该用户的角色关系
	 *
	 * @param userId 用户ID
	 * @return boolean
	 */
	Boolean deleteByUserId(@Param("userId") Integer userId);
}
