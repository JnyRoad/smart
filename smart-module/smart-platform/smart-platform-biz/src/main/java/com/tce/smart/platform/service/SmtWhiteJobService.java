package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.WhiteJobRemoveDTO;
import com.tce.smart.platform.core.entity.SmtWhiteJob;
import com.tce.smart.platform.core.vo.WhiteJobVO;

public interface SmtWhiteJobService extends IService<SmtWhiteJob> {

	Result removeVisitorById(WhiteJobRemoveDTO whiteJobRemoveDTO);

	Result saveWhiteJob(SmtWhiteJob smtWhiteJob);

	IPage<WhiteJobVO> page(Page page, SmtWhiteJob smtWhiteJob);

}
