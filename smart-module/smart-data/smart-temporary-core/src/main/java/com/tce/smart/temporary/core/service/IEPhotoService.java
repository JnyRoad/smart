package com.tce.smart.temporary.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.temporary.core.dto.SaveEPhotoDto;
import com.tce.smart.temporary.core.entity.EPhoto;

/**
 * EHR员工头像服务接口
 *
 * @author mkwu
 * @date 2019-07-31
 */
public interface IEPhotoService extends IService<EPhoto> {

	/**
	 * 根据eid查询图片信息
	 *
	 * @param eid
	 * @return
	 */
	EPhoto getInfoByEid(Integer eid);

	/**
	 * 保存人事员工人脸图片信息
	 *
	 * @param saveEPhotoDto 保存EHR员工图片
	 * @return true-成功,false-失败
	 */
	Boolean saveOrUpdatePhoto(SaveEPhotoDto saveEPhotoDto);

}
