package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import com.tce.smart.schedule.task.AuthOperationTimerTask;
import org.junit.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;
import static org.mockito.Mockito.*;

/** 真实 ISC 全管道：共享来源、外部批次、缺项保护与持久明细游标恢复。 */
public class AuthOperationIscPipelineOracleTest extends AuthOperationCapacityFixture {
    private AuthOperationScheduler scheduler;
    private AuthOperationTimerTask timer;
    private AuthOperationSchedulerProperties settings;
    private AuthOperationProperties enabled;
    private RemoteDispatcherService remote;
    private SmtImageService images;
    private final AtomicInteger detailMode=new AtomicInteger();
    private final AtomicInteger configCount=new AtomicInteger();
    private final AtomicInteger downloadCount=new AtomicInteger();
    private final AtomicInteger pageTwoCount=new AtomicInteger();
    private final AtomicInteger emptyCount=new AtomicInteger();
    private final List<String> externalRequests=Collections.synchronizedList(new ArrayList<>());

    @Before public void isc() {
        jdbc.update("UPDATE SMT_DEVICE SET IS_SYNC=1,CHANNEL_NO=1 WHERE PARK_ID=?",park);
        // 场景前提为已有设备权限；绑定真实历史下发身份，不能把工号远查单命中当可信凭据。
        jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,PERSON_ID,BADGE,IMAGE_ID,TASK_TYPE,CREATE_TIME,START_TIME,OVER_TIME) VALUES(?,?,1,1,1,?,?,?,?,?,1,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59')",
            1000000000000L+park,park,"employee-device-"+park,String.valueOf(employeeId),"isc-person-"+park,"badge-"+park,"image-ref-"+park);
        settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);
        AuthOperationSchedulerProperties.Instance instance=new AuthOperationSchedulerProperties.Instance();
        instance.setId(instanceId());instance.setAccessType("ISC");instance.setParks(Collections.singletonList(park));settings.getInstances().add(instance);
        enabled=new AuthOperationProperties();enabled.setEnabled(true);enabled.setEnabledParks(Collections.singleton(park));
        remote=mock(RemoteDispatcherService.class);images=mock(SmtImageService.class);
        when(images.getImageBase64ByCode("image-ref-"+park)).thenReturn("c3ludGhldGljLWZhY2U=");
        when(remote.dispatch(any(),anyString())).thenAnswer(call->{
            Assert.assertFalse("所有外部调用必须在真实本地事务之外",TransactionSynchronizationManager.isActualTransactionActive());
            DispatcherDTO<?> dto=call.getArgument(0);Assert.assertEquals(Integer.valueOf(park),dto.getParkId());
            JSONObject data=JSONUtil.parseObj(dto.getData());Integer event=dto.getEventType();
            if(EventEnum.ISC_PERSON_GET.getCode().equals(event))
                return Result.success("{\"list\":[{\"jobNo\":\"badge-"+park+"\",\"personId\":\"isc-person-"+park+"\",\"status\":1,\"personPhoto\":[{\"picUri\":\"frozen\"}]}]}");
            if(EventEnum.ISC_FACE_ADD.getCode().equals(event)) {
                Assert.assertEquals("isc-person-"+park,data.getStr("personId"));
                Assert.assertEquals("补脸只读取冻结图片引用","c3ludGhldGljLWZhY2U=",data.getStr("faceData"));
                return Result.success(new JSONObject().put("faceId","isc-face-"+park).put("personId","isc-person-"+park).toString());
            }
            if(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(event)||EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(event)) {
                Assert.assertEquals("employee-device-"+park,data.getJSONArray("resourceInfos").getJSONObject(0).getStr("resourceIndexCode"));
                Assert.assertEquals("isc-person-"+park,data.getJSONArray("personDatas").getJSONObject(0).getJSONArray("indexCodes").getStr(0));
                int n=configCount.incrementAndGet();return Result.success("{\"taskId\":\"isc-config-"+park+"-"+n+"\"}");
            }
            if(EventEnum.ISC_AUTH_CONFIG_PROCESS_GET.getCode().equals(event)) {
                Assert.assertTrue(data.getStr("taskId").startsWith("isc-config-"+park+"-"));externalRequests.add(data.getStr("taskId"));
                return Result.success("{\"isFinished\":true,\"isConfigFinished\":true,\"failedNum\":0,\"successedNum\":1}");
            }
            if(EventEnum.ISC_AUTH_CONFIG_DOWN.getCode().equals(event)) {
                Assert.assertEquals(Integer.valueOf(5),data.getInt("taskType"));
                int n=downloadCount.incrementAndGet();return Result.success("{\"taskId\":\"isc-download-"+park+"-"+n+"\"}");
            }
            if(EventEnum.ISC_TASK_RECORD_DETAIL_GET.getCode().equals(event)) {
                Assert.assertTrue(data.getStr("taskId").startsWith("isc-download-"+park+"-"));externalRequests.add(data.getStr("taskId"));
                Assert.assertEquals(Integer.valueOf(200),data.getInt("pageSize"));
                int mode=detailMode.get(),page=data.getInt("pageNo");
                if(mode==0)return Result.success("{\"list\":[{\"personId\":\"unrelated\",\"persondownloadResult\":\"0\"}],\"total\":1}");
                if(mode==1) {
                    if(page==2){pageTwoCount.incrementAndGet();return Result.success(null);}
                    JSONArray rows=new JSONArray();for(int i=0;i<200;i++)rows.add(new JSONObject().put("personId","unrelated-"+i).put("persondownloadResult","0"));
                    return Result.success(new JSONObject().put("list",rows).put("total",201).toString());
                }
                if(mode==3){emptyCount.incrementAndGet();return Result.success("{\"list\":[],\"total\":0}");}
                if(page==2)pageTwoCount.incrementAndGet();
                return Result.success("{\"list\":[{\"personId\":\"isc-person-"+park+"\",\"persondownloadResult\":\"0\"}],\"total\":201}");
            }
            throw new AssertionError("未预期外部协议事件："+event);
        });
    }

    @Test public void sharedSourceSurvivesMissingDetailAndRestartThenLastSourceIsRevoked() throws Exception {
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park+1,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+1,park+1,"employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park+1,employeeId,park+1);
        entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());start();
        await(()->count("SMT_AUTH_TRANSPORT_PHASE","PHASE='ISC_DOWNLOAD' AND STATE='ACCEPTED'")==1,40);
        Assert.assertEquals("缺项不能删除任意来源",2,sourceCount());
        Assert.assertEquals("共享来源应保持物理权限", "ADD",jdbc.queryForObject("SELECT ACTION FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",String.class,park));
        detailMode.set(1);
        await(()->count("SMT_AUTH_TRANSPORT_PHASE","PHASE='ISC_DOWNLOAD' AND PAGE_NO=2")==1,30);
        Assert.assertEquals(2,sourceCount());stopScheduler(scheduler);scheduler=null;
        int previousPageTwo=pageTwoCount.get();detailMode.set(2);start();
        await(()->sourceCount()==1,40);
        Assert.assertTrue("恢复必须从已持久化的第2页继续",pageTwoCount.get()>previousPageTwo);
        Assert.assertEquals(Integer.valueOf(park+1),jdbc.queryForObject("SELECT AUTH_ID FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId));
        Assert.assertEquals(1,count("SMT_ISC_DOWN_RECORD","1=1"));
        SmtAuthTransportPhase downloaded=phases.byId(jdbc.queryForObject("SELECT ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND PHASE='ISC_DOWNLOAD'",Long.class,park));
        Assert.assertEquals("isc-download-"+park+"-1",downloaded.getExternalId());
        Assert.assertEquals("可信历史人员身份必须沿真实阶段传递","isc-person-"+park,downloaded.getPersonId());
        Assert.assertEquals("必须真实回写本次task记录，不能只保留历史记录",Long.valueOf(downloaded.getTaskId()),jdbc.queryForObject("SELECT TASK_ID FROM SMT_ISC_DOWN_RECORD WHERE PARK_ID=?",Long.class,park));
        transport.receipt(park,instanceId(),downloaded.getId(),downloaded.getPersonId(),downloaded.getDeviceId(),downloaded.getExternalId(),"explicit-duplicate-"+park,true,"可信重复回执");
        Assert.assertEquals("重复ACK不能新增记录",1,count("SMT_ISC_DOWN_RECORD","1=1"));
        entry.removeAuthToDevice(Collections.singletonList(park+1),Collections.emptyList());
        await(()->sourceCount()==0,40);
        Assert.assertEquals("最后来源撤销后记录必须删除",0,count("SMT_ISC_DOWN_RECORD","1=1"));
        Assert.assertEquals(2,count("SMT_AUTH_OPERATION_BATCH","STATUS='CONVERGED'"));
        Assert.assertEquals(2,configCount.get());Assert.assertEquals(2,downloadCount.get());
        Assert.assertTrue(externalRequests.contains("isc-config-"+park+"-1"));
        Assert.assertTrue(externalRequests.contains("isc-download-"+park+"-2"));
    }

    @Test public void emptyDeviceDetailCannotConfirmDelete() throws Exception {
        detailMode.set(3);entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());start();
        await(()->emptyCount.get()>0,40);
        stopScheduler(scheduler);scheduler=null;
        Assert.assertEquals(1,sourceCount());
        Assert.assertEquals("空回执必须保留历史设备权限记录",1,count("SMT_ISC_DOWN_RECORD","1=1"));
        Assert.assertEquals(0,count("SMT_AUTH_OPERATION_BATCH","STATUS='CONVERGED'"));
        Assert.assertEquals(0,count("SMT_AUTH_OPERATION_TARGET","STATE IN ('CONFIRMED','CONVERGED')"));
    }

    private void start() {
        AuthOperationTransportFacade facade=proxy(new AuthOperationTransportFacade(transport,remote,images,settings),new DataSourceTransactionManager(pool));
        scheduler=new AuthOperationScheduler(settings,enabled,ledger,facade,employee,service);timer=new AuthOperationTimerTask(scheduler);scheduler.start();
    }
    private String instanceId(){return "capacity-"+park;}
    private int sourceCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId);}
    private int count(String table,String clause){return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=? AND "+clause,Integer.class,park);}
    private void await(BooleanSupplier condition,int seconds) throws Exception {
        long end=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(seconds);
        while(System.nanoTime()<end){timer.advance();if(condition.getAsBoolean())return;Thread.sleep(100);}
        Assert.fail("ISC管道超时："+jdbc.queryForList("SELECT PHASE,STATE,ERROR_CODE,EXTERNAL_ID,PAGE_NO FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?",park));
    }
    @After public void stopIsc() throws Exception {if(scheduler!=null)stopScheduler(scheduler);}
}
