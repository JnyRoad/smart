package com.tce.smart.platform.core.mapper.watermeter;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.vo.SmtWaterMeterVO;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:30
 */
public interface SmtWaterMeterMapper extends BaseMapper<SmtWaterMeter> {

	IPage<SmtWaterMeterVO> getPage(Page page);
}
