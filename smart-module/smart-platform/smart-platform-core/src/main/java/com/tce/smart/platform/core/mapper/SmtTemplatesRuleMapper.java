package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtTemplatesRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtTemplatesRuleMapper
 * @date: 2020-07-13 17:24
 * @author: wuling
 * @version: 1.0
 */
public interface SmtTemplatesRuleMapper extends BaseMapper<SmtTemplatesRule> {
	/**
	 * 查询登录用户可使用的所有水电模板配置规则
	 * @return
	 */
	List<SmtTemplatesRule> getSDTemplateRules(@Param("park") List<Integer> parkIdList);
}
