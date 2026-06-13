package com.tce.smart.platform.service.impl;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.mapper.SmtFellowVisitorMapper;
import com.tce.smart.platform.service.SmtFellowVisitorService;

import java.util.List;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@Service
public class SmtFellowVisitorServiceImpl extends ServiceImpl<SmtFellowVisitorMapper, SmtFellowVisitor> implements SmtFellowVisitorService {

	@Override
	public List<GetSmtFellowVisitorVO> selectListByVisitorId(SmtVisitor smtVisitor) {
		return this.baseMapper.selectListByVisitorId(smtVisitor);
	}
}
