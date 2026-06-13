package com.tce.smart.platform.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.AutoAllotRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.FloorCountQueryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.FloorStatisticsQueryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryRoomRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryStatisticsListRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.FloorRoomListRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.SearchDormitoryRoomDetailRespDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryRoomAttrDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.model.*;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.SmtDormitoryBedService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 园区宿舍楼中每个楼层的房间信息
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:10
 */
@Api(tags = "宿舍管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/room")
public class SmtDormitoryRoomController extends BaseController {

    private final SmtDormitoryRoomService smtDormitoryRoomService;

    private final SmtDormitoryBedService smtDormitoryBedService;

    /**
     * 分页查询
     *
     * @param page             分页对象
     * @param smtDormitoryRoom 园区宿舍楼中每个楼层的房间信息
     * @return
     */
    @GetMapping("/page")
    public Result getSmtDormitoryRoomPage(Page page, SmtDormitoryRoom smtDormitoryRoom) {
        return smtDormitoryRoomService.getSmtDormitoryRoomPage(page, smtDormitoryRoom);
    }

	@GetMapping("/list")
	public Result<List<DormitoryRoomVO>> getSmtDormitoryRoomList(SmtDormitoryRoom smtDormitoryRoom) {
		return new Result<>(smtDormitoryRoomService.getSmtDormitoryRoomList(smtDormitoryRoom));
	}

    //可视化房间信息的查询

    @PostMapping("/roomVisual")
    public Result getRoomVisual(@RequestBody String floorList) {
        return smtDormitoryRoomService.getRoomVisual(floorList);
    }

	@ApiOperation("可视化房间信息查询")
	@PostMapping("/queryRoomVisual")
	public Result<Page<SearchDormitoryRoomDetailRespDTO>> queryRoomVisual(@RequestBody SearchDormitoryRoomDetailReqDTO searchDormitoryRoomDetailReqDTO) {
		return new Result<>(smtDormitoryRoomService.queryRoomVisual(searchDormitoryRoomDetailReqDTO));
	}


	@PostMapping("/queryRoom")
	public Result<List<RoomBedRespDTO>> queryRoom(@RequestBody SearchDormitoryRoomDetailReqDTO smtDormitoryRoom) {
		return new Result<>(smtDormitoryRoomService.queryRoom(smtDormitoryRoom));
	}

	/**
	 * 通过园区ID、楼栋ID、楼层ID  查询房间列表
	 * @param smtDormitoryRoom
	 * @return
	 */
	@PostMapping("/queryRoomList")
	public Result queryRoomList(@RequestBody SmtDormitoryRoom smtDormitoryRoom) {
		Assert.notNull(smtDormitoryRoom.getParkId(),"园区不能为NULL");
		Assert.notNull(smtDormitoryRoom.getDormitoryId(),"楼栋不能为NULL");
		Assert.notNull(smtDormitoryRoom.getFloorId(),"楼层不能为NULL");
		return new Result<>(smtDormitoryRoomService.queryRoomList(smtDormitoryRoom));
	}

    /**
     * 通过id查询园区宿舍楼中每个楼层的房间信息
     *
     * @param id id
     * @return Result
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Integer id) {
        return new Result<>(smtDormitoryRoomService.getById(id));
    }

    /**
     * 新增园区宿舍楼中每个楼层的房间信息
     *
     * @param smtDormitoryRoom
     *            园区宿舍楼中每个楼层的房间信息
     * @return Result
     */
    // @SysLog("新增园区宿舍楼中每个楼层的房间信息")
    // @PostMapping
    // public Result save(@RequestBody SmtDormitoryRoom smtDormitoryRoom) {
    // return new Result<>(smtDormitoryRoomService.save(smtDormitoryRoom));
    // }

    /**
     * 修改园区宿舍楼中每个楼层的房间信息
     *
     * @param smtDormitoryRoom 园区宿舍楼中每个楼层的房间信息
     * @return Result
     */
    @SysLog("修改园区宿舍楼中每个楼层的房间信息")
    @PostMapping("updateRoom")
    public Result<Boolean> updateById(@RequestBody SmtDormitoryRoom smtDormitoryRoom) {
        return success(smtDormitoryRoomService.updateDormitoryRoomById(smtDormitoryRoom));
    }

	/**
	 * 批量修改房间属性
	 *
	 * @param smtDormitoryRoom 房间信息
	 * @return Result
	 */
	@SysLog("批量修改房间属性")
	@PostMapping("/batchUpdateAttr")
	public Result batchUpdateRoomAttr(@RequestBody SmtDormitoryRoom smtDormitoryRoom){
		return new Result<>(smtDormitoryRoomService.batchUpdateRoomAttr(smtDormitoryRoom));
	}

	/**
	 * 通过房间id列表批量修改房间属性
	 *
	 * @param dormitoryRoomAttrDTO 房间信息
	 * @return Result
	 */
	@PostMapping("/batchUpdateAttrByIds")
	public Result batchUpdateRoomAttrByIds(@RequestBody DormitoryRoomAttrDTO dormitoryRoomAttrDTO){
		return new Result<>(smtDormitoryRoomService.batchUpdateRoomAttrByIds(dormitoryRoomAttrDTO));
	}

