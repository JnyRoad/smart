package com.tce.smart.platform.service.print;

import com.tce.smart.platform.core.entity.print.PrintTemplate;
import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import java.util.Map;

/** 发布前调用可信渲染器；失败必须抛错，返回值只保留校验摘要。 */
public interface PrintPublicationValidator {
    Map<String, Object> validate(PrintTemplate template, PrintTemplateVersion version);
}
