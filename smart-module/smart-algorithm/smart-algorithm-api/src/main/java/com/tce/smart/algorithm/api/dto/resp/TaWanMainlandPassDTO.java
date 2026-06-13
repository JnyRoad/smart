package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: HKMacaoInlandPassDTO
 * @Package com.tce.operator.jsiot.bean
 * @Description:
 * @Author tan.hongwei
 * @Date 2019/9/10 10:28
 * @Version V2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaWanMainlandPassDTO extends BaseDTO {
    /**
     * 证件号码
     */
    @Desc(type = "证件号码")
	@ApiModelProperty("证件号码")
    private String id;
    /**
     * 中文姓名
     */
    @Desc(type = "中文姓名")
	@ApiModelProperty("中文姓名")
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
     * 有效期限
     */
    @Desc(type = "有效期限")
	@ApiModelProperty("有效期限")
    private String expire;
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
     * 签发地点
     */
    @Desc(type = "签发地点")
	@ApiModelProperty("签发地点")
    private String issueAddress;
    /**
     * 换证次数
     */
    @Desc(type = "换证次数")
	@ApiModelProperty("换证次数")
    private String issueTime;
    /**
     * 签发机关
     */
    @Desc(type = "签发机关")
	@ApiModelProperty("签发机关")
    private String issueOffice;


}
