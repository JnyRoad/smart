package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.ExternalDeptReqDTO;
import com.tce.smart.platform.api.dto.resp.ExternalDepTree;
import com.tce.smart.platform.core.entity.SmtExternalDept;

import java.util.List;

/**
 *
 *
 * @author
 * @date 2019-04-15 11:34:43
 */
public interface SmtExternalDeptService extends IService<SmtExternalDept> {

	List<ExternalDepTree> getCompTree(List<Integer> parkIds);

	Boolean editDept(ExternalDeptReqDTO externalDeptReqDTO);

	SmtExternalDept getByName(String deptName, Long compId);

	List<SmtExternalDept> getList();

	List<SmtExternalDept> getList(Long compId);

	Boolean deleteDept(Long id);

	Boolean deleteDirector(Long id);
}
