package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.dto.req.VisitorAgainReqDTO;
import com.tce.smart.platform.api.dto.req.VisitorRecordReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppSmtVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SaveSmtVisitor;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.SearchAppSmtVisitorVO;
import com.tce.smart.platform.core.vo.SearchAppVisitorDetailVO;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import com.tce.smart.platform.service.SmtSnapVehicleService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.tool.enums.AdmittanceVehicleTypeEnum;
import com.tce.smart.tool.enums.HfVisitCarryItemsEnum;
import com.tce.smart.tool.enums.VisitorEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 访客预约记录控制器
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Api(tags = "访客管理")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/visitor")
public class SmtVisitorController extends BaseController {

  @Autowired
  private final SmtVisitorService smtVisitorService;
  @Autowired
  private final SmtSnapVehicleService smtSnapVehicleService;



  /**
   * 分页查询
   * @param page 分页对象
   * @param searchSmtVisitorDTO 访客表
   * @return
   */
  @GetMapping(value = "/page")
  public Result<IPage<SearchSmtVisitorVO> > getSmtVisitorPage(Page page,  SearchSmtVisitorDTO searchSmtVisitorDTO) {
	  return  new Result<>(smtVisitorService.getSmtVisitorPage(page,searchSmtVisitorDTO));
  }

	/**
	 * 分页查询预约记录
	 * @return
	 */
	@GetMapping("/record/list")
	public Result<IPage<VisitorListRespDTO>> wechatGetVisitRecord(VisitorRecordReqDTO visitorRecordReqDTO){
		return new Result<>(smtVisitorService.getVisitRecord(new Page(visitorRecordReqDTO.getCurrent(),visitorRecordReqDTO.getSize()),visitorRecordReqDTO.getVisitorPhone()));
	}

	/**
	 * 修改访客状态表 访客审核
	 * @param smtVisitor 访客表
	 * @return Result
	 */
	@SysLog("修改访客状态")
	@PostMapping("/app/updateVisitorStatus")
	public Result updateVisitorStatus(@RequestBody SmtVisitor smtVisitor){
		return new Result<>(smtVisitorService.updateVisitorStatusById(smtVisitor));
	}

