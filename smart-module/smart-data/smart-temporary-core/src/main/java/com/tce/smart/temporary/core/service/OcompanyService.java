package com.tce.smart.temporary.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.temporary.core.entity.Ocompany;

public interface OcompanyService extends IService< Ocompany> {

	Ocompany getByComId(Integer compId);

}
