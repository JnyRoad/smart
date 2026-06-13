package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.AddFeedBackReqDTO;
import com.tce.smart.platform.core.dto.FeedBackQueryDTO;
import com.tce.smart.platform.core.entity.SmtFeedBack;
import com.tce.smart.platform.core.vo.FeedBackQueryVO;

public interface SmtFeedBackService  extends IService<SmtFeedBack> {

	IPage<SmtFeedBack> page(Page page, FeedBackQueryDTO feedBackQueryDTO);

	Boolean updateSmtFeedBack(SmtFeedBack smtFeedBack);

	Boolean addSmtFeedBack(AddFeedBackReqDTO feedBack);

	FeedBackQueryVO getDetailById(Integer id);

}
