package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.core.entity.SmtWechatBanding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 微信绑定表
 *
 * @author fushiping
 * @date 2021-10-09 17:20:23
 */
public interface SmtWechatBandingMapper extends BaseMapper<SmtWechatBanding> {

	IPage<SmtWechatBanding> queryPage(Page page, @Param("query") WechatBandingReqDTO query,@Param("parkIds")  List<Integer> parkIds);

}
