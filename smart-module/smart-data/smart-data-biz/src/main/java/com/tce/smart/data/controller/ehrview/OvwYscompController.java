//package com.tce.smart.data.controller.ehrview;
//
//
//import com.tce.smart.common.core.model.Result;
//import com.tce.smart.common.core.wrapper.BaseController;
//import com.tce.smart.common.security.annotation.Inner;
//import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
//import com.tce.smart.ehrview.core.entity.OvwYscomp;
//import com.tce.smart.ehrview.core.service.IOvwYscompService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
///**
// * <p>
// *  前端控制器
// * </p>
// *
// * @author WangJinbo123
// * @since 2019-05-03
// */
//@RestController
//@RequestMapping("/ys/comp")
//public class OvwYscompController extends BaseController {
//    @Autowired
//    private IOvwYscompService iOvwYscompService;
//
//    /**
//     * 根据compId获取公司信息
//     * @param compId
//     * @return
//     */
//    @Inner
//    @GetMapping("/info")
//    public Result<OvwYscompRespDTO> getByCompId(@RequestParam("compId") String compId){
//        OvwYscomp ovwYscomp = iOvwYscompService.getByCompId(compId);
//        return success(ovwYscomp, OvwYscompRespDTO.class);
//    }
//    /**
//     * 获取所有公司信息
//     * @return
//     */
//    @Inner
//    @GetMapping("/list")
//    public Result<List<OvwYscompRespDTO>> getList(){
//        List<OvwYscomp> ovwYscompList = iOvwYscompService.getList();
//        return success(ovwYscompList, OvwYscompRespDTO.class);
//    }
//}
//
