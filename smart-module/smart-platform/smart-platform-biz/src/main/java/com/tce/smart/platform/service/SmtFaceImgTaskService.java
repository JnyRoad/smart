package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.FaceImgTaskQueryReqDTO;
import com.tce.smart.platform.core.dto.CheckFacePicDTO;
import com.tce.smart.platform.core.entity.SmtFaceImgTask;

/**
 *
 * 员工人脸上传任务表
 *
 * @author fushiping
 * @date 2021-07-20 17:44:40
 */
public interface SmtFaceImgTaskService extends IService<SmtFaceImgTask> {

	/**
	 * 分页查询
	 * @param page
	 * @param queryReqDTO
	 * @return
	 */
	IPage<SmtFaceImgTask> getPage(Page page, FaceImgTaskQueryReqDTO queryReqDTO);

	/**
	 * 批量上传员工人脸图
	 * @param check
	 * @return
	 */
	Boolean checkFacePic(CheckFacePicDTO check);

	/**
	 * 删除任务
	 * @param taskId
	 * @return
	 */
	Boolean deleteTask(Long taskId);

}
