package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.OfficeZoneApproveQueryDTO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 16:54
 */

public interface SmtArticlesReleaseMapper extends BaseMapper<SmtArticlesRelease> {

	IPage<SmtArticlesRelease> getOfficeZoneApprovalPage(Page page, @Param("query") OfficeZoneApproveQueryDTO queryDTO, @Param("parkIds") List<Integer> parkIds, @Param("approveBadge") String approveBadge);
}
