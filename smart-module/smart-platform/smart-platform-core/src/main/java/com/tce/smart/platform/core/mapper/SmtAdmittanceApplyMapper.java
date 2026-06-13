package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import com.tce.smart.platform.core.dto.SearchTodayVisitorDTO;
import com.tce.smart.platform.core.dto.SearchVisitorAppDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.vo.SearchAppSmtVisitorVO;
import com.tce.smart.platform.core.vo.SearchAppVisitorDetailVO;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
public interface SmtAdmittanceApplyMapper extends BaseMapper<SmtAdmittanceApply> {

	SearchVisitorDetail selectVisitorById(@Param("query") SmtVisitor smtVisitor);

	IPage<SearchTodayVisitor> getTodayVisitor(Page page, @Param("query") SearchTodayVisitorDTO searchTodayVisitorDTO, @Param("park") List<Integer> parkIdList);

	SearchTadayVisitorDetail selectTodayVisitorById(@Param("query") SmtVisitor smtVisitor);

	IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, @Param("query") SearchSmtVisitorDTO searchSmtVisitorDTO, @Param("park") List<Integer> parkIdList);

	SearchAppVisitorDetailVO selectAppVisitorById(@Param("query") SmtVisitor smtVisitor);

	IPage<SearchAppSmtVisitorVO> searchAppVisitorPage(Page page, @Param("query") SearchVisitorAppDTO searchVisitorAppDTO);

	List<SmtStaff> searchReceptionist(@Param("query") SmtVisitor smtVisitor);

	List<SmtStaff> searchReceptionistForTemp(@Param("query") SmtVisitor smtVisitor);

	Boolean updateSmsCode(@Param("id") Long id);

	int countActiveMainFellowOverlapByCertNo(@Param("certNo") String certNo,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime);

}
