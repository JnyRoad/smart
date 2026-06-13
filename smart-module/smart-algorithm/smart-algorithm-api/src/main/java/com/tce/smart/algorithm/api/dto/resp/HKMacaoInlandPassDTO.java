package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.algorithm.api.annotation.Desc;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: HKMacaoInlandPassDTO
 * @Package com.tce.smart.algorithm.api.dto.resp
 * @Description:
 * @Author wuxinjian
 * @Date 2019/9/10 10:28
 * @Version V2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HKMacaoInlandPassDTO extends BaseDTO {
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
     * 其他姓名
     */
    @Desc(type = "其他姓名")
	@ApiModelProperty("其他姓名")
    private String otherName;
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
     * 本证有效期至
     */
    @Desc(type = "本证有效期至")
	@ApiModelProperty("本证有效期至")
    private String expire;
    /**
     * 港澳证件号码
     */
    @Desc(type = "港澳证件号码")
	@ApiModelProperty("港澳证件号码")
    private String hkmID;
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
     * 签发机关
     */
    @Desc(type = "签发机关")
	@ApiModelProperty("签发机关")
    private String issueOffice;
    /**
     * 签发日期
     */
    @Desc(type = "签发日期")
	@ApiModelProperty("签发日期")
    private String issueDate;
    /**
     * 换证次数
     */
    @Desc(type = "换证次数")
	@ApiModelProperty("换证次数")
    private String changeTime;
    /**
     * 归属地
     */
    @Desc(type = "归属地")
	@ApiModelProperty("归属地")
    private String belongArea;


}
