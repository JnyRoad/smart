package com.tce.smart.platform.service;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
public interface SmtFellowVisitorService extends IService<SmtFellowVisitor> {
	List<GetSmtFellowVisitorVO> selectListByVisitorId(SmtVisitor smtVisitor);
}
