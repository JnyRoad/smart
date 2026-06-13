package com.tce.smart.platform.core.mapper;


import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchOutDormitoryDTO;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;



/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtOutDormitoryStaffMapper extends BaseMapper<SmtOutDormitoryStaff> {

	IPage<SearchOutDormitoryVO> getOutDormitoryPageList(Page page, @Param("query") SearchOutDormitoryDTO searchOutDormitoryDTO);



}
