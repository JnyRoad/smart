package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: PassPortDTO
 * @Package com.tce.operator.jsiot.bean
 * @Description:
 * @Author tan.hongwei
 * @Date 2019/9/10 10:28
 * @Version V2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PassPortDTO extends BaseDTO {
    /**
     * 护照号码
     */
    @Desc(type = "护照号码")
	@ApiModelProperty("护照号码")
    private String id;
    /**
     * 本国姓名
     */
    @Desc(type = "本国姓名")
	@ApiModelProperty("本国姓名")
    private String name;
    /**
     * 英文姓名
     */
    @Desc(type = "英文姓名")
	@ApiModelProperty("英文姓名")
    private String englishName;
    /**
     * 性别
     */
    @Desc(type = "性别")
	@ApiModelProperty("性别")
    private String sex;
    /**
     * 出生日期
     */
    @Desc(type = "出生日期")
	@ApiModelProperty("出生日期")
    private String birthDate;
    /**
     * 持证人国籍代码
     */
    @Desc(type = "持证人国籍代码")
	@ApiModelProperty("持证人国籍代码")
    private String nationalityCode;
    /**
     * 出生地点
     */
    @Desc(type = "出生地点")
	@ApiModelProperty("出生地点")
    private String birthAddress;
    /**
     * 头像
     */
    @Desc(type = "头像")
	@ApiModelProperty("头像")
    private String headImg;
    /**
     * 处理后照片
     */
    @Desc(type = "处理后的图片")
	@ApiModelProperty("处理后的图片")
    private String handleImg;
    /**
     * 签发国代码
     */
    @Desc(type = "签发国代码")
	@ApiModelProperty("签发国代码")
    private String issueCountry;
    /**
     * 签发地点
     */
    @Desc(type = "签发地点")
	@ApiModelProperty("签发地点")
    private String issueAddress;
    /**
     * 签发日期
     */
    @Desc(type = "签发日期")
	@ApiModelProperty("签发日期")
    private String issueDate;
    /**
     * MR1码
     */
    @Desc(type = "MR1码")
	@ApiModelProperty("MR1码")
    private String mrz1;
    /**
     * MR2码
     */
    @Desc(type = "MR2码")
	@ApiModelProperty("MR2码")
    private String mrz2;

}
