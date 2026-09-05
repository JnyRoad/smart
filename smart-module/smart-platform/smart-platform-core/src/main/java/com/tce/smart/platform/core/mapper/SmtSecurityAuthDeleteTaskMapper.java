package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteTask;

/**
 * 保密区权限自动删除审计任务关联 Mapper。
 *
 * <p>关联写入沿用调用方事务，任何插入失败都由服务向上抛出。</p>
 */
public interface SmtSecurityAuthDeleteTaskMapper extends BaseMapper<SmtSecurityAuthDeleteTask> {
}
