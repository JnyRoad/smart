package com.tce.smart.push.service.impl;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.APNTemplate;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.google.gson.Gson;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.push.config.CommonProperties;
import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;
import com.tce.smart.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Slf4j
@Service
@Configuration
public class PushServiceImpl implements PushService {
    private final Gson gson = new Gson();

    private IGtPush push;
    @Autowired
    CommonProperties commonProperties;
    @Override
    public boolean notice(NoticeMessageDTO noticeMessageDTO) {
        Validate.isTrue( null!=noticeMessageDTO && StringUtils.isNotEmpty(noticeMessageDTO.getClientId()) , "推送通知发送数据有误");
        /*LinkTemplate linkTemplate = generateLinkTemplate(noticeMessageDTO);*/
        TransmissionTemplate transmissionTemplate = generateTransmissionTemplate(noticeMessageDTO);
        SingleMessage message = new SingleMessage();
        message.setData(transmissionTemplate);
        message.setOffline(true);
        message.setOfflineExpireTime(commonProperties.getOfflineExpireTime());
        Target target = new Target();
        target.setClientId(noticeMessageDTO.getClientId());
        target.setAppId(commonProperties.getAppId());
        IPushResult result = getPushClient().pushMessageToSingle(message, target);
        if (result.getResponse().get("result").equals("ok")) {
            log.info("个推推送消息:{} 成功", noticeMessageDTO.getTitle());
            return true;
        }else {
            log.warn("个推推送消息:{} 失败,错误消息:{}",noticeMessageDTO.getTitle(),result.getResponse().toString());
        }
        return false;
    }

    @Override
    public boolean transmission(ApnsMessageDTO apnsMessageDTO) {
        Validate.isTrue( null!=apnsMessageDTO && StringUtils.isNotEmpty(apnsMessageDTO.getDeviceToken()) , "透传发送数据有误");
        APNTemplate template = generateAPNTemplate(apnsMessageDTO);
        SingleMessage message = new SingleMessage();
        message.setData(template);
        message.setOffline(true);
        message.setOfflineExpireTime(commonProperties.getOfflineExpireTime());
        IPushResult result = getPushClient().pushAPNMessageToSingle(commonProperties.getAppId(),apnsMessageDTO.getDeviceToken(),message);
        if(result.getResponse().get("result").equals("ok")){
            log.info("APNS推送消息:{} 成功",apnsMessageDTO.getTitle());
            return true;
        }else {
            log.warn("APNS推送消息:{} 失败,错误消息:{}",apnsMessageDTO.getTitle(),result.getResponse().toString());
        }

        return false;
    }





    @Override
    public boolean pushAll(PushMessageDTO pushMessageDTO) {
        Validate.isTrue( null!=pushMessageDTO , "批量推送数据有误");
        List<String> appIds = new ArrayList<String>();
        appIds.add(commonProperties.getAppId());
        //批量发送通知
        LinkTemplate linkTemplate = generateLinkTemplate(pushMessageDTO);
        AppMessage message = new AppMessage();
        message.setData(linkTemplate);
        message.setAppIdList(appIds);
        message.setOffline(true);
        message.setOfflineExpireTime(commonProperties.getOfflineExpireTime());
        push.pushMessageToApp(message);

        //批量发送透传
        TransmissionTemplate transmissionTemplate = generateTransmissionTemplate(pushMessageDTO);
        AppMessage transmissionMessage = new AppMessage();
        transmissionMessage.setData(transmissionTemplate);
        transmissionMessage.setAppIdList(appIds);
        transmissionMessage.setOffline(true);
        transmissionMessage.setOfflineExpireTime(commonProperties.getOfflineExpireTime());

        push.pushMessageToApp(transmissionMessage);

        return false;
    }

    /**
     * 构建TransmissionTemplate
     * @param pushMessageDTO
     * @return
     */
    private TransmissionTemplate generateTransmissionTemplate(PushMessageDTO pushMessageDTO) {
        TransmissionTemplate t = new TransmissionTemplate();
        t.setAppId(commonProperties.getAppId());
        t.setAppkey(commonProperties.getAppKey());
        t.setTransmissionContent(assembleTransmissionContent(pushMessageDTO));
        t.setTransmissionType(1);
        return t;
    }

    /**
     * 构建LinkTemplate
     * @param pushMessageDTO
     * @return
     */
    private LinkTemplate generateLinkTemplate(PushMessageDTO pushMessageDTO) {
        LinkTemplate template = new LinkTemplate();
        template.setAppId(commonProperties.getAppId());
        template.setAppkey(commonProperties.getAppKey());
        template.setTitle(pushMessageDTO.getTitle());
        template.setText(pushMessageDTO.getContent());

        template.setUrl(pushMessageDTO.getUrl());
        //Payload payload = new Payload();
        //template.setAPNInfo(payload);
        template.setUrl(pushMessageDTO.getUrl());
        return template;
    }

    private APNTemplate generateAPNTemplate(PushMessageDTO pushMessageDTO) {
        APNTemplate t = new APNTemplate();
        t.setAppId(commonProperties.getAppId());
        t.setAppkey(commonProperties.getAppKey());
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

        t.setAPNInfo(payload);
        return t;
    }

    /**
     * 组装apns透传消息
     * @param pushMessageDTO
     * @return
     */
    private String assembleTransmissionContent(PushMessageDTO pushMessageDTO){
        Map<String,String> contentMap = new HashMap<>();
        contentMap.put("title",pushMessageDTO.getTitle());
        contentMap.put("content",pushMessageDTO.getContent());
        contentMap.put("payload",pushMessageDTO.getPayload());
        return gson.toJson(contentMap);
    }



    private IGtPush getPushClient(){
        if(null==push){
            push = new IGtPush(commonProperties.getUrl(), commonProperties.getAppKey(), commonProperties.getMasterSecret());
        }
        return push;
    }
}
