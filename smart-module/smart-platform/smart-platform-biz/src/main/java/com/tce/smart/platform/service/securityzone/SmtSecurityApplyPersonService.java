package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityApplyPerson;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
public interface SmtSecurityApplyPersonService extends IService<SmtSecurityApplyPerson> {

	/**
	 * 人员保存
	 * @param personReq
	 * @param applyId
	 * @return
	 */
	Boolean savePerson(List<SecurityApplyPersonReqDTO> personReq, Long applyId);

}
