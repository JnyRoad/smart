package com.tce.smart.data.controller.ehrview;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.ehrview.core.entity.EvwCcdFlstandard;
import com.tce.smart.ehrview.core.service.EvwCcdFlstandardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 *
 * 控制器
 * 职层信息
 * @author tce
 *
 */
@RestController
@RequestMapping("/flstandard")
public class EvwCcdFlstandardController extends BaseController {


	@Autowired
	private EvwCcdFlstandardService  evwCcdFlstandardService;


	@SysLog("根据id获取职层信息")
	@Inner
    @GetMapping("/get")
    public Result<EvwCcdFlstandardDTO> getById(@RequestParam("id") String id, @RequestParam(value = "Pzid",required = false) Integer Pzid){
	List<EvwCcdFlstandard> evwCcdFlstandards = evwCcdFlstandardService.list(Wrappers. <EvwCcdFlstandard>query().lambda()
                .eq(!Objects.isNull(Pzid),EvwCcdFlstandard::getPzid, Pzid)
                .eq(EvwCcdFlstandard::getJchenid, id)
        );
		if(CollUtil.isNotEmpty(evwCcdFlstandards)) {
			EvwCcdFlstandardDTO evwCcdFlstandardDTO = new EvwCcdFlstandardDTO();
			BeanUtils.copyProperties(evwCcdFlstandards.get(0),evwCcdFlstandardDTO);
			return success(evwCcdFlstandardDTO);
		}
		return success();
    }
}
