package com.tce.smart.platform.core.entity.ext;

import com.tce.smart.platform.core.entity.SmtEmailReceive;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MsgTemplateExt implements Serializable {

	private static final long serialVersionUID = 9138288943396176951L;

	  private SmtMsgTemplate msgTemplate;

	  private List<SmtEmailReceive> receiveList;
}
