package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizAregotRegister;

import java.util.List;

public interface EvwBizAregotRegisterService extends IService<EvwBizAregotRegister> {
	List<EvwBizAregotRegister> list(String badge, String queryMonth);
}
