package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新员工须知列表返回
 * @author fushiping
 * @date 2019/10/15 12:01
 **/
@Data
public class AppEmployeeNoteListVo extends BaseVO {

	private static final long serialVersionUID = 2730731118173887668L;

	private Integer id;
	private String subjectName;
	private String picBinary;
	private Integer parkId;
	private String parkName;
	private LocalDateTime createTime;
}
