package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author
 * @date 2019-04-15 11:34:54
 */
public interface SmtExternalDeptMapper extends BaseMapper<SmtExternalDept> {
	Boolean deleteDirector(@Param("id") Long id);
}
