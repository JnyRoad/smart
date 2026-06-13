package com.tce.smart.platform.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.SearchPaperListRespDTO;
import com.tce.smart.platform.core.dto.AddOrUpdatePaperDTO;
import com.tce.smart.platform.core.dto.AddOrUpdateQuestionDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtPaperBu;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtQuestion;
import com.tce.smart.platform.core.entity.SmtSelect;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtPaperMapper;
import com.tce.smart.platform.core.vo.PaperStatisticsVO;
import com.tce.smart.platform.core.vo.SearchPaperDetailVO;
import com.tce.smart.platform.core.vo.SearchQuestionDetailVO;
import com.tce.smart.platform.service.SmtPaperBuService;
import com.tce.smart.platform.service.SmtPaperService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtQuestionService;
import com.tce.smart.platform.service.SmtSelectService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.PaperStatusEnum;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SmtPaperServiceImpl  extends ServiceImpl<SmtPaperMapper, SmtPaper> implements SmtPaperService {

	@Autowired
	private SmtParkBuService smtParkBuService;

	private final RemoteDictService remoteDictService;

	@Autowired
	private SmtPaperBuService smtPaperBuService;
	@Autowired
	private SmtQuestionService smtQuestionService;
	@Autowired
	private SmtSelectService  smtSelectService;
	@Autowired
	private SmtStaffService smtStaffService;


	@Override
	public IPage<SmtPaper> page(Page page, SmtPaper smtPaper) {
		// TODO Auto-generated method stub
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return  this.baseMapper.selectPage(page, Wrappers.<SmtPaper>query().lambda().like(StringUtils.isNotBlank(smtPaper.getTitle()), SmtPaper::getTitle, smtPaper.getTitle())
				.in(ObjectUtil.isNotNull(smtPaper.getParkId()), SmtPaper::getParkId,parkIds)
				.like(StringUtils.isNotBlank(smtPaper.getTitle()), SmtPaper::getTitle,smtPaper.getTitle())
				.eq(ObjectUtil.isNotNull(smtPaper.getParkId()), SmtPaper::getParkId,smtPaper.getParkId())
				.eq(ObjectUtil.isNotNull(smtPaper.getStatus()), SmtPaper::getStatus,smtPaper.getStatus())
				.eq(SmtPaper::getIsDelete, 0));
	}


	@Override
	@Transactional(rollbackFor=Exception.class)
	public Boolean addPaper(AddOrUpdatePaperDTO addOrUpdatePaperDTO) {
		// TODO Auto-generated method stub
		//操作用户名
		//String username = SecurityUtils.getUser().getUsername();
		String username ="admin";
		SmtPaper smtPaper=new SmtPaper();
		smtPaper.setCreateTime(LocalDateTime.now());
		smtPaper.setCreateUser(username);
		smtPaper.setParkId(addOrUpdatePaperDTO.getParkId());

		smtPaper.setTitle(addOrUpdatePaperDTO.getTitle());
		smtPaper.setIsDelete(0);
		DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startTime = LocalDateTime.parse(addOrUpdatePaperDTO.getStartTime(),df);
		smtPaper.setStartTime(startTime);
		LocalDateTime endTime = LocalDateTime.parse(addOrUpdatePaperDTO.getEndTime(),df);
		smtPaper.setEndTime(endTime);
		LocalDateTime now=LocalDateTime.now();
		if(now.toEpochSecond(ZoneOffset.of("+8"))>startTime.toEpochSecond(ZoneOffset.of("+8")) && now.toEpochSecond(ZoneOffset.of("+8"))<endTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.STARTING.getCode());
		}
		if(now.toEpochSecond(ZoneOffset.of("+8"))<startTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.NO_START.getCode());
		}
		if( now.toEpochSecond(ZoneOffset.of("+8"))>endTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.END.getCode());
		}
		smtPaper.insert();
		List<Integer> compIds = addOrUpdatePaperDTO.getCompIds();
		for (Integer integer : compIds) {
			SmtPaperBu smtPaperBu=new  SmtPaperBu();
			smtPaperBu.setCompId(integer);
			smtPaperBu.setPaperId(smtPaper.getId());
			smtPaperBu.insert();
		}

		//问题列表
		List<AddOrUpdateQuestionDTO> questions = addOrUpdatePaperDTO.getQuestions();
		for (AddOrUpdateQuestionDTO addOrUpdateQuestionDTO : questions) {
			SmtQuestion question=new SmtQuestion();
			question.setTitle(addOrUpdateQuestionDTO.getTitle());
			question.setType(addOrUpdateQuestionDTO.getType());
			question.setPaperId(smtPaper.getId());
			question.insert();
			List<String> answers = addOrUpdateQuestionDTO.getAnswers();
			for (String string : answers) {
				SmtSelect select=new SmtSelect();
				select.setAnswer(string);
				select.setQuestionId(question.getId());
				select.insert();
			}
		}
		return true;
	}


	@Override
	public List<SmtParkBu> getBu(Integer parkId) {
		// TODO Auto-generated method stub
		List<SmtParkBu> listByParkId = smtParkBuService.listByParkId(parkId);
		  Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.COMP_ABBR,SecurityConstants.FROM_IN);
		  List<SysDict> data = findByType.getData();

		for (SmtParkBu smtParkBu : listByParkId) {
			 for (SysDict sysDict : data) {
				 if(sysDict.getValue().equals(smtParkBu.getCompId()))
				 {
					 smtParkBu.setCompName(sysDict.getDescription());
					 break;
				 }
			}
		}
		return listByParkId;
	}


	@Override
	public SearchPaperDetailVO detailById(Integer id) {
		// TODO Auto-generated method stub
		SearchPaperDetailVO vo=new SearchPaperDetailVO();
		SmtPaper selectById = this.baseMapper.selectById(id);
		BeanUtil.copyProperties(selectById, vo);
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		vo.setStartTime(df.format(selectById.getStartTime()));
		vo.setEndTime(df.format(selectById.getEndTime()));

		List<SmtPaperBu> paperBuList = smtPaperBuService.list(Wrappers.<SmtPaperBu>query().lambda().eq(SmtPaperBu::getPaperId, selectById.getId()));
		List<String> compIds=new ArrayList<String>();
		for (SmtPaperBu smtPaperBu : paperBuList) {
			compIds.add(smtPaperBu.getCompId().toString());
		}
		vo.setCompIds(compIds);
		List<SearchQuestionDetailVO> questions=new ArrayList<>();

		List<SmtQuestion> questionList = smtQuestionService.list(Wrappers.<SmtQuestion>query().lambda().eq( SmtQuestion::getPaperId, selectById.getId()));
		for (SmtQuestion smtQuestion : questionList) {

			SearchQuestionDetailVO searchQuestionDetailVO=new SearchQuestionDetailVO();
			searchQuestionDetailVO.setTitle(smtQuestion.getTitle());
			searchQuestionDetailVO.setType(smtQuestion.getType());
			searchQuestionDetailVO.setId(smtQuestion.getId());
			List<SmtSelect> selectList = smtSelectService.list(Wrappers.<SmtSelect>query().lambda().eq( SmtSelect::getQuestionId, smtQuestion.getId()));
			List<String> answers=new ArrayList<>();
			for (SmtSelect smtSelect : selectList) {
				answers.add(smtSelect.getAnswer());
			}
			searchQuestionDetailVO.setAnswers(answers);
			questions.add(searchQuestionDetailVO);
		}
		vo.setQuestions(questions);
		return vo;
	}


	@Override
	@Transactional(rollbackFor=Exception.class)
	public Boolean update(AddOrUpdatePaperDTO addOrUpdatePaperDTO) {
		// TODO Auto-generated method stub
		//删除该问卷的问题和选项
		List<SmtQuestion> questions = smtQuestionService.list(Wrappers.<SmtQuestion>query().lambda().eq( SmtQuestion::getPaperId, addOrUpdatePaperDTO.getId()));
		for (SmtQuestion smtQuestion : questions) {
			boolean remove = smtSelectService.remove(Wrappers.<SmtSelect>query().lambda().eq( SmtSelect::getQuestionId, smtQuestion.getId()));
			smtQuestion.deleteById();
		}
		//删除该问卷的发布范围bu
		 boolean remove = smtPaperBuService.remove(Wrappers.<SmtPaperBu>query().lambda().eq(SmtPaperBu::getPaperId, addOrUpdatePaperDTO.getId()));
		 List<Integer> compIds = addOrUpdatePaperDTO.getCompIds();
		 //添加问卷的发布范围
		 for (Integer integer : compIds) {
			SmtPaperBu smtPaperBu=new  SmtPaperBu();
			smtPaperBu.setCompId(integer);
			smtPaperBu.setPaperId(addOrUpdatePaperDTO.getId());
			smtPaperBu.insert();
		 }

		//添加问题列表和选项
		List<AddOrUpdateQuestionDTO> questionsUp = addOrUpdatePaperDTO.getQuestions();
		for (AddOrUpdateQuestionDTO addOrUpdateQuestionDTO : questionsUp) {
			SmtQuestion question=new SmtQuestion();
			question.setTitle(addOrUpdateQuestionDTO.getTitle());
			question.setType(addOrUpdateQuestionDTO.getType());
			question.setPaperId(addOrUpdatePaperDTO.getId());
			question.insert();
			List<String> answers = addOrUpdateQuestionDTO.getAnswers();
			for (String string : answers) {
				SmtSelect select=new SmtSelect();
				select.setAnswer(string);
				select.setQuestionId(question.getId());
				select.insert();
			}
		}
		//修改问卷基本信息
		SmtPaper smtPaper=this.baseMapper.selectById(addOrUpdatePaperDTO.getId());
		smtPaper.setTitle(addOrUpdatePaperDTO.getTitle());
		DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime startTime = LocalDateTime.parse(addOrUpdatePaperDTO.getStartTime(),df);
		smtPaper.setStartTime(startTime);
		LocalDateTime endTime = LocalDateTime.parse(addOrUpdatePaperDTO.getEndTime(),df);
		smtPaper.setEndTime(endTime);
		LocalDateTime now=LocalDateTime.now();
		if(now.toEpochSecond(ZoneOffset.of("+8"))>startTime.toEpochSecond(ZoneOffset.of("+8")) && now.toEpochSecond(ZoneOffset.of("+8"))<endTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.STARTING.getCode());
		}
		if(now.toEpochSecond(ZoneOffset.of("+8"))<startTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.NO_START.getCode());
		}
		if( now.toEpochSecond(ZoneOffset.of("+8"))>endTime.toEpochSecond(ZoneOffset.of("+8")))
		{
			smtPaper.setStatus(PaperStatusEnum.END.getCode());
		}
		smtPaper.updateById();
		return true;
	}


	@Override
	public List<SearchPaperListRespDTO> getPaperByBadge(String badge) {
		// TODO Auto-generated method stub
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda()
				.eq(SmtStaff::getBadge, badge));
		List<SmtPaperBu> paperBuList = smtPaperBuService.list(Wrappers.<SmtPaperBu>query().lambda().eq(SmtPaperBu::getCompId, selectOne.getCompId()));
		List<SearchPaperListRespDTO> dtoList=new ArrayList<>();
		for (SmtPaperBu smtPaperBu : paperBuList) {
			SmtPaper selectById = this.baseMapper.selectById(smtPaperBu.getPaperId());
			SearchPaperListRespDTO dto=new SearchPaperListRespDTO();
			BeanUtil.copyProperties(selectById, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}


	@Override
	public Boolean remove(Integer id) {
		// TODO Auto-generated method stub
		SmtPaper selectById = this.baseMapper.selectById(id);
		if(selectById.getStatus().equals(1))
		{
			throw new TCEException("调查问卷正在进行中，不能删除");
		}
		return selectById.deleteById();
	}


	@Override
	public void statusRefresh() {
		// TODO Auto-generated method stub

		List<SmtPaper> list = this.list();
		for (SmtPaper smtPaper : list) {
			LocalDateTime now=LocalDateTime.now();
			if(now.toEpochSecond(ZoneOffset.of("+8"))>smtPaper.getStartTime().toEpochSecond(ZoneOffset.of("+8")) && now.toEpochSecond(ZoneOffset.of("+8"))<smtPaper.getEndTime().toEpochSecond(ZoneOffset.of("+8")))
			{
				smtPaper.setStatus(PaperStatusEnum.STARTING.getCode());
			}
			if(now.toEpochSecond(ZoneOffset.of("+8"))<smtPaper.getStartTime().toEpochSecond(ZoneOffset.of("+8")))
			{
				smtPaper.setStatus(PaperStatusEnum.NO_START.getCode());
			}
			if( now.toEpochSecond(ZoneOffset.of("+8"))>smtPaper.getEndTime().toEpochSecond(ZoneOffset.of("+8")))
			{
				smtPaper.setStatus(PaperStatusEnum.END.getCode());
			}
			smtPaper.updateById();
		}
	}

}
