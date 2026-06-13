package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.ApplicationDTO;
import com.tce.smart.platform.core.dto.ApplicationListDTO;
import com.tce.smart.platform.core.entity.SmtApplication;
import com.tce.smart.platform.core.entity.SmtApplicationProcess;
import com.tce.smart.platform.core.entity.SmtApplicationResume;
import com.tce.smart.platform.core.vo.ApplicationListVO;
import com.tce.smart.platform.core.vo.ApplicationVO;
import com.tce.smart.platform.core.vo.FaceApplicationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
public interface SmtApplicationMapper extends BaseMapper<SmtApplication> {

	IPage<ApplicationVO> getSmtApplictionPage(Page page, @Param("query") ApplicationDTO applicationDTO, @Param("parkIds")List<Integer> parkIds);


	List<SmtApplicationProcess> getApplicationProcess(Long id);


	SmtApplicationResume getApplicationResume(Long id);


	IPage<ApplicationListVO> getSmtApplictionPage(Page page, @Param("query") ApplicationListDTO applicationDTO);


	IPage<ApplicationListVO> getSmtApplictionList(Page page, @Param("query") ApplicationListDTO applicationDTO, @Param("ids") List<String> staffRecruitAuthLeve, @Param("parks") List<Integer> parks );


	FaceApplicationVO queryFaceApplication(@Param("applicationId") Long applicationId);


}
