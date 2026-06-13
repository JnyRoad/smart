package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.DormitoryBedReqDTO;
import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.BedReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.SearchDormitoryRoomDetailRespDTO;
import com.tce.smart.platform.core.dto.DormitoryBedPageQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.vo.DormitoryStaffFamilyVO;
import com.tce.smart.platform.core.vo.DormitoryStaffVO;

import java.util.List;

/**
 * 园区宿舍楼l楼层中房间的床位数
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:21
 */
public interface SmtDormitoryBedService extends IService<SmtDormitoryBed> {

	IPage<DormitoryStaffVO> getDormitoryBedOfStaff(Page page, DormitoryBedPageQueryDTO bed);


	Result addDormitoryBedOfStaff(SmtDormitoryStaff smtDormitoryStaff);

	Result deleteStaffBed(SmtDormitoryStaff smtDormitoryStaff);

	Result updateDormitoryBedOfStaff(SmtDormitoryStaff smtDormitoryStaff);

	/**
	 * 获取家属信息
	 * @param bed
	 * @return
	 */
	List<DormitoryStaffFamilyVO> getDormitoryStaffFamily(DormitoryBedPageQueryDTO bed);

	/**
	 * 修改床位名称
	 * @param bedReqDTO
	 * @return
	 */
	Boolean updateBedName(BedReqDTO bedReqDTO);

	/**
	 * 切换床位是否删除
	 * @param bedReqDTO
	 * @return
	 */
	Boolean switchDelFlg(BedReqDTO bedReqDTO,SmtDormitoryRoomService smtDormitoryRoomService);

	Integer getLeaveStaff(DormitoryBedPageQueryDTO bed);

	/**
	 * 获取空闲的床位数
	 * @return
	 */
	Integer getFreeBedCount(Integer parkId);

	/**
	 * 查询房间的住宿详情
	 * @param roomId
	 * @return
	 */
	List<SearchDormitoryRoomDetailRespDTO.BedDetail> getBedDetail(Integer roomId);


	Result<List<SmtDormitoryBed>> queryBed(DormitoryBedReqDTO bed);
}
