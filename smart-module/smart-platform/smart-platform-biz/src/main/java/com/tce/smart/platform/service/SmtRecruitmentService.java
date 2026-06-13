package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtRecruitment;
import com.tce.smart.platform.core.vo.JobListVO;
import com.tce.smart.platform.core.vo.RecruitmentVO;

import java.util.List;

/**
 * 招聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:34
 */
public interface SmtRecruitmentService extends IService<SmtRecruitment> {

	Result addRecruitment(SmtRecruitment smtRecruitment);

	Result updateRecruitmentById(SmtRecruitment smtRecruitment);

	Result removeRecruitmentById(Integer id);

	IPage getPage(Page page, SmtRecruitment smtRecruitment, List<Integer> parkIds);
	IPage getJobList(Page page, SmtRecruitment recruitment);

	RecruitmentVO getRecruitById(Integer id);

	Result getJche();

	Result getComp();

	Result getDep(Integer compId);

	Result getJob(Integer depId);

	List<JobListVO> getJobList(Integer parkId);


	Result refreshRecruitmentById();

	Result updateIsUp(SmtRecruitment smtRecruitment);

	Result getJobInfo(Integer jobId);

	void refreshComp();





}
