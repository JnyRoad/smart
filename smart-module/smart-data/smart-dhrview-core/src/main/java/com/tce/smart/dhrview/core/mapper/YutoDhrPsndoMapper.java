package com.tce.smart.dhrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YutoDhrPsndoMapper extends BaseMapper<YutoDhrPsndo> {
	IPage<YutoDhrPsndo> getDhrEmpList(Page page, @Param("pkOrgs") List<String> pkOrgs);

	YutoDhrPsndo getByUserId(@Param("userId") String userId);
}
