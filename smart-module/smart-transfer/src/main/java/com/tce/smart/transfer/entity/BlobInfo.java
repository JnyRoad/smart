package com.tce.smart.transfer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: BlobInfo
 * @date: 2020/11/13 11:45
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlobInfo {
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
}
