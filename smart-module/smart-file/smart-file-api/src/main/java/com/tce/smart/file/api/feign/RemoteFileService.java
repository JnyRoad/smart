package com.tce.smart.file.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign分发接口
 *
 * @Author WangJinbo
 * @Date 2019/11/06
 */
@FeignClient(value = ServiceNameConstants.SMART_FILE)
public interface RemoteFileService {
	/**
	 * 根据ID获取文件
	 *
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/inner/file/search/{id}")
	@ApiOperation("根据ID获取文件")
	Result<byte[]> getById(@PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据ID查询园区存储的文件
	 *
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/inner/file/search/{parkId}/{id}")
	@ApiOperation("根据ID查询园区存储的文件")
	Result<byte[]> getById(@PathVariable("parkId") Integer parkId, @PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 保存文件，自动创建ID，不指定文件类型
	 *
	 * @param file
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/save")
	@ApiOperation("保存文件（自动创建ID）")
	Result<String> save(@RequestBody byte[] file, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 保存文件，ID手动传入，不指定文件类型
	 *
	 * @param file
	 * @param id
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/save/{id}")
	@ApiOperation("保存文件（ID手动传入）")
	Result<Boolean> save(@RequestBody byte[] file, @PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据ID删除文件
	 *
	 * @param id
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/delete/{id}")
	@ApiOperation("根据ID删除文件")
	Result<Boolean> deleteById(@PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据ID修改文件
	 *
	 * @param file
	 * @param id
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/update/{id}")
	@ApiOperation("根据ID修改文件")
	Result<Boolean> updateById(@RequestBody byte[] file, @PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 保存文件，自动创建ID，不指定文件类型
	 *
	 * @param file
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/cover")
	@ApiOperation("覆盖保存文件（自动创建ID）")
	Result<String> coverSave(@RequestBody byte[] file, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 保存文件，ID手动传入，不指定文件类型
	 *
	 * @param file
	 * @param id
	 * @param from
	 * @return
	 */
	@PostMapping("/inner/file/cover/{id}")
	@ApiOperation("覆盖保存文件（ID手动传入）")
	Result<Boolean> coverSave(@RequestBody byte[] file, @PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

}
