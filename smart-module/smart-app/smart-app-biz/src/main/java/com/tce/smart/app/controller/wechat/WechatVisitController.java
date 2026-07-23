package com.tce.smart.app.controller.wechat;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.api.dto.WechatVisitorRecordDetailReqDTO;
import com.tce.smart.app.api.dto.WechatVisitorRecordReqDTO;
import com.tce.smart.app.vo.fore.VisitorDetailVo;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import com.tce.smart.app.ao.wechat.AddVisitMemberAo;
import com.tce.smart.app.ao.wechat.AddVisitorAo;
import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.ao.wechat.CheckHostAo;
import com.tce.smart.app.service.fore.VisitorService;
import com.tce.smart.app.vo.wechat.PhotoVisitorVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.SmtParkDTO;

import lombok.AllArgsConstructor;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 微信公众号访客控制器
 *
 * @author mingkai.wu
 * @date 2019-05-13 09:25:37
 */
@Api(tags = "访客预约个人中心")
@RestController
@AllArgsConstructor
@RequestMapping("/wechat/visit")
public class WechatVisitController extends BaseController {

	private VisitorService visitorService;

	/**
	 * 判断图片是否检测到了人脸
	 *
	 * @param checkFaceAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/checkFace")
	public Result<?> checkFace(
			@RequestHeader(value = "X-Visitor-Action-Capability", required = false) String capability,
			@RequestHeader(value = "X-Visitor-Draft-Id", required = false) String draftId,
			@RequestBody CheckFaceAo checkFaceAo) {
		return success(visitorService.checkFace(checkFaceAo, capability, draftId));
	}
	/**
	 * 根据图片的id获取图片的bASE64位
	 *
	 * @param photoVisitorVo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/getFace")
	public Result<?> getFace(@RequestBody PhotoVisitorVo photoVisitorVo) {
		return success(visitorService.getFace(photoVisitorVo));
	}

	/**
	 * 被访对象信息查询
	 *
	 * @param checkHostAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/checkhost")
	public Result<?> checkhost(@RequestBody CheckHostAo checkHostAo) {
		return success(visitorService.checkhost(checkHostAo));
	}

	/**
	 * 添加访客信息
	 *
	 * @param addVisitorAo 访客信息
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/add")
	public Result<?> addVisitor(@RequestBody AddVisitorAo addVisitorAo) {
		return success(visitorService.addVisitorFromWechat(addVisitorAo));
	}

	/**
	 * 园区驻场员工验证身份证招聘
	 * @param addVisitorAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/addCheck")
	public Result<?> addCheck(@RequestBody AddVisitorAo addVisitorAo) {
		return success(visitorService.addCheck(addVisitorAo));
	}

	/**
	 * 添加随行人员
	 * @param addVisitMemberAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/member/add")
	public Result<?> addVisitMember(@RequestBody AddVisitMemberAo addVisitMemberAo) {
		visitorService.addFellowVisitorWechat(addVisitMemberAo);
		return success();
	}

	/**
	 * 获取来访事由
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/reason/type")
	public Result<?> getVisitorType() {
		return success(visitorService.getVisitorType());
	}

	/**
	 * 检测访客身份证是否是黑名单
	 * @param addVisitorAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/checkBlackVisitor")
	public Result<?> checkBlackVisitor(
			@RequestHeader(value = "X-Visitor-Action-Capability", required = false) String capability,
			@RequestHeader(value = "X-Visitor-Draft-Id", required = false) String draftId,
			@RequestBody AddVisitorAo addVisitorAo) {
		return visitorService.checkBlackVisitor(addVisitorAo, capability, draftId);
	}

	/**
	 * 检测访客是否是黑名单
	 * @param addVisitorAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/checkBlackVehicle")
	public Result<?> checkBlackVehicle( @RequestBody AddVisitorAo addVisitorAo) {
		return visitorService.checkBlackVehicle(addVisitorAo);
	}


	/**
	 * 获取所有园区
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/getPark")
	public Result<List<SmtParkDTO>> getPark() {
		return success(visitorService.getPark());
	}

	/**
	 * 分页查询微信公众号预约记录
	 * @return
	 */
	@ApiOperation("分页查询微信公众号预约记录")
	@GetMapping("/record/list")
	public Result<IPage<VisitorListRespDTO>> getVisitRecord(WechatVisitorRecordReqDTO wechatVisitorRecordReqDTO){
		return new Result<>(visitorService.getVisitRecord(wechatVisitorRecordReqDTO));
	}

	/**
	 * 查询微信公众号预约记录详情
	 * @return
	 */
	@ApiOperation("查询微信公众号预约记录详情")
	@GetMapping("/record/detail")
	public Result<VisitorDetailVo> getVisitRecordDetail(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO){
		return new Result<>(visitorService.getVisitRecordDetail(wechatVisitorRecordDetailReqDTO));
	}

	/**
	 * 微信公众号预约 再约一次
	 * @return
	 */
	@ApiOperation("微信公众号预约 再约一次")
	@PostMapping("/again")
	public Result<Boolean> wechatVisitorAgain(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO){
		return new Result<>(visitorService.wechatVisitorAgain(wechatVisitorRecordDetailReqDTO));
	}

	/**
	 * 通过预约记录ID查询预约详情
	 * @return
	 */
	@ApiOperation("通过预约记录ID查询预约详情")
	@GetMapping("/record/detailById/{id}")
	public Result<VisitorDetailVo> getVisitRecordDetailById(@ApiParam(name = "id",value = "记录ID",required = true) @RequestParam Long id){
		return new Result<>(visitorService.getVisitRecordDetailById(id));
	}
}
