package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/15 .
 * @Modified By:
 */
@Data
public class OrgrelationAO{
    private Integer orgrelationId;   //关系id，新增时为空，修改有值
    private Integer relationType;    //亲属关系@详见接口备注
    private String orgPersonEid;    //员工工号
    private String orgPersonName;   //亲属姓名
    private String orgPersonGender; //性别@详见接口备注
    private String orgPersonBu; //工作单位
    private String orgPersonDept;   //部门
    private String orgPersonSection; //课别
    private String relationDetail;
    private String orgPersonJob; //岗位


}