	/**
	 * 按照房间ID列表批量修改房间的水电模板
	 *
	 * @param dormitoryRoomAttrDTO 房间信息
	 * @return Result
	 */
	@SysLog("批量修改房间的水电模板")
	@PostMapping("/batchUpdateSDTemp")
	public Result batchUpdateRoomSDTemplate(@RequestBody DormitoryRoomAttrDTO dormitoryRoomAttrDTO){
		return new Result<>(smtDormitoryRoomService.batchUpdateSDTemp(dormitoryRoomAttrDTO));
	}

	/**
	 * 通过id删除园区宿舍楼中每个楼层的房间信息
	 *
	 * @param id
	 * @return Result
	 */
     @SysLog("删除园区宿舍楼中每个楼层的房间信息")
     @PostMapping("/{id}")
     public Result removeById(@PathVariable Integer id) {
	return smtDormitoryRoomService.removeRoomById(id);
     }


    @SysLog("按楼层统计宿舍入住情况")
    @PostMapping("/count/floor")
    public Result countByFloor(@RequestBody FloorCountQueryReqDTO query) {
        List<DormitoryCountByFloor> dormitoryCountByFloors = smtDormitoryRoomService.countByFloor(query);
        return success(dormitoryCountByFloors, DormitoryCountByFloorVO.class);
    }

    @SysLog("按性别统计宿舍入住情况")
    @PostMapping("/count/sex")
    public Result countBySex(@RequestBody FloorCountQueryReqDTO query) {
        List<DormitoryCountBySex> dormitoryCountBySexes = smtDormitoryRoomService.countBySex(query);
        return success(dormitoryCountBySexes, DormitoryCountBySexVO.class);
    }

    @SysLog("按职层统计宿舍入住情况")
    @PostMapping("/count/jche")
    public Result countByjche(@RequestBody FloorCountQueryReqDTO query) {
        List<DormitoryCountJche> dormitoryCountBySexes = smtDormitoryRoomService.countByjche(query);
        return success(dormitoryCountBySexes);
    }


	@SysLog("按空床位统计宿舍入住情况")
	@PostMapping("/count/free")
	public Result countByFree(@RequestBody FloorCountQueryReqDTO query) {
		List<DormitoryCountByFloor> dormitoryCountByFloors = smtDormitoryRoomService.countByFree(query);
		return success(dormitoryCountByFloors, DormitoryCountByFloorVO.class);
	}

	@ApiOperation("按房间类型统计")
	@GetMapping("/count/room-type/{parkId}")
	public Result countRoomType(@PathVariable Integer parkId) {
		List<DormitoryRoomExt> dormitoryCountByRoomTypes = smtDormitoryRoomService.countByRoomType(parkId);
		return success(dormitoryCountByRoomTypes);
	}

    @SysLog("统计男、女入住人数、剩余床位数、总床位数")
    @GetMapping("/count/type")
    public Result countByType(@RequestParam("parkId") Integer parkId) {
        List<DormitoryCountByType> dormitoryCountByTypes = smtDormitoryRoomService.countByType(parkId);
        return success(dormitoryCountByTypes, DormitoryCountByTypeVO.class);
    }

	@SysLog("查询曾住宿舍")
	@GetMapping("/query/past/dormitory")
	public Result getDormitoryStaffHistory(@RequestParam("parkId") Integer parkId, @RequestParam("certno") String certno) {
		return success(smtDormitoryRoomService.getDormitoryStaffHistory(parkId, certno));
	}

	/**
	 * 根据园区统计入住率
	 * @param parkId
	 * @return
	 */
	@GetMapping("/count/list")
    public Result countList(@RequestParam("parkId") Integer parkId) {
        List<DormitoryCountListRespDTO> dormitoryCountList = smtDormitoryRoomService.countList(parkId);
        return new Result<>(dormitoryCountList);
    }

	/**
	 * 根据园区统计入住率
	 * @param parkId
	 * @return
	 */
	@ApiOperation("根据园区统计入住率")
	@GetMapping("/count/park/list")
	public Result<List<DormitoryCountListRespDTO>> countParkList(@RequestParam("parkId") Integer parkId) {
		List<DormitoryCountListRespDTO> dormitoryCountList = smtDormitoryRoomService.countList(parkId);
		return new Result<>(dormitoryCountList);
	}

	/**
	 * 根据楼栋统计入住率
	 * @param parkId
	 * @return
	 */
	@ApiOperation("根据楼栋统计入住率")
	@GetMapping("/count/dorm/list")
	public Result<List<DormitoryCountByBuildingRespDTO>> countDormList(@RequestParam("parkId") Integer parkId) {
		List<DormitoryCountByBuildingRespDTO> dormitoryCountList = smtDormitoryRoomService.countDormList(parkId);
		return new Result<>(dormitoryCountList);
	}


