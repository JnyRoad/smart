package com.tce.smart.data.controller.businesstrip;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMain;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMainDt1;
import com.tce.smart.businesstrip.core.entity.CcdFormtableMainDt2;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainDt1Service;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainDt2Service;
import com.tce.smart.businesstrip.core.service.CcdFormtableMainService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.businesstrip.req.CcdFormtableMainDt1ReqDTO;
import com.tce.smart.data.api.dto.businesstrip.req.CcdFormtableMainDt2ReqDTO;
import com.tce.smart.data.api.dto.businesstrip.req.CcdFormtableMainReqDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt1RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt2RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 出差 数据库查询
 *
 * @author ly
 * @date 2019-06-28
 */
@RestController
@RequestMapping("/formtableMain")
public class FormtableMainController extends BaseController {

	@Autowired
	private CcdFormtableMainService ccdFormtableMainService;
	@Autowired
	private CcdFormtableMainDt1Service ccdFormtableMainServiceDt1;
	@Autowired
	private CcdFormtableMainDt2Service ccdFormtableMainServiceDt2;

	/**
	 * 获取出差分页信息
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@SuppressWarnings("unchecked")
	@GetMapping("/info")
	public Result<IPage<CcdFormtableMainRespDTO>> info(Page page, CcdFormtableMainReqDTO ccdFormtableMainReqDTO) {
		CcdFormtableMain queryCcdFormtableMain = new CcdFormtableMain();
		BeanUtils.copyProperties(ccdFormtableMainReqDTO,queryCcdFormtableMain);

		IPage<CcdFormtableMain> pageInfo = ccdFormtableMainService.page(page,Wrappers.query(queryCcdFormtableMain).lambda().orderByDesc(CcdFormtableMain::getApplicationTime));
		return  success(pageInfo,CcdFormtableMainRespDTO.class);

/*		return new Result<List<CcdFormtableMain>>(ccdFormtableMainService.selectList());
*/	}

	/**
	 * 获取出差分页详情信息
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/infoTravel")
	public Result<CcdFormtableMainRespDTO> infoTravel(CcdFormtableMainReqDTO ccdFormtableMainReqDTO) {
		CcdFormtableMain queryCdFormtableMain = new CcdFormtableMain();
		BeanUtils.copyProperties(ccdFormtableMainReqDTO,queryCdFormtableMain);

		CcdFormtableMain ccdFormtableMainRs =ccdFormtableMainService.getOne(Wrappers.query(queryCdFormtableMain));
		CcdFormtableMainRespDTO ccdFormtableMainRespDTO = new CcdFormtableMainRespDTO();
		BeanUtils.copyProperties(ccdFormtableMainRs,ccdFormtableMainRespDTO);

		return  success(ccdFormtableMainRespDTO);
	}

	/**
	 * 根据出差的主键id  获取出差的日程数据
	 * @param ccdFormtableMainDt1ReqDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/infoDay")
	public Result<List<CcdFormtableMainDt1RespDTO>> infoDay(CcdFormtableMainDt1ReqDTO ccdFormtableMainDt1ReqDTO) {

		QueryWrapper<CcdFormtableMainDt1> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(CcdFormtableMainDt1::getMainId, ccdFormtableMainDt1ReqDTO.getMainId());
		queryWrapper.lambda().orderByAsc(CcdFormtableMainDt1::getDepartureTime);

		List<CcdFormtableMainDt1> list = ccdFormtableMainServiceDt1.list(queryWrapper);

		return success(list, CcdFormtableMainDt1RespDTO.class);
	}
	/**
	 * 根据出差的主键id  获取出差的报告数据
	 * @param ccdFormtableMainDt2ReqDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/infoReport")
	public Result<List<CcdFormtableMainDt2RespDTO>> infoReport(CcdFormtableMainDt2ReqDTO ccdFormtableMainDt2ReqDTO) {
		CcdFormtableMainDt2 queryCcdFormtableMainDt2 = new CcdFormtableMainDt2();
		BeanUtils.copyProperties(ccdFormtableMainDt2ReqDTO,queryCcdFormtableMainDt2);

		List<CcdFormtableMainDt2> tempList = ccdFormtableMainServiceDt2.list(Wrappers.query(queryCcdFormtableMainDt2));
		return success(tempList,CcdFormtableMainDt2RespDTO.class);
	}

}
