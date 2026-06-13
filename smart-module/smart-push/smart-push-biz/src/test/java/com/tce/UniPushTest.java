package com.tce;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.notify.Notify;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.base.payload.Payload;
import com.gexin.rp.sdk.dto.GtReq;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.APNTemplate;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/1 .
 * @Modified By:
 */
public class UniPushTest {
    //定义常量, appId、appKey、masterSecret 采用本文档 "第二步 获取访问凭证 "中获得的应用配置
    private static final String appId = config("smart.unipush.app-id", "SMART_UNIPUSH_APP_ID");
    private static final String appKey = config("smart.unipush.app-key", "SMART_UNIPUSH_APP_KEY");
    private static final String masterSecret = config("smart.unipush.master-secret", "SMART_UNIPUSH_MASTER_SECRET");
    private static final String url = config("smart.unipush.url", "SMART_UNIPUSH_URL", "http://sdk.open.api.igexin.com/apiex.htm");

    public static void main(String[] args) throws IOException {
        UniPushTest uniPush = new UniPushTest();
        uniPush.push3();
    }
    private void push1(){
        IGtPush push = new IGtPush(url, appKey, masterSecret);

        // 定义"点击链接打开通知模板"，并设置标题、内容、链接
        LinkTemplate template = new LinkTemplate();
        template.setAppId(appId);
        template.setAppkey(appKey);
        template.setTitle("test");
        template.setText("test yuto");
        template.setUrl("http://getui.com");

        List<String> appIds = new ArrayList<String>();
        appIds.add(appId);

        // 定义"AppMessage"类型消息对象，设置消息内容模板、发送的目标App列表、是否支持离线发送、以及离线消息有效期(单位毫秒)
        AppMessage message = new AppMessage();
        message.setData(template);
        message.setAppIdList(appIds);
        message.setOffline(true);
        message.setOfflineExpireTime(1000 * 600);
        SingleMessage var1 = new SingleMessage();
        var1.setData(template);
        var1.setOffline(true);
        var1.setOfflineExpireTime(1000 * 600);
        Target var2 = new Target();
        var2.setClientId("Cid00001");
        var2.setAppId(appId);
        IPushResult ret = push.pushMessageToSingle(var1,var2); //pushMessageToApp(message);
        System.out.println(ret.getResponse().toString());
    }
    private void push2(){
        IGtPush push = new IGtPush(url, appKey, masterSecret);

        TransmissionTemplate t = new TransmissionTemplate();
        t.setAppId(appId);
        t.setAppkey(appKey);
        t.setTransmissionContent("{title:\"标题\",content:\"内容\",payload:\"自定义数据\"}");
        t.setTransmissionType(1);

        // 定义"点击链接打开通知模板"，并设置标题、内容、链接
//        LinkTemplate template = new LinkTemplate();
//        template.setAppId(appId);
//        template.setAppkey(appKey);
//        template.setTitle("你好你好");
//        template.setText("哦，好吧");
//        template.setLogoUrl("");
//        template.setUrl("http://getui.com");

        List<String> appIds = new ArrayList<String>();
        appIds.add(appId);

        // 定义"AppMessage"类型消息对象，设置消息内容模板、发送的目标App列表、是否支持离线发送、以及离线消息有效期(单位毫秒)
        AppMessage message = new AppMessage();
        message.setData(t);
        message.setAppIdList(appIds);
        message.setOffline(true);
        message.setOfflineExpireTime(1000 * 600);

        IPushResult ret = push.pushMessageToApp(message);
      /* Target target = new Target();
        target.setClientId("xxxxx");
        push.pushMessageToSingle(null,target);*/
        System.out.println(ret.getResponse().toString());
    }

    public void  push3(){
        IGtPush push = new IGtPush(url, appKey, masterSecret);
        APNTemplate transmissionTemplate  = new APNTemplate();
        transmissionTemplate.setAppId(appId);
        transmissionTemplate.setAppkey(appKey);
        com.gexin.rp.sdk.base.sms.SmsInfo smsInfo = new com.gexin.rp.sdk.base.sms.SmsInfo();
        //smsInfo.se
        //transmissionTemplate.setSmsInfo(smsInfo);
        //transmissionTemplate.setTransmissionContent("{title:\"标题\",content:\"内容\",payload:\"自定义数据\"}");
        //transmissionTemplate.setTransmissionType(2);
        APNPayload payload = new APNPayload();
        APNPayload.DictionaryAlertMsg alertMsg = new APNPayload.DictionaryAlertMsg();
        alertMsg.setBody("content");         //通知文本消息字符串

        //IOS8.2支持字段
        alertMsg.setTitle("title");
        //alertMsg.TitleLocKey = "";
        //alertMsg.addTitleLocArg("");

        payload.setAlertMsg(alertMsg);
        payload.setBadge(1);//应用icon上显示的数字
        //apnpayload.ContentAvailable = 1;//推送直接带有透传数据
        //apnpayload.Category = "";
        //apnpayload.Sound = "";//通知铃声文件名
       // apnpayload.addCustomMsg("", "");//增加自定义的数据

        transmissionTemplate.setAPNInfo(payload);
        SingleMessage message = new SingleMessage();
        message.setData(transmissionTemplate);
        message.setOffline(true);
        message.setPushNetWorkType(0);
        message.setOfflineExpireTime(600000);

        IPushResult result = push.pushAPNMessageToSingle(appId,"74E4ACC0E302136C6CCD7FD96A32CEA20F07FCC999BA3B7762367A722E52447A",message);
        System.out.println(result.getResponse().toString());
    }

    private static String config(String propertyName, String envName) {
        return config(propertyName, envName, "");
    }

    private static String config(String propertyName, String envName, String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        value = System.getenv(envName);
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

}
