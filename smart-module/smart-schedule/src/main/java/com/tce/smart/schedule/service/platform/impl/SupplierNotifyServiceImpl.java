package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaNotifyConfigDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaNotifyListDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SupplierNotifyStatusReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import com.tce.smart.platform.api.feign.RemoteSecurityAreaSupplierService;
import com.tce.smart.schedule.service.platform.SupplierNotifyService;
import com.tce.smart.tool.enums.SupplierNotifyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SupplierNotifyServiceImpl implements SupplierNotifyService {

	@Resource
	private RemoteSecurityAreaSupplierService remoteSecurityAreaSupplierService;

	@Override
	public void notifyProcess() {
		//查询所有供应商通知配置
		Result<List<SecurityAreaNotifyConfigDTO>> notifyAllConfig = remoteSecurityAreaSupplierService.getNotifyAllConfig();
		List<SecurityAreaNotifyConfigDTO> allConfigData = notifyAllConfig.data();
		if(CollectionUtil.isNotEmpty(allConfigData)){
			//根据园区查询保密协议即将到期的供应商数据
			for(SecurityAreaNotifyConfigDTO config : allConfigData){
				try {
					Result<List<SmtSecurityAreaSupplierRespDTO>> listResult = remoteSecurityAreaSupplierService.notifyList(SecurityAreaNotifyListDTO.builder()
							.parkId(config.getParkId())
							.days(config.getDays())
							.build());
					List<SmtSecurityAreaSupplierRespDTO> supplierRespDTOS = listResult.data();
					if(CollectionUtil.isNotEmpty(supplierRespDTOS)){
						for(SmtSecurityAreaSupplierRespDTO supplierRespDTO : supplierRespDTOS){
							//通知
							String template = config.getTemplate();
							//更新已通知
							remoteSecurityAreaSupplierService.updateNotifyStatus(SupplierNotifyStatusReqDTO.builder()
									.id(supplierRespDTO.getId())
									.notifyStatus(SupplierNotifyEnum.NOTIFY.getCode())
									.build());
						}

					}
				}catch (Exception e){
					log.error("查询保密协议即将到期供应商数据异常:",e);
				}
			}
		}
	}
}
