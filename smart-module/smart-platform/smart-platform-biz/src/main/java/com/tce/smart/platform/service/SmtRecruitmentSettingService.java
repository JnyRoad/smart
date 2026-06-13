package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.ao.RecruitSetSaveAO;
import com.tce.smart.platform.core.entity.SmtRecruitmentSetting;
import com.tce.smart.platform.core.vo.RecruitSetCompListVO;
import com.tce.smart.platform.core.vo.RecruitSetListVO;
import com.tce.smart.platform.core.vo.RecruitSetWorkBaseListVO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;

import java.util.List;

/**
 * 招聘设置服务接口
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
public interface SmtRecruitmentSettingService extends IService<SmtRecruitmentSetting> {

	/**
	 * 根据园区ID获取配置信息
	 *
	 * @param parkId 园区ID
	 * @return RecruitSetListVO
	 */
	RecruitSetListVO listRecruit(Integer parkId, String buId);

	/**
	 * 根据园区ID删除配置
	 *
	 * @param parkId
	 * @return
	 */
	Boolean removeByParkId(Integer parkId);

	/**
	 * 根据标题关键字签约单位查询
	 *
	 * @param keyword 标题关键字
	 * @return List<RecruitSetCompListVO>
	 */
	List<RecruitSetCompListVO> getListByTitle(String keyword);

	/**
	 * 根据工作地点关键字查询
	 *
	 * @param keyword 工作地点关键字
	 * @return List<RecruitSetBaseListVO>
	 */
	List<RecruitSetWorkBaseListVO> getWorkBaseCodeList(String keyword);

	/**
	 * 根据Bu关键字查询
	 *
	 * @param keyword Bu关键字
	 * @return List<OvwYscompVO>
	 */
	List<OvwYscompRespDTO> getCompeList(String keyword);

	/**
	 * 批量添加招聘设置
	 *
	 * @param recruitSetSaveAO 招聘设置表
	 * @return Boolean
	 */
	Boolean batchSaveRecruit(RecruitSetSaveAO recruitSetSaveAO);
}
