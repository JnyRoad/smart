package com.tce.smart.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysRole;
import com.tce.smart.admin.api.entity.SysRoleMenu;
import com.tce.smart.admin.mapper.SysRoleMapper;
import com.tce.smart.admin.mapper.SysRoleMenuMapper;
import com.tce.smart.admin.service.SysRoleService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
@Slf4j
@AllArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 通过用户ID，查询角色信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<SysRole> findRolesByUserId(Integer userId) {
        return baseMapper.listRolesByUserId(userId);
    }

    /**
     * 通过角色ID，删除角色,并清空角色菜单缓存
     *
     * @param id
     * @return
     */
    @Override
    @CacheEvict(value = "menu_details", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> removeRoleById(Integer id) {
		if (id.equals(SecurityConstants.ROLE_ORDINARY_ID)) {
			log.warn("不能删除普通员工角色:{}", SecurityConstants.ROLE_ORDINARY);
			return new Result<>(Boolean.FALSE, "不能删除普通员工角色");
		}
        SysRole sysRole = this.baseMapper.selectById(id);
        if (sysRole.getRoleCode().equals(SecurityConstants.ROLE_ADMIN)) {
            log.warn("不能删除管理员角色:{}", sysRole.getRoleCode());
            return new Result<>(Boolean.FALSE, "不能删除管理员角色");
        }
        if (sysRole.getRoleCode().equals(SecurityConstants.ROLE_ORDINARY)) {
            log.warn("不能删除普通员工角色:{}", sysRole.getRoleCode());
            return new Result<>(Boolean.FALSE, "不能删除普通员工角色");
        }
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>update().lambda().eq(SysRoleMenu::getRoleId, id));
        return new Result<>(this.removeById(id));
    }
}