	/**
	 * 判断访客的被访人是否存在
	 * @param smtVisitor 访客表
	 * @return Result
	 */
	@SysLog("判断访客的被访人是否存在")
	@PostMapping("/app/searchReceptionist")
	public Result<SmtVisitorDTO> searchReceptionist(@RequestBody SmtVisitor smtVisitor) {
		SmtVisitor smtVisitorRs = smtVisitorService.searchReceptionist(smtVisitor);
		SmtVisitorDTO smtVisitorDTO = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitorRs, smtVisitorDTO);
		return success(smtVisitorDTO);
	}

	/**
	 * 判断访客的被访人是否存在
	 * @param smtVisitorDTO 访客表
	 * @return Result
	 */
	@SysLog("判断访客的被访人是否存在")
	@PostMapping("/wechat/searchReceptionist")
	public Result<SmtVisitorDTO> searchReceptionistForApp(@RequestBody SmtVisitorDTO smtVisitorDTO) {
		SmtVisitor smtVisitor = new SmtVisitor();
		BeanUtils.copyProperties(smtVisitorDTO, smtVisitor);
		SmtVisitor smtVisitorRs = smtVisitorService.searchReceptionistForApp(smtVisitor);
		SmtVisitorDTO smtVisitorDTORes = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitorRs, smtVisitorDTORes);
		return success(smtVisitorDTORes);
	}

	/**
	 * 新增添加访客
	 * @param saveSmtVisitor
	 * @return Result
	 */
	@SysLog("新增访客信息")
	@PostMapping("/addVisitor")
	public Result<SmtVisitorDTO> addSmtVisitor(@RequestBody SaveSmtVisitor saveSmtVisitor) {
		SmtVisitor smtVisitor = smtVisitorService.saveSmtVisitor(saveSmtVisitor);
		SmtVisitorDTO smtVisitorDTO = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitor, smtVisitorDTO);
		return success(smtVisitorDTO);
	}

	/**
	 * 新增公众号添加访客
	 * @param saveWechatSmtVisitor
	 * @return Result
	 */
	@SysLog("新增访客信息")
	@PostMapping("/addWechatVisitor")
	public Result<SmtVisitorDTO> addWechatVisitor(@RequestBody SaveWechatSmtVisitorDTO saveWechatSmtVisitor) {
		SmtVisitor smtVisitor = smtVisitorService.saveWechatSmtVisitor(saveWechatSmtVisitor);
		SmtVisitorDTO smtVisitorDTO = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitor, smtVisitorDTO);
		return success(smtVisitorDTO);
	}

	/**
	 * 微信公众号预约 再约一次
	 * @param visitorAgainReqDTO
	 * @return Result
	 */
	@PostMapping("/wechatVisitorAgain")
	public Result<Boolean> wechatVisitorAgain(@RequestBody VisitorAgainReqDTO visitorAgainReqDTO) {
		return success(smtVisitorService.wechatSmtVisitorAgain(visitorAgainReqDTO));
	}

	/**
	 * 公众号新增随行人员
	 * @param addWechatFellowVisitorDTO
	 * @return Result
	 */
	@SysLog("新增访客随行人员信息")
	@PostMapping("/addWechatFellowVisitor")
	public Result addWechatFellowVisitor( @RequestBody AddWechatFellowVisitorDTO addWechatFellowVisitorDTO){
		smtVisitorService.addWechatFellowVisitor(addWechatFellowVisitorDTO);
		return success();
	}
	/**
	 * 新增随行人员
	 * @param addFellowVisitorDTO
	 * @return Result
	 */
	@SysLog("新增访客随行人员信息")
	@PostMapping("/addFellowVisitor")
	public Result addFellowVisitor( @RequestBody AddFellowVisitorDTO addFellowVisitorDTO){
		smtVisitorService.addFellowVisitor(addFellowVisitorDTO);
		return success();
	}

	/**
	 * 通过id查询访客的详情
	 * @param id id
	 * @return Result
	 */
	@SysLog("通过id查询访客的详情")
	@SuppressWarnings("rawtypes")
	@GetMapping("/searchVisitorDetail/{id}")
	public Result searchVisitorDetail(@PathVariable("id") Long id){
		return new Result<>(smtVisitorService.searchVisitorDetailById(id, smtSnapVehicleService));
	}
	/**
	 * app端通过id查询访客的详情
	 * @param id id
	 * @return Result
	 */
	@SysLog("app端通过id查询访客的详情")
	@SuppressWarnings("rawtypes")
	@GetMapping("/app/searchAppVisitorDetail/{id}")
	public Result<SearchAppVisitorDetailRespDTO> searchAppVisitorDetail(@PathVariable("id") Long id) {
		SearchAppVisitorDetailVO searchAppVisitorDetailVO = smtVisitorService.searchAppVisitorDetailById(id);
		SearchAppVisitorDetailRespDTO searchAppVisitorDetailRespDTO = new SearchAppVisitorDetailRespDTO();
		BeanUtils.copyProperties(searchAppVisitorDetailVO, searchAppVisitorDetailRespDTO);
		return success(searchAppVisitorDetailRespDTO);
	}
	/**
	 * app端通过查询访客的信息
	 * @param searchAppVisitorDTO
	 * @return Result
	 */
	@SysLog("app端通过查询访客的信息")
	@GetMapping("/app/searchAppVisitorPage")
	public Result<Page<SearchAppSmtVisitorRespDTO>> searchAppVisitorPage(Page page, SearchAppVisitorDTO searchAppVisitorDTO) {
		Page<SearchAppSmtVisitorRespDTO> newPage = new Page<>();
		List<SearchAppSmtVisitorRespDTO> newList = new ArrayList<>();

		//转换Bean
		IPage<SearchAppSmtVisitorVO> tempPgae = smtVisitorService.searchAppVisitorPage(page, searchAppVisitorDTO);
		if (Objects.nonNull(tempPgae) && CollectionUtils.isNotEmpty(tempPgae.getRecords())) {
			SearchAppSmtVisitorRespDTO searchAppSmtVisitorRespDTO;
			for (SearchAppSmtVisitorVO element : tempPgae.getRecords()) {
				searchAppSmtVisitorRespDTO = new SearchAppSmtVisitorRespDTO();
				BeanUtils.copyProperties(element, searchAppSmtVisitorRespDTO);
				newList.add(searchAppSmtVisitorRespDTO);
			}

			newPage.setCurrent(tempPgae.getCurrent());
			newPage.setSize(tempPgae.getSize());
			newPage.setTotal(tempPgae.getTotal());
			newPage.setPages(tempPgae.getPages());
		}

		newPage.setRecords(newList);

		return success(newPage);
	}

	/**
	 * app端查询该员工为审核的个数
	 * @param searchAppVisitorCountDTO
	 * @return Result
	 */
	@SysLog("app端查询该员工为审核的个数")
	@GetMapping("/app/searchAppVisitorCount")
	public Result searchAppVisitorCount(SearchAppVisitorCountDTO searchAppVisitorCountDTO){
		return new Result<>(smtVisitorService.searchAppVisitorCount(searchAppVisitorCountDTO));
	}

	  /**
	   * 查询当天的信息
	   * @param page 分页对象
	   * @param page 访客表p
	   * @return
	   * */
	  @GetMapping("/searchTodayVisitor")
	  public Result getTodayVisitor(Page page) {
		    return new Result<>(smtVisitorService.getTodayVisitor(page));
	  }


	  /**
	   * 查询最新条抓拍数据的信息
	   * @return
	   */
	  @GetMapping("/searchNewSnapVisitor")
	  public Result getNewSnapVisitor() {
		  return new Result<>(smtVisitorService.getNewSnapVisitor());
	  }

	  /**
	   * 查询当天的时间段的访客进厂数据人数
	   * @return
	   */
	  @GetMapping("/searchVisitorInToday")
	  public Result searchComeInToday() {
		  return new Result<>(smtVisitorService.searchComeInToday());
	  }
	  /**
	   * 查询当天的时间段的访客出厂数据人数
	   * @return
	   */
	  @GetMapping("/searchVisitorOutToday")
	  public Result searchVisitorOutTaday() {
		  return new Result<>(smtVisitorService.searchVisitorOutToday());
	  }
	  /**
	   * 查询当天的访客分析数据
	   * @return
	   */
	  @GetMapping("/searchVisitorAnalysisToday/{parkId}")
	  public Result searchVisitorAnalysisToday(@PathVariable("parkId") Integer parkId) {
		  return new Result<>(smtVisitorService.searchVisitorAnalysisToday(parkId));
	  }
	  /**
	   * 查询当天的访客分析数据
	   * @return
	   */
	  @GetMapping("/searchVisitorDeviceToday")
	  public Result searchVisitorDeviceToday() {
		  return new Result<>(smtVisitorService.searchVisitorDeviceToday());
	  }
	  /**
	   * 查询当天的访客分析数据
	   * @return
	   */
	  @GetMapping("/searchVisitorDeviceAnalysisToday")
	  public Result searchVisitorDeviceAnalysisToday() {
		  return new Result<>(smtVisitorService.searchVisitorDeviceAnalysisToday());
	  }

