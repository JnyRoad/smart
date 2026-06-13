package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SmtSecurityAreaSupplierDTO;
import com.tce.smart.platform.core.dto.securityarea.SecuritySupplierDTO;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaNotify;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSecurityAreaNotifyMapper
 * @date: 2020-07-21 9:16
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSecurityAreaNotifyMapper extends BaseMapper<SmtSecurityAreaNotify> {
}
