package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppCatalog;
import com.tce.smart.app.mapper.AppCatalogMapper;
import com.tce.smart.app.service.AppCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 主题分类
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:12
 */
@Service
public class AppCatalogServiceImpl extends ServiceImpl<AppCatalogMapper, AppCatalog> implements AppCatalogService {

}
