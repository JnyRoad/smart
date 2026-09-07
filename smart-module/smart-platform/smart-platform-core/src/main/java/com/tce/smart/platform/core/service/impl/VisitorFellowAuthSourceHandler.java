package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.mapper.VisitorAuthOperationMapper;
import org.springframework.stereotype.Component;
/** 固定普通访客来源类型注册，禁止跨家庭或主体类型降级。 */
@Component public class VisitorFellowAuthSourceHandler extends VisitorAuthSourceSupport {
 public VisitorFellowAuthSourceHandler(VisitorAuthOperationMapper mapper){super(mapper);}
 public SourceKind sourceKind(){return SourceKind.VISITOR_FELLOW;}
 public SubjectType subjectType(){return SubjectType.VISITOR_FELLOW;}
}
