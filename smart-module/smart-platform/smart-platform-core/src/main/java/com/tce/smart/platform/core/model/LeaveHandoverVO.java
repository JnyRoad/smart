package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.core.vo.LeaveHandoverApplicationVO;
import com.tce.smart.platform.core.vo.LeaveHandoverItemVO;
import lombok.Data;

import java.util.List;

@Data
public class LeaveHandoverVO extends BaseVO{

      private static final long serialVersionUID = 1L;

      private LeaveHandoverApplicationVO employee;

      private List<LeaveHandoverItemVO> handover;

}
