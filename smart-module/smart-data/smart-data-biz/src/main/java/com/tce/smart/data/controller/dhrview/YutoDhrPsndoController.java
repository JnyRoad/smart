package com.tce.smart.data.controller.dhrview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
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
     * 根据 BU 列表分页获取 DHR 员工数据。
     *
     * 返回内容包含证件号、手机号和邮箱等敏感字段，只允许服务令牌用于定时同步，
     * 禁止继续通过旧的通用 {@code /page} 路径访问。
     *
     * @return DHR 员工分页数据
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/internal/page")
    public Result<Page<YutoDhrPsndoDTO>> page(@RequestParam("current") Integer current,
											  @RequestParam("size") Integer size,
											  @RequestParam("buIds") List<Integer> buIds) {
        Page page = new Page(current,size);
        return success(dhrPsndoService.getPage(page,buIds));
    }

	/**
	 * 由受控服务根据员工工号读取员工性质。
	 *
	 * 工号是可枚举的员工标识，不能向已认证外部用户直接开放。
	 *
	 * @return 员工性质
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/properties")
	public Result<String> getProperties(@RequestParam("badge") String badge) {
		return success(dhrPsndoService.getProperties(badge));
	}

	/**
	 * 由受控服务根据用户标识读取员工工号。
	 *
	 * 用户标识与工号的映射不得作为外网枚举入口。
	 *
	 * @return 员工工号
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/internal/badge/{userId}")
	public Result<String> getBadgeByUserId(@PathVariable("userId") String userId) {
		return success(dhrPsndoService.getBadgeByUserId(userId));
	}
}
