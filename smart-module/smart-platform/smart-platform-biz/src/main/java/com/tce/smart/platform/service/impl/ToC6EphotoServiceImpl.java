package com.tce.smart.platform.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.SearchToC6DTO;
import com.tce.smart.platform.core.entity.ToC6Ephoto;
import com.tce.smart.platform.core.mapper.ToC6EphotoMapper;
import com.tce.smart.platform.core.vo.SearchToC6VO;
import com.tce.smart.platform.service.ToC6EphotoService;

import cn.hutool.core.util.ObjectUtil;


/**
 * 供c6同步员工头像
 * @author QIPEI
 * 2019/11/08
 */
@Slf4j
@Service
public class ToC6EphotoServiceImpl extends ServiceImpl<ToC6EphotoMapper, ToC6Ephoto> implements ToC6EphotoService {@Override


	public Result saveToC6(ToC6Ephoto toC6Ephoto) {
		// TODO Auto-generated method stub
		ToC6Ephoto selectOne = this.baseMapper.selectOne(Wrappers.<ToC6Ephoto> query().lambda().eq(ToC6Ephoto::getEmpNo,toC6Ephoto.getEmpNo()));

		if(ObjectUtil.isNotNull(selectOne))
		{
			selectOne.setPhoto(toC6Ephoto.getPhoto());
			selectOne.setIsDispose(0);
			return new Result<>(selectOne.updateById());
		}
		else
		{
			toC6Ephoto.setIsDispose(0);
			return new Result<>(toC6Ephoto.insert());
		}

}

@Override
public IPage<SearchToC6VO> searchPage(Page page, SearchToC6DTO searchToC6DTO) {
	// TODO Auto-generated method stub
	IPage<SearchToC6VO> pageResult =this.baseMapper.searchPage(page,searchToC6DTO);
	List<SearchToC6VO> records = pageResult.getRecords();
		for (SearchToC6VO searchToC6VO : records) {
			byte[] photo = searchToC6VO.getPhoto();
			if(ObjectUtil.isNotNull(photo))
			{
				String base64 = Base64.getEncoder().encodeToString(photo);
				searchToC6VO.setPhotos(base64);
			}
		}
	return pageResult;
}



}
