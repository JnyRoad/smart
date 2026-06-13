package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.GetSmtFellowVisitorVO;

/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
public interface SmtFellowVisitorMapper extends BaseMapper<SmtFellowVisitor> {

	List<GetSmtFellowVisitorVO> selectListByVisitorId(@Param("query")SmtVisitor smtVisitor);

}
