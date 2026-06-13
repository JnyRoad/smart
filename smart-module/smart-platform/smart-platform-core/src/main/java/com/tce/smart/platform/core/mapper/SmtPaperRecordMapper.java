package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtPaperRecord;
import com.tce.smart.platform.core.vo.QuestionStatisticsVO;
import com.tce.smart.platform.core.vo.SelectStatisticsVO;

public interface SmtPaperRecordMapper  extends BaseMapper<SmtPaperRecord> {


	List<String> getPaperStaffTotal(Integer id);

	List<QuestionStatisticsVO> getQuestionStatistics(Integer id);

	List<SelectStatisticsVO> getSelectStatistics(Integer id);

}
