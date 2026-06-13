package com.tce.smart.xcc6.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.xcc6.core.entity.RsEmp;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 人事信息Mapper
 *
 * @author mkwu
 * @date 2019-07-29
 */
public interface RsXCEmpMapper extends BaseMapper<RsEmp> {

	RsEmp getRsEmpByEmpNo(@Param("empNo") String empNo);

	Boolean updateRsEmp(@Param("rsEmp") RsEmp rsEmp);

	Map<String,Object> queryEmpPhoto(@Param("empNo") String empNo);
}
