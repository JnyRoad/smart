package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SmtDormitoryRepairsDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryRepairs;
import com.tce.smart.platform.core.vo.SmtDormitoryRepairsDetailVO;
import com.tce.smart.platform.core.vo.SmtDormitoryRepairsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtDormitoryRepairsMapper.xml
 * @date: 2020-07-20 13:56
 * @author: wuling
 * @version: 1.0
 */
public interface SmtDormitoryRepairsMapper extends BaseMapper<SmtDormitoryRepairs> {

	IPage<SmtDormitoryRepairsVO> getDormitoryRepairsPage(Page page, @Param("query") SmtDormitoryRepairsDTO smtDormitoryRepairsDTO, @Param("park") List<Integer> parkIdList);

	SmtDormitoryRepairsDetailVO getStaffReportDetail(@Param("id") Long id);
}
