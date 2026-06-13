package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 紧急联系人
 * @author qipei
 *
 */
@Data
public class RelationAo {

	private String applicationId;

	private String relationType;

	private String emergencyName;

	private String emergencyPhone;


}
