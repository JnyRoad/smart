package com.tce.smart.platform.core.dto.authtransport;

import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import lombok.*;

/** 旧出口的精确命令对照；调用方的园区仅是对照值，不能决定保护范围。 */
public final class AuthDirectTakeover {
    private AuthDirectTakeover() { }
    public enum Outcome { LEGACY_ALLOWED, OWNED_BY_TRANSPORT, VERIFYING }
    @Value @Builder public static class Decision {
        Outcome outcome; String reason; SmtAuthTransportPhase phase;
        public boolean legacyAllowed() { return outcome == Outcome.LEGACY_ALLOWED; }
    }
    @Data public static class RouteCapability { Integer parkId; String instanceId; Integer directTakeoverVersion; }
    @Value @Builder(toBuilder=true) public static class LegacyIdentity {
        Integer taskId; Integer action; String serialNo; String deviceId; String cardNo;
        Integer deviceType; Integer serviceType; Integer cardType; String general; String imageId;
        Long startTime; Long overTime;
        Integer wirePark; String wireEnvelopeDevice; String wireDevice; String wireCard; String wireSerial; Integer wireTask;
        String wireOperation; String wireGeneral; Integer wireCardType; Long wireStart; Long wireEnd;
        public static LegacyIdentity of(SmtDeviceTask t) {
            if(t==null)return null;
            return builder().taskId(t.getId()).action(t.getAction()).serialNo(t.getSerialNo())
                .deviceId(t.getDeviceCode()).cardNo(t.getCardNo()).deviceType(t.getDeviceType())
                .serviceType(t.getServiceType()).cardType(t.getCardType()).general(t.getGeneral()).imageId(t.getImageId())
                .startTime(t.getStartTime()).overTime(t.getOverTime()).build();
        }
    }
}
