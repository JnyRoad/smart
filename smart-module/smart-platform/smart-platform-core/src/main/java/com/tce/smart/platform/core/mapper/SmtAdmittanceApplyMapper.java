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

	/**
	 * 按证件号、有效时间和申请区域统计未失效的重叠申请；所有随行人员均参与判重。
	 *
	 * @param certNo 需要校验的访客证件号，不能为空
	 * @param startTime 待申请通行开始时间
	 * @param endTime 待申请通行结束时间
	 * @param areaTypes 待申请区域；为空时按未知区域与全部区域冲突处理
	 * @return 重叠申请数量
	 */
	int countActiveFellowOverlapByCertNo(@Param("certNo") String certNo,
			@Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime,
			@Param("areaTypes") List<Integer> areaTypes);

	/**
	 * 对已存在的证件哈希锁行加 Oracle 行锁；无锁行时返回空，由调用方创建后重试。
	 *
	 * @param certNoHash 证件号的 SHA-256 哈希，不能记录明文证件号
	 * @return 被锁定的证件哈希；不存在时返回空
	 */
	String lockAdmittanceCertByHash(@Param("certNoHash") String certNoHash);

	/**
	 * 创建证件哈希锁行，唯一键冲突由调用方重新获取行锁。
	 *
	 * @param certNoHash 证件号的 SHA-256 哈希，不能记录明文证件号
	 * @return 新增锁行数量
	 */
	int insertAdmittanceCertLock(@Param("certNoHash") String certNoHash);

}
