package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.entity.SmtAuthSelectionSource;
/** 只在调用方本库事务内锁定、核验或 CAS；不得发送 HTTP，也不自行完成 selection。 */
public interface AuthSourceHandler<B extends BusinessSnapshot> {
 SourceKind sourceKind();
 SubjectType subjectType();
 int snapshotVersion();
 Class<B> snapshotType();
 /** 按该业务明确的主体/父行锁序核对服务器给出的完整冻结投影。 */
 void lockAndValidate(SourceSelection<B> source);
 boolean applyExact(SmtAuthSelectionSource source,B business);
}
