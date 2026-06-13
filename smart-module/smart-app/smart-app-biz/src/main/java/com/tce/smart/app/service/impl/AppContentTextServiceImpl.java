package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.mapper.AppContentTextMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 文本内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Service
public class AppContentTextServiceImpl extends ServiceImpl<AppContentTextMapper, AppContentText> implements AppContentTextService {

	@Autowired
	private AppSubjectMapper appSubjectMapper;
	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	/**
	 * 修改文本内容
	 *
	 * @param addAppSubjectAo
	 * @return
	 */
	public void updateTextContent(AddAppSubjectAo addAppSubjectAo) {
		Integer id = addAppSubjectAo.getId();
		AppContentText appContentText = new AppContentText();
		Integer textId = appSubjectMapper.selectTextId(id);
		if (addAppSubjectAo.getPicBinary().startsWith("http")) {
			AppContentText contentText = this.getById(textId);
			String image = new String(contentText.getPicBinary());
			addAppSubjectAo.setPicBinary(image);
		}
		BeanUtils.copyProperties(addAppSubjectAo, appContentText);
		if (textId == null) {
			Integer tId = insertTextContent(addAppSubjectAo);
			appSubjectContentTextService.insertTextInSubject(tId, id);
			return;
		}
		appContentText.setId(textId);
		if (addAppSubjectAo.getPicBinary() != null) {
            appContentText.setPicBinary(addAppSubjectAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
        }
		if (addAppSubjectAo.getEnclosure() != null) {
            appContentText.setEnclosure(addAppSubjectAo.getEnclosure().getBytes(StandardCharsets.UTF_8));
        }
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentText.updateById();
	}

	/**
	 * 添加文本内容
	 *
	 * @param appSubjectAo
	 * @return
	 */
	public Integer insertTextContent(AddAppSubjectAo appSubjectAo) {
		AppContentText appContentText = new AppContentText();
		BeanUtils.copyProperties(appSubjectAo, appContentText);
		if (appSubjectAo.getPicBinary() != null) {
            appContentText.setPicBinary(appSubjectAo.getPicBinary().getBytes(StandardCharsets.UTF_8));
        }
		if (appSubjectAo.getEnclosure() != null) {
			String file = (appSubjectAo.getEnclosure());
			byte[] bytes;
            bytes = file.getBytes(StandardCharsets.UTF_8);
            appContentText.setEnclosure(bytes);

        }
		appContentText.setCreateTime(LocalDateTime.now());
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentText.setDelFlag(DeleteState.NORMOL.getCode());
		appContentText.setTextOrder(0);
		appContentText.insert();
		return appContentText.getId();
	}

	/**
	 * 删除文本内容
	 *
	 * @param id
	 */
	@Override
	public void deleteTextContent(Integer id) {
		AppContentText appContentText = new AppContentText();
		appContentText.setId(id);
		appContentText.setDelFlag(DeleteState.DELETE.getCode());
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentText.updateById();
	}


}
