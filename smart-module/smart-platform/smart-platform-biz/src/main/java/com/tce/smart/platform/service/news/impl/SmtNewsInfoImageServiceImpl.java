package com.tce.smart.platform.service.news.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import com.tce.smart.platform.core.mapper.news.SmtNewsInfoImageMapper;
import com.tce.smart.platform.service.news.SmtNewsInfoImageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
@Service
public class SmtNewsInfoImageServiceImpl extends ServiceImpl<SmtNewsInfoImageMapper, SmtNewsInfoImage> implements SmtNewsInfoImageService {

	@Override
	public Boolean removeByInfoId(Long infoId) {
		return this.remove(Wrappers.<SmtNewsInfoImage>query().lambda().eq(SmtNewsInfoImage::getInfoId, infoId));
	}

	@Override
	public List<SmtNewsInfoImage> getByInfoId(Long infoId) {
		return this.list(Wrappers.<SmtNewsInfoImage>query().lambda().eq(SmtNewsInfoImage::getInfoId, infoId));
	}
}
