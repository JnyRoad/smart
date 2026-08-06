package com.tce.smart.data.controller.dhrview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.dhrview.resp.YutoDhrPsndoDTO;
import com.tce.smart.data.service.dhr.DhrPsndoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author wuling
 * @since 2021-05-27
 */
@RestController
@RequestMapping("/empdhr/ys")
public class YutoDhrPsndoController extends BaseController {

    @Autowired
    private DhrPsndoService dhrPsndoService;

    /**
     * 根据BU列表分页获取DHR员工数据
     * @return
     */
    @GetMapping("/page")
    public Result<Page<YutoDhrPsndoDTO>> page(@RequestParam("current") Integer current,
											  @RequestParam("size") Integer size,
											  @RequestParam("buIds") List<Integer> buIds) {
        Page page = new Page(current,size);
        return success(dhrPsndoService.getPage(page,buIds));
    }

	/**
	 * 根据员工工号获得员工性质
	 * @return
	 */
	@GetMapping("/get/properties")
	public Result<String> getProperties(@RequestParam("badge") String badge) {
		return success(dhrPsndoService.getProperties(badge));
	}

	/**
	 * 根据userId获取员工工号
	 * @return
	 */
	@GetMapping("/badge/{userId}")
	public Result<String> getBadgeByUserId(@PathVariable("userId") String userId) {
		return success(dhrPsndoService.getBadgeByUserId(userId));
	}
}
