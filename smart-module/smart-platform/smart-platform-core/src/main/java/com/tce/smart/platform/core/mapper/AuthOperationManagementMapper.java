package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementAttemptRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限操作管理端的只读分页查询。
 */
public interface AuthOperationManagementMapper {

	IPage<AuthOperationManagementBatchRow> selectBatchPage(Page<AuthOperationManagementBatchRow> page,
			@Param("filter") AuthOperationManagementBatchFilter filter);

	AuthOperationManagementBatchRow selectBatch(@Param("batchId") Long batchId,
			@Param("allowedParkIds") List<Integer> allowedParkIds);

	IPage<AuthOperationManagementTargetRow> selectTargetPage(Page<AuthOperationManagementTargetRow> page,
			@Param("filter") AuthOperationManagementTargetFilter filter);

	List<AuthOperationManagementAttemptRow> selectLatestAttempts(@Param("targetIds") List<Long> targetIds);
}
