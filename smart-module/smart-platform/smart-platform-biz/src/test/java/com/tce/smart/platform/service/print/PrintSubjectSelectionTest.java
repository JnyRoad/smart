package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.controller.print.PrintSubjectController;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 旧入口只交接ID；批量摘要须重读权限过滤来源并剥离凭证和照片。 */
public class PrintSubjectSelectionTest {
    @Test public void selectionReloadsAuthoritativeSubjectsAndReturnsOnlySafeSummary() {
        PrintSubjectSource source=mock(PrintSubjectSource.class);
        ObjectNode subject=PrintJson.object().put("parkId","1").put("subjectId","21").put("subjectType","ADMITTANCE");
        subject.putObject("fields").put("visitorName","当前访客").put("cardNo","secret-card").put("visitorCredentialPayload","secret-code");
        subject.putArray("resources").addObject().put("bytesBase64","secret-photo");
        when(source.load("1","ADMITTANCE","21")).thenReturn(subject);
        ObjectNode request=PrintJson.object().put("parkId","1");request.putArray("subjects").addObject().put("subjectType","ADMITTANCE").put("subjectId","21");
        Object response=new PrintSubjectController(source).selection(request,new MockHttpServletRequest());
        JsonNode json=PrintJson.tree(response);assertEquals("当前访客",json.at("/data/records/0/displayName").asText());
        assertFalse(PrintJson.canonical(response).contains("secret"));verify(source).load("1","ADMITTANCE","21");
    }
    @Test public void untrustedFieldsDuplicateAndOversizedSelectionsAreRejectedBeforeLoading() {
        for(int test=0;test<4;test++) {
            PrintSubjectSource source=mock(PrintSubjectSource.class);ObjectNode request=PrintJson.object().put("parkId","1");
            ObjectNode item=PrintJson.object().put("subjectType","ADMITTANCE").put("subjectId","21");request.putArray("subjects").add(item);
            if(test==0)item.put("fields","spoof");if(test==1)request.withArray("subjects").add(item.deepCopy());
            if(test==2)for(int i=0;i<100;i++)request.withArray("subjects").add(item.deepCopy().put("subjectId",String.valueOf(i+22)));
            if(test==3)request.put("photo","spoof");
            try {new PrintSubjectController(source).selection(request,new MockHttpServletRequest());fail("非法选择必须拒绝");}
            catch(PrintApiException expected){assertEquals(422,expected.getStatus());}verifyZeroInteractions(source);
        }
    }
}
