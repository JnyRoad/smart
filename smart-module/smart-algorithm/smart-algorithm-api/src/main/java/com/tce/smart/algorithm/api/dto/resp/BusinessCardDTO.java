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
public class BusinessCardDTO extends BaseDTO {
    /**
     * 统一社会信用代码
     */
    @Desc(type = "统一社会信用代码")
	@ApiModelProperty("统一社会信用代码")
    private String creditCode;
    /**
     * 组织机构码
     */
    @Desc(type = "组织机构代码")
	@ApiModelProperty("组织机构代码")
    private String organizationCode;
    /**
     * 税务码
     */
    @Desc(type = "税务登记证号")
	@ApiModelProperty("税务登记证号")
    private String taxCode;
    /**
     * 社保码
     */
    @Desc(type = "社保登记号")
	@ApiModelProperty("社保登记号")
    private String socialCode;
    /**
     *
     */
    @Desc(type = "统计证证号")
	@ApiModelProperty("统计证证号")
    private String statisticalCode;
    /**
     * 名称
     */
    @Desc(type = "名称")
	@ApiModelProperty("名称")
    private String name;
    /**
     * 类型
     */
    @Desc(type = "类型")
	@ApiModelProperty("类型")
    private String type;
    /**
     * 住址
     */
    @Desc(type = "住所")
	@ApiModelProperty("住所")
    private String address;
    /**
     * 法人
     */
    @Desc(type = "法定代表人")
	@ApiModelProperty("法定代表人")
    private String legalPerson;
    /**
     * 组成形式
     */
    @Desc(type = "组成形式")
	@ApiModelProperty("组成形式")
    private String formation;
    /**
     * 注册资金
     */
    @Desc(type = "注册资本")
	@ApiModelProperty("注册资本")
    private String money;
    /**
     * 成立日期
     */
    @Desc(type = "成立日期")
	@ApiModelProperty("成立日期")
    private String createTime;
    /**
     * 营业期限
     */
    @Desc(type = "营业期限")
	@ApiModelProperty("营业期限")
    private String operatingPeriod;
    /**
     * 营业范围
     */
    @Desc(type = "经营范围")
	@ApiModelProperty("经营范围")
    private String Scope;
    /**
     * 登记机关
     */
    @Desc(type = "登记机关")
	@ApiModelProperty("登记机关")
    private String authority;
    /**
     * 登记日期
     */
    @Desc(type = "登记日期")
	@ApiModelProperty("登记日期")
    private String registrationDate;
    /**
     * 二维码
     */
    @Desc(type = "二维码")
	@ApiModelProperty("二维码")
    private String qrCode;
    /**
     * 副本
     */
    @Desc(type = "副本")
	@ApiModelProperty("副本")
    private String copy;

}
