package com.tce.smart.platform.service.print.schema;

import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.*;
import java.util.*;
import static org.junit.Assert.*;

/** 用本版本发布器建库，再运行现有 Mapper 的全部打印表插入，不复制测试夹具 DDL。 */
public class PrintSchemaMapperTest {
    @Test public void everyCurrentPrintInsertWorksAgainstReleasedSchema() throws Exception {
        try(Connection db=DriverManager.getConnection("jdbc:h2:mem:mapper_release_"+UUID.randomUUID()+";MODE=Oracle","sa","")) {
            new PrintSchemaRelease().apply(db,"PUBLIC","mapper-contract-release");
            String[] names={"PrintTemplateMapper","PrintBindingMapper","PrintJobMapper","PrintObjectMapper","PrintPreviewMapper"};
            Resource[] resources=new Resource[names.length];
            for(int i=0;i<names.length;i++) resources[i]=new ClassPathResource("mapper/"+names[i]+".xml");
            SqlSessionFactoryBean factory=new SqlSessionFactoryBean();
            factory.setDataSource(new SingleConnectionDataSource(db,true)); factory.setMapperLocations(resources);
            SqlSessionFactory sessions=factory.getObject();
            int inserted=0;
            try(SqlSession session=sessions.openSession(true)) {
                for(Object value:sessions.getConfiguration().getMappedStatements()) {
                    if(!(value instanceof MappedStatement)) continue;
                    MappedStatement mapped=(MappedStatement)value;
                    if(mapped.getSqlCommandType()!=SqlCommandType.INSERT) continue;
                    // MyBatis 同时暴露短名和全名；同一语句按 ID 去重。
                    if(!seen.add(mapped.getId())) continue;
                    Map<String,Object> params=new HashMap<>();
                    for(ParameterMapping parameter:mapped.getBoundSql(params).getParameterMappings()) params.put(parameter.getProperty(),sample(parameter));
                    assertEquals(mapped.getId(),1,session.insert(mapped.getId(),params)); inserted++;
                }
            }
            assertEquals("应覆盖 14 张当前打印业务表",14,inserted);
        }
    }
    private final Set<String> seen=new HashSet<>();
    private Object sample(ParameterMapping p) {
        String name=p.getProperty(); String lower=name.toLowerCase(Locale.ROOT);
        if(p.getJdbcType()==org.apache.ibatis.type.JdbcType.NUMERIC || "sizeBytes".equals(name)) return 1L;
        if(p.getJdbcType()==org.apache.ibatis.type.JdbcType.TIMESTAMP) return new Timestamp(1_700_000_000_000L);
        if(p.getJdbcType()==org.apache.ibatis.type.JdbcType.CLOB) return "{\"合成验收\":true}";
        if(p.getJdbcType()==org.apache.ibatis.type.JdbcType.BLOB) return new byte[]{1,2,3};
        if(lower.contains("hash")) return "sha256:"+String.join("",Collections.nCopies(64,"a"));
        if(lower.endsWith("status")) return "ACTIVE";
        if("faceRole".equals(name) || "face".equals(name)) return "FRONT";
        if("printItemType".equals(name)) return "STAFF_CARD";
        if("personType".equals(name)) return "EMPLOYEE";
        if("printMode".equals(name)) return "SINGLE";
        if("scopeType".equals(name)) return "COMPANY";
        if("purpose".equals(name)) return "LOGO";
        if("accessScope".equals(name)) return "TEMPLATE";
        if("name".equals(name)) return "旅途合成测试";
        return UUID.randomUUID().toString();
    }
}
