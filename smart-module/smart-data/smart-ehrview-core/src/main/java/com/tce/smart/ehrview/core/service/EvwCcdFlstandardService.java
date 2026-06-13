package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwCcdFlstandard;

public interface EvwCcdFlstandardService extends IService<EvwCcdFlstandard> {

	EvwCcdFlstandard getFlById(String id);

}
