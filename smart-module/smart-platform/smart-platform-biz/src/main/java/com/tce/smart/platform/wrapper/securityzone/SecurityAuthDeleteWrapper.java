package com.tce.smart.platform.wrapper.securityzone;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityWhiteRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityWhite;
import com.tce.smart.platform.service.securityzone.SmtSecurityWhiteService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: SecurityAuthDeleteWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityAuthDeleteWrapper extends BaseWrapper<SmtSecurityAuthDelete, SecurityAuthDeleteRespDTO> {

	@Autowired
	private SmtSecurityWhiteService smtSecurityWhiteService;

    @Override
    protected SecurityAuthDeleteRespDTO warp(SmtSecurityAuthDelete bean) throws IOException {
		SecurityAuthDeleteRespDTO resp = BeanUtils.transform(SecurityAuthDeleteRespDTO.class, bean);
		List<SmtSecurityWhite> whites =  smtSecurityWhiteService.getList(bean.getId());
		if(CollUtil.isNotEmpty(whites)) {
			List<SecurityWhiteRespDTO> respDTOS = BeanUtils.batchTransform(SecurityWhiteRespDTO.class, whites);
			resp.setWhiteList(respDTOS);
		}
        return resp;
    }
}
