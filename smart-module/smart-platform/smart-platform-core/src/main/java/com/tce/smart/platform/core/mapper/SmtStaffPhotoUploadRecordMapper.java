package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchPhotoRecordDTO;
import com.tce.smart.platform.core.entity.SmtStaffPhotoUploadRecord;

public interface SmtStaffPhotoUploadRecordMapper  extends BaseMapper<SmtStaffPhotoUploadRecord> {

	IPage<SmtStaffPhotoUploadRecord> getSmtRecordPage(Page page,  @Param("query") SearchPhotoRecordDTO searchPhotoRecordDTO,@Param("park") List<Integer> parkIdList);

}