//	  @GetMapping("/getOvertime")
//	  public Result getOvertime() {
//		 visitorTimingTaskService.VisitorOverTime();
//		 visitorTimingTaskService.VisitorArrive();
//		 visitorTimingTaskService.VisitorSengMsg();
//		  return success();
//	  }
	  /**
	   * 查询访客拒绝原因
	   * @return
	   */
	  @GetMapping("/refuseType")
	  public Result getVisitorRefuseType() {
		  return smtVisitorService.getVisitorRefuseType();
	  }


	/**
	 * 获取访客以及随行人员信息
	 */
	@GetMapping("/searchVisitorById/{id}")
	public Result searchVisitorById(@PathVariable("id") Long id){
		return new Result<>(smtVisitorService.searchVisitorById(id));
	}

	/**
	 * 获取访客以及随行人员信息
	 */
	@GetMapping("/searchVisitorByCode/{code}")
	public Result searchVisitorByCode(@PathVariable("code") String code){
		return new Result<>(smtVisitorService.searchVisitorByCode(code));
	}

	@GetMapping("/delSmsCode/{id}")
	public Result delSmsCode(@PathVariable("id") Long id) {
		return new Result<>(smtVisitorService.delSmsCode(id));
	}

	@GetMapping("/smbPutPhoto/{id}")
	public Result smbPutPhoto(@PathVariable("id") Long id) {
		return new Result<>(smtVisitorService.smbPutPhoto(id));
	}

	/**
	 * 重新下发访客设备通行权限
	 * @param id 记录id
	 */
	@ApiOperation("重新下发访客设备通行权限")
	@PostMapping("/repeat/visitor/auth")
	public Result<Boolean> repeatVisitorDeviceAuth(@ApiParam(name = "id",value = "访客预约记录Id",required = true) @RequestParam String id){
		return new Result<>(smtVisitorService.repeatVisitorDeviceAuth(Long.parseLong(id)));
	}

	@SysLog("新增访客信息")
	@PostMapping("/hf/saveVisitor")
	public Result<SmtVisitorDTO> saveHf(@RequestBody SaveSmtVisitor saveSmtVisitor) {
		SmtVisitor smtVisitor = smtVisitorService.saveHfVisitor(saveSmtVisitor);
		SmtVisitorDTO smtVisitorDTO = new SmtVisitorDTO();
		BeanUtils.copyProperties(smtVisitor, smtVisitorDTO);
		return success(smtVisitorDTO);
	}

	@SysLog("合肥访客邀约推送信息")
	@PostMapping("/hf/record/push")
	public Result<String> saveHfInvitation(@RequestBody SaveSmtVisitor saveSmtVisitor, @RequestParam("token") String token) {
		return success(smtVisitorService.saveHfInvitation(saveSmtVisitor, token));
	}

	@ApiOperation("HF访客预约OA携带物品枚举")
	@GetMapping("/enum/carry/type")
	public Result<List<Map<String, Object>>> getVehicleTypeEnum() {
		return success(HfVisitCarryItemsEnum.getTypeList());
	}

	@ApiOperation("HF访客的的来访事由枚举")
	@GetMapping("/enum/cause/type")
	public Result<List<Map<String, Object>>> getCauseEnum() {
		return success(VisitorEnum.getTypeList());
	}
}
