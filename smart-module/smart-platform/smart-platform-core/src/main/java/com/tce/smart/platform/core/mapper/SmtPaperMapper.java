package com.tce.smart.platform.core.mapper;


import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtPaper;

public interface SmtPaperMapper  extends BaseMapper<SmtPaper> {

	List<String> getPaperStaffTotal(Integer id);


}
