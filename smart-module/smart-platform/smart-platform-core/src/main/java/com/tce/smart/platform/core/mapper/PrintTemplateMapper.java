package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.api.dto.req.print.PrintListQuery;
import com.tce.smart.platform.core.entity.print.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import java.util.List;

/** 打印模板、不可变发布版本、组合与幂等审计的持久化边界。 */
public interface PrintTemplateMapper {
    int insertTemplate(PrintTemplate value);
    int updateTemplate(PrintTemplate value);
    PrintTemplate findTemplate(@Param("id") String id);
    PrintTemplate lockTemplate(@Param("id") String id);
    List<PrintTemplate> listTemplates(PrintListQuery query, RowBounds bounds);
    long countTemplates(PrintListQuery query);
    int insertTemplateVersion(PrintTemplateVersion value);
    int updateTemplateVersion(PrintTemplateVersion value);
    PrintTemplateVersion findTemplateVersion(@Param("id") String id);
    List<PrintTemplateVersion> findVersions(@Param("templateId") String templateId);
    long nextVersionNo(@Param("templateId") String templateId);
    int insertTemplatePair(PrintTemplatePair value);
    int updateTemplatePair(PrintTemplatePair value);
    PrintTemplatePair findTemplatePair(@Param("id") String id);
    PrintTemplatePair lockTemplatePair(@Param("id") String id);
    List<PrintTemplatePair> listPairs(PrintListQuery query, RowBounds bounds);
    long countPairs(PrintListQuery query);
    PrintOperation findOperation(@Param("principalId") String principalId, @Param("key") String key);
    int insertOperation(PrintOperation operation);
    int completeOperation(PrintOperation operation);
    int insertAudit(PrintAudit audit);
}
