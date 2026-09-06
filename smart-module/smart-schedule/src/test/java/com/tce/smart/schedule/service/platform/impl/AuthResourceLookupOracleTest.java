package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.tce.smart.platform.core.entity.SmtAuthOperationTarget;
import com.tce.smart.platform.core.entity.SmtAuthSourceResource;
import com.tce.smart.platform.core.mapper.AuthOperationWorkflowMapper;
import com.tce.smart.platform.core.mapper.SmtAuthSourceResourceMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.junit.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/** 只在专属Oracle窗口验证资源查询；每例单连接事务回滚，不执行DDL或统计变更。 */
public class AuthResourceLookupOracleTest {
    Connection connection;
    int park;
    long batch;
    String prefix;
    int sequence;
    final Set<String> resources=new HashSet<>();
    final List<Arm> arms=new ArrayList<>();

    public static class Arm {
        public final String name;
        public final MybatisConfiguration configuration;
        public final SqlSession session;
        final AuthOperationWorkflowMapper workflow;
        final SmtAuthSourceResourceMapper contributions;
        Arm(String name,MybatisConfiguration configuration,Connection connection) {
            this.name=name;this.configuration=configuration;
            session=new SqlSessionFactoryBuilder().build(configuration).openSession(connection);
            workflow=session.getMapper(AuthOperationWorkflowMapper.class);
            contributions=session.getMapper(SmtAuthSourceResourceMapper.class);
        }
    }

    public static MybatisConfiguration configuration(String directory) throws Exception {
        MybatisConfiguration c=new MybatisConfiguration();c.setMapUnderscoreToCamelCase(true);
        c.setLocalCacheScope(LocalCacheScope.STATEMENT);c.setCacheEnabled(false);c.setDefaultStatementTimeout(20);
        c.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        c.setEnvironment(new Environment("resource-lookup",new JdbcTransactionFactory(),new UnpooledDataSource()));
        for(String name:Arrays.asList("SmtAuthOperationTargetMapper","SmtAuthDeleteRequestMapper","SmtAuthSourceCoordMapper","SmtAuthSourceResourceMapper","AuthOperationWorkflowMapper")) {
            String resource="mapper/"+name+".xml";
            try(InputStream in=directory==null?AuthResourceLookupOracleTest.class.getClassLoader().getResourceAsStream(resource):Files.newInputStream(Paths.get(directory,resource))) {
                Assert.assertNotNull(resource,in);new XMLMapperBuilder(in,c,resource,c.getSqlFragments()).parse();
            }
        }
        c.getMappedStatementNames();return c;
    }

