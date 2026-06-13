package com.tce.smart.platform.core.mapper;


import java.util.List;

import com.tce.smart.platform.core.dto.dormitorymanage.BedDetailDTO;
import com.tce.smart.platform.core.vo.DormitoryStaffFamilyVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DormitoryBedPageQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.core.vo.DormitoryStaffVO;

/**
 * 园区宿舍楼l楼层中房间的床位数
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:21
 */
public interface SmtDormitoryBedMapper extends BaseMapper<SmtDormitoryBed> {

	IPage<DormitoryStaffVO> getDormitoryBedOfStaff(Page page, @Param("query") DormitoryBedPageQueryDTO bed,@Param("park") List<Integer> parkIdList);

	List<DormitoryStaffFamilyVO> getStaffFamily(@Param("query") DormitoryBedPageQueryDTO bed, @Param("park") List<Integer> parkIdList);

	Integer getLeaveStaff( @Param("query") DormitoryBedPageQueryDTO bed);

	Integer getFreeBedCount(@Param("parkId") Integer parkId);

	List<BedDetailDTO> getBedDetail(@Param("roomId") Integer roomId);

	Integer delBedByIds(@Param("ids") List<Integer> ids);

}
