package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtDeviceTag;

import java.util.List;

/**
 * @author sunfujian
 * @date 2021/7/29 11:24
 */
public interface SmtDeviceTagService extends IService<SmtDeviceTag> {

	IPage<SmtDeviceTag> getPage(Page page, String tagName);

	Boolean save(String tagName);

	Boolean update(Long id, String tagName);

	Boolean exist(String tagName);

	List<SmtDeviceTag> getByDeviceId(String deviceId);
}
