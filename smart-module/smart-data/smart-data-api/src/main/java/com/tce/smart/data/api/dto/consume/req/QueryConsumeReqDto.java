package com.tce.smart.data.api.dto.consume.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * App员工人脸信息采集
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Data
public class QueryConsumeReqDto implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 分页信息
	 */
	private Page<?> page;

	/**
	 * 员工号
	 */
	private String empNo;

	/**
	 * 查询开始时间
	 */
	private Date startDate;

	/**
	 * 查询结束时间
	 */
	private Date endDate;
}
