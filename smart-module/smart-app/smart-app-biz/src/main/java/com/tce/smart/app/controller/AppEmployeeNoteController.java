package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppEmployeeNoteService;
import com.tce.smart.app.vo.AppEmployeeNoteListVo;
import com.tce.smart.app.vo.AppEmployeeNoteVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新员工须知控制器
 *
 * @author fushiping
 * @version 1.0
 * @date 2019/4/29 13:43
 **/
@RestController
@AllArgsConstructor
@RequestMapping("/appemployeenote")
public class AppEmployeeNoteController extends BaseController{
    private final AppEmployeeNoteService appEmployeeNoteService;

    /**
     *  分页查询
     * @param page
     * @param employeeNoteAo
     * @return
     */
    @PostMapping("/page/list")
    public Result getPageList(Page page, @RequestBody(required = false) EmployeeNoteAo employeeNoteAo) {
		IPage<AppSubject> iPage = appEmployeeNoteService.getPageListFilterByPark(page, employeeNoteAo);
		return success(iPage, AppEmployeeNoteListVo.class);
    }

    /**
     *  新员工须知详情
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result noteDetail(@PathVariable Integer id) {
		AppSubject appSubject = appEmployeeNoteService.noteDetail(id);
		return success(appSubject, AppEmployeeNoteVo.class);
    }

    /**
     *  新员工须知删除
     * @param id
     * @return
     */
    @GetMapping("/del/{id}")
    public Result noteDel(@PathVariable Integer id) {
        appEmployeeNoteService.noteDel(id);
		return success();
	}

    /**
     *  新员工须知修改
     * @param employeeNoteAo
     * @return
     */
    @PostMapping("/updateSubject")
    public Result noteUpdate(@RequestBody EmployeeNoteAo employeeNoteAo) {
        appEmployeeNoteService.noteUpdate(employeeNoteAo);
		return success();
	}

    /**
     *  新员工须知添加
     * @param employeeNoteAo
     * @return
     */
    @PostMapping("/add")
    public Integer noteAdd(@RequestBody EmployeeNoteAo employeeNoteAo/*,@RequestParam("file") MultipartFile file*/) {
        return appEmployeeNoteService.noteAdd(employeeNoteAo);
    }

	/**
	 *  获取所有园区集合
	 * @param
	 * @return
	 */
	@GetMapping("/getParkId")
	public List<AppParkSubject> getParkIdArray() {
		return appEmployeeNoteService.prakIdArray();
	}

	/**
	 *  获取所有园区集合
	 * @param
	 * @return
	 */
	@GetMapping("/user/parkIs")
	public List<SmtParkDTO> getUserParks() {
		return appEmployeeNoteService.getUserPark();
	}

	/**
	 *  获取当前登录用户关联的园区集合
	 * @param
	 * @return
	 */
	@GetMapping("/getParkList")
	public List<SmtParkDTO> getParkList() {
		return appEmployeeNoteService.getParkList();
	}

}
