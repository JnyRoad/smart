package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import com.tce.smart.platform.core.dto.SearchTodayVisitorDTO;
import com.tce.smart.platform.core.dto.SearchVisitorAppDTO;
import com.tce.smart.platform.core.entity.SearchTadayVisitorDetail;
import com.tce.smart.platform.core.entity.SearchTodayVisitor;
import com.tce.smart.platform.core.entity.SearchVisitorDetail;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.vo.SearchAppSmtVisitorVO;
import com.tce.smart.platform.core.vo.SearchAppVisitorDetailVO;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;

/**
 * 访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
public interface SmtVisitorMapper extends BaseMapper<SmtVisitor> {

	/**
	 * @Title:TODO
	 * @Param :
	 * @Return :SearchVisitorDetail
	 * @Exception :
	 * @Author:TCE-Liangyuan
	 * @Date： 2019年4月17日 上午10:25:01
	 */

	SearchVisitorDetail selectVisitorById(@Param("query")SmtVisitor smtVisitor);

	IPage<SearchTodayVisitor> getTodayVisitor(Page page, @Param("query") SearchTodayVisitorDTO searchTodayVisitorDTO,@Param("park") List<Integer> parkIdList);

	SearchTadayVisitorDetail selectTodayVisitorById(@Param("query")SmtVisitor smtVisitor);

	IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, @Param("query") SearchSmtVisitorDTO searchSmtVisitorDTO, @Param("park") List<Integer> parkIdList);

	SearchAppVisitorDetailVO selectAppVisitorById(@Param("query")SmtVisitor smtVisitor);

	IPage<SearchAppSmtVisitorVO>  searchAppVisitorPage(Page page, @Param("query")SearchVisitorAppDTO searchVisitorAppDTO);

	List<SmtStaff> searchReceptionist(@Param("query") SmtVisitor smtVisitor);

	List<SmtStaff> searchReceptionistForTemp(@Param("query") SmtVisitor smtVisitor);

	Boolean updateSmsCode(@Param("id") Long id);


}
