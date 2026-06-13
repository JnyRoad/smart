package com.tce.smart.temporary.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.temporary.core.entity.EleaveJjitem;
import com.tce.smart.temporary.core.mapper.EleaveJjitemMapper;
import com.tce.smart.temporary.core.service.IEleaveJjitemService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class EleaveJjitemServiceImpl extends ServiceImpl<EleaveJjitemMapper, EleaveJjitem> implements IEleaveJjitemService {

    @Override
    public boolean saveBatchEleaveJjitem(List<EleaveJjitem> list) {
        int num = this.baseMapper.saveBatchEleaveJjitem(list);
        return num > 0;
    }

}
