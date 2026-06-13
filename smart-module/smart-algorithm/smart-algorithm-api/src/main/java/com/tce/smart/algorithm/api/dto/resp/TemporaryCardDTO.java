package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wuxinjian
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TemporaryCardDTO extends BaseDTO {
    /**
     *姓名
     */
    @Desc(type = "姓名")
	@ApiModelProperty("姓名")
    private String name;
    /**
     *性别
     */
    @Desc(type = "性别")
	@ApiModelProperty("性别")
    private String sex;
    /**
     *证件号码
     */
    @Desc(type = "公民身份号码")
	@ApiModelProperty("公民身份号码")
    private String idNum;
    /**
     *名族
     */
    @Desc(type = "民族")
	@ApiModelProperty("民族")
    private String nation;
    /**
     *出生日期
     */
    @Desc(type = "出生")
	@ApiModelProperty("出生")
    private String birth;
    /**
     *地址
     */
    @Desc(type = "住址")
	@ApiModelProperty("住址")
    private String address;
    /**
     *头像
     */
    @Desc(type = "头像")
	@ApiModelProperty("头像")
    private String headImg;
    /**
     *处理后照片
     */
    @Desc(type = "处理后的图片")
	@ApiModelProperty("处理后的图片")
    private String handleImg;
    /**
     *签发机关
     */
    @Desc(type = "签发机关")
	@ApiModelProperty("签发机关")
    private String issueAuthority;
    /**
     *有效期限
     */
    @Desc(type = "有效期限")
	@ApiModelProperty("有效期限")
    private String expiryDate;
}
