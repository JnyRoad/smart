package com.tce.smart.app.ao;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wangxinyang
 * @version 1.0
 * @date 2019/4/29 13:56
 **/
@Data
public class EmployeeNoteAo {
	private Integer id;
    private String createTime;
    private String subjectName;
    private Integer parkId;
    private String picBinary;
    private String textDesc;
    private String enclosureName;
	private String enclosure;
    private String subjectUrl;
}
