package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationResume;
import com.tce.smart.platform.core.mapper.SmtApplicationResumeMapper;
import com.tce.smart.platform.service.SmtApplicationResumeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@Service
@AllArgsConstructor
public class SmtApplicationResumeServiceImpl extends ServiceImpl<SmtApplicationResumeMapper, SmtApplicationResume> implements SmtApplicationResumeService {

	private final SmtApplicationResumeMapper mapper;

	@Override
	public Result getResumeById(Integer id) {
		// TODO Auto-generated method stub
		//根据应聘id返回简历
		SmtApplicationResume selectOne = mapper.selectOne(Wrappers.<SmtApplicationResume> query().lambda().eq(SmtApplicationResume::getApplicationId, id));
		return new Result<>(selectOne.getResume());
	}

	@Override
	public Result updateApplicationResume(MultipartFile file , SmtApplicationResume smtApplicationResume) {
		// TODO Auto-generated method stub
		if(file==null)
		{
			return new Result<>(Boolean.FALSE, "请上传简历");
		}
		try {
			byte[] bytes = file.getBytes();
			 smtApplicationResume.setResume(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		smtApplicationResume.setResumeName(file.getName());
		return new Result<>(mapper.updateById(smtApplicationResume));
	}


	/**
	 * 将blob转化为byte[],可以转化二进制流的
	 *
	 * @param blob
	 * @return
	 */
	private byte[] blobToBytes(Blob blob) {
		InputStream is = null;
		byte[] b = null;
		try {
			is = blob.getBinaryStream();
			b = new byte[(int) blob.length()];
			is.read(b);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return b;
	}





}
