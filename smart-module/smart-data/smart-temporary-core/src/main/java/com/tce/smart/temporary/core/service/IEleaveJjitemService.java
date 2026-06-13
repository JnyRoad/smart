package com.tce.smart.temporary.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.temporary.core.entity.EleaveJjitem;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IEleaveJjitemService extends IService<EleaveJjitem> {
    boolean saveBatchEleaveJjitem(List<EleaveJjitem> list);
}
