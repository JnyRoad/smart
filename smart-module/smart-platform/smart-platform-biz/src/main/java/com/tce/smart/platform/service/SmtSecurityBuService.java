package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtSecurityBuReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityBuRespDTO;
import com.tce.smart.platform.core.entity.SmtAppHrAuth;
import com.tce.smart.platform.core.entity.SmtSecurityBu;

import java.util.List;

/**
 *
 *
 * @author
 * @date 2019-06-12 11:17:37
 */
public interface SmtSecurityBuService extends IService<SmtSecurityBu> {

	List<SmtSecurityBuRespDTO> getBuList(Integer parkId);

	List<SmtSecurityBuRespDTO.SecurityList> getRelationSecurity(String buId, Integer parkId);

	List<Integer> getRelationSecuritys(String buId, List<Integer> parkIds);

	Boolean editRelation(List<SmtSecurityBuReqDTO> reqDTOS);


}
