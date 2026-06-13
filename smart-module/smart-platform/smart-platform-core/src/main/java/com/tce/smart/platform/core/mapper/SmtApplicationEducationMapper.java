package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtApplicationEducation;

/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:38
 */
public interface SmtApplicationEducationMapper extends BaseMapper<SmtApplicationEducation> {

	List<SmtApplicationEducation> getSmtApplicationEducationList(String jobId);

	/**
	 * 查询最高学历
	 * @param applicationId
	 * @return
	 */
	Integer getMaxEdu(@Param("applicationId") String applicationId);
}
