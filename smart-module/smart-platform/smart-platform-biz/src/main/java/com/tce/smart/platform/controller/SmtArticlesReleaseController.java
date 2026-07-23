package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.ArticlesReleaseDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.ArticlesReleaseListRespDTO;
import com.tce.smart.platform.api.dto.resp.BackFactoryConfirmListDTO;
import com.tce.smart.platform.api.dto.resp.OfficeReleaseDraftRespDTO;
import com.tce.smart.platform.api.dto.resp.ReleaseStaffLookupRespDTO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import com.tce.smart.platform.service.SmtArticlesReleaseService;
import com.tce.smart.tool.enums.ArticlesRemarkEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 *  物品放行
 * @Auther: guohongtai
 * @Date: 2020-07-23 16:56
 */
@RestController
@AllArgsConstructor
@RequestMapping("/articlesrelease")
@Api(value = "articles_release", tags = "物品放行")
public class SmtArticlesReleaseController extends BaseController {
	private final SmtArticlesReleaseService smtArticlesReleaseService;

	@GetMapping("/page")
	@ApiOperation(value = "公众号物品放行申请列表分页查询")
	public Result<IPage<ArticlesReleaseListRespDTO>> getArticlesReleasePage(Page page, QueryArticlesReleaseReqDTO reqDTO) {
		return success(smtArticlesReleaseService.getArticlesReleasePage(page, reqDTO), ArticlesReleaseListRespDTO.class);
	}

	@GetMapping("/office/approval/page")
	@ApiOperation(value = "办公区物品放行审批列表查询")
	public Result<IPage<ArticlesReleaseListRespDTO>> getArticlesReleasePage(Page page, OfficeZoneApproveQueryDTO queryDTO) {
		return success(smtArticlesReleaseService.getOfficeReleasePage(page, queryDTO), ArticlesReleaseListRespDTO.class);
	}

	@GetMapping("/office/page")
	@ApiOperation(value = "办公区物品放行列表查询")
	public Result<IPage<ArticlesReleaseListRespDTO>> getOfficeReleasePage(Page page, QueryArticlesReleaseReqDTO reqDTO) {
		return success(smtArticlesReleaseService.getPCOfficePage(page, reqDTO), ArticlesReleaseListRespDTO.class);
	}

	@GetMapping("/office/export")
	@ApiOperation(value = "办公区物品放行Excel导出")
	public ResponseEntity<byte[]> exportOfficeReleaseOARecord() {
		return smtArticlesReleaseService.exportOfficeReleaseOARecord();
	}

	@GetMapping("/back/page")
	@ApiOperation(value = "物品返厂确认分页查询")
	public Result<IPage<BackFactoryConfirmListDTO>> getBackFactoryPage(Page page, ArticlesBackFactoryReqDTO reqDTO) {
		return success(smtArticlesReleaseService.getBackFactoryPage(page, reqDTO), BackFactoryConfirmListDTO.class);
	}

	@GetMapping("/detail/{id}")
	@ApiOperation(value = "根据放行ID查询生活区物品放行详情")
	public Result<ArticlesReleaseDetailRespDTO> getById(@PathVariable("id") Long id){
		SmartUser currentUser = currentAuthenticatedUser();
		return success(smtArticlesReleaseService.getReleaseForAuthorizedUser(
				currentUser.getUsername(), currentUser.getParkIdList(), id), ArticlesReleaseDetailRespDTO.class);
	}

	@GetMapping("/detail/approveId/{id}")
	@ApiOperation(value = "根据审批ID查询生活区物品放行详情")
	public Result<ArticlesReleaseDetailRespDTO> getByApproveId(@PathVariable("id") String approveId){
		SmtArticlesRelease release = smtArticlesReleaseService.getByApproveId(approveId);
		SmartUser currentUser = currentAuthenticatedUser();
		return success(smtArticlesReleaseService.getReleaseForAuthorizedUser(
				currentUser.getUsername(), currentUser.getParkIdList(), release.getId()), ArticlesReleaseDetailRespDTO.class);
	}

	@Inner
	@GetMapping("/app/status/update")
	@ApiOperation(value = "石岩APP物品放行审批状态更新")
	public Result<Boolean> statusForApp(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge, @RequestParam("status") Integer status, @RequestParam(value = "remark", required = false) String remark){
		return success(smtArticlesReleaseService.status(id, approveBadge, status, remark));
	}

	@Inner
	@GetMapping("/status/update")
	@ApiOperation(value = "许昌物品放行审批状态更新")
	public Result<Boolean> status(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge, @RequestParam("status") Integer status, @RequestParam(value = "remark", required = false) String remark){
		return success(smtArticlesReleaseService.livingStatusUpdate(id, approveBadge, status, remark));
	}

