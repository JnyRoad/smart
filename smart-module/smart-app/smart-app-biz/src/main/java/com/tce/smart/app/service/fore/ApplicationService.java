package com.tce.smart.app.service.fore;

import java.util.Map;

import com.tce.smart.app.ao.fore.ApplicationAo;
import com.tce.smart.app.ao.fore.OperationAo;
import com.tce.smart.app.vo.fore.ApplicationDetailVo;
import com.tce.smart.common.core.model.Result;

/**
 * 招聘管理接口
 * @author qipei
 *
 */
public interface ApplicationService {

	Result getApplicationList(Map<String, Object> params, ApplicationAo application);

	ApplicationDetailVo getApplicationDetail(String applicationId);

	Result getJobsiftList(Integer parkId);

	Result getOtptypeList();

	Result getRecord(String applicationId);

	Result getFaceList(String facePhoto);

	Result operationApplication(OperationAo operationAo);


}
