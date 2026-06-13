package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.SearchToC6DTO;
import com.tce.smart.platform.core.entity.ToC6Ephoto;
import com.tce.smart.platform.core.vo.SearchToC6VO;

/**
 * 供c6同步员工头像
 * @author QIPEI
 *
 */
public interface ToC6EphotoService extends IService<ToC6Ephoto> {

	Result saveToC6(ToC6Ephoto toC6Ephoto);

	IPage<SearchToC6VO> searchPage(Page page, SearchToC6DTO searchToC6DTO);

}
