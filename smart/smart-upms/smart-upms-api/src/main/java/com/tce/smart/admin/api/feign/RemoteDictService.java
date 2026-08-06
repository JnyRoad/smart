package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteDictService {
	/**
	 * 通过ID获取字典数据
	 *
	 * @param id
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/dict/id")
    Result<SysDict> findById(@RequestParam("id") Integer id , @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据类型查询字典
	 *
	 * @param type
	 * @param from  调用标志
	 * @return
	 */
	@GetMapping("/dict/type")
    Result<List<SysDict>> findByType(@RequestParam("type") String type , @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据类型和数据值查询字典
	 *
	 * @param type
	 * @param value
	 * @param from  调用标志
	 * @return
	 */
	@GetMapping("/dict/value")
    Result<SysDict> findByValue(@RequestParam("type") String type , @RequestParam("value") String value , @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据类型和数据值保存字典
	 *
	 * @param type
	 * @param value
	 * @param from  调用标志
	 * @return
	 */
	@GetMapping("/dict/save")
    Result saveDict(@RequestParam("type") String type, @RequestParam("label") String label, @RequestParam("value") String value,
                    @RequestHeader(SecurityConstants.FROM) String from,
                    @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
