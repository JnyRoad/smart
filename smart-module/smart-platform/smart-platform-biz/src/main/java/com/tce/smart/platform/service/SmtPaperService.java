package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.resp.SearchPaperListRespDTO;
import com.tce.smart.platform.core.dto.AddOrUpdatePaperDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.vo.PaperStatisticsVO;
import com.tce.smart.platform.core.vo.SearchPaperDetailVO;

public interface SmtPaperService extends IService<SmtPaper> {

	IPage<SmtPaper> page(Page page, SmtPaper smtPaper);

	Boolean addPaper(AddOrUpdatePaperDTO addOrUpdatePaperDTO);

	List<SmtParkBu> getBu(Integer parkId);

	SearchPaperDetailVO detailById(Integer id);

	Boolean update(AddOrUpdatePaperDTO addOrUpdatePaperDTO);

	List<SearchPaperListRespDTO> getPaperByBadge(String badge);

	Boolean remove(Integer id);

	void statusRefresh();


}
