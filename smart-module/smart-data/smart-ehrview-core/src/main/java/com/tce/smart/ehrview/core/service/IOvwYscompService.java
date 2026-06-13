package com.tce.smart.ehrview.core.service;

import com.tce.smart.ehrview.core.entity.OvwYscomp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IOvwYscompService extends IService<OvwYscomp> {

    OvwYscomp getByCompId(String compId);

    List<OvwYscomp> getList();
}
