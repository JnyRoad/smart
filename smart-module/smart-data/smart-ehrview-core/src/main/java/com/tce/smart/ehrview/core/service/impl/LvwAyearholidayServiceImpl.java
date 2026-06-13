package com.tce.smart.ehrview.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.ehrview.core.entity.LvwAyearholiday;
import com.tce.smart.ehrview.core.mapper.LvwAyearholidayMapper;
import com.tce.smart.ehrview.core.service.ILvwAyearholidayService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Service
public class LvwAyearholidayServiceImpl extends ServiceImpl<LvwAyearholidayMapper, LvwAyearholiday> implements ILvwAyearholidayService {

    @Override
    public LvwAyearholiday getByBadge(String badge) {
        return this.baseMapper.selectOne(Wrappers.<LvwAyearholiday> query().lambda().eq(LvwAyearholiday::getBadge, badge));
    }
}
