package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;
import org.apache.ibatis.annotations.Param;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
public interface SmtParkBuMapper extends BaseMapper<SmtParkBu> {

	List<SmtPark> getParkListByBu(Long compId);

	List<SmtPark> getUserParkByBu(@Param("compId") Integer compId,@Param("parkIds") List<Integer> parkIds);

}
