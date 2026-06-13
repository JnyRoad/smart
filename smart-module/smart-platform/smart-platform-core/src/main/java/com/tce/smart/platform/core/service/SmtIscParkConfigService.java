package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.isc.EditIscParkConfigReqDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;

import java.util.Collection;
import java.util.Set;

public interface SmtIscParkConfigService extends IService<SmtIscParkConfig> {

	Boolean editConfig(EditIscParkConfigReqDTO reqDTO);

	IPage<SmtIscParkConfig> getPage(Page page, Integer parkId);

	SmtIscParkConfig getConfigByPark(Integer parkId);

	SmtIscParkConfig getActiveConfigById(Long id);

	Boolean removeConfigById(Long id);

	Set<Integer> getCardSyncDispatcherParkIds(Collection<Integer> businessParkIds);
}
