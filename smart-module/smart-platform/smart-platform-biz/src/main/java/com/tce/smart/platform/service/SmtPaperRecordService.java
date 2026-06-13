package com.tce.smart.platform.service;


import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.AddPaperRecordReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPaperRecordReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchPaperRecordDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtPaperRecord;
import com.tce.smart.platform.core.vo.PaperStatisticsVO;

public interface SmtPaperRecordService extends IService<SmtPaperRecord> {


	SearchPaperRecordDetailRespDTO getDetail(SearchPaperRecordReqDTO searchPaperRecordReqDTO);

	Boolean addRecord(AddPaperRecordReqDTO addPaperRecordReqDTO);

	PaperStatisticsVO statistics(Integer id);

	void export(HttpServletResponse response, Integer id);



}
