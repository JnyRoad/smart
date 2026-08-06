package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 园区信息
 *
 * @author mingkai.wu
 * @date 2019-05-09 17:19:50
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteParkService {

	/**
	 * 通过id查询园区表
	 *
	 * @return
	 */
	@GetMapping("/park/app/{id}")
	Result<SmtParkDTO> getPakrById(@RequestParam("id") final Integer parkId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取所有园区信息(不分页,未过滤)
	 *
	 * @return
	 */
	@GetMapping("/park/app/all")
	Result<List<SmtParkDTO>> getParkList(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 展示所有的园区(根据用户所属园区过滤过滤)
	 * @return
	 */
	@GetMapping("park/all")
	Result<List<SmtParkDTO>> getParks(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 分页获取园区信息
	 *
	 * @param current  当前页
	 * @param size     分页大小
	 * @param parkName 园区名称
	 * @return
	 */
	@GetMapping("/park/app/page")
	Result<Page<SmtParkRespDTO>>  getParkByPage(@RequestParam("current") final Integer current, @RequestParam("size") final Integer size,
												@RequestParam("parkName") final String parkName, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 分页查询token 信息
	 *
	 * @param smtPark 园区信息
	 * @return Result<?>
	 */
	@PostMapping("/park/location")
	Result<SmtParkDTO> locationPark(@RequestBody SmtParkDTO smtPark,@RequestHeader(SecurityConstants.FROM) String from);
}
