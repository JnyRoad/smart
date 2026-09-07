package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import com.tce.smart.schedule.task.AuthOperationTimerTask;
import org.junit.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.mockito.Mockito.*;

/** 显式启用的真实业务容量探针；10k/20k完整管道，100k只测展开保护，不绕过受理或限流。 */
public class AuthOperationCapacityTest extends AuthOperationCapacityFixture {
    private final Map<String,Object> report=new LinkedHashMap<>();
    private final List<Long> accepts=new ArrayList<>();
    private final AtomicInteger http=new AtomicInteger();
    private final AtomicLong firstHttp=new AtomicLong();
    private AuthOperationScheduler scheduler;
    private long maxHeap,maxConnections,maxInflight;
    private int configuredMaxInflight=400;
    private int expandSeconds=120,pipelineSeconds=600;
    private long started;
    private long firstAcceptStarted;
    private org.springframework.test.web.servlet.MockMvc httpEntry;
    private ScheduledExecutorService timerDriver;
    private volatile AuthOperationTimerTask activeTimer;
    private final AtomicInteger timerTicks=new AtomicInteger();
    private final AtomicReference<Throwable> asynchronousFailure=new AtomicReference<>();

    @Override @Before public void setup() throws Exception {
        Assume.assumeTrue("容量用例必须显式启用", "true".equals(System.getenv("SMART_AUTH_CAPACITY_RUN")));
        super.setup();
    }

    @Override @After public void cleanup() {
        // 超时退出也必须证明后台线程终止；若等待失败，保留自有数据，禁止并发清理或关闭连接池。
        Assert.assertNull("Timer未确认终止，保留本用例园区数据："+park,timerDriver);
        Assert.assertNull("scheduler未确认终止，保留本用例园区数据："+park,scheduler);
        super.cleanup();
    }

