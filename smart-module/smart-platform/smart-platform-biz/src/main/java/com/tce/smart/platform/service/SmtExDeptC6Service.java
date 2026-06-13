package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.resp.ExternalDepC6Tree;
import com.tce.smart.platform.core.entity.SmtExDeptC6;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
public interface SmtExDeptC6Service extends IService<SmtExDeptC6> {

	SmtExDeptC6 getForDId(Long dId);
}
