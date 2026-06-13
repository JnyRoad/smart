package com.tce.smart.data.api.dto.intergration.req;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: luohongwen.
 * @Date:Created in 2019/11/25 .
 * @Description:
 */
@Data
public class EhrSyncPersonReqDTO implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * 同步操作类型
     * 1：新增
     * 2：修改
     * 3：删除
     */
    private Integer syncType;

    /**
     * 主键
     */
    private Long id;
    /**
     * 人员分类ID
     */
    private Integer categoryId;
    /**
     *  人员名称
     */
    private String personName;
    /**
     * 人员编号
     */
    private String personNo;
    /**
     * 手机号
     */
    private String phoneNo;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 身份证号
     */
    private String cardNo;
    /**
     * 民族
     */
    private String nation;
    /**
     * 出生日期
     */
    private String birthday;
    /**
     * 家庭住址
     */
    private String address;
    /**
     * 签发机关
     */
    private String signOrg;
    /**
     * 签发日期
     */
    private String signDate;
    /**
     * 有效期至
     */
    private String validityEndDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 系统用户ID
     */
    private Long sysUserId;
    /**
     * 实名验证状态
     * 1:成功 0:失败 2:未实名
     */
    private Integer realNameState;
    /**
     * 园区编号
     */
    private Integer parkId;

    /**
     * 公司ID
     */
    private Integer compId;

    /**
     * 公司名称
     */
    private String compName;

    /**
     * 部门编号
     */
    private Integer deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 职位编号
     */
    private Integer positionId;
    /**
     * 职位名称
     */
    private String positionName;
    /**
     * 员工状态
     */
    private Integer workerState;
    /**
     * 员工状态描述
     */
    private String workerStateDesc;
    /**
     * 籍贯编号
     */
    private Integer nativePlaceId;
    /**
     * 籍贯描述
     */
    private String nativePlaceDesc;

    /**
     * 职级ID
     */
    private Integer empGrade;

    /**
     * 职级描述
     */
    private String empGradeName;

    /**
     * 直接上级工号
     */
    private String reportToBadge;

    /**
     * 人员信息MD5
     */
    private String personMd5;
}
