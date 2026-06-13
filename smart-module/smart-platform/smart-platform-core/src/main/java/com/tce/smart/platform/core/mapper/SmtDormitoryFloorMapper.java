package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.vo.FloorVO;

/**
 * 园区宿舍楼的楼层
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:15
 */
public interface SmtDormitoryFloorMapper extends BaseMapper<SmtDormitoryFloor> {

	Integer queryBedByRomm(SmtDormitoryRoom room);

	List<SmtDormitoryFloor> queryFloor(Integer dormitoryId);

	IPage<List<FloorVO>> getSmtDormitoryFloorPage(Page page, @Param("query")SmtDormitoryFloor smtDormitoryFloor,@Param("park") List<Integer> parkIdList);

	Integer selectMaxFloor(Integer dormitoryId);

}
