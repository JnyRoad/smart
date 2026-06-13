package com.tce.smart.app.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.dto.AppSubjectDto;
import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.entity.*;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.tool.exception.TCEException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fushiping
 * @date 2019/5/29 09:21
 **/
@Service
@Slf4j
public class AppSubjectBasicServiceImpl extends ServiceImpl<AppSubjectMapper, AppSubject> implements AppSubjectBasicService {

	/**
	 * 正则自定义label
	 */
	public final static String PATTER_LOCAL_PIC_KEY = "localPic";
	/**
	 * 文本内容里面本地图片信息
	 */
	public final static Pattern PATTER_LOCAL_PIC = Pattern.compile("src=\"(?<localPic>data:image/\\w+;base64,(.+?))\"");

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private AppContentTextService appContentTextService;

	@Autowired
	private AppSubjectContentPictureService subjectContentPictureService;

	@Autowired
	private AppContentPictureService apContentPictureService;

	@Autowired
	private AppParkSubjectService appParkSubjectService;

	@Autowired
	private AppCommService appCommService;


	/**
	 * 修改主题
	 *
	 * @param addAppSubjectAo
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateSubject(AddAppSubjectAo addAppSubjectAo) {
		this.updateSubjectContent(addAppSubjectAo);
		//将base64替换成URL
		String textDesc = replaceSaveBase64ContentImg(addAppSubjectAo.getId(), addAppSubjectAo.getSubjectName(), addAppSubjectAo.getTextDesc());
		//重新覆盖文本内容
		addAppSubjectAo.setTextDesc(textDesc);
		appContentTextService.updateTextContent(addAppSubjectAo);
		if (addAppSubjectAo.getParkId() != null) {
			this.updateParkSubject(addAppSubjectAo.getParkId(), addAppSubjectAo.getId());
		}
	}

	/**
	 * 修改主题内容
	 *
	 * @param addAppSubjectAo
	 * @return
	 */
	@Override
	public void updateSubjectContent(AddAppSubjectAo addAppSubjectAo) {
		AppSubjectDto appSubjectDto = new AppSubjectDto();
		BeanUtils.copyProperties(addAppSubjectAo, appSubjectDto);
		appSubjectDto.setUpdateTime(LocalDateTime.now());
		appSubjectDto.updateById();
	}

	/**
	 * 添加主题
	 *
	 * @param addAppSubjectAo
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer addSubject(AddAppSubjectAo addAppSubjectAo, String calalogCode) {
		Integer subjectId = this.insertSubjectContent(addAppSubjectAo, calalogCode);

		//将base64替换成URL
		String textDesc = replaceSaveBase64ContentImg(subjectId, addAppSubjectAo.getSubjectName(), addAppSubjectAo.getTextDesc());
		addAppSubjectAo.setTextDesc(textDesc);

		Integer textContentId = appContentTextService.insertTextContent(addAppSubjectAo);
		if (addAppSubjectAo.getParkId() != null) {
			addParkSubject(addAppSubjectAo.getParkId(), subjectId);
		}
		appSubjectContentTextService.insertTextInSubject(textContentId, subjectId);
		return subjectId;
	}

	/**
	 * 添加园区与主题关联
	 */
	public void addParkSubject(Integer parkId, Integer subjectId) {
		AppParkSubject appParkSubject = new AppParkSubject();
		appParkSubject.setParkId(parkId);
		appParkSubject.setSubjectId(subjectId);
		appParkSubjectService.save(appParkSubject);
	}

	/**
	 * 修改园区主题关联
	 *
	 * @param parkId
	 * @param subjectId
	 */
	public void updateParkSubject(Integer parkId, Integer subjectId) {
		AppParkSubject appParkSubject =
				appParkSubjectService.getOne(Wrappers.<AppParkSubject>query().lambda()
						.eq(AppParkSubject::getSubjectId, subjectId));
		if (appParkSubject != null) {
			appParkSubject.setParkId(parkId);
			appParkSubject.updateById();
		}
		if (appParkSubject == null) {
			AppParkSubject appParkSubjectInsert = new AppParkSubject();
			appParkSubjectInsert.setParkId(parkId);
			appParkSubjectInsert.setSubjectId(subjectId);
			appParkSubjectInsert.insert();
		}

	}

