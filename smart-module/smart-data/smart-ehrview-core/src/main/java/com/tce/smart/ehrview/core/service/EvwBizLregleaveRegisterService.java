package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;

import java.util.List;

public interface EvwBizLregleaveRegisterService extends IService<EvwBizLregleaveRegister> {
	List<EvwBizLregleaveRegister> list(String badge, String queryMonth);
}
