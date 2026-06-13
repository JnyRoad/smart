package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtDormitoryType;
import com.tce.smart.platform.core.vo.DormitoryTypeVO;

/**
 * 园区宿舍类型
 *
 * @author 齐佩
 * @date 2019-04-13 18:16:57
 */
public interface SmtDormitoryTypeMapper extends BaseMapper<SmtDormitoryType> {

	IPage<DormitoryTypeVO> getSmtDormitoryTypePage(Page page,@Param("query") SmtDormitoryType smtDormitoryType,@Param("park") List<Integer> parkIdList);

}
