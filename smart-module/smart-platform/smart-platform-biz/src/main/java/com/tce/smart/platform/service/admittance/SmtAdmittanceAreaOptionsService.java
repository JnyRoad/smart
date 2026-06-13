package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;

public interface SmtAdmittanceAreaOptionsService {
	AdmittanceAreaOptionsRespDTO getAreaOptions(Integer parkId);
}