    @Test public void realBusinessCapacityIsBoundedAndRecoverable() throws Exception {
        int targets=Integer.parseInt(value("SMART_AUTH_CAPACITY_TARGETS","201"));
        Assert.assertTrue("只接受明确的探针或规格规模",Arrays.asList(201,1000,10000,20000,100000).contains(targets));
        int defaultDevices=targets==100000?10:targets==20000?2:1;
        int devices=Integer.parseInt(value("SMART_AUTH_CAPACITY_DEVICES",String.valueOf(defaultDevices)));
        Assert.assertTrue("设备数必须为正且目标数可以整除，禁止缩小目标总数",devices>0&&devices<=targets&&targets%devices==0);
        int people=targets/devices;
        String stage=value("SMART_AUTH_CAPACITY_STAGE","FULL");
        Assert.assertTrue("阶段只允许完整管道或独立受理诊断",Arrays.asList("FULL","ACCEPT_ONLY").contains(stage));
        if("ACCEPT_ONLY".equals(stage)) {
            Assert.assertTrue("独立受理诊断仅接受201或1000人",targets==201||targets==1000);
            Assert.assertEquals("大人群受理诊断保留一设备，不能以较少人员乘设备数代替",1,devices);
        }
        String action=value("SMART_AUTH_CAPACITY_ACTION","DELETE");
        Assert.assertTrue(Arrays.asList("DELETE","ADD","RESEND").contains(action));
        expandSeconds=diagnosticSeconds("SMART_AUTH_CAPACITY_EXPAND_SECONDS",120);
        pipelineSeconds=diagnosticSeconds("SMART_AUTH_CAPACITY_PIPELINE_SECONDS",600);
        String expansion=value("SMART_AUTH_CAPACITY_EXPANSION","SERVICE_LOOP");
        String access=value("SMART_AUTH_CAPACITY_ACCESS","DIRECT");
        Assert.assertTrue(Arrays.asList("SERVICE_LOOP","TIMER").contains(expansion));
        Assert.assertTrue(Arrays.asList("DIRECT","ISC").contains(access));
        Assert.assertFalse("100k仅保留服务循环展开保护；Timer会自然进入外发，不能称为只展开",targets==100000&&"TIMER".equals(expansion));
        if("true".equals(System.getenv("SMART_AUTH_CAPACITY_PRIORITY"))) {
            Assert.assertEquals("现有SC002诊断独立保留服务展开方式", "SERVICE_LOOP",expansion);
        }
        started=System.nanoTime();report.put("park",park);report.put("targets",targets);report.put("people",people);report.put("devices",devices);
        report.put("requestedStage",stage);report.put("action",action);report.put("unverified",Arrays.asList("SC002_20k_ADD_backlog_small_DELETE","SC003_normal_list_baseline","SC005_mixed_device_failures","real_socket_and_security_filters","real_ISC_devices","JVM_crash_recovery"));report.put("environment","Oracle26ai23.26.3;2CPU;3GiB;pool4;controlledRemote;DIRECT;noProductionDevice");
        report.put("result","FAIL");report.put("stage","seed");
        report.put("expansionMode",expansion);report.put("access",access);
        report.put("diagnosticExpandSeconds",expandSeconds);report.put("diagnosticPipelineSeconds",pipelineSeconds);
        report.put("diagnosticExpandOverride",System.getenv("SMART_AUTH_CAPACITY_EXPAND_SECONDS")!=null);report.put("diagnosticPipelineOverride",System.getenv("SMART_AUTH_CAPACITY_PIPELINE_SECONDS")!=null);
        report.put("diagnosticDeadlineBasis","TIMER pipeline begins at scheduler start and includes expansion; SERVICE_LOOP pipeline begins after expansion; probe deadlines are not product SLOs");
        report.put("deviceCountOverride",System.getenv("SMART_AUTH_CAPACITY_DEVICES")!=null);
        report.put("capacityScenario",devices==20&&(targets==10000||targets==20000)?"distributed_20_device_main_queue":devices==1?"single_device_hotspot":"explicit_device_distribution_or_expansion_diagnostic");
        report.put("targetCountInvariant",people*devices);
        report.put("configuredTheory",configuredTheory(targets,people,devices,action,access));
        report.put("environment","Oracle26ai23.26.3;2CPU;3GiB;pool4;controlledRemote;"+access+";noProductionDevice");
        boolean primaryFailure=false;
        try {
            seed(people,devices,"ADD".equals(action));
            if("ISC".equals(access))seedIscHistory("ADD".equals(action));
            report.put("stage","accept");long injection=System.nanoTime();sqlTiming.deadlineAfterSeconds(120);
            int chunk=Integer.parseInt(value("SMART_AUTH_CAPACITY_REQUEST_SIZE",String.valueOf(people)));
            Assert.assertTrue(chunk>0 && chunk<=people);
            if("ACCEPT_ONLY".equals(stage))Assert.assertEquals("受理诊断必须是全量单次MVC请求，不能客户端拆批",people,chunk);
            report.put("requestSizePeople",chunk);report.put("entryBoundary","Spring MVC MockMvc; full JSON request; no real socket; scoped synthetic principal");
            com.tce.smart.platform.controller.SmtStaffDeviceAuthController controller=new com.tce.smart.platform.controller.SmtStaffDeviceAuthController();
            org.springframework.test.util.ReflectionTestUtils.setField(controller,"smtStaffDeviceAuthService",entry);
            httpEntry=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
            for(int offset=0;offset<people;offset+=chunk) {
                int end=Math.min(people,offset+chunk);List<String> ids=new ArrayList<>();
                for(int i=offset;i<end;i++){ids.add(String.valueOf(employeeId+i));}
                long before=System.nanoTime();
                if(firstAcceptStarted==0)firstAcceptStarted=before;
                UpdateDeviceAuthDTO dto=new UpdateDeviceAuthDTO();dto.setIds(ids);dto.setDeviceAuthIds("DELETE".equals(action)?Collections.emptyList():Collections.singletonList(park));dto.setStartTime("2026-09-01");dto.setEndTime("2026-09-30");
                int type="DELETE".equals(action)?2:"ADD".equals(action)?1:3;
                org.springframework.mock.web.MockHttpServletResponse response=httpEntry.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/staff/device/auth/updateAuth/"+type)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(cn.hutool.json.JSONUtil.toJsonStr(dto))).andReturn().getResponse();
                Assert.assertEquals(200,response.getStatus());Assert.assertTrue(cn.hutool.json.JSONUtil.toBean(response.getContentAsString(),Result.class).isSuccess());
                long elapsed=millis(before);accepts.add(elapsed);sample();
                Assert.assertTrue("单次受理超过120秒，停止本规模并保留失败证据",elapsed<=120000);
            }
            sqlTiming.clearDeadline();report.put("injectionMs",millis(injection));report.put("acceptSamples",accepts);report.put("observedAcceptP95Ms",percentile(accepts,.95));report.put("observedAcceptP99Ms",percentile(accepts,.99));report.put("acceptPercentileEvidence",accepts.size()>=20?"sampled requests":"INSUFFICIENT_SAMPLE_SINGLE_LARGE_REQUEST");
            Assert.assertEquals("冻结资源不能丢失",Integer.valueOf(targets),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",Integer.class,park));
            List<Long> batches=jdbc.queryForList("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? ORDER BY ID",Long.class,park);
            report.put("batchIds",batches);
            if("ACCEPT_ONLY".equals(stage)) {
                assertAcceptedOnly(people,targets,action,batches.size());
                report.put("stage","accept-only-complete");report.put("result","DIAGNOSTIC_ACCEPT_ONLY_COMPLETE");
                report.put("capacityAcceptance","NOT_T024_PASS");
                report.put("unverified",Arrays.asList("target_expansion","queue_and_confirmation","source_convergence","SC001_10k20k","SC002_priority","SC003_population_percentiles_and_list","SC004_100k","SC005_fault_isolation","real_socket_and_devices"));
                return;
            }
            if("TIMER".equals(expansion)) {
                report.put("stage","timer-expand");
                runScheduledPipeline(targets,people,action,access,true);
            } else {
            report.put("stage","expand");long expand=System.nanoTime();sqlTiming.deadlineAfterSeconds(expandSeconds);int staged=0,bound=0;
            // 真实分片与lane绑定逐次推进，持久游标决定进度；每个阶段有限时间，绝不直插目标。
            for(Long batch:batches) {
                while(employee.stageNext(batch)){staged++;if(staged==1){employee=proxy(new com.tce.smart.platform.core.service.impl.EmployeeAuthOperationService(selection,service),new DataSourceTransactionManager(pool));report.put("expansionRecovery","reconstructed EmployeeAuthOperationService after first committed shard; persisted cursor resumed");}sample();if(millis(expand)>TimeUnit.SECONDS.toMillis(expandSeconds))failExpansion(staged,bound,expand);}
                while(employee.bindNextLane(batch,null)!=null){bound++;sample();if(millis(expand)>TimeUnit.SECONDS.toMillis(expandSeconds))failExpansion(staged,bound,expand);}
                employee.finish(batch);
            }
            sqlTiming.clearDeadline();report.put("expandMs",millis(expand));report.put("stageCalls",staged);report.put("bindCalls",bound);
            Assert.assertEquals(Integer.valueOf(targets),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",Integer.class,park));
            // 从已消费的来源/目标游标恢复应无新增，期望数保持不变。
            for(Long batch:batches){Assert.assertFalse(employee.stageNext(batch));Assert.assertNull(employee.bindNextLane(batch,null));}
            if(targets==100000){report.put("result","PASS_EXPANSION_ONLY");return;}
            if("true".equals(System.getenv("SMART_AUTH_CAPACITY_PRIORITY"))){
                Assert.assertEquals("SC002需要20k ADD积压",20000,targets);Assert.assertEquals("ADD",action);
                report.put("stage","priority-delete");runPriorityDelete(access);report.put("unverified",Arrays.asList("SC001_full20k_confirmation","SC003_percentiles_and_normal_list","SC005_mixed_device_failures","real_socket_and_devices","JVM_crash_recovery"));report.put("result","PASS_SC002_CONTROLLED_"+access+"_ONLY");return;
            }
            report.put("stage","queue-confirm-converge");
            if("ISC".equals(access))runScheduledPipeline(targets,people,action,access,false);else runDirect(targets);
            }
            report.put("elapsedMs",millis(started));
            report.put("completeFromAcceptStartMs",millis(firstAcceptStarted));
            if(targets>=10000) {
                Assert.assertTrue("注入超过60秒诊断窗口；该窗口不是SC-001产品门槛",((Long)report.get("injectionMs"))<=60000);
                Assert.assertTrue("单次/样本受理耗时P99门槛",percentile(accepts,.99)<=5000);
                if(accepts.size()>=20)Assert.assertTrue("SC-003受理P95",percentile(accepts,.95)<=2000);
            }
            report.put("result",targets<10000?"PASS_PROBE_ONLY":"PASS_SINGLE_ACTION_"+access+"_SC003_REQUIRES_REQUEST_SAMPLES");
        } catch(Exception|AssertionError failure) {
            primaryFailure=true;report.put("failure",failure.getClass().getSimpleName()+": "+failure.getMessage());throw failure;
        } finally {
            sqlTiming.clearDeadline();
            stopTimerDriver();
            if(scheduler!=null){stopScheduler(scheduler);scheduler=null;}
            report.put("httpRequests",http.get());report.put("maxHeapBytes",maxHeap);report.put("jvmMaxHeapBytes",Runtime.getRuntime().maxMemory());report.put("maxConnections",maxConnections);report.put("maxInflight",maxInflight);
            report.put("observedHttpRequestsPerTarget",((double)http.get())/targets);
            report.put("sqlTiming",sqlTiming.snapshot());report.put("wallMs",millis(started));report.put("sourceRemaining",jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)",Integer.class,"employee-test-"+park));
            report.put("batchCursors",jdbc.queryForList("SELECT ID,EXPECTED_COUNT,EXPANDED_COUNT,EXPANSION_CURSOR,STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",park));
            report.put("targetsByState",jdbc.queryForList("SELECT STATE,COUNT(*) N FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? GROUP BY STATE",park));
            report.put("timerTicks",timerTicks.get());
            if(firstHttp.get()>0)report.put("firstHttpFromAcceptStartMs",TimeUnit.NANOSECONDS.toMillis(firstHttp.get()-firstAcceptStarted));
            report.put("transportStates",jdbc.queryForList("SELECT PHASE,STATE,ERROR_CODE,COUNT(*) N FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? GROUP BY PHASE,STATE,ERROR_CODE",park));
            report.put("measuredQueueWallMs",report.containsKey("pipelineMs")?report.get("pipelineMs"):report.get("confirmConvergeMs"));
            if("FAIL".equals(report.get("result")))report.put("failureInterpretation","DIAGNOSTIC_INCOMPLETE; compare configured rate floor and actual failed stage before attributing an implementation defect; no SLO or quota was changed");
            try {
                Path root=reportRoot();Files.createDirectories(root);
                Path destination=root.resolve("smart-auth-012-capacity-report-"+park+".json");
                Files.write(destination,cn.hutool.json.JSONUtil.toJsonPrettyStr(report).getBytes(StandardCharsets.UTF_8));
                System.out.println("容量报告："+destination+" result="+report.get("result")+" stage="+report.get("stage"));
            } catch(Exception reportFailure) {
                System.err.println("容量报告写入失败："+reportFailure.getMessage());
                if(!primaryFailure)throw new AssertionError("容量报告写入失败",reportFailure);
            }
        }
    }

    private static Path reportRoot() {
        String configured=System.getenv("SMART_AUTH_TEST_TMPDIR");
        return Paths.get(configured==null||configured.trim().isEmpty()?System.getProperty("java.io.tmpdir"):configured).toAbsolutePath().normalize();
    }

    /** 只核真实受理事务的冻结证据；不得展开、派发，也不得把诊断完成等同容量验收通过。 */
    private void assertAcceptedOnly(int people,int targets,String action,int batchCount) {
        int sources=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",Integer.class,park);
        int pending=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? AND STATE='PENDING' AND SOURCE_COORD_ID IS NULL",Integer.class,park);
        int resources=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND RESOURCE_COORD_ID IS NULL",Integer.class,park);
        int expected=jdbc.queryForObject("SELECT COALESCE(SUM(EXPECTED_COUNT),0) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Integer.class,park);
        Assert.assertEquals("全量来源必须冻结",people,sources);Assert.assertEquals("全部来源必须仍待展开",people,pending);
        Assert.assertEquals("全部资源必须冻结且尚未绑定",targets,resources);Assert.assertEquals("冻结预期目标数不得缩小",targets,expected);
        Assert.assertEquals("批次必须可追踪且尚未展开",Integer.valueOf(batchCount),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS='PREPARING' AND EXPANDED_COUNT=0 AND EXPANSION_CURSOR=0",Integer.class,park));
        Assert.assertTrue("必须持久化受理批次",batchCount>0);
        Assert.assertEquals("受理诊断不得创建目标",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",Integer.class,park));
        Assert.assertEquals("受理诊断不得进入外发阶段",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?",Integer.class,park));
        Assert.assertEquals("受理不能提前改变业务授权",Integer.valueOf("ADD".equals(action)?0:people),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)",Integer.class,"employee-test-"+park));
        Assert.assertEquals(0,http.get());Assert.assertNull(scheduler);
        report.put("frozenSources",sources);report.put("pendingSources",pending);report.put("frozenUnboundResources",resources);report.put("expectedTargets",expected);
        report.put("operationKeys",jdbc.queryForList("SELECT DISTINCT OPERATION_KEY FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? ORDER BY OPERATION_KEY",String.class,park));
    }
    /** 只报告容量场景实际采用的lane配额、在途和兼容组下界，不改变产品默认配置。 */
    private Map<String,Object> configuredTheory(int targets,int people,int devices,String action,String access) {
        AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();
        AuthOperationSchedulerProperties.Instance limits=capacityInstance(access,targets);settings.getInstances().add(limits);
        boolean delete="DELETE".equals(action);int reserved=delete?limits.getDeleteHttp():limits.getAddHttp();
        int parkLane=Math.min(limits.getParkHttpPerSecond()*3/8,reserved),deviceLane=limits.getDeviceHttpPerSecond()*3/8;
        int rate=Math.min(limits.getHttpPerSecond(),Math.min(reserved+limits.getBorrowHttp(),Math.min(parkLane,devices*deviceLane)));
        int deviceSlots=limits.getPerDeviceInflight()-(delete?limits.getPerDeviceAddInflight():limits.getPerDeviceDeleteInflight());
        int parkSlots=limits.getPerParkInflight()-(delete?limits.getPerParkAddInflight():limits.getPerParkDeleteInflight());
        int instanceSlots=limits.getMaxInflight()-(delete?limits.getAddInflight():limits.getDeleteInflight());
        Map<String,Object> theory=new LinkedHashMap<>();
        theory.put("actualCapacityInstanceProperties",JSONUtil.parseObj(limits));theory.put("actualSchedulerProperties",JSONUtil.parseObj(settings));
        theory.put("businessLane",delete?"DELETE":"ADD");theory.put("laneLayerFraction","ADD/DELETE=3/8; CONFIG/RECEIPT=1/8; integer floor");
        theory.put("businessParkHttpPerSecond",parkLane);theory.put("businessDeviceHttpPerSecond",deviceLane);
        theory.put("availableDeviceInflightIgnoringCurrentOccupancy",deviceSlots);theory.put("availableParkInflightIgnoringCurrentOccupancy",parkSlots);theory.put("availableInstanceInflightIgnoringCurrentOccupancy",instanceSlots);
        theory.put("peoplePerDevice",people);theory.put("targetCount",targets);theory.put("deviceCount",devices);theory.put("diagnosticQueueDeadlineSeconds",pipelineSeconds);
        long steps=(long)people+targets+3;theory.put("singleBatchStageBindFinishStepsEstimate",steps);theory.put("idealExpansionTickFloorSeconds",(steps+settings.getExpansionSteps()-1)/settings.getExpansionSteps());
        if("DIRECT".equals(access)) {
            double seconds=((double)targets)/rate;
            theory.put("protocolPeoplePerHttp",1);theory.put("optimisticHttpPerSecond",rate);theory.put("steadyStateMinimumSendSeconds",seconds);
            theory.put("singleDeviceSteadyStateMinimumSendSeconds",((double)people)/deviceLane);
            theory.put("maximumHttpPerSubmitWorkItem",3);theory.put("maximumSubmitHttpPerTickBeforeQuota",settings.getDispatchWorkItems()*3);
            theory.put("rateFloorExceedsDiagnosticDeadline",seconds>pipelineSeconds);
            theory.put("interpretation","DIRECT lower bound uses actual business lane share; claim/prepare may use zero HTTP work items, and DRAIN work-count/wall checkpoints may prevent using all quota");
        } else {
            int groupPeople=Math.min(limits.getIscBatchTargetSize(),Math.min(200,Math.min(settings.getPageSize(),Math.min(deviceSlots,Math.min(parkSlots,instanceSlots)))));
            long perDeviceGroups=(people+groupPeople-1L)/groupPeople,groups=perDeviceGroups*devices;
            int configPark=Math.min(limits.getParkHttpPerSecond()/8,limits.getConfigHttp()),configDevice=limits.getDeviceHttpPerSecond()/8;
            int receiptPark=Math.min(limits.getParkHttpPerSecond()/8,limits.getReceiptHttp()),receiptDevice=limits.getDeviceHttpPerSecond()/8;
            Map<String,Object> config=new LinkedHashMap<>(),progressDownload=new LinkedHashMap<>(),receipt=new LinkedHashMap<>();
            // ISC业务队列每组保守预留3个HTTP，即使可信历史DELETE仅实际发送一个配置请求。
            config.put("actualConfigHttpMinimum",groups);config.put("reservedHttpPerGroup",3);config.put("steadyStateReservationFloorSeconds",Math.max(groups*3.0/parkLane,perDeviceGroups*3.0/deviceLane));
            progressDownload.put("lane","CONFIG");progressDownload.put("parkHttpPerSecond",configPark);progressDownload.put("deviceHttpPerSecond",configDevice);progressDownload.put("minimumHttp",groups*2);progressDownload.put("steadyStateMinimumSeconds",Math.max(groups*2.0/configPark,perDeviceGroups*2.0/configDevice));
            receipt.put("lane","RECEIPT");receipt.put("parkHttpPerSecond",receiptPark);receipt.put("deviceHttpPerSecond",receiptDevice);receipt.put("minimumHttp",groups);receipt.put("steadyStateMinimumSeconds",Math.max(groups*1.0/receiptPark,perDeviceGroups*1.0/receiptDevice));
            theory.put("optimisticCompatiblePeoplePerGroup",groupPeople);theory.put("optimisticGroupsPerDevice",perDeviceGroups);theory.put("optimisticTransportGroups",groups);
            theory.put("configurationSubmission",config);theory.put("configurationProgressAndDownload",progressDownload);theory.put("downloadReceipt",receipt);
            theory.put("minimumTransportProtocolHttpExcludingPersonAssets",groups*4);
            theory.put("minimumNewPersonHttpForAddOnly","ADD".equals(action)?people:0);
            theory.put("interpretation","ISC estimates separate business/config/receipt lanes and current in-flight group bound; compatibility, occupied slots, retries and extra polls can make actual groups smaller. Person GET/CREATE/FACE costs and DRAIN work/checkpoint limits need measured breakdown. Do not apply DELETE's 3/8 share to all protocol HTTP.");
        }
        theory.put("assumptions","One park, one accepted batch, no other occupancy and ideal compatible grouping; initial-window burst, identity assets, phase overlap, claim/prepare work and checkpoint limits are not completion predictions. Diagnostic windows are not product SLOs; the 10k/20k ISC scenario uses an explicit capacity profile rather than production defaults.");
        return theory;
    }
    /** 大批ISC专用受控档：一个设备同一业务方向最多一个200人请求，同时保留两侧20个反向目标。 */
    private AuthOperationSchedulerProperties.Instance capacityInstance(String access,int targets) {
        AuthOperationSchedulerProperties.Instance instance=new AuthOperationSchedulerProperties.Instance();instance.setId("capacity-"+park);instance.setAccessType(access);instance.setParks(Collections.singletonList(park));
        if("ISC".equals(access) && targets>=10000) {
            instance.setIscBatchTargetSize(200);instance.setHttpPerSecond(300);instance.setDeleteHttp(100);instance.setAddHttp(50);instance.setConfigHttp(30);instance.setReceiptHttp(30);instance.setBorrowHttp(20);
            instance.setMaxInflight(2000);instance.setDeleteInflight(200);instance.setAddInflight(200);instance.setPerParkInflight(2000);instance.setPerParkDeleteInflight(200);instance.setPerParkAddInflight(200);
            instance.setPerDeviceInflight(220);instance.setPerDeviceDeleteInflight(20);instance.setPerDeviceAddInflight(20);instance.setParkHttpPerSecond(200);instance.setDeviceHttpPerSecond(32);
        }
        return instance;
    }
    private void seed(int people,int devices,boolean add) {
        staffCount=people;
        if(people>1) {
            jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) SELECT ?+LEVEL,?,1,?,'capacity-badge-'||?||'-'||LEVEL,'合成员工' FROM DUAL CONNECT BY LEVEL<=?",employeeId,"employee-test-"+park,"image-ref-"+park,park,people-1);
            if(!add)jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) SELECT ?+LEVEL,?+LEVEL,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2 FROM DUAL CONNECT BY LEVEL<=?",park,employeeId,park,people-1);
        }
        if(add)jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE ID=? AND STAFF_ID=?",park,employeeId);
        for(int i=1;i<devices;i++) {
            String id="capacity-device-"+park+"-"+i;
            jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)",id,park);
            jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+i,park,id,park);
        }
    }
    /** 只合成既有业务身份；新增权限必须沿远查为空、真实建人响应建立身份。 */
    private void seedIscHistory(boolean add) {
        jdbc.update("UPDATE SMT_DEVICE SET IS_SYNC=1,CHANNEL_NO=1 WHERE PARK_ID=?",park);
        if(!add)jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,PERSON_ID,BADGE,IMAGE_ID,TASK_TYPE,CREATE_TIME,START_TIME,OVER_TIME) SELECT ?+ROWNUM,?,1,1,1,D.ID,TO_CHAR(S.ID),'isc-capacity-person-'||TO_CHAR(S.ID),S.BADGE,S.FACE_PIC_ID,1,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59' FROM SMT_STAFF S CROSS JOIN SMT_DEVICE D WHERE S.COMP_ID=? AND D.PARK_ID=?",
            1000000000000L+((long)park)*100000L,park,"employee-test-"+park,park);
        report.put("iscIdentityFixture",add?"empty remote jobNo lookup then real PERSON_ADD acknowledgement":"scoped legacy down records per person/device; no alias or target insertion");
    }