    @GetMapping("/count/floorList")
    public Result countFloor(@RequestParam("parkId") Integer dormitoryId) {
        List<DormitoryCountFloor> dormitoryCountFloor = smtDormitoryRoomService.countFloor(dormitoryId);
        //return success(dormitoryCountFloor, DormitoryCountListByBuildingVO.class);
        return new Result<>(dormitoryCountFloor);
    }
    @GetMapping("/count/building")
    public Result countBuilding(@RequestParam("parkId") Integer parkId) {
        List<DormitoryCountBuilding> dormitoryCountBuilding = smtDormitoryRoomService.countBuilding(parkId);
        //return success(dormitoryCountBuilding, DormitoryCountListByFloorVO.class);
        return new Result<>(dormitoryCountBuilding);
    }

    @GetMapping("/getJche/freeBed")
    public Result getJcheFreeBed(@RequestParam("parkId") Integer parkId, @RequestParam("badge") String badge) {
        return new Result<>(smtDormitoryRoomService.getJcheFreeBed(parkId, badge));
    }


	/**
	 * 自动分配宿舍
	 * @param autoAllotRoomReqDTO
	 * @return
	 */
	@ApiOperation("自动分配宿舍")
	@PostMapping("/autoallot")
	public Result<List<DormitoryQuickStaffRespDTO>> autoAllot(@RequestBody AutoAllotRoomReqDTO autoAllotRoomReqDTO) {
		return new Result<>(smtDormitoryRoomService.autoAllot(autoAllotRoomReqDTO, smtDormitoryBedService));
	}

	@ApiOperation("补打凭条")
	@GetMapping("/print/info")
	public Result<DormitoryQuickStaffRespDTO> printInfo(@RequestParam("recordId") String recordId) {
		return new Result<>(smtDormitoryRoomService.printInfo(Long.parseLong(recordId)));
	}

	/**
	 * 查询房间的入住详情
	 * @param roomId
	 * @return
	 */
	@ApiOperation("查询房间的入住详情")
	@PostMapping("/bedDetail/{roomId}")
	public Result<List<DormitoryRoomDetailRespDTO>> bedDetail(@ApiParam(name = "roomId",value = "房间标识ID",required = true) @PathVariable Integer roomId) {
		return new Result<>(smtDormitoryRoomService.bedDetail(roomId));
	}

	/**
	 * 根据楼栋查询楼层和房间列表
	 * @param dormitoryId
	 * @return
	 */
	@ApiOperation("根据楼栋查询楼层和房间列表")
	@PostMapping("/frlist/{dormitoryId}")
	public Result<List<FloorRoomListRespDTO>> getFloorRoomList(@ApiParam(name = "dormitoryId",value = "楼栋ID",required = true) @PathVariable Integer dormitoryId) {
		return new Result<>(smtDormitoryRoomService.getFloorRoomList(dormitoryId));
	}

	/**
	 * 根据楼层数组查询房间列表
	 * @param floors
	 * @return
	 */
	@ApiOperation("根据楼层数组查询房间列表")
	@PostMapping("/frlist/byfloors")
	public Result<List<FloorRoomListRespDTO>> getRoomListByFloors(@ApiParam(name = "floors",value = "楼层列表','隔开",required = true) @RequestParam String floors) {
		if(StringUtils.isBlank(floors)){
			return new Result<>();
		}
		List<String> floorList = Arrays.asList(floors.split(","));
		return new Result<>(smtDormitoryRoomService.getRoomListByFloors(floorList.stream().map(Integer::parseInt).collect(Collectors.toList())));
	}

	/**
	 * 根据条件查询房间列表
	 * @param dormitoryRoomReqDTO
	 * @return
	 */
	@ApiOperation("根据条件查询房间列表")
	@GetMapping("/search/condition")
	public Result<List<DormitoryRoomRespDTO>> getRoomListByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO) {
		return new Result<>(smtDormitoryRoomService.getRoomListByCondition(dormitoryRoomReqDTO));
	}

	/**
	 * 楼层统计
	 * @param queryReqDTO
	 * @return
	 */
	@ApiOperation("楼层统计")
	@PostMapping("/search/room/statistics")
	public Result<List<DormitoryStatisticsListRespDTO>> getRoomStatistics(@RequestBody FloorStatisticsQueryReqDTO queryReqDTO) {
		return success(smtDormitoryRoomService.getRoomStatistics(queryReqDTO));
	}

	/**
	 * 楼层统计
	 * @param queryReqDTO
	 * @return
	 */
	@ApiOperation("楼层统计表格下载")
	@PostMapping("/search/room/statistics/excel")
	public ResponseEntity<byte[]> getRoomStatisticsExcel(@RequestBody FloorStatisticsQueryReqDTO queryReqDTO) {
		try {
			return smtDormitoryRoomService.getRoomStatisticsExcel(queryReqDTO);
		} catch (Exception e) {
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "导出excel异常");
		}

	}

}
