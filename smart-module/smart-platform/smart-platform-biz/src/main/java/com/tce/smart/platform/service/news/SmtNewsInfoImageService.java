package com.tce.smart.platform.service.news;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.news.SmtNewsInfoImage;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-02-16 18:00:09
 */
public interface SmtNewsInfoImageService extends IService<SmtNewsInfoImage> {

	/**
	 * @param infoId
	 * @return
	 */
	Boolean removeByInfoId(Long infoId);

	/**
	 * @param infoId
	 * @return
	 */
	List<SmtNewsInfoImage> getByInfoId(Long infoId);

}
