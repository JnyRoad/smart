package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.vo.AppPictureVo;

import java.util.List;

/**
 * 图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:47
 */
public interface AppContentPictureService extends IService<AppContentPicture> {
	Integer addBootPage(AppPictureDto appPictureDto);
	Integer addStartPage(AppPictureDto appPictureDto);
	AppPictureVo bootPage();
	AppPictureDto startPage();
	void updatePage(AppPictureDto appPictureDto);

	Boolean deletePicContent(Integer contentPictureId);
}
