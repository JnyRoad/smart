package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtFaceImgTaskDetails;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-20 17:44:48
 */
public interface SmtFaceImgTaskDetailsService extends IService<SmtFaceImgTaskDetails> {

	/**
	 * 删除任务详情
	 * @param taskId
	 * @return
	 */
	Boolean deleteTaskDetail(Long taskId);

	/**
	 * 同步下发状态
	 * @param taskId
	 * @return
	 */
	Boolean syncTaskStatus(Long taskId);

	/**
	 * 获取列表
	 * @param status
	 * @param taskId
	 * @return
	 */
	List<SmtFaceImgTaskDetails> getByTaskId(Integer status, Long taskId);

	/**
	 * 获取列表
	 * @param status
	 * @param taskId
	 * @return
	 */
	IPage<SmtFaceImgTaskDetails> getPage(Page page, Integer status, Long taskId);

	/**
	 * excel下载
	 * @return
	 */
	ResponseEntity<byte[]> downLoadExcel(Integer status, Long taskId);

	/**
	 * 获得某状态下任务数量
	 * @param status
	 * @param taskId
	 * @return
	 */
	Integer countStatus(Integer status, Long taskId);
}
