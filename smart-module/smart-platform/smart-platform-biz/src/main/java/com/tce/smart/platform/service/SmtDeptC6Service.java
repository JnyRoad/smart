package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.resp.ExternalDepC6Tree;
import com.tce.smart.platform.core.entity.SmtDeptC6;
import com.tce.smart.platform.core.entity.SmtExDeptC6;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
public interface SmtDeptC6Service extends IService<SmtDeptC6> {

	/**
	 * 获得许昌c6部门树
	 * @param
	 * @return
	 */
	List<ExternalDepC6Tree> getC6List();

	SmtDeptC6 getByC6No(String no);
}
