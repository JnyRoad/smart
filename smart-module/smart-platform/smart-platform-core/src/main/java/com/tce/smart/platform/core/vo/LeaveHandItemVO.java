package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;

@Data
public class LeaveHandItemVO extends BaseVO{

      private static final long serialVersionUID = 1L;

      private String depName;

      private List<LeaveHandoverDepItemVO> handItem;

}