    @Before public void setup() throws Exception {
        Assume.assumeTrue("专属Oracle窗口未启用","true".equals(System.getenv("SMART_AUTH_RESOURCE_LOOKUP_ORACLE_ENABLED")));
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",System.getenv("SMART_AUTH_ORACLE_URL"));
        Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        connection=DriverManager.getConnection(System.getenv("SMART_AUTH_ORACLE_URL"),System.getenv("SMART_AUTH_ORACLE_USER"),System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
        connection.setAutoCommit(false);
        park=810000000+(int)Math.floorMod(UUID.randomUUID().getLeastSignificantBits(),80000000L);
        batch=810000000000000000L+park;prefix="lookup-"+park+"-";
        Assert.assertEquals("N",scalar("SELECT NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SOURCE_RESOURCE' AND COLUMN_NAME='BINDING_REVISION'"));
        Assert.assertEquals("VALID",scalar("SELECT STATUS FROM USER_INDEXES WHERE INDEX_NAME='IX_ASR_RESOURCE' AND TABLE_NAME='SMT_AUTH_SOURCE_RESOURCE'"));
        Assert.assertEquals("VISIBLE",scalar("SELECT VISIBILITY FROM USER_INDEXES WHERE INDEX_NAME='IX_ASR_RESOURCE'"));
        Assert.assertEquals("RESOURCE_COORD_ID,SOURCE_COORD_ID,SOURCE_GENERATION",scalar("SELECT LISTAGG(COLUMN_NAME,',') WITHIN GROUP(ORDER BY COLUMN_POSITION) FROM USER_IND_COLUMNS WHERE INDEX_NAME='IX_ASR_RESOURCE'"));
        String original=System.getProperty("lookup.original");
        if(original!=null)arms.add(new Arm("A",configuration(original),connection));
        arms.add(new Arm("B",configuration(System.getProperty("lookup.candidate")),connection));
        insertBatch(batch);insertBatch(batch+1);
        System.out.println("RESOURCE_LOOKUP_BEGIN park="+park+" connectionLimit=1 arms="+arms.size());
    }

    @After public void cleanup() throws Exception {
        if(connection==null)return;
        try {
            connection.rollback();
            for(String table:Arrays.asList("SMT_AUTH_OPERATION_BATCH","SMT_AUTH_OPERATION_TARGET","SMT_AUTH_SOURCE_COORD","SMT_AUTH_RESOURCE_COORD"))
                Assert.assertEquals(table,0L,((Number)scalar("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=?",park)).longValue());
            Assert.assertEquals(0L,((Number)scalar("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_RESOURCE WHERE ID LIKE ?",prefix+"%")).longValue());
            System.out.println("RESOURCE_LOOKUP_ROLLBACK_VERIFIED park="+park+" tables=5 rows=0");
        } finally {
            for(Arm arm:arms)try{arm.session.close();}catch(Exception ignored){}
            connection.close();System.out.println("RESOURCE_LOOKUP_CONNECTION_CLOSED="+connection.isClosed());
        }
    }

    @Test public void laneKeepsHistoricalBindingsDeduplicationAndMultipleTargetFailure() throws Exception {
        long first=target(1,batch,"FAILED"),second=target(2,batch,"CONVERGED"),other=target(3,batch+1,"PREPARING");
        contribution("r-bound","a",2,1,9,0,"ADD","TOMBSTONE","ADD","CONVERGED",first,"#");
        contribution("r-bound","b",1,1,1,17,"DELETE","EXPANDING","DELETE","PENDING_REMOVE",first,"#");
        contribution("r-negative","c",1,1,1,-1,"DELETE","EXPANDING","DELETE","PENDING_REMOVE",first,"#");
        contribution("r-other","d",1,1,1,0,"DELETE","EXPANDING","DELETE","PENDING_REMOVE",other,"#");
        contribution("r-null","e",1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-dangling","f",1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",batch+9999,"#");
        for(Arm a:arms) {
            Assert.assertEquals(a.name,Long.valueOf(first),a.workflow.laneTarget(batch,rid("r-bound")).getId());
            for(String r:Arrays.asList("r-negative","r-other","r-null","r-dangling","missing"))Assert.assertNull(a.name+":"+r,a.workflow.laneTarget(batch,rid(r)));
            Assert.assertNull(a.workflow.laneTarget(batch,null));Assert.assertNull(a.workflow.laneTarget(batch,""));
        }
        contribution("r-bound","g",1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",second,"#");
        for(Arm a:arms) {
            try{a.workflow.laneTarget(batch,rid("r-bound"));Assert.fail("多个真实target不得被MIN或ROWNUM隐藏");}
            catch(TooManyResultsException expected){Assert.assertNotNull(expected.getMessage());}
        }
    }

    @Test public void ownerKeepsNumericMinimumCurrentGenerationsAndTombstoneCompensation() throws Exception {
        contribution("r-owner","a",1,1,7,0,"DELETE","TOMBSTONE","DELETE","CONVERGED",19L,"#");
        contribution("r-owner","b",1,1,7,0,"ADD","ACTIVE","ADD","ACTIVE",2L,"#");
        contribution("r-owner","c",1,1,7,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-owner","d",2,1,7,0,"ADD","ACTIVE","ADD","ACTIVE",1L,"#");
        contribution("r-owner","e",1,1,8,0,"ADD","ACTIVE","ADD","ACTIVE",1L,"#");
        contribution("r-owner","f",1,1,7,1,"ADD","ACTIVE","ADD","ACTIVE",1L,"#");
        contribution("r-owner","g",1,1,7,-1,"ADD","ACTIVE","ADD","ACTIVE",1L,"#");
        contribution("r-null","h",1,1,7,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-tombstone","i",1,1,7,0,"DELETE","TOMBSTONE","DELETE","CONVERGED",19L,"#");
        for(Arm a:arms) {
            Assert.assertEquals(a.name,Long.valueOf(2),a.contributions.executionOwner(rid("r-owner"),7));
            Assert.assertEquals(Long.valueOf(19),a.contributions.executionOwner(rid("r-tombstone"),7));
            for(String r:Arrays.asList("r-null","missing"))Assert.assertNull(a.contributions.executionOwner(rid(r),7));
            Assert.assertNull(a.contributions.executionOwner(rid("r-owner"),99));Assert.assertNull(a.contributions.executionOwner(null,7));
        }
    }

    @Test public void currentKeepsExactSourceBasisAndWindowMetadata() throws Exception {
        contribution("r-current","19",2,2,77,0,"ADD","ACTIVE","ADD","CONVERGED",null,"#");
        contribution("r-current","2",1,1,1,0,"ADD","EXPANDING","ADD","ACTIVE",null,"a;b");
        contribution("r-current","z",1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",null,"abc");
        contribution("r-current","bad-generation",2,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-current","bad-revision",1,1,1,1,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-current","bad-negative",1,1,1,-1,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-current","bad-c-action",1,1,1,0,"ADD","ACTIVE","DELETE","ACTIVE",null,"#");
        contribution("r-current","bad-s-action",1,1,1,0,"DELETE","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-current","bad-state",1,1,1,0,"ADD","TOMBSTONE","ADD","ACTIVE",null,"#");
        for(Arm a:arms) {
            List<SmtAuthSourceResource> rows=a.contributions.currentForResource(rid("r-current"));assertSources(rows,"19","2","z");
            Assert.assertNull(rows.get(0).getCurrentSourceRowId());Assert.assertEquals("current-19",rows.get(0).getCurrentSourceFingerprint());
            Assert.assertEquals("frozen-19",rows.get(0).getSourceFingerprint());
            Assert.assertEquals(0,rows.get(0).getWindowCount().intValue());Assert.assertEquals(1,rows.get(0).getWindowLength().intValue());
            Assert.assertEquals(2,rows.get(1).getWindowCount().intValue());Assert.assertEquals(3,rows.get(1).getWindowLength().intValue());
            Assert.assertEquals(1,rows.get(2).getWindowCount().intValue());
            Assert.assertTrue(a.contributions.currentForResource(null).isEmpty());Assert.assertTrue(a.contributions.currentForResource("").isEmpty());
        }
    }

    @Test public void currentKeeps1001SentinelAfterAllFilteringAndSorting() throws Exception {
        contribution("r-cap","000-filtered",1,1,1,0,"ADD","TOMBSTONE","ADD","ACTIVE",null,"#");
        for(int i=0;i<1002;i++) {
            contribution("r-cap",String.format(Locale.ROOT,"s%04d",i),1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
            if(i==999 || i==1000 || i==1001)for(Arm a:arms) {
                List<SmtAuthSourceResource> rows=a.contributions.currentForResource(rid("r-cap"));
                Assert.assertEquals(i==999?1000:1001,rows.size());
                Assert.assertEquals(sid("s0000"),rows.get(0).getSourceCoordId());
                Assert.assertEquals(sid(i==999?"s0999":"s1000"),rows.get(rows.size()-1).getSourceCoordId());
            }
        }
    }

    @Test public void auditKeepsDeleteUnionGenerationAndStrictLexicalCursor() throws Exception {
        contribution("r-audit","19",1,1,99,0,"ADD","ACTIVE","DELETE","PENDING_REMOVE",null,"#");
        contribution("r-audit","2",1,1,1,0,"DELETE","EXPANDING","ADD","PENDING_REMOVE",null,"#");
        contribution("r-audit","z",1,1,1,0,"DELETE","EXPANDING","DELETE","PENDING_REMOVE",null,"#");
        contribution("r-audit","bad-add",1,1,1,0,"ADD","ACTIVE","ADD","ACTIVE",null,"#");
        contribution("r-audit","bad-done",1,1,1,0,"DELETE","ACTIVE","DELETE","CONVERGED",null,"#");
        contribution("r-audit","bad-tomb",1,1,1,0,"DELETE","TOMBSTONE","DELETE","PENDING_REMOVE",null,"#");
        contribution("r-audit","bad-gen",2,1,1,0,"DELETE","ACTIVE","DELETE","PENDING_REMOVE",null,"#");
        contribution("r-audit","bad-rev",1,1,1,1,"DELETE","ACTIVE","DELETE","PENDING_REMOVE",null,"#");
        contribution("r-audit","bad-neg",1,1,1,-1,"DELETE","ACTIVE","DELETE","PENDING_REMOVE",null,"#");
        for(Arm a:arms) {
            assertSources(a.contributions.auditForResource(rid("r-audit"),null,201),"19","2","z");
            assertSources(a.contributions.auditForResource(rid("r-audit"),sid("19"),1),"2");
            assertSources(a.contributions.auditForResource(rid("r-audit"),sid("2"),200),"z");
            for(String after:Arrays.asList("",sid("z")))Assert.assertTrue(a.contributions.auditForResource(rid("r-audit"),after,200).isEmpty());
            for(int limit:new int[]{0,-1})Assert.assertTrue(a.contributions.auditForResource(rid("r-audit"),null,limit).isEmpty());
            Assert.assertTrue(a.contributions.auditForResource(null,null,201).isEmpty());
        }
    }

    @Test public void auditKeeps201SentinelAndFollowingPage() throws Exception {
        for(int i=0;i<202;i++)contribution("r-audit-cap",String.format(Locale.ROOT,"s%04d",i),1,1,1,0,"DELETE","ACTIVE","DELETE","PENDING_REMOVE",null,"#");
        for(Arm a:arms) {
            List<SmtAuthSourceResource> rows=a.contributions.auditForResource(rid("r-audit-cap"),null,201);
            Assert.assertEquals(201,rows.size());Assert.assertEquals(sid("s0000"),rows.get(0).getSourceCoordId());Assert.assertEquals(sid("s0200"),rows.get(200).getSourceCoordId());
            assertSources(a.contributions.auditForResource(rid("r-audit-cap"),sid("s0200"),201),"s0201");
            Assert.assertEquals(200,a.contributions.auditForResource(rid("r-audit-cap"),null,200).size());
        }
    }

    void assertSources(List<SmtAuthSourceResource> rows,String... keys) {
        Assert.assertEquals(Arrays.stream(keys).map(this::sid).collect(Collectors.toList()),rows.stream().map(SmtAuthSourceResource::getSourceCoordId).collect(Collectors.toList()));
    }
    String rid(String key){return prefix+"r-"+key;}
    String sid(String key){return prefix+"s-"+key;}
    void insertBatch(long id) throws Exception {
        update("INSERT INTO SMT_AUTH_OPERATION_BATCH(ID,PARK_ID,IDEMPOTENCY_KEY,ACTION,SOURCE_TYPE,SELECTION_SNAPSHOT,PAYLOAD_FINGERPRINT,EXPECTED_COUNT) VALUES(?,?,?,'DELETE','3','lookup','lookup',10000)",id,park,"lookup-"+id);
    }
    long target(int ordinal,long targetBatch,String state) throws Exception {
        long id=batch+100+ordinal;
        update("INSERT INTO SMT_AUTH_OPERATION_TARGET(ID,BATCH_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_TYPE,DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE) VALUES(?,?,?,?,'STAFF','lookup','PERSON','lookup-door',?,'DIRECT','DELETE','DELETE',1,?)",id,targetBatch,park,"lookup-target-"+ordinal,"lookup-target-"+ordinal,state);
        return id;
    }
    void resource(String key) throws Exception {
        if(!resources.add(key))return;
        String id=rid(key);
        update("INSERT INTO SMT_AUTH_RESOURCE_COORD(ID,PARK_ID,SUBJECT_TYPE,SUBJECT_ID,ACCESS_TYPE,DEVICE_ID,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL,RESOURCE_KEY,GENERATION,APPLIED_GENERATION,ACTION,WINDOWS,DESIRED_FINGERPRINT,BASIS_FINGERPRINT,CREATE_TIME,UPDATE_TIME) VALUES(?,?,'STAFF','lookup','DIRECT','lookup-door','PERSON',?,'3','FACE',?,1,0,'DELETE','#','lookup','lookup',SYSTIMESTAMP,SYSTIMESTAMP)",id,park,id,id);
    }
    void contribution(String resource,String source,long currentGeneration,long sourceGeneration,long resourceGeneration,long revision,String sourceAction,String sourceState,String action,String state,Long target,String windows) throws Exception {
        resource(resource);String id=sid(source);
        update("INSERT INTO SMT_AUTH_SOURCE_COORD(ID,PARK_ID,SOURCE_KIND,STABLE_KEY,SUBJECT_TYPE,SUBJECT_ID,SOURCE_ROW_ID,SOURCE_FINGERPRINT,GENERATION,INTENT_KEY,INTENT_FINGERPRINT,BATCH_ID,ACTION,STATE,EXPANDED,WINDOWS,CREATE_TIME,UPDATE_TIME) VALUES(?,?,'STAFF_AUTH',?,'STAFF','lookup',NULL,?,?,?,'lookup',?,?,?,1,'#',SYSTIMESTAMP,SYSTIMESTAMP)",id,park,id,"current-"+source,currentGeneration,id,batch,sourceAction,sourceState);
        update("INSERT INTO SMT_AUTH_SOURCE_RESOURCE(ID,SOURCE_COORD_ID,SOURCE_GENERATION,RESOURCE_COORD_ID,RESOURCE_GENERATION,SOURCE_ROW_ID,SOURCE_FINGERPRINT,WINDOWS,ACTION,STATE,REQUEST_ID,TARGET_ID,BINDING_REVISION,SOURCE_ACTION,INTENT_KEY,INTENT_FINGERPRINT,CREATE_TIME,UPDATE_TIME) VALUES(?,?,?,?,?,NULL,?,?,?,?,?,?,?,?,?,'lookup',SYSTIMESTAMP,SYSTIMESTAMP)",prefix+"c-"+(++sequence),id,sourceGeneration,rid(resource),resourceGeneration,"frozen-"+source,windows,action,state,batch+999,target,revision,sourceAction,id);
    }
    void update(String sql,Object... args) throws Exception {try(PreparedStatement s=connection.prepareStatement(sql)){s.setQueryTimeout(20);bind(s,args);s.executeUpdate();}}
    Object scalar(String sql,Object... args) throws Exception {try(PreparedStatement s=connection.prepareStatement(sql)){s.setQueryTimeout(20);bind(s,args);try(ResultSet r=s.executeQuery()){Assert.assertTrue(r.next());return r.getObject(1);}}}
    static void bind(PreparedStatement s,Object... args) throws Exception {for(int i=0;i<args.length;i++)if(args[i]==null)s.setNull(i+1,Types.VARCHAR);else s.setObject(i+1,args[i]);}
}
