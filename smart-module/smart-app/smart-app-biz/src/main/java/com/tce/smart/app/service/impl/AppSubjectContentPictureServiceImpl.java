package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppSubjectContentPicture;
import com.tce.smart.app.mapper.AppSubjectContentPictureMapper;
import com.tce.smart.app.service.AppContentPictureService;
import com.tce.smart.app.service.AppSubjectContentPictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 主题图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:02
 */
@Service
public class AppSubjectContentPictureServiceImpl extends ServiceImpl<AppSubjectContentPictureMapper, AppSubjectContentPicture> implements AppSubjectContentPictureService {

    @Autowired
	private AppSubjectContentPictureService subjectContentPictureService;

	@Autowired
	private AppContentPictureService contentPictureService;

	@Override
	public List<AppSubjectContentPicture> selectBySubjectId(Integer subjectId) {
		QueryWrapper<AppSubjectContentPicture> queryWrapper = new QueryWrapper<AppSubjectContentPicture>();
		queryWrapper.lambda().eq(AppSubjectContentPicture::getSubjectId, subjectId);

		return baseMapper.selectList(queryWrapper);

	}

	@Override
	public Integer getBySubjectId(Integer subjectId) {
		AppSubjectContentPicture  appSubjectContentPicture = subjectContentPictureService.getBaseMapper().selectOne(Wrappers
				.<AppSubjectContentPicture>query().lambda().eq(AppSubjectContentPicture::getSubjectId,subjectId));
		if(Objects.nonNull(appSubjectContentPicture)) {
			return appSubjectContentPicture.getContentPictureId();
		}
		return null;
	}

	@Transactional(rollbackFor = Exception.class)
	public Boolean cascadeDelete(Integer subjectId) {
		List<AppSubjectContentPicture> rsList = this.list(Wrappers.<AppSubjectContentPicture>query().lambda().eq(AppSubjectContentPicture::getSubjectId,subjectId));
		if(CollectionUtils.isNotEmpty(rsList)){
			for(AppSubjectContentPicture element : rsList){
//				//删除主题图片内容
//				element.deleteById();
				//删除文本内
				contentPictureService.deletePicContent(element.getContentPictureId());
			}
		}

		return Boolean.TRUE;
	}
}
