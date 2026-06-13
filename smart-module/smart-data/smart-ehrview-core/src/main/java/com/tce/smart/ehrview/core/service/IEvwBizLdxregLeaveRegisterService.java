package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizLdxregLeaveRegister;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:40
 */

public interface IEvwBizLdxregLeaveRegisterService extends IService<EvwBizLdxregLeaveRegister> {
	List<EvwBizLdxregLeaveRegister> list(String badge, String queryMonth);
}
