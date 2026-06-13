package com.tce.smart.platform.controller.admittance;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyCodeDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorWechatIdentityRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaOptionsService;
import com.tce.smart.platform.service.admittance.VisitorSelfQueryService;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.ToolUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
@RestController
@Api(tags = "platform-入厂申请预约")
@AllArgsConstructor
@RequestMapping("/admittance/apply")
public class SmtAdmittanceApplyController extends BaseController {

	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;
	@Autowired
	private SmtVisitorService smtVisitorService;
	@Autowired
	private SmtAdmittanceAreaOptionsService smtAdmittanceAreaOptionsService;
	@Autowired
	private VisitorSelfQueryService visitorSelfQueryService;

	/**
	 * 新增入厂预约
	 *
	 * @param saveAdmittanceApply
	 * @return Result
	 */
	@ApiOperation("新增入厂预约")
	@PostMapping("/save/apply")
	public Result<AdmittanceApplyDetailRespDTO> addSmtVisitor(@RequestBody SaveAdmittanceApplyReqDTO saveAdmittanceApply) {
		return success(smtAdmittanceApplyService.saveAdmittanceApply(saveAdmittanceApply), AdmittanceApplyDetailRespDTO.class);
	}

	/**
	 * 新增货车预约
	 *
	 * @param saveAdmittanceApply
	 * @return Result
	 */
	@ApiOperation("新增货车预约")
	@PostMapping("/save/car/apply")
	public Result<AdmittanceApplyDetailRespDTO> addSmtCarApply(@RequestBody SaveAdmittanceCarApplyReqDTO saveAdmittanceApply) {
		return success(smtAdmittanceApplyService.saveAdmittanceCarApply(saveAdmittanceApply), AdmittanceApplyDetailRespDTO.class);
	}

	/**
	 * 获得openId
	 *
	 * @param
	 * @return Result
	 */
	@ApiOperation("获得openId")
	@GetMapping("/get/openId")
	public Result<VisitorWechatIdentityRespDTO> getOpenId(@RequestParam("code") String code) {
		return success(smtAdmittanceApplyService.getOpenId(code));
	}



	/**
	 * 被访人重复检查
	 *
	 * @param saveAdmittanceApply
	 * @return Result
	 */
	@ApiOperation("被访人重复检查")
	@PostMapping("/equal/check")
	public Result visitorEqualCheck(@RequestBody SaveAdmittanceApplyReqDTO saveAdmittanceApply) {
		return success(smtAdmittanceApplyService.visitorEqualCheck(saveAdmittanceApply));
	}


	/**
	 * 分页查询
	 *
	 * @param page                分页对象
	 * @param searchSmtVisitorDTO 访客表
	 * @return
	 */
	@ApiOperation("pc分页查询")
	@GetMapping(value = "/page")
	public Result<IPage<SearchSmtVisitorVO>> getSmtVisitorPage(Page page, SearchSmtVisitorDTO searchSmtVisitorDTO) {
		return new Result<>(smtAdmittanceApplyService.getSmtVisitorPage(page, searchSmtVisitorDTO));
	}

	/**
	 * app分页查询预约记录
	 *
	 * @return
	 */
	@ApiOperation("app分页查询预约记录")
	@GetMapping("/record/list")
	public Result<IPage<VisitorListRespDTO>> wechatGetVisitRecord(Page page, @RequestParam("phone") String phone) {
		return new Result<>(smtAdmittanceApplyService.getVisitRecord(page, phone));
	}

	@ApiOperation("访客自助查询本人入厂申请记录")
	@PostMapping("/app/listMyApply")
	public Result<VisitorSelfQueryRespDTO> listMyApply(@RequestBody(required = false) VisitorSelfQueryReqDTO request,
			@RequestHeader(value = "X-Visitor-Query-Token", required = false) String queryToken) {
		return success(visitorSelfQueryService.listMyApply(request, queryToken));
	}

