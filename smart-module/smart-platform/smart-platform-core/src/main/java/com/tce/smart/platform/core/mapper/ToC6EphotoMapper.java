package com.tce.smart.platform.core.mapper;


import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchToC6DTO;
import com.tce.smart.platform.core.entity.ToC6Ephoto;
import com.tce.smart.platform.core.vo.SearchToC6VO;


public interface ToC6EphotoMapper extends BaseMapper<ToC6Ephoto> {

	IPage<SearchToC6VO> searchPage(Page page,@Param("query") SearchToC6DTO searchToC6DTO);


}
