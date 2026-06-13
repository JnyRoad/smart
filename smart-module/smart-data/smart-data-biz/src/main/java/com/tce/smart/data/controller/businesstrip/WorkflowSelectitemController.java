package com.tce.smart.data.controller.businesstrip;

import com.tce.smart.businesstrip.core.service.impl.VWorkflowSelectitemServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.oa.resp.WorkflowSelectitemRespDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author fushiping
 * @since
 */
@RestController
@RequestMapping("/oa/view")
public class WorkflowSelectitemController extends BaseController {

    @Autowired
    private VWorkflowSelectitemServiceImpl workflowSelectitemService;

    /**
     * 获取所有OA区域列表
     * @return
     */
    @GetMapping("/list")
    public Result<List<WorkflowSelectitemRespDTO>> getList(@RequestParam("selectIdList") List<Integer> selectIdList, @RequestParam("fieldId") Integer fieldId) {
        return success(workflowSelectitemService.getList(selectIdList, fieldId), WorkflowSelectitemRespDTO.class);
    }
}
