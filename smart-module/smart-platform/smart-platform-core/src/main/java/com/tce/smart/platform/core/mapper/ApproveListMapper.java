package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.api.dto.req.approval.ApproveListQueryDTO;
import com.tce.smart.platform.core.dto.RepairsApprovalListDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.ApproveList;

import java.util.List;

/**
 * 待审批表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface ApproveListMapper extends BaseMapper<ApproveList> {

    /**
     * 更新审批状态
     * @param approveList
     * @return
     */
    int updateState(@Param("query") ApproveList approveList);

    /**
     * 分页查询
     * @param approveList
     * @return
     */
    IPage getPage(Page page, @Param("query") ApproveList approveList);


    IPage getPageEnd(Page page, @Param("query") ApproveList approveList);

    IPage getPageStart(Page page, @Param("query") ApproveList approveList);

	IPage<RepairsApprovalListDTO> getRepairsWaitPass(Page page, @Param("query") ApproveList approveList);

	IPage<RepairsApprovalListDTO> getRepairsPass(Page page, @Param("query") ApproveList approveList);

    IPage getNewPage(Page page, @Param("query") ApproveListQueryDTO queryDTO, @Param("stateList") List<Integer> stateList, @Param("approveBadge") String approveBadge);
}
