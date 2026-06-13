package com.tce.smart.platform.api.dto.resp;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 员工问卷详情
 * @author 齐佩
 *
 */
@Data
public class SearchPaperRecordDetailRespDTO {




	/**
	 * 问卷id
	 */
	private Integer paperId;

	/**
	 * 问卷标题
	 */
	private String title;

	/**
	 * 问卷备注
	 */
	private String remark;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;


	/**
	 * 所属园区id
	 */
	private Integer parkId;


	/**
	 * 问题列表
	 */
	private List<SearchQuestionListRespDTO> questionList;


}
