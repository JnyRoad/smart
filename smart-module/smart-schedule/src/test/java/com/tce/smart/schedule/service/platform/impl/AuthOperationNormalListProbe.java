package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.service.impl.*;
import com.tce.smart.platform.controller.*;
import org.junit.Assert;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.lang.reflect.Constructor;
import java.util.*;

/** 普通员工与权限列表的真实 MVC/Service/Mapper 路径，同一个四连接池，不返回伪造页面。 */
final class AuthOperationNormalListProbe {
 private final AuthOperationSloFixture fixture;
 private final MockMvc mvc;
 private final int expectedStaff;
 private final Map<String,Set<String>> firstPageIds=new HashMap<>();
 AuthOperationNormalListProbe(AuthOperationSloFixture fixture,int people) throws Exception {
  this.fixture=fixture;expectedStaff=people;requireSchema(fixture);
  MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);cfg.setDefaultStatementTimeout(30);cfg.addInterceptor(fixture.sqlTiming);
  MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(fixture.pool);factory.setConfiguration(cfg);
  PaginationInterceptor pagination=new PaginationInterceptor();pagination.setDialectType("oracle");factory.setPlugins(new org.apache.ibatis.plugin.Interceptor[]{pagination});
  List<Resource> resources=new ArrayList<>();for(Class<?> mapper:Arrays.asList(SmtStaffMapper.class,SmtDeviceAuthorityMapper.class,SmtParkBuMapper.class,SmtOrganizeRelationMapper.class,SmtParkMapper.class)){cfg.addMapper(mapper);resources.add(new ClassPathResource("mapper/"+mapper.getSimpleName()+".xml"));}
  factory.setMapperLocations(resources.toArray(new Resource[0]));SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
  SmtStaffServiceImpl staff=new SmtStaffServiceImpl();ReflectionTestUtils.setField(staff,"baseMapper",session.getMapper(SmtStaffMapper.class));
  SmtDeviceAuthorityServiceImpl authority=realWithUnusedConstructorDependencies(SmtDeviceAuthorityServiceImpl.class);ReflectionTestUtils.setField(authority,"baseMapper",session.getMapper(SmtDeviceAuthorityMapper.class));ReflectionTestUtils.setField(authority,"smtDeviceAuthorityMapper",session.getMapper(SmtDeviceAuthorityMapper.class));
  SmtParkBuServiceImpl parkBu=new SmtParkBuServiceImpl();ReflectionTestUtils.setField(parkBu,"baseMapper",session.getMapper(SmtParkBuMapper.class));ReflectionTestUtils.setField(parkBu,"mapper",session.getMapper(SmtParkBuMapper.class));
  SmtOrganizeRelationServiceImpl organization=new SmtOrganizeRelationServiceImpl();ReflectionTestUtils.setField(organization,"baseMapper",session.getMapper(SmtOrganizeRelationMapper.class));
  SmtParkServiceImpl park=realWithUnusedConstructorDependencies(SmtParkServiceImpl.class);ReflectionTestUtils.setField(park,"baseMapper",session.getMapper(SmtParkMapper.class));ReflectionTestUtils.setField(park,"mapper",session.getMapper(SmtParkMapper.class));
  ReflectionTestUtils.setField(staff,"smtDeviceAuthorityService",authority);ReflectionTestUtils.setField(staff,"smtParkBuService",parkBu);ReflectionTestUtils.setField(staff,"smtOrganizeRelationService",organization);ReflectionTestUtils.setField(staff,"smtParkService",park);
  mvc=MockMvcBuilders.standaloneSetup(new SmtStaffController(staff,null,null,null,null,null),new SmtDeviceAuthorityController(authority)).build();
 }
 static void requireSchema(AuthOperationSloFixture fixture) {
  Map<String,String> required=new LinkedHashMap<>();
  required.put("SMT_STAFF","ID BADGE NAME DEP_ABBR COMP_NAME DEP_NAME JCHE_NAME JOB_NAME CREATE_TIME STATUS DORMITORY_STATUS CERTNO FACE_PIC_ID COMP_ID");
  required.put("SMT_DEVICE_AUTHORITY","ID TYPE AREA_TYPE AUTHORITY_NAME REMARK CREATE_TIME PARK_ID");
  required.put("SMT_PARK","ID PARK_NAME PARK_LONGITUDE PARK_LATITUDE PARK_ADDRESS RADIUS PARK_PHONE BRIDGE_URL AREA WORK_SHOP_NUM DINING_ROOM_NUM");
  required.put("SMT_ORGANIZE_RELATION","ID USER_ID COMP_NAME PARK_ID USER_NAME SOURCE COMP_ID CREATE_TIME COMP_TYPE");
  required.put("SMT_APP_STAFF_AUTH","STAFF_ID AUTH_ID");required.put("SMT_APP_AUTH","ID AUTH_NAME");
  List<String> missing=new ArrayList<>();
  required.forEach((table,columns)->{Set<String> actual=new HashSet<>(fixture.jdbc.queryForList("SELECT COLUMN_NAME FROM USER_TAB_COLUMNS WHERE TABLE_NAME=?",String.class,table));for(String column:columns.split(" "))if(!actual.contains(column))missing.add(table+"."+column);});
  fixture.report.put("normalListRequiredColumns",required);fixture.report.put("normalListMissingColumns",missing);Assert.assertTrue("真实普通列表 schema 前置缺失，禁止自动 DDL: "+missing,missing.isEmpty());
 }
 private static <T> T realWithUnusedConstructorDependencies(Class<T> type) throws Exception {
  // 只装配页面实际调用的依赖；其它业务方法不在测试范围，不能用 mock list 绕过页面链。
  Constructor<?>[] constructors=type.getConstructors();Assert.assertEquals("明确唯一生产构造器",1,constructors.length);return type.cast(constructors[0].newInstance(new Object[constructors[0].getParameterCount()]));
 }
 Map<String,Object> request(String endpoint,String phase,int index,long planned) {
  Map<String,Object> record=new LinkedHashMap<>();record.put("endpoint",endpoint);record.put("phase",phase);record.put("sample",index);record.put("plannedNanos",planned);long entered=System.nanoTime();record.put("enteredNanos",entered);
  try {
   SmartUser user=new SmartUser(1,1,"slo-list",Collections.singletonList(fixture.park),"unused",true,true,true,true,Collections.emptyList());SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,Collections.emptyList()));
   org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request;
   if("staff".equals(endpoint))request=MockMvcRequestBuilders.post("/staff/page").param("current","1").param("size","20").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(new JSONObject().put("compId",fixture.companyId()).put("parkId",fixture.park).put("status",1).toString());
   else request=MockMvcRequestBuilders.get("/device/authority/page").param("current","1").param("size","20").param("type","1").param("parkId",String.valueOf(fixture.park));
   org.springframework.mock.web.MockHttpServletResponse response=mvc.perform(request).andReturn().getResponse();long returned=System.nanoTime();record.put("returnedNanos",returned);record.put("durationNanos",returned-entered);record.put("status",response.getStatus());Assert.assertEquals(200,response.getStatus());
   JSONObject root=JSONUtil.parseObj(response.getContentAsString());JSONObject page=root.getJSONObject("data");Assert.assertNotNull("正常列表必须返回真实分页数据",page);
   long total=page.getLong("total");Assert.assertEquals("staff".equals(endpoint)?expectedStaff:5,total);JSONArray records=page.getJSONArray("records");Assert.assertEquals("staff".equals(endpoint)?20:5,records.size());
   Set<String> ids=new TreeSet<>();for(Object value:records){JSONObject row=JSONUtil.parseObj(value);ids.add(row.getStr("id"));Assert.assertEquals("slo-park-"+fixture.park,row.getStr("parkName"));}
   synchronized(firstPageIds){Set<String> previous=firstPageIds.putIfAbsent(endpoint,ids);if(previous!=null)Assert.assertEquals("同数据同页不能缺行或换页",previous,ids);}
   record.put("total",total);record.put("recordIds",ids);record.put("contentVerified",true);
  }catch(Throwable failure){record.put("error",failure.getClass().getName()+": "+failure.getMessage());record.put("contentVerified",false);record.putIfAbsent("returnedNanos",System.nanoTime());}
  finally{SecurityContextHolder.clearContext();}
  return record;
 }
}
