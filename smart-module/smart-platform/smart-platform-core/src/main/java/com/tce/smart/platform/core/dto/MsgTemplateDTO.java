package com.tce.smart.platform.core.dto;

import java.util.List;

import com.tce.smart.platform.core.entity.SmtEmailReceive;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;

import lombok.Data;

@Data
public class MsgTemplateDTO {


	  private  SmtMsgTemplate msgTemplate;

	  private List<SmtEmailReceive> receiveList;
}
