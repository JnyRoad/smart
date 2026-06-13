package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.OrganizeRelationDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author
 * @date 2019-04-15 11:34:54
 */
public interface SmtOrganizeRelationMapper extends BaseMapper<SmtOrganizeRelation> {
	OrganizeRelationDTO getOrgRelation(@Param("id") Long id);
}
