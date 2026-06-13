package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtLbejConfig;
import com.tce.smart.platform.core.vo.SearchAreaVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuling
 *
 */
public interface SmtLbejConfigMapper extends BaseMapper<SmtLbejConfig> {

}
