package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.entity.SmtIscCardTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IscCardTaskPageVO extends SmtIscCardTask {

	private String name;

	private String parkName;
}
