package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtRecruitmentDTO;
import com.tce.smart.platform.api.dto.resp.JobListRespDTO;
import com.tce.smart.platform.api.dto.resp.RecruitmentRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位招聘
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteRecruitmentService {



	/**
	 * 获取招聘岗位列表
	 * @param jobListAO
	 * @return
	 */
/*	@PostMapping("/recruitment/job/list")
	Result<Page<SmtRecruitment>> getJobList(@RequestBody JobListAO jobListAO, @RequestHeader(SecurityConstants.FROM) String from);
*/

	/**
	 * 获取招聘详情
	 * @param id
	 * @return
	 */
	@GetMapping("/recruitment/app/{id}")
	Result<RecruitmentRespDTO> getById(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 修改招聘岗位
	 * @param smtRecruitmentDTO smtRecruitmentDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/recruitment/app/updateRecruitment")
    Result updateById(@RequestBody SmtRecruitmentDTO smtRecruitmentDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取所有岗位列表
	 * @param smtRecruitment
	 * @param fromIn
	 * @return
	 */
	@PostMapping("/recruitment/job/list")
	Result<List<JobListRespDTO>> getJobList(@RequestBody SmtRecruitmentDTO smtRecruitment, @RequestHeader(SecurityConstants.FROM) String fromIn);


	/**
	 * 定时更新招聘岗位时间
	 * @param fromIn
	 * @return
	 */
	@GetMapping("/recruitment/refreshRecruitment")
	Result refreshRecruitmentById(@RequestHeader(SecurityConstants.FROM) String fromIn,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 定时更新组织信息
	 * @param fromIn
	 * @return
	 */
	@GetMapping("/recruitment/refreshComp")
	Result refreshComp(@RequestHeader(SecurityConstants.FROM) String fromIn,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
