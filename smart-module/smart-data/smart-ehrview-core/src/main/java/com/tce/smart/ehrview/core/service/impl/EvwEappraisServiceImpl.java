package com.tce.smart.ehrview.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.EvwEapprais;
import com.tce.smart.ehrview.core.mapper.EvwEappraisMapper;
import com.tce.smart.ehrview.core.service.IEvwEappraisService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class EvwEappraisServiceImpl extends ServiceImpl<EvwEappraisMapper, EvwEapprais> implements IEvwEappraisService {

    @Override
    public List<EvwEapprais> getListByBadge(String badge) {
        return this.baseMapper.selectList(Wrappers.<EvwEapprais>query().lambda().eq(EvwEapprais::getBadge, badge));
    }
}
