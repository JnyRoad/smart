package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class LeaveHandoverApplicationVO extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
     * 员工号
     */
      private String employeeId;
      /**
       * 员工名称
       */
      private String employeeName;
      /**
       * 公司名称
       */
      private String buName;
      /**
       * 部门名称
       */
      private String depName;
      /**
       * 岗位名称
       */
      private String jobName;
      /**
       * 入职时间 yyyy-MM-dd
       */
      private String entryTime;
      /**
       * 离职时间 yyyy-MM-dd
       */
      private String dismissionDate;
      /**
       * 离职类型
       */
      private String dismissionTypeDesc;

      /**
       * 离职原因
       */
      private String dismissionReasonDesc;
      /**
       * 剩余年假
       */
      private Double restDatCount;

      /**
       * 流程编号
       */
      private String processId;

      /**
       * 0：审批中；
	 * 1：通过；
       * 2：拒绝；
	 * 3：交接开始；；
	 * 4：交接完成
       */
      private Integer approveStatus;
}
