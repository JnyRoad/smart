package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.OvwYsdep;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IOvwYsdepService extends IService<OvwYsdep> {

    List<OvwYsdep> getByCompId(Integer compId);

    OvwYsdep getByDepId(Integer depId);

	List<OvwYsdep> getParentDep(Integer depId);
}
