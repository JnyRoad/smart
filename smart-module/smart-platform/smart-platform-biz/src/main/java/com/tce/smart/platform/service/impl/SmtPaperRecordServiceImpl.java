package com.tce.smart.platform.service.impl;



import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.AddPaperRecordReqDTO;
import com.tce.smart.platform.api.dto.req.AddQuestionRecordReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPaperRecordReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchPaperRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchQuestionListRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchSelectRespDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtPaperRecord;
import com.tce.smart.platform.core.entity.SmtQuestion;
import com.tce.smart.platform.core.entity.SmtSelect;
import com.tce.smart.platform.core.mapper.SmtPaperRecordMapper;
import com.tce.smart.platform.core.vo.ExportPaperVO;
import com.tce.smart.platform.core.vo.PaperStatisticsVO;
import com.tce.smart.platform.core.vo.QuestionStatisticsVO;
import com.tce.smart.platform.core.vo.SelectStatisticsVO;
import com.tce.smart.platform.service.SmtPaperRecordService;
import com.tce.smart.platform.service.SmtPaperService;
import com.tce.smart.platform.service.SmtQuestionService;
import com.tce.smart.platform.service.SmtSelectService;
import com.tce.smart.tool.exception.TCEException;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SmtPaperRecordServiceImpl  extends ServiceImpl<SmtPaperRecordMapper, SmtPaperRecord> implements SmtPaperRecordService {

	@Autowired
	private SmtPaperService smtPaperService;

	@Autowired
	private SmtSelectService smtSelectService;

	@Autowired
	private SmtQuestionService smtQuestionService;



	@Override
	public SearchPaperRecordDetailRespDTO getDetail(SearchPaperRecordReqDTO searchPaperRecordReqDTO) {
		// TODO Auto-generated method stub

		SearchPaperRecordDetailRespDTO dto=new SearchPaperRecordDetailRespDTO();
		Integer id = searchPaperRecordReqDTO.getPaperId();  //问卷id
		if(ObjectUtil.isNull(id))
		{
			 throw new TCEException("未获取到调查问卷的唯一标识");
		}

		String badge = searchPaperRecordReqDTO.getBadge(); //员工号
		if(ObjectUtil.isNull(badge))
		{
			 throw new TCEException("未获取到员工号");
		}
		SmtPaper byId = smtPaperService.getById(id);
		dto.setPaperId(id);
		dto.setTitle(byId.getTitle());
		dto.setRemark(byId.getRemark());

		List<SmtQuestion> questionList = smtQuestionService.list(Wrappers.<SmtQuestion>query().lambda().eq(ObjectUtil.isNotNull(id), SmtQuestion::getPaperId, id));
		List<SearchQuestionListRespDTO> dtoList=new ArrayList<>();
		for (SmtQuestion smtQuestion : questionList) {
			SearchQuestionListRespDTO questionDto=new SearchQuestionListRespDTO();
			BeanUtil.copyProperties(smtQuestion,questionDto);
			questionDto.setQuestionId(smtQuestion.getId());
			SmtPaperRecord selectOne = this.baseMapper.selectOne(Wrappers.<SmtPaperRecord>query().lambda().eq(SmtPaperRecord::getQuestionId, smtQuestion.getId()).eq(SmtPaperRecord::getBadge, searchPaperRecordReqDTO.getBadge()));
			if(ObjectUtil.isNotNull(selectOne))
			{
				questionDto.setAnswer(selectOne.getAnswer());
			}

			List<SmtSelect> selectList = smtSelectService.list( Wrappers.<SmtSelect>query().lambda().eq(ObjectUtil.isNotNull(smtQuestion.getId()), SmtSelect::getQuestionId, smtQuestion.getId()));

			List<SearchSelectRespDTO> selectListResp=new ArrayList<>();
			for (SmtSelect smtSelect : selectList) {
				SearchSelectRespDTO selectDto=new SearchSelectRespDTO();
				BeanUtil.copyProperties(smtSelect,selectDto);
				selectDto.setSelectId(smtSelect.getId());
				selectListResp.add(selectDto);
			}
			questionDto.setSelectList(selectListResp);
			dtoList.add(questionDto);

		}
		dto.setQuestionList(dtoList);
		return dto;
	}


	@Override
	public Boolean addRecord(AddPaperRecordReqDTO addPaperRecordReqDTO) {
		// TODO Auto-generated method stub
		List<AddQuestionRecordReqDTO> record = addPaperRecordReqDTO.getRecord();
		for (AddQuestionRecordReqDTO addQuestionRecordReqDTO : record) {

			SmtPaperRecord smtPaperRecord=new SmtPaperRecord();
			BeanUtil.copyProperties(addQuestionRecordReqDTO, smtPaperRecord);
			smtPaperRecord.insert();

		}
		return true;
	}


	@Override
	public PaperStatisticsVO statistics(Integer id) {
		// TODO Auto-generated method stub
		SmtPaper selectById = smtPaperService.getById(id);
		PaperStatisticsVO vo=new PaperStatisticsVO();
		vo.setTitle(selectById.getTitle());
		List<String> staffList=this.baseMapper.getPaperStaffTotal(id);
		vo.setTotalCount(staffList.size());
		List<QuestionStatisticsVO> questionVO =this.baseMapper.getQuestionStatistics(id);
		for (QuestionStatisticsVO questionStatisticsVO : questionVO) {
			List<SelectStatisticsVO> selectVo=this.baseMapper.getSelectStatistics(questionStatisticsVO.getId());
			questionStatisticsVO.setSelects(selectVo);
			Integer num=0;
			for (SelectStatisticsVO selectStatisticsVO : selectVo) {
				num+=selectStatisticsVO.getNum();
			}
			questionStatisticsVO.setTotalNum(num);
		}
		vo.setQuestions(questionVO);
		return vo;
	}


	@Override
	public void export(HttpServletResponse response, Integer id) {
		// TODO Auto-generated method stub
		SmtPaper selectById = smtPaperService.getById(id);
		// 通过工具类创建writer，默认创建xls格式
        ExcelWriter writer = ExcelUtil.getWriter();
        List<ExportPaperVO> list=new ArrayList<>();

        List<QuestionStatisticsVO> questionVO =this.baseMapper.getQuestionStatistics(id);
		for (QuestionStatisticsVO questionStatisticsVO : questionVO) {

			ExportPaperVO vo=new ExportPaperVO();
			vo.setName(questionStatisticsVO.getTitle());
			vo.setValue("-");
			list.add(vo);

			List<SelectStatisticsVO> selectVo=this.baseMapper.getSelectStatistics(questionStatisticsVO.getId());
			questionStatisticsVO.setSelects(selectVo);
			Integer num=0;
			for (SelectStatisticsVO selectStatisticsVO : selectVo) {
				num+=selectStatisticsVO.getNum();
			}
			questionStatisticsVO.setTotalNum(num);
			for (SelectStatisticsVO selectStatisticsVO : selectVo) {
				ExportPaperVO vos=new ExportPaperVO();
				vos.setName(selectStatisticsVO.getAnswer());
				int numAnswer=selectStatisticsVO.getNum();
				int total=questionStatisticsVO.getTotalNum();
				DecimalFormat df = new DecimalFormat("0.00");//格式化小数
				String f1 = df.format((float)numAnswer/total);//返回的是String类型
				Float ff=Float.parseFloat(f1)*100;
				vos.setValue(ff+"%");
				list.add(vos);
			}
		}

		java.util.ArrayList<ExportPaperVO> newArrayList = CollUtil.newArrayList(list);

        //自定义标题别名
        writer.addHeaderAlias("name", "问卷调查");
        writer.addHeaderAlias("value", "结果占比");
        // 合并单元格后的标题行，使用默认标题样式
        writer.merge(3, "调查表："+selectById.getTitle());
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(newArrayList, true);
        //out为OutputStream，需要写出到的目标流
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition","attachment;filename=analysis.xls");
        ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        writer.flush(out, true);
        // 关闭writer，释放内存
        writer.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(out);

	}

}
