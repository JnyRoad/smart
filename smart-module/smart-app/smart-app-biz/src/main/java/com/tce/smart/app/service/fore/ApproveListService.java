package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.ApproveListRequestVO;
import com.tce.smart.common.core.model.Result;

/**
 * 待审批
 *
 * @author 王艳勇
 * @date 2019-05-10 16:16:08
 */
public interface ApproveListService {

    /**
     * 待审批
     * @param approveListRequestVO
     * @return
     */
    Result getProcessRecord(ApproveListRequestVO approveListRequestVO);

}
