package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService;
import com.tce.smart.platform.core.entity.SmtApplication;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtRecruitment;
import com.tce.smart.platform.core.mapper.SmtRecruitmentMapper;
import com.tce.smart.platform.core.vo.DicContentVO;
import com.tce.smart.platform.core.vo.JobListVO;
import com.tce.smart.platform.core.vo.RecruitmentVO;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtRecruitmentService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 招聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:34
 */
@Service
@AllArgsConstructor
public class SmtRecruitmentServiceImpl extends ServiceImpl<SmtRecruitmentMapper, SmtRecruitment> implements SmtRecruitmentService {

	private final SmtRecruitmentMapper mapper;

	private final RemoteDictService remoteDictService;

	private final SmtParkService parkService;

	private final RemoteOvwYsdepService depService;

	private final RemoteOvwYsjobService jobService;

	private final RemoteOvwYscompService  remoteOvwYscompService;


	@Override
	public Result addRecruitment(SmtRecruitment smtRecruitment) {
		// TODO Auto-generated method stub
		checkRecruitment(smtRecruitment);
		String badge=SecurityUtils.getUser().getUsername();
		smtRecruitment.setCreateUser(badge);
		smtRecruitment.setStatus(smtRecruitment.getStatus());
		smtRecruitment.setCreateTime(LocalDateTime.now());
		smtRecruitment.setIsUp(0);
		return new Result<>(mapper.insert(smtRecruitment));
	}

	@Override
	public Result updateRecruitmentById(SmtRecruitment smtRecruitment) {
		// TODO Auto-generated method stub
		checkRecruitment(smtRecruitment);
		return new Result<>(this.saveOrUpdate(smtRecruitment));
	}

	@Override
	public Result removeRecruitmentById(Integer id) {
		// TODO Auto-generated method stub
		SmtApplication application = new SmtApplication();
		Integer selectCount = application
				.selectCount(Wrappers.<SmtApplication> query().lambda().eq(SmtApplication::getRecruitId, id));
		if (selectCount > 0) {
			return new Result<>(Boolean.FALSE, "该招聘下已有应聘人员，删除失败");
		}
		return new Result<>(this.removeById(id));
	}


	public Result checkRecruitment(SmtRecruitment smtRecruitment)
	{
		if (smtRecruitment == null) {
			return new Result<>(Boolean.FALSE, "招聘不能为空");
		}
		if(smtRecruitment.getSalaryStart()<0 || smtRecruitment.getSalaryEnd()<0)
		{
			return new Result<>(Boolean.FALSE, "工资最低值不能小于0");
		}
		if (smtRecruitment.getSalaryStart() > smtRecruitment.getSalaryEnd()) {
			return new Result<>(Boolean.FALSE, "最低工资不可大于最高工资");
		}
//		if(!RegexUtils.matchAge(smtRecruitment.getAgeStart().toString()))
//		{
//			return new Result<>(Boolean.FALSE, "员工年龄在18-70范围内");
//		}
//		if(!RegexUtils.matchAge(smtRecruitment.getAgeEnd().toString()))
//		{
//			return new Result<>(Boolean.FALSE, "员工年龄在18-70范围内");
//		}
//		if (smtRecruitment.getAgeStart() > smtRecruitment.getAgeEnd()) {
//			return new Result<>(Boolean.FALSE, "最低年龄不可大于最高年龄");
//		}
		if(smtRecruitment.getRecruitNum()<=0)
		{
			return new Result<>(Boolean.FALSE, "招聘人数至少为1人");
		}
		if(smtRecruitment.getMajor()==null)
		{
			smtRecruitment.setMajor("");
		}
		if(smtRecruitment.getEducation()==null)
		{
			smtRecruitment.setEducation("");
		}
		if(smtRecruitment.getWorkYear()==null)
		{
			smtRecruitment.setWorkYear(0);
		}
		return new Result<>(true);
	}

	@Override
	public IPage getPage(Page page, SmtRecruitment smtRecruitment, List<Integer> parkIds) {
		// TODO Auto-generated method stub
		return mapper.selectPage(page, smtRecruitment, parkIds);
	}

	@Override
	public RecruitmentVO getRecruitById(Integer id) {
		return mapper.getRecruitById(id);

	}

