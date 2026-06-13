package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.entity.AppSubject;

import java.awt.*;
import java.util.List;


/**
 * @author fushiping
 * @date 2019/5/22 13:55
 **/
public interface AppBannerMapper extends BaseMapper<AppSubject> {
	/**
	 * banner分页列表
	 * @param page
	 * @return
	 */
	IPage<AppSubject> getBannerList(Page page);

	List<AppSubject> getOnlineBannerList();

}
