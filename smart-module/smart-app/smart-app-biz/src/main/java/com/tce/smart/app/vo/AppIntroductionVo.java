package com.tce.smart.app.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lbw
 * @version 1.0
 * @date 2019/4/30 10:50
 **/
@Data
public class AppIntroductionVo {

    private Integer id;
    private byte[] picBinary;
    private String subjectName;
    private Integer subjectOrder;
    private String textDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
