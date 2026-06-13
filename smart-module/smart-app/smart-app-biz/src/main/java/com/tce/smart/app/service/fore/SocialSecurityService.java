package com.tce.smart.app.service.fore;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;

public interface SocialSecurityService {

	Result<List<SearchSocialSecurityRespDTO>> getSmtSocialSecurityList();

}
