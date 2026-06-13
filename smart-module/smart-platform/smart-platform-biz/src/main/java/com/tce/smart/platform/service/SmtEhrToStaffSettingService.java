package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddEhrToStaffSettingDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaffSetting;

import java.util.List;


public interface SmtEhrToStaffSettingService  extends IService<SmtEhrToStaffSetting>{

	Boolean addList(AddEhrToStaffSettingDTO dto);

	List<SmtEhrToStaffSetting> getListEHR();

	Boolean addListEHR(AddEhrToStaffSettingDTO dto);

	List<SmtEhrToStaffSetting> getListDHR();

	Boolean addListDHR(AddEhrToStaffSettingDTO dto);
}