	@ApiOperation("访客自助查询本人入厂申请详情")
	@GetMapping("/app/applyDetail")
	public Result<VisitorApplyRecordDetailRespDTO> applyDetail(@RequestParam("applyId") String applyId,
			@RequestHeader(value = "X-Visitor-Query-Token", required = false) String queryToken) {
		return success(visitorSelfQueryService.getApplyDetail(applyId, queryToken));
	}

	@ApiOperation("访客自助查询本人入厂申请审批进度")
	@GetMapping("/app/approvalProgress")
	public Result<VisitorApprovalProgressRespDTO> approvalProgress(@RequestParam("applyId") String applyId,
			@RequestHeader(value = "X-Visitor-Query-Token", required = false) String queryToken) {
		return success(visitorSelfQueryService.getApprovalProgress(applyId, queryToken));
	}

	/**
	 * 判断访客的被访人是否存在
	 *
	 * @param smtVisitor 访客表
	 * @return Result
	 */
	@ApiOperation("判断访客的被访人是否存在")
	@SysLog("判断访客的被访人是否存在")
	@PostMapping("/app/searchReceptionist")
	public Result<SmtVisitorDTO> searchReceptionist(@RequestBody SmtAdmittanceApply smtVisitor) {
		SmtAdmittanceApply smtVisitorRs = smtAdmittanceApplyService.searchReceptionist(smtVisitor);
		SmtVisitorDTO smtVisitorDTO = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitorRs, smtVisitorDTO);
		return success(smtVisitorDTO);
	}

	/**
	 * 通过id查询访客的详情
	 *
	 * @param id id
	 * @return Result
	 */
	@ApiOperation("通过id查询预约的详情")
	@SysLog("通过id查询预约的详情")
	@SuppressWarnings("rawtypes")
	@GetMapping("/search/Detail/{id}")
	public Result searchVisitorDetail(@PathVariable("id") String id) {
		return success(smtAdmittanceApplyService.searchDetailById(Long.parseLong(id)), AdmittanceApplyDetailRespDTO.class);
	}

	/**
	 * 获取访客以及随行人员信息
	 */
	@GetMapping("/searchVisitorByCode/{code}")
	public Result searchVisitorByCode(@PathVariable("code") String code) {
		return success(smtAdmittanceApplyService.searchVisitorByCode(code), AdmittanceApplyCodeDetailRespDTO.class);
	}

	/**
	 * 刷身份证获得访客信息
	 */
	@GetMapping("/search/byCard/{idCard}")
	public Result searchByIdCard(@PathVariable("idCard") String idCard) {
		return success(smtAdmittanceApplyService.getByIdCard(idCard), AdmittanceApplyDetailRespDTO.class);
	}

	/**
	 * 刷身份证获得访客信息
	 */
	@GetMapping("/search/byCard/new/{idCard}")
	public Result getLastByIdCard(@PathVariable("idCard") String idCard) {
		return success(smtAdmittanceApplyService.getLastByIdCard(idCard), AdmittanceApplyCodeDetailRespDTO.class);
	}

	/**
	 * 删除访客设备通行权限
	 * @param id 记录id
	 */
	@ApiOperation("删除访客设备通行权限")
	@PostMapping("/del/auth")
	public Result<Boolean> delVisitorDeviceAuth(@ApiParam(name = "id",value = "访客预约记录Id",required = true) @RequestParam String id){
		return new Result<>(smtAdmittanceApplyService.delDeviceAuth(Long.parseLong(id)));
	}


	/**
	 * 重新下发访客设备通行权限
	 *
	 * @param id 记录id
	 */
	@ApiOperation("重新下发设备通行权限")
	@PostMapping("/repeat/auth")
	public Result<Boolean> repeatVisitorDeviceAuth(@ApiParam(name = "id", value = "访客预约记录Id", required = true) @RequestParam String id) {
		return new Result<>(smtAdmittanceApplyService.repeatVisitorDeviceAuth(Long.parseLong(id)));
	}

	@ApiOperation("携带类型枚举")
	@GetMapping("/enum/carry")
	public Result<List<Map<String, Object>>> getCarryItemsEnum() {
		return success(AdmittanceCarryItemsEnum.getTypeList());
	}

	@ApiOperation("OA货车预约申请事由枚举")
	@GetMapping("/enum/car/cause")
	public Result<List<Map<String, Object>>> getCarCauseEnum() {
		return success(AdmittanceCarCauseEnum.getTypeList());
	}

	@ApiOperation("OA入厂申请事由枚举")
	@GetMapping("/enum/cause")
	public Result<List<Map<String, Object>>> getCauseEnum() {
		return success(AdmittanceCauseEnum.getTypeList());
	}

	@ApiOperation("OA人员证件类型枚举")
	@GetMapping("/enum/person/cert")
	public Result<List<Map<String, Object>>> getPersonCertType() {
		return success(AdmittancePersonCertTypeEnum.getTypeList());
	}

	@ApiOperation("H5入厂申请区域选项")
	@GetMapping("/app/area-options")
	public Result<AdmittanceAreaOptionsRespDTO> getAreaOptions(@ApiParam(name = "parkId", value = "园区ID", required = true) @RequestParam("parkId") Integer parkId) {
		return success(smtAdmittanceAreaOptionsService.getAreaOptions(parkId));
	}

	@ApiOperation("OA人员证件类型枚举")
	@GetMapping("/enum/factory/type")
	public Result<List<Map<String, Object>>> getPersonCertType(@RequestParam(value = "flag",required = false) Integer flag) {
		return success(AdmittanceOaAreaEnum.getType(flag));
	}

	@ApiOperation("OA车辆证件类型枚举")
	@GetMapping("/enum/vehicle/cert")
	public Result<List<Map<String, Object>>> getVehicleCertTypeEnum() {
		return success(AdmittanceVehicleCertTypeEnum.getTypeList());
	}

	@ApiOperation("OA入厂申请车辆类型枚举")
	@GetMapping("/enum/vehicle/type")
	public Result<List<Map<String, Object>>> getVehicleTypeEnum() {
		return success(AdmittanceVehicleTypeEnum.getTypeList());
	}

	@ApiOperation("车辆颜色枚举")
	@GetMapping("/enum/vehicle/color")
	public Result<List<Map<String, Object>>> getVehicleColorEnum() {
		return success(VehicleColorEnum.getTypeList());
	}

	/**
	 * 检查已到达并且访问时间早于当前时间的访客并删除
	 *
	 * @return
	 */
	@Inner
	@GetMapping("/comeOnTime")
	public Result visitorComeOnTime() {
		smtAdmittanceApplyService.visitorComeOnTime();
		return success();
	}

	@Inner
	@GetMapping("/update/oa")
	public Result updateOaStatusTask() {
		smtAdmittanceApplyService.updateOaStatusTask();
		smtVisitorService.updateOaStatusTask();
		return success();
	}

	/**
	 * 访客超时提示
	 *
	 * @return
	 */
	@Inner
	@GetMapping("/overTime")
	public Result visitorOverTime() {
		smtAdmittanceApplyService.visitorOverTime();
		return success();
	}

	/**
	 * 超时未离开
	 *
	 * @return
	 */
	@Inner
	@GetMapping("/overTimeNoLeave")
	public Result overTimeNoLeave() {
		smtAdmittanceApplyService.visitorOverTimeNoLeave();
		return success();
	}

	/**
	 * 访客未到达时发送提示
	 */
	@Inner
	@GetMapping("/remind")
	public Result visitorRemind() {
		smtAdmittanceApplyService.visitorRemind();
		return success();
	}

	@GetMapping("/smbPutPhoto/{id}")
	public Result smbPutPhoto(@PathVariable("id") Long id) {
		return success(smtAdmittanceApplyService.smbPutPhoto(id));
	}

}
