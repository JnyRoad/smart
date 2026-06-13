package com.tce.smart.platform.api.dto.req;

import java.util.List;

import lombok.Data;

/**
 * 提交问卷调查答案
 * @author 齐佩
 *
 */
@Data
public class AddPaperRecordReqDTO {

	List<AddQuestionRecordReqDTO> record;
}
