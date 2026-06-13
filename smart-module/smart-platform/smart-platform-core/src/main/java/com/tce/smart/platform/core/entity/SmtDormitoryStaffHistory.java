package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 员工宿舍信息历史表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Data
@TableName("smt_dormitory_staff_history")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryStaffHistory extends Model<SmtDormitoryStaffHistory> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 员工id
   */
    private Long staffId;
    /**
   * 员工名称
   */
    private String staffName;

    /**
     * 性别
     */
    private Integer staffSex;
    /**
   * 员工工号
   */
    private String staffBadge;
    /**
   * 园区id
   */
    private Integer parkId;
    /**
   * 园区名称
   */
    private String parkName;
    /**
   * 宿舍楼id
   */
    private Integer dormitoryId;
    /**
   * 宿舍楼名称
   */
    private String dormitoryName;
    /**
   * 楼层id
   */
    private Integer floorId;
    /**
   * 楼层层数
   */
    private Integer floorName;
    /**
   * 房间id
   */
    private Integer roomId;
    /**
   * 房间号
   */
    private Integer roomName;
    /**
   * 床位id
   */
    private Integer bedId;
    /**
   * 床位编号  1-10
   */
    private Integer bedNumber;
    /**
   * 宿舍类型id
   */
    private Integer dormitoryTypeId;
    /**
   * 宿舍类型名称
   */
    private String dormitoryTypeName;

    /**
     * 住宿类型  0-入住  1-换宿  2-退宿
     */
    private Integer  type;

    /**
     * 最初入住时间
     */
    private Date inTime;

    /**
   * 操作时间  如：type是0，该时间是入住时间，type是1，该时间是换宿时间，type是2，该时间是退宿时间
   */
    private Date time;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 关联员工入住表
     */
    private Integer dfId;

    /**
     * 是否是员工 0-否  1-是
     */
    private Integer isStaff;

	/**
	 * 岗位名称
	 */
	private String jobName;

	/**
	 * buname
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 是否参与统计 0.不参与 1.参与
	 */
	private Integer statisFlag;

	/**
	 * 操作人名称
	 */
	private String optUser;

	/**
	 * 入住操作人
	 */
	private String inOptUser;

	/**
	 * 入住操作时间
	 */
	private Date inCreateTime;
}
