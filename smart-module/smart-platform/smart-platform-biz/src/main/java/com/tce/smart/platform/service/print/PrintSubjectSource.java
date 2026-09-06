package com.tce.smart.platform.service.print;
import com.fasterxml.jackson.databind.node.ObjectNode;
/** 只从受权限保护的人员或申请记录构造打印资料，浏览器不能覆盖字段。 */
public interface PrintSubjectSource {
    ObjectNode load(String parkId,String subjectType,String subjectId);
    /** 保持既有单方法实现兼容；搜索由实际业务适配器显式提供。 */
    default ObjectNode search(String parkId,String subjectType,String keyword,int current,int size) {
        throw new PrintApiException(503,"PRINT_SUBJECT_SEARCH_NOT_SUPPORTED","人员源尚未支持搜索");
    }
}
