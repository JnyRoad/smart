package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtRecruitment;
import com.tce.smart.platform.core.vo.JobListVO;
import com.tce.smart.platform.core.vo.RecruitmentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 招聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:34
 */
public interface SmtRecruitmentMapper extends BaseMapper<SmtRecruitment> {

	IPage<List<RecruitmentVO>> selectPage(Page page, @Param("query") SmtRecruitment smtRecruitment, @Param("parkIds")List<Integer> parkIds);

	RecruitmentVO getRecruitById(@Param("id") Integer id);

	IPage<List<RecruitmentVO>> getJobList(Page page, @Param("query") SmtRecruitment recruitment);

	List<JobListVO> getJobList(Integer parkId);

}
