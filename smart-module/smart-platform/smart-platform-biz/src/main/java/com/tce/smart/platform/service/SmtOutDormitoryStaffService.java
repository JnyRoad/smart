package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.platform.api.dto.req.SearchOutDormitoryReqDTO;
import com.tce.smart.platform.api.dto.resp.AllowanceStatusRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitorySituationRespDTO;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.OutDormitoryDetailVO;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;

import java.util.List;

/**
 * 员工外宿信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtOutDormitoryStaffService extends IService<SmtOutDormitoryStaff> {

	AllowanceStatusRespDTO status(String staffBadge);

	Result addOutDormitory(SmtOutDormitoryStaff apply);

	Result getAllowance(String staffBadge,Integer type);

	Result getOutDormitoryInfo(String staffBadge, Integer type);

	void approvalNotice(String staffBadge, String code, Integer id, boolean flag);

	Result<OutDormitoryDetailVO> getOutDormitoryInfoDetail(Integer id);

	void refreshOutDormitory();

	IPage<SearchOutDormitoryVO> getOutDormitoryPageList(Page page, SearchOutDormitoryReqDTO searchOutDormitoryReqDTO);

	OutDormitoryDetailVO getOutDormitoryDetailById(Integer id);

	Integer getDormitroySet();

	DormitorySituationRespDTO getOvwYsCallOwanceDetails(String staffBadge, Integer type, Integer parkId, String nowDor, SmtDormitoryStaffService smtDormitoryStaffService);

	Result<OutDormitoryDetailVO> outRoomApplyDetailById(String recordId);

	void getOAProcess(String processId);

	void getOAProcessFlow(String processId, List<FlowVO> list);
}
