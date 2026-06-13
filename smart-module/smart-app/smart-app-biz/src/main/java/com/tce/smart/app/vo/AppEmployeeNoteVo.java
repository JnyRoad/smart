package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fsp
 * @version 1.0
 * @date 2019/4/29 16:00
 **/
@Data
public class AppEmployeeNoteVo extends BaseVO {

    private Integer id;
    private String subjectName;
    private String subjectUrl;
	private String textName;
    private String textDesc;
	private String picBinary;
	private Integer parkId;
    private String parkName;
	private String enclosure;
	private String enclosureName;
    private LocalDateTime createTime;
}
