package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭成员
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/15 .
 * @Modified By:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FamilyMemberAO extends BaseAO {
    private static final long serialVersionUID = 2989503428698873117L;

    private Integer familyMemberId;  //关系id，新增时为空，修改有值
    private Integer relationType;    //亲属关系@详见接口备注
    private String familyName;  //亲属姓名
    private String familyGender;    //亲属性别@详见接口备注
    private String familyBirthday;  //出生日期
    private String familyCompany;   //工作单位
    private String familyJob;   //职务
    private String emergencyPhone;  //紧急联系人电话
    private Integer staffId; //员工id

}
