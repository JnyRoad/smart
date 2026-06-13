package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.SearchPhotoRecordDTO;
import com.tce.smart.platform.core.entity.SmtStaffPhotoUploadRecord;
import com.tce.smart.platform.core.vo.StaffPhotoUploadDetailVO;

public interface SmtStaffPhotoUploadRecordService extends IService<SmtStaffPhotoUploadRecord> {

	IPage<SmtStaffPhotoUploadRecord> getSmtRecordPage(Page page, SearchPhotoRecordDTO searchPhotoRecordDTO);

	StaffPhotoUploadDetailVO getSmtRecordDetail(Integer id);

}
