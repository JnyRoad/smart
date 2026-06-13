package com.tce.smart.platform.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.commonsd.CommonSDRecordDTO;
import com.tce.smart.platform.core.entity.SmtCommonSD;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.entity.SmtSupplier;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 公摊水电
 *
 */
public interface SmtCommonSDMapper extends BaseMapper<SmtCommonSD> {

	IPage<CommonSDRecordDTO> getCommonSDCategoryRecord(Page page,@Param("categoryId") Integer categoryId, @Param("park") List<Integer> parkIdList);

	List<CommonSDRecordDTO> getAllCommonSDCategoryRecord(@Param("parkId") Integer parkId,
														 @Param("dormitoryId") Integer dormitoryId,
														 @Param("categoryId") Integer categoryId,
														 @Param("meterMonth") Date meterMonth,
														 @Param("park") List<Integer> parkIdList);
}
