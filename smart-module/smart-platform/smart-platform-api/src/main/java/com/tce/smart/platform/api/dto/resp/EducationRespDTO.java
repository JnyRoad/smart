package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 应聘者教育查询
 * @author tce
 *
 */
@Data
public class EducationRespDTO extends BaseVO {


	private Integer id;

	  private String educationHisId;
	    /**
	   * 教育开始时间
	   */
	    private String startTime;
	    /**
	   * 教育结束时间
	   */
	    private String endTime;
	    /**
	   * 学校名称
	   */
	    private String schoolName;
	    /**
	   * 专业
	   */
	    private String major;
	    /**
	   * 学历
	   */
	    private String education;

	    /**
	     * 学位
	     */
	    private String degree;

	    private String degreeDesc;

	    private String educationDesc;

	    /**
	     * 是否最高学历
	     */
	    private Integer isHighEduType;

	    /**
	     * 是否最高学位
	     */
	    private Integer isHighDegreeType;
}