	/**
	 * 保安放行
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/status/security/update")
	@ApiOperation(value = "保安放行")
	public Result<Boolean> securityUpdate(@RequestBody GuardReleaseConfirmReqDTO reqDTO){
		SmartUser currentUser = currentAuthenticatedUser();
		SmtArticlesRelease release = smtArticlesReleaseService.getReleaseForAuthorizedUser(
				currentUser.getUsername(), currentUser.getParkIdList(), reqDTO.getId());
		// 保安身份与图片所属园区只能由认证主体和持久化申请记录决定。
		reqDTO.setBadge(currentUser.getUsername());
		reqDTO.setParkId(release.getParkId());
		return success(smtArticlesReleaseService.securityUpdate(reqDTO));
	}

	@PostMapping("/back/confirm/{releaseId}")
	@ApiOperation(value = "返厂确认")
	public Result<Boolean> securityBackConfirm(@PathVariable("releaseId") Long releaseId) {
		SmartUser currentUser = currentAuthenticatedUser();
		smtArticlesReleaseService.getReleaseForAuthorizedUser(currentUser.getUsername(), currentUser.getParkIdList(), releaseId);
		return success(smtArticlesReleaseService.securityBackConfirm(releaseId));
	}

	/**
	 * 石岩APP生活区物品放行
	 * @param reqDTO
	 * @return
	 */
	@Inner
	@PostMapping("/save")
	@ApiOperation(value = "石岩APP生活区物品放行")
	public Result<Boolean> saveLiving(@RequestBody AddArticlesReleaseReqDTO reqDTO){
		return success(smtArticlesReleaseService.saveArticlesRelease(reqDTO));
	}

	/**
	 * 许昌公众号生活区物品放行
	 * @param reqDTO
	 * @return
	 */
	@Inner
	@PostMapping("/living/save")
	@ApiOperation(value = "许昌公众号生活区物品放行")
	public Result<Boolean> saveLivingForXC(@RequestBody AddArticlesReleaseReqDTO reqDTO){
		return success(smtArticlesReleaseService.saveLivingArticlesRelease(reqDTO));
	}

	/**
	 * 许昌办公区物品放行，公众号入口
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/office/save")
	@ApiOperation(value = "办公区物品放行")
	public Result<Boolean> saveOffice(@RequestBody OfficeZoneReleaseReqDTO reqDTO) {
		SmartUser currentUser = currentAuthenticatedUser();
		return success(smtArticlesReleaseService.saveOfficeArticlesRelease(
				currentUser.getUsername(), currentUser.getParkIdList(), reqDTO));
	}

	@PostMapping("/office/draft")
	@ApiOperation(value = "创建办公区物品放行草稿")
	public Result<OfficeReleaseDraftRespDTO> createOfficeDraft(@RequestBody @Valid CreateOfficeReleaseDraftReqDTO request) {
		SmartUser currentUser = currentAuthenticatedUser();
		if (currentUser.getParkIdList() == null || !currentUser.getParkIdList().contains(request.getParkId())) {
			throw new AccessDeniedException("无权在该园区创建物品放行草稿");
		}
		return success(smtArticlesReleaseService.createOfficeDraft(currentUser.getUsername(), request));
	}

	@GetMapping("/{releaseId}/staff/lookup")
	@ApiOperation(value = "按物品放行草稿查询人员")
	public Result<ReleaseStaffLookupRespDTO> lookupStaffForRelease(@PathVariable Long releaseId,
			@RequestParam String badge) {
		SmartUser currentUser = currentAuthenticatedUser();
		return success(smtArticlesReleaseService.lookupStaffForRelease(
				currentUser.getUsername(), currentUser.getParkIdList(), releaseId, badge));
	}

	@Inner
	@GetMapping("/list")
	@ApiOperation(value = "物品放行记录列表查询")
	public Result<IPage<ArticlesReleaseListRespDTO>> getRecord(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestParam("badge") String badge, @RequestParam(value = "status", required = false) Integer status) {
		if(status == 0){
			//待审核
			return success(smtArticlesReleaseService.page(new Page<>(current, size), Wrappers.<SmtArticlesRelease>query()
					.lambda().eq(SmtArticlesRelease::getBadge, badge).eq(SmtArticlesRelease::getStatus, 1).orderByDesc(SmtArticlesRelease::getCreateTime)), ArticlesReleaseListRespDTO.class);
		}else if(status == 1){
			//待审核以外状态
			return success(smtArticlesReleaseService.page(new Page<>(current, size), Wrappers.<SmtArticlesRelease>query()
					.lambda().eq(SmtArticlesRelease::getBadge, badge).ne(SmtArticlesRelease::getStatus, 1).orderByDesc(SmtArticlesRelease::getCreateTime)), ArticlesReleaseListRespDTO.class);
		}else{
			// 所有状态
			return success(smtArticlesReleaseService.page(new Page<>(current, size), Wrappers.<SmtArticlesRelease>query()
				.lambda().eq(SmtArticlesRelease::getBadge, badge).orderByDesc(SmtArticlesRelease::getCreateTime)), ArticlesReleaseListRespDTO.class);
		}
	}

	@GetMapping("/enum/remark")
	public Result<List<Map<String, Object>>> getArticlesRemark(){
		return success(ArticlesRemarkEnum.list());
	}

	private SmartUser currentAuthenticatedUser() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可访问物品放行记录");
		}
		SmartUser currentUser = SecurityUtils.getUser(authentication);
		if (currentUser == null || currentUser.getUsername() == null || currentUser.getUsername().trim().isEmpty()) {
			throw new AccessDeniedException("未认证用户不可访问物品放行记录");
		}
		return currentUser;
	}
}
