package com.tce.smart.data.controller.temporary;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.temporary.req.SaveEPhotoReqDTO;
import com.tce.smart.temporary.core.dto.SaveEPhotoDto;
import com.tce.smart.temporary.core.service.IEPhotoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * EHR员工图片信息
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Controller
@RequestMapping("/ephoto")
public class EPhotoController extends BaseController {

	@Autowired
	private IEPhotoService ePhotoService;

	/**
	 * 保存人事员工人脸图片信息
	 *
	 * @param saveEPhotoReqDTO 保存EHR员工图片
	 * @return true-成功,false-失败
	 */
	@PostMapping("/save")
	@ResponseBody
	private Result<Boolean> saveOrUpdatePhoto(@RequestBody SaveEPhotoReqDTO saveEPhotoReqDTO) {

		SaveEPhotoDto queryBean = new SaveEPhotoDto();
		BeanUtils.copyProperties(saveEPhotoReqDTO,queryBean);

		return success(ePhotoService.saveOrUpdatePhoto(queryBean));
	}
}