	/**
	 * 添加主题内容
	 *
	 * @param addAppSubjectAo
	 * @return
	 */
	@Override
	public Integer insertSubjectContent(AddAppSubjectAo addAppSubjectAo, String calalogCode) {
		AppSubjectDto appSubjectDto = new AppSubjectDto();
		BeanUtils.copyProperties(addAppSubjectAo, appSubjectDto);
		appSubjectDto.setCatalogCode(calalogCode);
		appSubjectDto.setCreateTime(LocalDateTime.now());
		appSubjectDto.setDelFlag(DeleteState.NORMOL.getCode());
		appSubjectDto.setPublishFlag(PublishState.INIT.getCode());
		appSubjectDto.setSubjectOrder(-1);
		appSubjectDto.setUpdateTime(LocalDateTime.now());
		appSubjectDto.insert();
		return appSubjectDto.getId();
	}

	/**
	 * 测试链接是否符合规范
	 *
	 * @param str
	 * @return
	 */
	public boolean isURL(String str) {
		//转换为小写
		str = str.toLowerCase();
		String regex = "^((https|http|ftp|rtsp|mms)?://)"
				+ "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}"
				+ "|"
				+ "([0-9a-z_!~*'()-]+\\.)*"
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\."
				+ "[a-z]{2,6})"
				+ "(:[0-9]{1,4})?"
				+ "((/?)|"
				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
		return str.matches(regex);
	}

	@Override
	public Integer num(String str) {
		if (str.contains("img")) {
			return 1;
		} else {
			String a = str.replaceAll("</?[^>]+>", "");
			Integer b = a.replaceAll("<a>\\s*|\t|\r|\n</a>", "").replaceAll("&nbsp;", "").trim().length();
			return b;
		}
	}

	/**
	 * 判断内容类型
	 *
	 * @param appSubject     主题
	 * @param appContentText 文本内容
	 * @return 1 自定义URL链接跳转,2 内容详情页跳转,3 APP模块跳转，4 PDF附件
	 */
	@Override
	public Integer getContentLinkType(AppSubject appSubject, AppContentText appContentText) {
		Integer contentLinkType;
		if (Objects.nonNull(appContentText) && StringUtils.isNotBlank(appContentText.getEnclosureName())) {
			contentLinkType = AppContentType.PDF.getType();
		} else if (StringUtils.isNotBlank(appSubject.getSubjectUrl())) {
			contentLinkType = AppContentType.LINK.getType();
		} else {
			contentLinkType = AppContentType.DESC.getType();
		}
		return contentLinkType;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public String replaceSaveBase64ContentImg(Integer subjectId, String picName, String contentText) {
		StringBuffer replaceText = new StringBuffer();

		if (!StringUtil.isNullOrEmpty(contentText)) {
			//正则查询内容汇总的img标签
			Matcher matcher = PATTER_LOCAL_PIC.matcher(contentText);
			AppContentPicture contentPicTemp;
			AppSubjectContentPicture appSubjectContentPicture;
			String localPicBase64;
			String localPicUrl;
			while (matcher.find()) {
				localPicBase64 = matcher.group(PATTER_LOCAL_PIC_KEY);

                contentPicTemp = new AppContentPicture(picName, localPicBase64.getBytes(StandardCharsets.UTF_8),LocalDateTime.now());

                //保存图片内容表
				apContentPictureService.save(contentPicTemp);

				appSubjectContentPicture = new AppSubjectContentPicture(subjectId, contentPicTemp.getId());
				//保存主图图片内容表
				subjectContentPictureService.save(appSubjectContentPicture);

				//将base64字符串替换成src="url"
				localPicUrl = String.format("src=\"%s\"",appCommService.buildContntPicImageUrl(contentPicTemp.getId()));
				matcher.appendReplacement(replaceText, localPicUrl);
			}

			matcher.appendTail(replaceText);
		}

		return replaceText.toString();
	}
}
