package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.tce.smart.platform.core.dto.SnapPersonStatisDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchSnapPersonAccessDTO;
import com.tce.smart.platform.core.entity.SmtSnapPerson;
import com.tce.smart.platform.core.vo.SearchSmtSnapPersonVO;
import com.tce.smart.platform.core.vo.SmtSnapPersonDetailVO;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
public interface SmtSnapPersonMapper extends BaseMapper<SmtSnapPerson> {

	SmtSnapPersonDetailVO getSnapPersonVisotorById(@Param("query") SmtSnapPerson smtSnapPerson);

	SmtSnapPersonDetailVO getSnapPersonFellowVisotorById( @Param("query")SmtSnapPerson smtSnapPerson);

	SmtSnapPersonDetailVO getSnapPersonAdmittanceFellowById( @Param("query")SmtSnapPerson smtSnapPerson);

	SmtSnapPersonDetailVO getSnapPersonById( @Param("query")SmtSnapPerson smtSnapPerson);

	IPage<SearchSmtSnapPersonVO> getSmtSnapPersonPage(Page page, @Param("query") SearchSnapPersonAccessDTO searchSnapPersonAccessDto,@Param("park") List<Integer> parkIdList);

	IPage<SearchSmtSnapPersonVO> getSmtSnapVisitorPersonPage(Page page, @Param("query") SearchSnapPersonAccessDTO searchSnapPersonAccessDto,@Param("park") List<Integer> parkIdList);

	List<SmtSnapPerson> getSnapPersonList(@Param("query") SearchSnapPersonAccessDTO searchSnapPersonAccessDto);

	List<SnapPersonStatisDTO> getSnapPersonStatis(@Param("parkId") Integer parkId);

}
