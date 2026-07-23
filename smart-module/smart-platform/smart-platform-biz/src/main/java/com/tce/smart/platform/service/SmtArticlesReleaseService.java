package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.OfficeReleaseDraftRespDTO;
import com.tce.smart.platform.api.dto.resp.ReleaseStaffLookupRespDTO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import com.tce.smart.platform.core.entity.SmtArticlesReleasePerson;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseThing;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 16:56
 */

public interface SmtArticlesReleaseService extends IService<SmtArticlesRelease> {
	SmtArticlesRelease getByApproveId(String approveId);

	SmtArticlesRelease getByReleaseId(Long id);

	IPage<SmtArticlesRelease> getArticlesReleasePage(Page page, QueryArticlesReleaseReqDTO reqDTO);

	IPage<SmtArticlesRelease> getBackFactoryPage(Page page, ArticlesBackFactoryReqDTO reqDTO);

	/**
	 * 获取办公区物品放行待审批列表
	 * @param page
	 * @param queryDTO
	 * @return
	 */
	IPage<SmtArticlesRelease> getOfficeReleasePage(Page page, OfficeZoneApproveQueryDTO queryDTO);

	IPage<SmtArticlesRelease> getPCOfficePage(Page page, QueryArticlesReleaseReqDTO reqDTO);

	/**
	 * 保安查询待审批列表
	 * @param parkId
	 * @param type
	 * @return
	 */
	List<SmtArticlesRelease> guardGetList(Integer parkId, Integer type);

	Boolean status(Long id, String approveBadge, Integer status, String remark);

	Boolean livingStatusUpdate(Long id, String approveBadge, Integer status, String remark);

	/**
	 * 保安放行
	 * @param guardBadge 认证保安工号
	 * @param guardParkIds 认证保安可访问园区
	 * @param reqDTO
	 * @return
	 */
	Boolean securityUpdateForGuard(String guardBadge, List<Integer> guardParkIds, GuardReleaseConfirmReqDTO reqDTO);

	/**
	 * 保安确认返厂
	 * @param guardBadge 认证保安工号
	 * @param guardParkIds 认证保安可访问园区
	 * @param releaseId
	 * @return
	 */
	Boolean securityBackConfirmForGuard(String guardBadge, List<Integer> guardParkIds, Long releaseId);

	/**
	 * 保存APP生活区物品放行
	 * @param reqDTO
	 * @return
	 */
	Boolean saveArticlesRelease(AddArticlesReleaseReqDTO reqDTO);
	/**
	 * 保存许昌公众号生活区物品放行
	 * @param reqDTO
	 * @return
	 */
	Boolean saveLivingArticlesRelease(AddArticlesReleaseReqDTO reqDTO);

	/**
	 * 保存办公区物品放行
	 * @param reqDTO
	 * @return
	 */
	Boolean saveOfficeArticlesRelease(String ownerBadge, List<Integer> ownerParkIds, OfficeZoneReleaseReqDTO reqDTO);

	OfficeReleaseDraftRespDTO createOfficeDraft(String ownerBadge, CreateOfficeReleaseDraftReqDTO request);

	ReleaseStaffLookupRespDTO lookupStaffForRelease(String currentBadge, List<Integer> currentParkIds, Long releaseId, String badge);

	SmtArticlesRelease getReleaseForAuthorizedUser(String currentBadge, List<Integer> currentParkIds, Long releaseId);

	List<SmtArticlesReleasePerson> queryPerson(String badge);

	Boolean savePerson(ReleaseApplyPersonDetail personDetail);

	Boolean deletePerson(Long id);

	List<SmtArticlesReleaseThing> queryThing(String badge);

	Boolean saveThing(ReleaseApplyThingDetail thingDetail);

	Boolean deleteThing(Long id);

	/**
	 * 导出许昌办公区物品放行OA记录
	 * @return
	 */
	ResponseEntity<byte[]> exportOfficeReleaseOARecord();
}
