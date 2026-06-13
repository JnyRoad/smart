package com.tce.smart.bridge.isc.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/30 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class BlobInfo implements Serializable {

    private static final long serialVersionUID = -7925824593621326827L;

    private String base64;   //对象base64编码
    private String name;    //对象名
    private String id;      //对象id
    private String info;    //对象描述
    private String time;    //对象生成时间
    private String type;    //对象业务类型
    private String size;    //对象大小
    private String code;    //对象大小

    private String space;   //对象表所在namespace
    private String tableName; //对象表名

    private String small; //图片缩略图base64


    @Builder
    public BlobInfo(String base64, String name, String id, String info, String time, String type, String size, String code, String space, String tableName,String small) {
        this.base64 = base64;
        this.name = name;
        this.id = id;
        this.info = info;
        this.time = time;
        this.type = type;
        this.size = size;
        this.code = code;
        this.space = space;
        this.tableName = tableName;
        this.small = small;
    }
}