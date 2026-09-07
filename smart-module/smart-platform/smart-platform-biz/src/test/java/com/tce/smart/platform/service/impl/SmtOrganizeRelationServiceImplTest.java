package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.api.dto.req.OrganizeRelationReqDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;

public class SmtOrganizeRelationServiceImplTest {
	@Test public void reliableRejectionKeepsOrganizationAccessUnchanged() {
		for(Class<?> type:Arrays.asList(SmtStaff.class,SmtOrganizeAccess.class,SmtOrganizeRelation.class))TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),type);
		SmtOrganizeRelationServiceImpl service=Mockito.spy(new SmtOrganizeRelationServiceImpl());
		EmployeeAuthOperationAdapter adapter=Mockito.mock(EmployeeAuthOperationAdapter.class);Mockito.when(adapter.isEnabled()).thenReturn(true);Mockito.when(adapter.organizationDiff(Mockito.anyList(),Mockito.anyList(),Mockito.anyList(),Mockito.anyInt())).thenReturn(false);
		SmtOrganizeAccessService accesses=Mockito.mock(SmtOrganizeAccessService.class);Mockito.when(accesses.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtStaffService staff=Mockito.mock(SmtStaffService.class);SmtStaff person=new SmtStaff();person.setId(10L);Mockito.when(staff.list(Mockito.any())).thenReturn(Collections.singletonList(person));
		SmtOrganizeRelation relation=new SmtOrganizeRelation();relation.setId(7L);relation.setParkId(1);Mockito.doReturn(relation).when(service).getById(7L);
		ReflectionTestUtils.setField(service,"employeeAuthOperationAdapter",adapter);ReflectionTestUtils.setField(service,"organizeAccessService",accesses);ReflectionTestUtils.setField(service,"staffService",staff);
		Assert.assertFalse(ReflectionTestUtils.invokeMethod(service,"authAccess",Collections.singletonList(9),relation));
		Mockito.verify(accesses,Mockito.never()).delByOrgId(7L);Mockito.verify(accesses,Mockito.never()).saveBatch(Mockito.anyCollection());
	}

 @Test public void organizationGateRunsBeforeStaffUpdateAcquiresRows() {
  for(Class<?> type:Arrays.asList(SmtStaff.class,SmtOrganizeAccess.class,SmtOrganizeRelation.class))TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),type);
  SmtOrganizeRelationServiceImpl service=Mockito.spy(new SmtOrganizeRelationServiceImpl());
  EmployeeAuthOperationAdapter adapter=Mockito.mock(EmployeeAuthOperationAdapter.class);Mockito.when(adapter.isEnabled()).thenReturn(true);
  Mockito.when(adapter.organizationDiff(Mockito.anyList(),Mockito.anyList(),Mockito.anyList(),Mockito.anyInt())).thenThrow(new IllegalStateException("组门禁拒绝"));
  SmtStaffMapper staffMapper=Mockito.mock(SmtStaffMapper.class);Mockito.when(staffMapper.update(Mockito.any(),Mockito.any())).thenAnswer(i->{throw new AssertionError("组门禁之前不应更新并锁住员工行");});
  SmtOrganizeAccessService accesses=Mockito.mock(SmtOrganizeAccessService.class);Mockito.when(accesses.list(Mockito.any())).thenReturn(Collections.emptyList());
  SmtStaffService staff=Mockito.mock(SmtStaffService.class);SmtStaff person=new SmtStaff();person.setId(10L);Mockito.when(staff.list(Mockito.any())).thenReturn(Collections.singletonList(person));
  SmtOrganizeRelation persisted=new SmtOrganizeRelation();persisted.setId(7L);persisted.setParkId(1);Mockito.doReturn(persisted).when(service).getById(7L);
  ReflectionTestUtils.setField(service,"employeeAuthOperationAdapter",adapter);ReflectionTestUtils.setField(service,"smtStaffMapper",staffMapper);
  ReflectionTestUtils.setField(service,"organizeAccessService",accesses);ReflectionTestUtils.setField(service,"staffService",staff);
  OrganizeRelationReqDTO request=new OrganizeRelationReqDTO();request.setId(7L);request.setUserName("synthetic-user");request.setCompName("合成组织");request.setParkId(1);request.setDeviceAuthId(Collections.singletonList(9));
  try{service.updateBu(request);Assert.fail("组门禁必须拒绝");}catch(IllegalStateException expected){Assert.assertEquals("组门禁拒绝",expected.getMessage());}
  Mockito.verify(staffMapper,Mockito.never()).update(Mockito.any(),Mockito.any());
 }
}
