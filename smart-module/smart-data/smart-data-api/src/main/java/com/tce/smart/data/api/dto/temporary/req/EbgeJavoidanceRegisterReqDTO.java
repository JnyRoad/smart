package com.tce.smart.data.api.dto.temporary.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工亲属表
 * @author QIPEI
 *
 */
@Data
public class EbgeJavoidanceRegisterReqDTO implements Serializable {

    private Integer id;

	private Integer ezid;

	private Integer EID;

	private Integer status;

	private String Badge;

	private String Name;

	private Integer Compid;

	private Integer Depid;

	private String Jobid;

	private Integer JchenID;

	private Integer RelativesGX;

	private String RelativesBadge;

	private Integer RelativesCompid;

	private Integer RelativesDepid;

	private Integer RelativesJobid;

	private Integer REJchenID;

	private String remark;

	private Integer seqid;

	private String qsgx;

}
