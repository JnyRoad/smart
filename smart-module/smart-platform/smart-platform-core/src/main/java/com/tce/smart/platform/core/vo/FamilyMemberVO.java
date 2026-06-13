package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/15 .
 * @Modified By:
 */
@Data
public class FamilyMemberVO  {
    private Integer familyMemberId; //家庭成员ID
    private Integer relationType; //亲属关系@详见接口备注
    private String relationTypeDesc; //亲属关系描述
    private String familyName; //亲属姓名
    private Integer familyGender;

    private String familyGenderDesc;  //性别

    private String familyCompany; //公司

    private String emergencyPhone;

    private String familyJob;

    private String familyBirthday;

}
