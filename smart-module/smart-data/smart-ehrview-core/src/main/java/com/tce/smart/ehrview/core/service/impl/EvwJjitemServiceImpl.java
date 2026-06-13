package com.tce.smart.ehrview.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwJjitem;
import com.tce.smart.ehrview.core.mapper.EvwJjitemMapper;
import com.tce.smart.ehrview.core.service.IEvwJjitemService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class EvwJjitemServiceImpl extends ServiceImpl<EvwJjitemMapper, EvwJjitem> implements IEvwJjitemService {

    @Override
    public List<EvwJjitem> getEvwJjitem(Integer ezid) {
        return this.baseMapper.getEvwJjitem(ezid);
    }

}
