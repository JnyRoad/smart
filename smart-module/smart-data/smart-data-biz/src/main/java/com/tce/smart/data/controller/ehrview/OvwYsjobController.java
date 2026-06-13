package com.tce.smart.data.controller.ehrview;


import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsjob;
import com.tce.smart.ehrview.core.service.IOvwYsjobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@RestController
@RequestMapping("/ys/job")
public class OvwYsjobController extends BaseController {
    @Autowired
    private IOvwYsjobService iOvwYsjobService;

    /**
     *
     * @param deptId
     * @return
     */
    @Inner
    @GetMapping("/dept")
    public Result<List<OvwYsjobRespDTO>> getByDeptId(@RequestParam("deptId") Integer deptId){
        List<OvwYsjob> ovwYsjobList = iOvwYsjobService.getByDeptId(deptId);
        return success(ovwYsjobList,OvwYsjobRespDTO.class);
    }
    @Inner
    @GetMapping("/getByCompId")

    public Result<Integer> getByCompId(@RequestParam("compId") Integer compId){
	Integer jobSize= iOvwYsjobService.getByCompId(compId);
        return success(jobSize);
    }
    @Inner
    @GetMapping("/getListByCompId")
    public Result<List<OvwYsjobRespDTO>> getListByCompId(@RequestParam("compId") Integer compId){
	List<OvwYsjob> ovwYsjobList = iOvwYsjobService.getListByCompId(compId);
        return success(ovwYsjobList,OvwYsjobRespDTO.class);
    }

	@Inner
	@GetMapping("/getJChenList")
	public Result<List<OvwYsjobRespDTO>> getJChenList(){
		List<OvwYsjob> ovwYsjobList = iOvwYsjobService.getJchenList();
		return success(ovwYsjobList,OvwYsjobRespDTO.class);
	}

    @Inner
    @GetMapping("/id")
    public Result<OvwYsjobRespDTO> getByDeptName(@RequestParam("jobId") String jobId){
        OvwYsjob ovwYsjob = iOvwYsjobService.getByJobId(jobId);
        return success(ovwYsjob,OvwYsjobRespDTO.class);
    }

}
