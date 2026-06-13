package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DormitoryBedPageQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.core.vo.DormitoryVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 园区宿舍楼表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:25
 */
public interface SmtDormitoryMapper extends BaseMapper<SmtDormitory> {


	Integer queryRoomByFloor(SmtDormitoryFloor floor);

	IPage<List<DormitoryVO>> getSmtDormitoryPage(Page page,  @Param("query") SmtDormitory smtDormitory,@Param("park") List<Integer> parkIdList);

	List<SmtDormitory> queryDormitory(Integer parkId);

	Integer getLeaveStaff( @Param("query") DormitoryBedPageQueryDTO bed);
}