	@Override
	public Result getJche() {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.JOB_LEVEL, SecurityConstants.FROM_IN);
		//判断集合是否为空
		List<DicContentVO> dicList=new ArrayList<DicContentVO>();
		if(findByType.getData().size()>0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				DicContentVO dic=new DicContentVO();
				dic.setTypeCode(findByType.getData().get(j).getValue());
				dic.setTypeName(findByType.getData().get(j).getLabel());
				dicList.add(dic);
			}
		}
		return new Result<>(dicList);
	}

	@Override
	public Result<List<OvwYscompRespDTO>> getComp() {
		//return compService.getList(SecurityConstants.FROM_IN);
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR, SecurityConstants.FROM_IN);
		List<OvwYscompRespDTO> comList=new ArrayList<OvwYscompRespDTO>();
		OvwYscompRespDTO com=null;
		if(findByType.getData().size()>0) {
			for (int j = 0; j < findByType.getData().size(); j++) {
				com=new OvwYscompRespDTO();
				com.setTitle(findByType.getData().get(j).getDescription());
				com.setCompid(Integer.parseInt(findByType.getData().get(j).getValue()) );
				com.setCompAbbr(findByType.getData().get(j).getLabel());
				comList.add(com);
			}
		}
		return new Result<>(comList);
	}

	@Override
	public Result getDep(Integer compId) {
		// TODO Auto-generated method stub
		return depService.getByCompId(compId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@Override
	public Result getJob(Integer depId) {
		// TODO Auto-generated method stub
		return jobService.getByDeptId(depId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@Override
	public IPage getJobList(Page page, SmtRecruitment recruitment) {
		// TODO Auto-generated method stub
		return mapper.getJobList(page, recruitment);
	}

	@Override
	public List<JobListVO> getJobList(Integer parkId) {
		// TODO Auto-generated method stub
		List<SmtRecruitment> selectList = mapper.selectList(Wrappers.<SmtRecruitment> query().lambda().
				eq(SmtRecruitment::getStatus, 1).
				eq(SmtRecruitment::getParkId, parkId).orderByDesc(SmtRecruitment::getCreateTime));
		List<JobListVO> voList=new ArrayList<>();
		for (SmtRecruitment smtRecruitment : selectList) {
			JobListVO vo=new JobListVO();
			vo.setRecruitId(smtRecruitment.getId());
			SmtPark byId = parkService.getById(parkId);
			vo.setJobAddress(byId.getParkName());
			vo.setJobCount(smtRecruitment.getRecruitNum());
			vo.setJobName(smtRecruitment.getJobName());
			String jobWage = smtRecruitment.getSalaryStart() + "-" + smtRecruitment.getSalaryEnd();
			vo.setJobWage(jobWage);
			// 发布日期
			DateTimeFormatter df = DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT);
			vo.setPublishDate(df.format(smtRecruitment.getCreateTime()));
			vo.setCompName(smtRecruitment.getCompName());
			voList.add(vo);
		}
		return voList;
	}

	@Override
	public Result refreshRecruitmentById() {
		// TODO Auto-generated method stub
		//查询要置顶的岗位列表
		List<SmtRecruitment> selectList = this.baseMapper.selectList(Wrappers.<SmtRecruitment> query().lambda().
				eq(SmtRecruitment::getIsUp, 1));
		for (SmtRecruitment smtRecruitment : selectList) {
			smtRecruitment.setCreateTime(LocalDateTime.now());
			smtRecruitment.updateById();
		}

		return new Result<>(true);
	}

	@Override
	public Result updateIsUp(SmtRecruitment smtRecruitment) {
		// TODO Auto-generated method stub
		SmtRecruitment selectById = this.baseMapper.selectById(smtRecruitment.getId());
		selectById.setIsUp(smtRecruitment.getIsUp());

		return new Result<>(selectById.updateById());
	}

	@Override
	public Result getJobInfo(Integer jobId) {
		// TODO Auto-generated method stub
		Result<OvwYsjobRespDTO> jobInfo = jobService.getByDeptName(jobId, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return jobInfo;
	}

	@Override
	public void refreshComp() {
		// TODO Auto-generated method stub
		List<SysDict> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR,
				SecurityConstants.FROM_IN).getData();
		//更新bu信息
		List<OvwYscompRespDTO>list = remoteOvwYscompService.getList(SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).getData();
		for (OvwYscompRespDTO ovwYscompRespDTO : list) {

			List<SmtRecruitment> selectList = this.baseMapper.selectList(Wrappers.<SmtRecruitment> query().lambda().
					eq(SmtRecruitment::getCompId, ovwYscompRespDTO.getCompid()));
			if(selectList.size()>0)
			{
				for (SmtRecruitment smtRecruitment : selectList) {
					smtRecruitment.setCompName(ovwYscompRespDTO.getTitle());
					smtRecruitment.updateById();
				}
			}
		}
		//更新部门信息
		for (SysDict sysDict : findByType) {
			 List<OvwYsjobRespDTO> byCompId = jobService.getListByCompId(Integer.parseInt(sysDict.getValue()), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).getData();
			 if(Objects.nonNull(byCompId)){
				 for (OvwYsjobRespDTO ovwYsjobRespDTO : byCompId) {
					 List<SmtRecruitment> selectList = this.baseMapper.selectList(Wrappers.<SmtRecruitment> query().lambda().eq(SmtRecruitment::getJobId, ovwYsjobRespDTO.getJobid()));
					 if(selectList.size()>0)
					 {
						 for (SmtRecruitment smtRecruitment : selectList) {
							 smtRecruitment.setDepName(ovwYsjobRespDTO.getDepname());
							 smtRecruitment.setJobName(ovwYsjobRespDTO.getJobname());
							 smtRecruitment.updateById();
						 }
					 }
				 }
			 }
		}
	}

}
