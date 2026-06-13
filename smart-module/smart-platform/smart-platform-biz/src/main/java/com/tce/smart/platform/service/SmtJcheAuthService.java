package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.core.entity.SmtJcheAuth;
import com.tce.smart.tool.enums.BusinessAuthorityEnum;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author fushiping
 * @date 2020/9/3 11:12
 **/

public interface SmtJcheAuthService extends IService<SmtJcheAuth> {


	Integer getJchebusinessCode(Integer jcheId, Integer parkId);


}
