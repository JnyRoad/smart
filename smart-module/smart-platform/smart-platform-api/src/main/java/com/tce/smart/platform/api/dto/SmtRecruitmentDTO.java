package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 招聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:34
 */
@Data
public class SmtRecruitmentDTO extends Model<SmtRecruitmentDTO> {
	private static final long serialVersionUID = 8973727816995991301L;

	/**
	 * 招聘id
	 */
	private Integer id;
	/**
	 * 所属园区id
	 */
	private Integer parkId;
	/**
	 * 岗位ID
	 */
	@NotBlank(message = "岗位不能为空")
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * BUId
	 */
	@NotBlank(message = "bu不能为空")
	private String compId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门ID
	 */
	@NotBlank(message = "部门不能为空")
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层ID
	 */
	@NotBlank(message = "职层不能为空")
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;
	/**
	 * 专业
	 */
	private String major;

	/**
	 * 学历
	 */
	private String education;

	/**
	 * 工作经验
	 */
	private Integer workYear;
	/**
	 * 福利层次
	 */
	@NotBlank(message = "福利层次不能为空")
	private String welfareLevel;
	/**
	 * 工资范围起始
	 */
	@NotBlank(message = "工资的最小值不能为空")
	private Integer salaryStart;
	/**
	 * 工资范围结束
	 */
	@NotBlank(message = "工资的最大值不能为空")
	private Integer salaryEnd;
	/**
	 * 年龄范围起始
	 */
	@NotBlank(message = "年龄的最小值不能为空")
	@Min(value = 18, message = "最小年龄不得低于18岁")
	private Integer ageStart;
	/**
	 * 年龄范围结束
	 */
	@NotBlank(message = "年龄的最小大值不能为空")
	private Integer ageEnd;
	/**
	 * 招聘人数
	 */
	@NotBlank(message = "招聘人数不能为空")
	private Integer recruitNum;
	/**
	 * 0-无，1-普通话， 2-英语 3-法语 4-德语
	 *
	 */
	private String reqLanguage;
	/**
	 * 0-无，1-一般，2-熟练 3-精通
	 */
	private String compRequire;
	/**
	 * 福利 以逗号去分割
	 */
	@NotBlank(message = "福利不能为空")
	private String welfare;
	/**
	 * 职位描述
	 */
	@NotBlank(message = "职位描述不能为空")
	private String jobCotent;
	/**
	 * 0-招聘结束 1-招聘中 2-停止招聘 默认是1
	 *
	 */
	@NotBlank(message = "招聘状态不能为空")
	private Integer status;
	/**
	 * 发布人姓名
	 */
	@NotBlank(message = "发布人姓名不能为空")
	private String createUser;
	/**
	 * 发布时间，单位秒
	 */
	private Date createTime;
	/**
	 * 开始时间 单位s
	 */
	@NotBlank(message = "招聘开始时间不能为空")
	private Date startTime;
	/**
	 * 结束时间，单位s
	 */
	@NotBlank(message = "招聘结束时间不能为空")
	private Date endTime;

	/**
	 * 是否置顶 0-否 1-是
	 */
	private Integer isUp;

	/**
	 * 联系方式
	 */
	private String relation;

}
