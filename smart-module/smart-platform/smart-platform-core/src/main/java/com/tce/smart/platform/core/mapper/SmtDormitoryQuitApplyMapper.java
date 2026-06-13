package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.DormitoryQuitApplyQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryQuitApply;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author FUHISPING
 * @since
 */
public interface SmtDormitoryQuitApplyMapper extends BaseMapper<SmtDormitoryQuitApply> {

	IPage<SmtDormitoryQuitApply> getPage(Page page, @Param("query") DormitoryQuitApplyQueryDTO reqDTO);
}