    /** 真实Timer按默认一秒固定延迟推进；观察线程只读状态和模拟DIRECT回执，不手动展开。 */
    private void runScheduledPipeline(int targets,int people,String action,String access,boolean expandByTimer) throws Exception {
        AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);
        AuthOperationSchedulerProperties.Instance instance=capacityInstance(access,targets);settings.getInstances().add(instance);
        if("ISC".equals(access) && targets>=10000) {settings.setExpansionSteps(200);settings.setExpansionBatchSteps(20);settings.setExpansionMillis(1000);settings.setDispatchWorkItems(32);settings.setDispatchMillis(1000);}
        configuredMaxInflight=instance.getMaxInflight();
        Map<String,Object> schedulerProfile=new LinkedHashMap<>();schedulerProfile.put("instance",JSONUtil.parseObj(instance));
        schedulerProfile.put("dispatchWorkItems",settings.getDispatchWorkItems());schedulerProfile.put("dispatchMillis",settings.getDispatchMillis());
        schedulerProfile.put("expansionSteps",settings.getExpansionSteps());schedulerProfile.put("expansionBatchSteps",settings.getExpansionBatchSteps());schedulerProfile.put("expansionMillis",settings.getExpansionMillis());
        report.put("schedulerProfile",schedulerProfile);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
        RemoteDispatcherService remote=mock(RemoteDispatcherService.class);SmtImageService images=mock(SmtImageService.class);
        when(images.getImageBase64ByCode("image-ref-"+park)).thenReturn("c3ludGhldGljLWZhY2U=");
        IscCapacityRemote isc="ISC".equals(access)?new IscCapacityRemote():null;
        when(remote.dispatch(any(),anyString())).thenAnswer(call->{
            try {
                Assert.assertFalse("所有外部HTTP必须在事务之外",TransactionSynchronizationManager.isActualTransactionActive());
                DispatcherDTO<?> dto=call.getArgument(0);Assert.assertEquals(Integer.valueOf(park),dto.getParkId());
                http.incrementAndGet();firstHttp.compareAndSet(0,System.nanoTime());
                if(isc!=null)return isc.dispatch(dto);
                Assert.assertTrue(EventEnum.DEVICE_ADD_CARD.getCode().equals(dto.getEventType())||EventEnum.DEVICE_DELETE_CARD.getCode().equals(dto.getEventType()));
                return Result.success(null);
            } catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);throw failure;}
        });
        AuthOperationTransportFacade raw=new AuthOperationTransportFacade(transport,remote,images,settings);
        // 与部署配置注入同义，仅提供受控ISC组织ID，不更改任何配额或限流。
        org.springframework.test.util.ReflectionTestUtils.setField(raw,"hfOrg","capacity-org-"+park);
        org.springframework.test.util.ReflectionTestUtils.setField(raw,"xcOrg","capacity-org-"+park);
        AuthOperationTransportFacade facade=proxy(raw,new DataSourceTransactionManager(pool));
        scheduler=new AuthOperationScheduler(settings,core,ledger,facade,employee,service);scheduler.start();
        activeTimer=new AuthOperationTimerTask(scheduler);
        report.put("timerFixedDelayMs",1000);report.put("timerCoverage",expandByTimer?"T020 actual Timer expansion required; no direct stage/bind calls":"service-loop expansion diagnostic only; Timer drives ISC transport");
        report.put("timerDriver","ScheduledExecutorService invokes the real AuthOperationTimerTask at its default fixed delay; Spring annotation discovery is not exercised");
        report.put("observationIntervalMs",1000);
        report.put("expansionTransportOverlap",expandByTimer);
        final long queue=System.nanoTime();long expandedAt=expandByTimer?0:queue,lastProgress=queue;boolean restarted=false;int ack=0;
        List<Map<String,Object>> progressSnapshots=new ArrayList<>();report.put("progressSnapshots",progressSnapshots);
        timerDriver=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"capacity-timer-"+park);t.setDaemon(true);return t;});
        timerDriver.scheduleWithFixedDelay(()->{try{timerTicks.incrementAndGet();activeTimer.advance();}catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);}},0,1000,TimeUnit.MILLISECONDS);
        try {
            while(millis(queue)<=TimeUnit.SECONDS.toMillis(pipelineSeconds)) {
                rethrowAsynchronousFailure();
                if("DIRECT".equals(access)) {
                    List<Long> accepted=jdbc.queryForList("SELECT ID FROM (SELECT ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND PHASE='DIRECT_SEND' AND STATE='ACCEPTED' ORDER BY ID) WHERE ROWNUM<=200",Long.class,park);
                    for(Long id:accepted){SmtAuthTransportPhase p=phases.byId(id);transport.receipt(park,instance.getId(),id,null,p.getDeviceId(),p.getSerialNo(),"capacity-timer-ack-"+id,true,"受控设备确认");ack++;}
                }
                sample();
                int expanded=jdbc.queryForObject("SELECT COALESCE(SUM(EXPANDED_COUNT),0) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Integer.class,park);
                int preparing=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS='PREPARING'",Integer.class,park);
                if(expandedAt==0&&millis(queue)>TimeUnit.SECONDS.toMillis(expandSeconds)) {
                    report.put("expandMs",millis(queue));report.put("expandedByTimer",expanded);
                    Assert.fail("真实一秒Timer在显式诊断时限"+expandSeconds+"秒内未完成展开；保留本规模失败，不等同产品SLO判定");
                }
                if(expandedAt==0&&expanded==targets&&preparing==0) {
                    expandedAt=System.nanoTime();report.put("expandMs",millis(queue));report.put("expandedByTimer",expanded);
                    report.put("stage","queue-confirm-converge");
                }
                // 重新构造调度实例恢复持久游标；不调用员工展开服务补齐缺口。
                long cursor=expandByTimer?jdbc.queryForObject("SELECT COALESCE(SUM(EXPANSION_CURSOR),0) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park):0;
                if(millis(lastProgress)>=60000) {
                    // 每分钟只读自有目标分布；不另开连接池，不参与任何调度决策。
                    List<Map<String,Object>> states=jdbc.queryForList("SELECT STATE,COUNT(*) N FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? GROUP BY STATE",park);
                    int observed=0;for(Map<String,Object> state:states)observed+=((Number)state.get("N")).intValue();
                    Map<String,Object> progress=new LinkedHashMap<>();progress.put("park",park);progress.put("elapsedMs",millis(queue));progress.put("stage",report.get("stage"));progress.put("ticks",timerTicks.get());progress.put("cursor",cursor);progress.put("expanded",expanded);progress.put("targetCount",observed);progress.put("targetStates",states);progress.put("http",http.get());
                    progressSnapshots.add(progress);System.out.println("CAPACITY_PROGRESS "+JSONUtil.toJsonStr(progress));lastProgress=System.nanoTime();
                }
                if(expandByTimer&&!restarted&&cursor>0&&expanded<targets) {
                    stopTimerDriver();stopScheduler(scheduler);
                    scheduler=new AuthOperationScheduler(settings,core,ledger,facade,employee,service);scheduler.start();activeTimer=new AuthOperationTimerTask(scheduler);
                    timerDriver=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"capacity-timer-resume-"+park);t.setDaemon(true);return t;});
                    timerDriver.scheduleWithFixedDelay(()->{try{timerTicks.incrementAndGet();activeTimer.advance();}catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);}},1000,1000,TimeUnit.MILLISECONDS);
                    report.put("expansionRecovery","restarted scheduler after persisted partial expansion; Timer alone resumed");restarted=true;
                }
                int remaining=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS!='CONVERGED'",Integer.class,park);
                if(remaining==0) {
                    Assert.assertTrue("收敛前必须展开全部预期目标",expandedAt!=0);
                    Assert.assertEquals(Integer.valueOf(targets),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",Integer.class,park));
                    Assert.assertEquals(Integer.valueOf(targets),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND STATE='CONVERGED'",Integer.class,park));
                    Assert.assertEquals(Integer.valueOf("DELETE".equals(action)?0:people),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)",Integer.class,"employee-test-"+park));
                    if(isc!=null) {
                        int finishedDownloads=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND PHASE='ISC_DOWNLOAD' AND STATE='FINISHED'",Integer.class,park);
                        Assert.assertEquals(targets,finishedDownloads);
                        Assert.assertEquals(Integer.valueOf("DELETE".equals(action)?0:targets),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE PARK_ID=?",Integer.class,park));
                        Assert.assertEquals("每个目标必须收到精确匹配的ISC明细",targets,isc.detailReturnedTargets.size());
                        if(targets>=10000) {
                            String event="DELETE".equals(action)?"CONFIG_DELETE":"CONFIG_ADD";Map<Integer,Integer> widths=isc.peopleSnapshot().get(event);
                            Assert.assertNotNull("大批ISC必须记录配置请求宽度",widths);Assert.assertTrue("大批ISC必须实际使用200人兼容请求分片",widths.containsKey(200));
                        }
                        report.put("ackCount",finishedDownloads);
                    } else {Assert.assertEquals(targets,ack);Assert.assertEquals(targets,http.get());report.put("ackCount",ack);}
                    report.put("pipelineMs",millis(queue));report.put("confirmConvergeMs",millis(expandedAt));report.put("completeFromAcceptStartMs",millis(firstAcceptStarted));
                    return;
                }
                Thread.sleep(1000);
            }
            Assert.fail("显式诊断时限"+pipelineSeconds+"秒内真实Timer管道未收敛，保留原规模与阶段失败");
        } finally {
            stopTimerDriver();
            if(scheduler!=null){stopScheduler(scheduler);scheduler=null;}
            if(isc!=null){report.put("iscHttpByEvent",isc.httpSnapshot());report.put("iscPeoplePerRequest",isc.peopleSnapshot());report.put("iscDetailReturnedTargets",isc.detailReturnedTargets.size());report.put("iscCorrelationReads",isc.correlationReads.get());report.put("iscCorrelationReadMs",TimeUnit.NANOSECONDS.toMillis(isc.correlationReadNanos.get()));}
        }
    }
    private void rethrowAsynchronousFailure() {
        Throwable failure=asynchronousFailure.get();if(failure!=null)throw new AssertionError("真实Timer或受控HTTP异步失败",failure);
    }
    private void stopTimerDriver() throws InterruptedException {
        if(timerDriver==null)return;timerDriver.shutdown();Assert.assertTrue("本测试Timer必须在退出时停止",timerDriver.awaitTermination(30,TimeUnit.SECONDS));timerDriver=null;
    }

    /** 仅替代远端协议；按当前请求键读取已持久化成员作精确回执关联，绝不扫描整批目标。 */
    private class IscCapacityRemote {
        final Map<String,String> personsByBadge=new ConcurrentHashMap<>();
        final Set<String> knownPersons=ConcurrentHashMap.newKeySet();
        final Map<String,RemoteBatch> configs=new ConcurrentHashMap<>(),downloads=new ConcurrentHashMap<>();
        final Map<String,AtomicInteger> requests=new ConcurrentHashMap<>();
        final Map<String,Map<Integer,AtomicInteger>> peoplePerRequest=new ConcurrentHashMap<>();
        final Set<Long> detailReturnedTargets=ConcurrentHashMap.newKeySet();
        final AtomicInteger externalIds=new AtomicInteger(),correlationReads=new AtomicInteger();
        final AtomicLong correlationReadNanos=new AtomicLong();
        IscCapacityRemote() {
            for(Map<String,Object> row:jdbc.queryForList("SELECT DISTINCT BADGE,PERSON_ID FROM SMT_ISC_DOWN_RECORD WHERE PARK_ID=?",park)){String person=(String)row.get("PERSON_ID");personsByBadge.put((String)row.get("BADGE"),person);knownPersons.add(person);}
        }
        Result<String> dispatch(DispatcherDTO<?> dto) {
            JSONObject data=JSONUtil.parseObj(dto.getData());Integer event=dto.getEventType();
            if(EventEnum.ISC_PERSON_GET.getCode().equals(event)) {
                JSONArray badges=data.getJSONArray("paramValue");Assert.assertEquals("jobNo",data.getStr("paramName"));Assert.assertTrue(badges.size()<=200);record("PERSON_GET",badges.size());
                JSONArray rows=new JSONArray();for(Object badge:badges){String person=personsByBadge.get(String.valueOf(badge));if(person!=null)rows.add(new JSONObject().put("jobNo",badge).put("personId",person).put("status",1).put("personPhoto",new JSONArray().put(new JSONObject().put("picUri","frozen"))));}
                return json(new JSONObject().put("list",rows));
            }
            if(EventEnum.ISC_PERSON_ADD.getCode().equals(event)) {
                record("PERSON_ADD",1);Assert.assertEquals("capacity-org-"+park,data.getStr("orgIndexCode"));Assert.assertNotNull(data.getStr("personName"));
                Assert.assertEquals("c3ludGhldGljLWZhY2U=",data.getJSONArray("faces").getJSONObject(0).getStr("faceData"));
                String badge=data.getStr("jobNo");Assert.assertNotNull(badge);String person="isc-created-"+park+"-"+badge;
                String previous=personsByBadge.putIfAbsent(badge,person);if(previous!=null)Assert.assertEquals("重复建人必须保持相同远端身份",previous,person);
                knownPersons.add(person);
                return json(new JSONObject().put("personId",person));
            }
            if(EventEnum.ISC_FACE_ADD.getCode().equals(event)) {
                record("FACE_ADD",1);Assert.assertTrue(knownPersons.contains(data.getStr("personId")));Assert.assertEquals("c3ludGhldGljLWZhY2U=",data.getStr("faceData"));
                return json(new JSONObject().put("personId",data.getStr("personId")).put("faceId","face-"+data.getStr("personId")));
            }
            if(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(event)||EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(event)) {
                String device=resource(data.getJSONArray("resourceInfos").getJSONObject(0));JSONArray ids=data.getJSONArray("personDatas").getJSONObject(0).getJSONArray("indexCodes");
                RemoteBatch batch=members(dto.getEventId(),"ISC_CONFIG",device);Assert.assertEquals(new HashSet<>(batch.people),new HashSet<>(ids.toList(String.class)));
                record(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(event)?"CONFIG_ADD":"CONFIG_DELETE",ids.size());String external="isc-capacity-config-"+park+"-"+externalIds.incrementAndGet();configs.put(external,batch);
                return json(new JSONObject().put("taskId",external));
            }
            if(EventEnum.ISC_AUTH_CONFIG_PROCESS_GET.getCode().equals(event)) {
                RemoteBatch batch=configs.get(data.getStr("taskId"));Assert.assertNotNull("进度只能引用远端实际返回的配置ID",batch);record("CONFIG_PROGRESS",batch.people.size());
                return json(new JSONObject().put("isFinished",true).put("isConfigFinished",true).put("failedNum",0).put("successedNum",batch.people.size()));
            }
            if(EventEnum.ISC_AUTH_CONFIG_DOWN.getCode().equals(event)) {
                Assert.assertEquals(Integer.valueOf(5),data.getInt("taskType"));String device=resource(data.getJSONArray("resourceInfos").getJSONObject(0));
                RemoteBatch batch=members(dto.getEventId(),"ISC_DOWNLOAD",device);record("DOWNLOAD",batch.people.size());String external="isc-capacity-download-"+park+"-"+externalIds.incrementAndGet();downloads.put(external,batch);
                return json(new JSONObject().put("taskId",external));
            }
            if(EventEnum.ISC_TASK_RECORD_DETAIL_GET.getCode().equals(event)) {
                RemoteBatch batch=downloads.get(data.getStr("taskId"));Assert.assertNotNull("明细只能引用远端实际返回的下载ID",batch);Assert.assertEquals(batch.device,resource(data.getJSONObject("resourceInfo")));
                Assert.assertEquals(Integer.valueOf(200),data.getInt("pageSize"));int page=data.getInt("pageNo");Assert.assertTrue(page>=1);int start=(page-1)*200,end=Math.min(start+200,batch.people.size());Assert.assertTrue("合法明细页不能越界",start<end);
                JSONArray rows=new JSONArray();for(int i=start;i<end;i++)rows.add(new JSONObject().put("personId",batch.people.get(i)).put("persondownloadResult","0"));
                record("DETAIL",rows.size());detailReturnedTargets.addAll(batch.targets);
                return json(new JSONObject().put("list",rows).put("total",batch.people.size()));
            }
            throw new AssertionError("未预期ISC容量协议事件："+event);
        }
        private RemoteBatch members(String requestKey,String phase,String device) {
            Assert.assertNotNull(requestKey);correlationReads.incrementAndGet();
            long readStarted=System.nanoTime();
            List<Map<String,Object>> rows=jdbc.queryForList("SELECT PERSON_ID,TARGET_ID,DEVICE_ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND INSTANCE_ID=? AND REQUEST_KEY=? AND PHASE=?",park,"capacity-"+park,requestKey,phase);
            correlationReadNanos.addAndGet(System.nanoTime()-readStarted);
            Assert.assertTrue("外发请求成员必须存在并受200人上限约束",!rows.isEmpty()&&rows.size()<=200);RemoteBatch batch=new RemoteBatch(device);
            for(Map<String,Object> row:rows){Assert.assertEquals(device,row.get("DEVICE_ID"));String person=(String)row.get("PERSON_ID");Assert.assertNotNull(person);Assert.assertTrue("配置身份必须已由受控远端或可信历史证明",knownPersons.contains(person));batch.people.add(person);batch.targets.add(((Number)row.get("TARGET_ID")).longValue());}
            return batch;
        }
        private String resource(JSONObject resource) {
            String id=resource.getStr("resourceIndexCode");Assert.assertTrue(id.equals("employee-device-"+park)||id.startsWith("capacity-device-"+park+"-"));Assert.assertEquals("acsDevice",resource.getStr("resourceType"));Assert.assertEquals(Integer.valueOf(1),resource.getJSONArray("channelNos").getInt(0));return id;
        }
        private Result<String> json(JSONObject value){return Result.success(value.toString());}
        private void record(String event,int people){requests.computeIfAbsent(event,k->new AtomicInteger()).incrementAndGet();peoplePerRequest.computeIfAbsent(event,k->new ConcurrentHashMap<>()).computeIfAbsent(people,k->new AtomicInteger()).incrementAndGet();}
        Map<String,Integer> httpSnapshot(){Map<String,Integer> result=new TreeMap<>();requests.forEach((key,count)->result.put(key,count.get()));return result;}
        Map<String,Map<Integer,Integer>> peopleSnapshot(){Map<String,Map<Integer,Integer>> result=new TreeMap<>();peoplePerRequest.forEach((event,sizes)->{Map<Integer,Integer> counts=new TreeMap<>();sizes.forEach((size,count)->counts.put(size,count.get()));result.put(event,counts);});return result;}
    }
    private static class RemoteBatch {
        final String device;final List<String> people=new ArrayList<>();final Set<Long> targets=new HashSet<>();
        RemoteBatch(String device){this.device=device;}
    }

    private void runDirect(int targets) throws Exception {
        AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);
        AuthOperationSchedulerProperties.Instance instance=new AuthOperationSchedulerProperties.Instance();instance.setId("capacity-"+park);instance.setAccessType("DIRECT");instance.setParks(Collections.singletonList(park));settings.getInstances().add(instance);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
        RemoteDispatcherService remote=mock(RemoteDispatcherService.class);SmtImageService images=mock(SmtImageService.class);
        when(images.getImageBase64ByCode("image-ref-"+park)).thenReturn("c3ludGhldGljLWZhY2U=");
        when(remote.dispatch(any(),anyString())).thenAnswer(call->{
            Assert.assertFalse("HTTP不得持有事务",TransactionSynchronizationManager.isActualTransactionActive());DispatcherDTO<?> d=call.getArgument(0);
            Assert.assertEquals(Integer.valueOf(park),d.getParkId());Assert.assertTrue(EventEnum.DEVICE_ADD_CARD.getCode().equals(d.getEventType())||EventEnum.DEVICE_DELETE_CARD.getCode().equals(d.getEventType()));
            http.incrementAndGet();firstHttp.compareAndSet(0,System.nanoTime());return Result.success(null);
        });
        AuthOperationTransportFacade facade=proxy(new AuthOperationTransportFacade(transport,remote,images,settings),new DataSourceTransactionManager(pool));
        scheduler=new AuthOperationScheduler(settings,core,ledger,facade,employee,service);scheduler.start();AuthOperationTimerTask timer=new AuthOperationTimerTask(scheduler);
        long queue=System.nanoTime();int ack=0;boolean restarted=false;
        while(millis(queue)<=TimeUnit.SECONDS.toMillis(pipelineSeconds)) {
            timer.advance();
            List<Long> accepted=jdbc.queryForList("SELECT ID FROM (SELECT ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND PHASE='DIRECT_SEND' AND STATE='ACCEPTED' ORDER BY ID) WHERE ROWNUM<=200",Long.class,park);
            for(Long id:accepted){SmtAuthTransportPhase p=phases.byId(id);transport.receipt(park,instance.getId(),id,null,p.getDeviceId(),p.getSerialNo(),"capacity-ack-"+id,true,"受控设备确认");ack++;}
            sample();
            if(!restarted && ack>=Math.min(100,targets/2)){stopScheduler(scheduler);scheduler=new AuthOperationScheduler(settings,core,ledger,facade,employee,service);scheduler.start();timer=new AuthOperationTimerTask(scheduler);restarted=true;}
            int remaining=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS!='CONVERGED'",Integer.class,park);
            if(remaining==0){report.put("confirmConvergeMs",millis(queue));report.put("ackCount",ack);report.put("firstHttpFromAcceptStartMs",TimeUnit.NANOSECONDS.toMillis(firstHttp.get()-started));Assert.assertEquals(targets,ack);Assert.assertEquals(targets,http.get());return;}
            Thread.sleep(100);
        }
        Assert.fail("显式诊断时限"+pipelineSeconds+"秒内未收敛，保留容量场景未完成证据");
    }
    /** 两万新增保留为真实未确认积压，再通过原有批量入口提交20个无目标冲突的撤权。 */
    private void runPriorityDelete(String access) throws Exception {
        final int small=20;final long firstSmall=employeeId+staffCount;final int priorityAuthority=700000000+park;final String priorityDevice="employee-device-"+park;
        // 小批撤权只指向一台设备：它必须在大量新增积压下优先开始，不能借4,000个删除目标伪装成小任务。
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",priorityAuthority,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",priorityAuthority+1,priorityAuthority,priorityDevice,park);
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) SELECT ?+LEVEL-1,?,1,?,'small-delete-'||?||'-'||LEVEL,'合成撤权员工' FROM DUAL CONNECT BY LEVEL<=?",firstSmall,"employee-test-"+park,"image-ref-"+park,park,small);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) SELECT ?+LEVEL-1,?+LEVEL-1,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2 FROM DUAL CONNECT BY LEVEL<=?",((long)park)+staffCount,firstSmall,priorityAuthority,small);
        Map<String,Long> acceptedAt=new ConcurrentHashMap<>(),deleteLatency=new ConcurrentHashMap<>();AtomicInteger adds=new AtomicInteger();
        AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);
        AuthOperationSchedulerProperties.Instance instance=capacityInstance(access,20000);settings.getInstances().add(instance);
        if("ISC".equals(access)) {settings.setExpansionSteps(200);settings.setExpansionBatchSteps(20);settings.setExpansionMillis(1000);settings.setDispatchWorkItems(32);settings.setDispatchMillis(1000);}
        configuredMaxInflight=instance.getMaxInflight();
        Map<String,Object> schedulerProfile=new LinkedHashMap<>();schedulerProfile.put("instance",JSONUtil.parseObj(instance));
        schedulerProfile.put("dispatchWorkItems",settings.getDispatchWorkItems());schedulerProfile.put("dispatchMillis",settings.getDispatchMillis());
        schedulerProfile.put("expansionSteps",settings.getExpansionSteps());schedulerProfile.put("expansionBatchSteps",settings.getExpansionBatchSteps());schedulerProfile.put("expansionMillis",settings.getExpansionMillis());
        report.put("schedulerProfile",schedulerProfile);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
        RemoteDispatcherService remote=mock(RemoteDispatcherService.class);SmtImageService images=mock(SmtImageService.class);
        when(images.getImageBase64ByCode("image-ref-"+park)).thenReturn("c3ludGhldGljLWZhY2U=");
        Map<String,String> priorityPersonSubjects=new ConcurrentHashMap<>();
        if("ISC".equals(access)) {
            jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,PERSON_ID,BADGE,IMAGE_ID,TASK_TYPE,CREATE_TIME,START_TIME,OVER_TIME) SELECT ?+ROWNUM,?,1,1,1,?,TO_CHAR(S.ID),'isc-priority-person-'||TO_CHAR(S.ID),S.BADGE,S.FACE_PIC_ID,1,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59' FROM SMT_STAFF S WHERE S.ID>=? AND S.ID<?",9000000000000L+((long)park)*100000L,park,priorityDevice,firstSmall,firstSmall+small);
            for(int i=0;i<small;i++)priorityPersonSubjects.put("isc-priority-person-"+(firstSmall+i),String.valueOf(firstSmall+i));
        }
        IscCapacityRemote isc="ISC".equals(access)?new IscCapacityRemote():null;
        when(remote.dispatch(any(),anyString())).thenAnswer(call->{
            Assert.assertFalse("HTTP不能持有数据库事务",TransactionSynchronizationManager.isActualTransactionActive());DispatcherDTO<?> dto=call.getArgument(0);
            Assert.assertEquals(Integer.valueOf(park),dto.getParkId());http.incrementAndGet();
            if(isc!=null) {
                if(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(dto.getEventType()))adds.incrementAndGet();
                if(EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(dto.getEventType())) {
                    JSONArray people=JSONUtil.parseObj(dto.getData()).getJSONArray("personDatas").getJSONObject(0).getJSONArray("indexCodes");
                    for(Object person:people) {
                        String subject=priorityPersonSubjects.get(String.valueOf(person));if(subject==null)continue;
                        Long accepted=acceptedAt.get(subject);Assert.assertNotNull("ISC撤权必须来自已受理的小批请求",accepted);
                        deleteLatency.putIfAbsent(subject,TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-accepted));
                    }
                }
                return isc.dispatch(dto);
            }
            if(EventEnum.DEVICE_ADD_CARD.getCode().equals(dto.getEventType()))adds.incrementAndGet();
            else {
                Assert.assertEquals(EventEnum.DEVICE_DELETE_CARD.getCode(),dto.getEventType());String subject=cn.hutool.json.JSONUtil.parseObj(dto.getData()).getStr("cardNo");
                Long accepted=acceptedAt.get(subject);Assert.assertNotNull("外发撤权必须来自已受理的小批请求",accepted);
                deleteLatency.putIfAbsent(subject,TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-accepted));
            }
            return Result.success(null);
        });
        AuthOperationTransportFacade raw=new AuthOperationTransportFacade(transport,remote,images,settings);
        // 大批新增必须由空查、建人和回执建立身份；不能把无证明的远端人员伪装成可复用身份。
        org.springframework.test.util.ReflectionTestUtils.setField(raw,"hfOrg","capacity-org-"+park);
        org.springframework.test.util.ReflectionTestUtils.setField(raw,"xcOrg","capacity-org-"+park);
        AuthOperationTransportFacade facade=proxy(raw,new DataSourceTransactionManager(pool));
        scheduler=new AuthOperationScheduler(settings,core,ledger,facade,employee,service);scheduler.start();AuthOperationTimerTask timer=new AuthOperationTimerTask(scheduler);
        long warmup=System.nanoTime();while(http.get()==0&&millis(warmup)<30000){timer.advance();Thread.sleep(100);}
        Assert.assertTrue("大批ADD必须已经开始受控ISC远端流程",http.get()>0);
        Assert.assertEquals("两万ADD仍是未确认积压",Integer.valueOf(20000),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND ACTION='ADD' AND STATE!='CONVERGED'",Integer.class,park));
        long priorityStart=System.nanoTime();
        List<String> subjects=new ArrayList<>();for(int i=0;i<small;i++)subjects.add(String.valueOf(firstSmall+i));
        UpdateDeviceAuthDTO dto=new UpdateDeviceAuthDTO();dto.setIds(subjects);dto.setDeviceAuthIds(Collections.emptyList());dto.setStartTime("2026-09-01");dto.setEndTime("2026-09-30");
        report.put("priorityDeleteEntry","one MVC bulk request for 20 staff; source expansion remains in the measured path");
        long accepted=System.nanoTime();for(String subject:subjects)acceptedAt.put(subject,accepted);
        org.springframework.mock.web.MockHttpServletResponse response=httpEntry.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/staff/device/auth/updateAuth/2").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(cn.hutool.json.JSONUtil.toJsonStr(dto))).andReturn().getResponse();
        Assert.assertEquals(200,response.getStatus());Assert.assertTrue(cn.hutool.json.JSONUtil.toBean(response.getContentAsString(),Result.class).isSuccess());timer.advance();
        while(deleteLatency.size()<small&&millis(priorityStart)<120000){timer.advance();sample();Thread.sleep(100);}
        List<Long> latencies=new ArrayList<>(deleteLatency.values());report.put("smallDeleteFirstHttpMs",latencies);report.put("smallDeleteHttpSamples",latencies.size());report.put("addRequestsBeforeDeleteCompletion",adds.get());
        Assert.assertEquals("SC002：每个无冲突小批撤权必须获得首次HTTP",small,latencies.size());
        report.put("smallDeleteP95Ms",percentile(latencies,.95));report.put("smallDeleteMaxMs",Collections.max(latencies));
        Assert.assertTrue("SC002 P95必须不超过30秒",percentile(latencies,.95)<=30000);Assert.assertTrue("SC002最大必须不超过120秒",Collections.max(latencies)<=120000);
        Assert.assertEquals("未回执的大批ADD不可假成功",Integer.valueOf(20000),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND ACTION='ADD' AND STATE!='CONVERGED'",Integer.class,park));
    }
    private void sample() {
        Runtime r=Runtime.getRuntime();maxHeap=Math.max(maxHeap,r.totalMemory()-r.freeMemory());maxConnections=Math.max(maxConnections,pool.getHikariPoolMXBean().getTotalConnections());
        if(scheduler!=null)maxInflight=Math.max(maxInflight,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND STATE IN ('EXECUTING','WAITING_CONFIRM')",Long.class,park));
        Assert.assertTrue("连接池必须有界",maxConnections<=4);Assert.assertTrue("容量档在途目标不得突破已配置上限",maxInflight<=configuredMaxInflight);
    }
    private void failExpansion(int staged,int bound,long expand) {
        sqlTiming.clearDeadline();report.put("expandMs",millis(expand));report.put("stageCalls",staged);report.put("bindCalls",bound);
        report.put("batchCursors",jdbc.queryForList("SELECT ID,EXPECTED_COUNT,EXPANDED_COUNT,EXPANSION_CURSOR,STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",park));
        Assert.fail("展开阶段超过显式诊断时限"+expandSeconds+"秒，停止并报告瓶颈，不能据探针外推万级通过");
    }
    /** 只控制显式诊断预算，不参与受理或小批撤权的产品门槛。 */
    private static int diagnosticSeconds(String key,int fallback){int seconds=Integer.parseInt(value(key,String.valueOf(fallback)));Assert.assertTrue("诊断时限必须为1至7200秒",seconds>=1&&seconds<=7200);return seconds;}
    private static String value(String key,String fallback){String v=System.getenv(key);return v==null?fallback:v;}
    private static long millis(long start){return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);}
    private static long percentile(List<Long> values,double p){List<Long> sorted=new ArrayList<>(values);Collections.sort(sorted);return sorted.get(Math.max(0,(int)Math.ceil(sorted.size()*p)-1));}
}
