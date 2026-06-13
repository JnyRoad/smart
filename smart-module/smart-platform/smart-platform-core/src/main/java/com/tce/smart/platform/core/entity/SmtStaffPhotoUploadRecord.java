package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("smt_staff_photo_upload_record")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SmtStaffPhotoUploadRecord extends Model<SmtStaffPhotoUploadRecord> {

	private static final long serialVersionUID = 1L;
	/**
	*
	*/
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;


	/**
	 * 上传时间
	 */
	private LocalDateTime createTime;
	/**
	 * 状态 0-失败  1-成功
	 */
	private Integer status;

	/**
	 * 创建者
	 */
	private String createUser;

	/**
	 * 上传的图片id
	 */
	private String facePicId;


}
