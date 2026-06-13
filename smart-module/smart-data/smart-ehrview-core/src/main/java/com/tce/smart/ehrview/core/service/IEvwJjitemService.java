package com.tce.smart.ehrview.core.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwJjitem;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IEvwJjitemService extends IService<EvwJjitem> {

    List<EvwJjitem> getEvwJjitem(Integer ezid);
}
