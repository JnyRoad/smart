package com.tce.smart.app.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.platform.api.dto.SmtParkDTO;

import java.util.List;

/**
 *  新员工须知
 * @author fushiping
 */
public interface AppEmployeeNoteService extends IService<AppSubject> {

	IPage<AppSubject> getPageList(Page page, EmployeeNoteAo employeeNoteAo);

	/**
	 * 条件分页查询,根据园区过滤
	 *
	 * @param page 分页
	 * @param employeeNoteAo 员工信息
	 * @return 分页返回
	 */
	IPage<AppSubject> getPageListFilterByPark(Page page, EmployeeNoteAo employeeNoteAo);

	AppSubject noteDetail(Integer id);

    void noteDel(Integer id);

    void noteUpdate(EmployeeNoteAo employeeNoteAo);

    Integer noteAdd(EmployeeNoteAo employeeNoteAo);

	List<AppParkSubject> prakIdArray();

	/**
	 * 获取当前登录用户所属的园区信息列表
	 * @return
	 */
	List<SmtParkDTO> getParkList();

	List<SmtParkDTO> getUserPark();
}
