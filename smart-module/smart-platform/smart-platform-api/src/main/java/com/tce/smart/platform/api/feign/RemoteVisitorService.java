package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SaveSmtVisitorDTO;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.dto.req.AddFellowVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.AddWechatFellowVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.SaveWechatSmtVisitorReqDTO;
import com.tce.smart.platform.api.dto.req.VisitorAgainReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppSmtVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 访客
 * @author 梁圆
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteVisitorService {

	/**
	 * 添加随行访客的信息
	 * @param addFellowVisitorDTO
	 * @return
	 */
	@PostMapping("/visitor/addFellowVisitor")
	Result addFellowVisitor(@RequestBody AddFellowVisitorReqDTO addFellowVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 公众号添加随行访客的信息
	 * @param addWechatFellowVisitorDTO
	 * @return
	 */
	@PostMapping("/visitor/addWechatFellowVisitor")
	Result addWechatFellowVisitor(@RequestBody AddWechatFellowVisitorReqDTO addWechatFellowVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 公众号添加访客的信息
	 * @param saveWechatSmtVisitorReqDTO saveWechatSmtVisitorReqDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/visitor/addWechatVisitor")
	Result<SmtVisitorDTO> addWechatVisitor(@RequestBody SaveWechatSmtVisitorReqDTO saveWechatSmtVisitorReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 微信公众号预约 再约一次
	 * @param id
	 * @param from from/visitor/addWechatVisitor
	 * @return
	 */
	@PostMapping("/visitor/wechatVisitorAgain")
	Result<Boolean> wechatVisitorAgain(@RequestBody VisitorAgainReqDTO visitorAgainReqDTO, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 添加访客的信息
	 * @param saveSmtVisitorDTO saveSmtVisitorDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/visitor/addVisitor")
	Result<SmtVisitorDTO> addSmtVisitor(@RequestBody SaveSmtVisitorDTO saveSmtVisitorDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查询访客的列表信息
	 * @param current,size,staffBadge
	 * @return
	 */
	@GetMapping("/visitor/app/searchAppVisitorPage")
	Result<Page<SearchAppSmtVisitorRespDTO>> searchAppVisitorPage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestParam("visitListType") final Integer visitListType, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查询员工未审批的
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/visitor/app/searchAppVisitorCount")
	Result searchAppVisitorCount(@RequestParam("staffBadge") final String staffBadge,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *查询访客的详细信息
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/visitor/app/searchAppVisitorDetail/{id}")
	Result<SearchAppVisitorDetailRespDTO> searchAppVisitorDetail(@PathVariable("id") Long id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *修改
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/app/updateVisitorStatus")
	Result updateVisitorStatus(@RequestBody SmtVisitorDTO smtVisitor, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *查询被访人的信息
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/app/searchReceptionist")
	Result<SmtVisitorDTO> SearchReceptionist(@RequestBody SmtVisitorDTO smtVisitor,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *查询被访人的信息
	 *
	 * @param smtVisitor
	 * @return
	 */
	@PostMapping("/visitor/wechat/searchReceptionist")
	Result<SmtVisitorDTO> SearchReceptionistForWechat(@RequestBody SmtVisitorDTO smtVisitor,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 检测车牌号是否是黑名单
	 * @param smtVisitor
	 * @param fromIn
	 * @return
	 */
	@PostMapping("/visitor/checkBlackVehicle")
	Result<?> checkBlackVehicle(@RequestBody SmtVisitorDTO smtVisitor, @RequestHeader(SecurityConstants.FROM) String fromIn);

	/**
	 * 检测身份证号是否是黑名单
	 * @param smtVisitor
	 * @param fromIn
	 * @return
	 */
	@PostMapping("/visitor/checkBlackVisitor")
	Result<?> checkBlackVisitor(@RequestBody SmtVisitorDTO smtVisitor, @RequestHeader(SecurityConstants.FROM) String fromIn);

	/**
	 * 查询拒绝访客的原因
	 * @return
	 */
	@GetMapping("/visitor/refuseType")
	Result getVisitorRefuseType();


	/**
	 * 微信公众号查询预约记录
	 * @param current,size,visitorPhone
	 * @return
	 */
	@GetMapping("/visitor/record/list")
	Result<Page<VisitorListRespDTO>> wechatGetVisitRecord(@RequestParam("current") Integer current, @RequestParam("size") Integer size, @RequestParam("visitorPhone") String visitorPhone, @RequestHeader(SecurityConstants.FROM) String from);


}
