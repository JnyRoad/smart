package com.tce.smart.platform.core.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryStaffDTO {


    /**
   * 员工id
   */
    private Long staffId;

	/**
	 * 员工号
	 */
	private String badge;

    /**
   * 床位id
   */
    private Integer bedId;


    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 入住时间
     */
    private String createTime;

    /**
     * 唯一识别id
     */
    private Integer id;


	/**
	 * BU名称
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 职位名称
	 */
	private String jobName;

	/**
	 * 备注
	 */
	private String simpleRemark;
}
