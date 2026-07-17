package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
public interface SmtSecurityTaskDetailsMapper extends BaseMapper<SmtSecurityTaskDetails> {

	/**
	 * 统计批次内去重后的受理人员数量。一个人员可对应多条权限明细，不能直接按明细行数展示。
	 */
	@Select("SELECT COUNT(DISTINCT STAFF_ID) FROM SMT_SECURITY_TASK_DETAILS "
			+ "WHERE APPLY_ID = #{applyId} AND DISPATCH_BATCH_ID = #{dispatchBatchId}")
	int countDispatchPeople(@Param("applyId") Long applyId, @Param("dispatchBatchId") Long dispatchBatchId);

}
