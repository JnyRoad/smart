package com.tce.smart.temporary.core.service.impl;

import cn.hutool.core.codec.Base64Decoder;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.temporary.core.dto.SaveEPhotoDto;
import com.tce.smart.temporary.core.entity.EPhoto;
import com.tce.smart.temporary.core.mapper.EPhotoMapper;
import com.tce.smart.temporary.core.service.IEPhotoService;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * EHR员工头像服务实现类
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Service
public class EPhotoServiceImpl extends ServiceImpl<EPhotoMapper, EPhoto> implements IEPhotoService {

	@Override
	public EPhoto getInfoByEid(Integer eid) {
		return this.getOne(Wrappers.<EPhoto>query().lambda().eq(EPhoto::getEID, eid));
	}

	@Override
	public Boolean saveOrUpdatePhoto(SaveEPhotoDto saveEPhotoDto) {

		if (Objects.isNull(saveEPhotoDto) || Objects.isNull(saveEPhotoDto.getEid())
				|| StringUtil.isNullOrEmpty(saveEPhotoDto.getPhoto())) {

			return false;
		}

		Integer eid = saveEPhotoDto.getEid();
		// base解码
		byte[] iamgeByte = Base64Decoder.decode(saveEPhotoDto.getPhoto());
		// 查询EHR员工照片信息
		EPhoto ePhoto = this.getInfoByEid(eid);
		if (Objects.nonNull(ePhoto)) {
			EPhoto updateEPhoto = new EPhoto();
			updateEPhoto.setPhoto(iamgeByte);

			UpdateWrapper<EPhoto> updateWrapper = new UpdateWrapper<EPhoto>();
			updateWrapper.lambda().eq(EPhoto::getEID,eid);
			return this.update(updateEPhoto, updateWrapper);
		} else {
			EPhoto addEPhoto = new EPhoto();
			addEPhoto.setEID(eid);
			addEPhoto.setPhoto(iamgeByte);
			addEPhoto.setIsDisPose("N");

			return this.save(addEPhoto);
		}

	}

}
