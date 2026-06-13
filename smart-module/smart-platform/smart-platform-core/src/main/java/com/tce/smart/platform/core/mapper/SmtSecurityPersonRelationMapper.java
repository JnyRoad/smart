package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SecurityPersonRelationDTO;
import com.tce.smart.platform.core.entity.ext.SecurityPersonRelationExt;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityPersonRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
public interface SmtSecurityPersonRelationMapper extends BaseMapper<SmtSecurityPersonRelation> {

	/**
	 * 获得分页数据
     * @param query
	 * @return
	 */
	IPage<SecurityPersonRelationDTO> getPage(Page page, @Param("query")SecurityPersonRelationExt query);

	/**
	 * 查询列表
	 * @param query
	 * @return
	 */
	List<SmtSecurityPersonRelation> getList(@Param("query") SecurityPersonRelationExt query);

	/**
	 * 根据id删除
	 * @param relationId
	 * @return
	 */
	List<SmtSecurityPersonRelation> getListById(@Param("relationId") List<Long> relationId);

	/**
	 * 根据id获得分页数据
	 * @param query
	 * @return
	 */
	IPage<SecurityPersonRelationDTO> getPageById(Page page, @Param("query")SecurityPersonRelationExt query);
}
