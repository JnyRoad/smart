package com.tce.smart.data.controller.ehrview;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import com.tce.smart.dhrview.core.service.YutoDhrPsndoService;
import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceDetails;
import com.tce.smart.ehrview.core.service.IOvwYsCallOwanceDetailsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

/**
 * 补贴控制器
 * @author QIPEI
 *
 */
@RestController
@RequestMapping("/ovwYsCallOwanceDetail")
public class OvwYsCallOwanceDetailsController extends BaseController {

	@Autowired
	private  IOvwYsCallOwanceDetailsService service;

	@Autowired
	private YutoDhrPsndoService yutoDhrPsndoService;

	@SysLog("查询宿补贴")
	@Inner
    @GetMapping("/get")
    public Result<OvwYsCallOwanceDetailsDTO> getInfo(@RequestParam("badge") String badge, @RequestParam("xtype") Integer xtype){
		YutoDhrPsndo yutoDhrPsndo = yutoDhrPsndoService.getByBadge(badge);
		if (Objects.isNull(yutoDhrPsndo) || StrUtil.isBlank(yutoDhrPsndo.getGlbdef28()) || "N".equals(yutoDhrPsndo.getGlbdef28())) {
			//没有福利数据 该员工已离职
			return new Result<OvwYsCallOwanceDetailsDTO>();
		}
//		OvwYsCallOwanceDetails ovwYsCallOwanceDetails = service.getOne(Wrappers.<OvwYsCallOwanceDetails> query().lambda().eq(OvwYsCallOwanceDetails::getBadge, badge).eq(OvwYsCallOwanceDetails::getXtype, xtype));
//		if(null == ovwYsCallOwanceDetails){
//			//没有福利数据 该员工已离职
//			return new Result<OvwYsCallOwanceDetailsDTO>();
//		}
		OvwYsCallOwanceDetails ovwYsCallOwanceDetails = new OvwYsCallOwanceDetails();
		ovwYsCallOwanceDetails.setId(1);
		ovwYsCallOwanceDetails.setEid(1);
		ovwYsCallOwanceDetails.setBadge(badge);
		ovwYsCallOwanceDetails.setXtype(xtype);
		ovwYsCallOwanceDetails.setBegindate(new Date());
		ovwYsCallOwanceDetails.setEnddate(new Date());
		ovwYsCallOwanceDetails.setAmount(1.1);
		ovwYsCallOwanceDetails.setComputationrule(1);
		ovwYsCallOwanceDetails.setConvertrule(1);

		OvwYsCallOwanceDetailsDTO ovwYsCallOwanceDetailsDTO = new OvwYsCallOwanceDetailsDTO();
		BeanUtils.copyProperties(ovwYsCallOwanceDetails,ovwYsCallOwanceDetailsDTO);
        return success(ovwYsCallOwanceDetailsDTO);
    }

	@SysLog("根据当前月份查询宿补贴")
	@Inner
	@GetMapping("/time/get")
	public Result<Boolean> getInfoByTime(@RequestParam("badge") String badge, @RequestParam("xtype") Integer xtype){
		OvwYsCallOwanceDetails ovwYsCallOwanceDetails = service.getOne(Wrappers.<OvwYsCallOwanceDetails> query().lambda()
				.eq(OvwYsCallOwanceDetails::getBadge, badge)
				.eq(OvwYsCallOwanceDetails::getXtype, xtype)
				.eq(OvwYsCallOwanceDetails::getBegindate, DateUtil.beginOfMonth(new Date()) ));
		if(Objects.nonNull(ovwYsCallOwanceDetails)){
			return success(Boolean.TRUE);
		}
		return success(Boolean.FALSE);
	}

	@SysLog("根据当前月份查询宿补贴")
	@Inner
	@GetMapping("/time/get/list")
	public Result<List<OvwYsCallOwanceDetailsDTO>> getInfoByTimeList(@RequestParam("xtype") Integer xtype){
		List<OvwYsCallOwanceDetails> ovwYsCallOwanceDetails = service.list(Wrappers.<OvwYsCallOwanceDetails> query().lambda()
				.eq(OvwYsCallOwanceDetails::getXtype, xtype));
		if(Objects.isNull(ovwYsCallOwanceDetails)){
			return success(new ArrayList<>());
		}
		List<OvwYsCallOwanceDetailsDTO> ovwYsCallOwanceDetailsDTOS = new ArrayList<>();
		ovwYsCallOwanceDetails.forEach(ovw -> {
			OvwYsCallOwanceDetailsDTO ovwYsCallOwanceDetailsDTO = new OvwYsCallOwanceDetailsDTO();
			BeanUtils.copyProperties(ovw,ovwYsCallOwanceDetailsDTO);
			ovwYsCallOwanceDetailsDTOS.add(ovwYsCallOwanceDetailsDTO);
		});

		return success(ovwYsCallOwanceDetailsDTOS);
	}

	@SysLog("查询是否存在住宿补贴")
	@Inner
	@GetMapping("get/byBadge")
	public Result<Boolean> getInfoByBadge(@RequestParam("badge") String badge){

		OvwYsCallOwanceDetails ovwYsCallOwanceDetails = service.getByBadge(badge);
		if(Objects.nonNull(ovwYsCallOwanceDetails)){
			return success(Boolean.TRUE);
		}
		return success(Boolean.FALSE);
	}

}
