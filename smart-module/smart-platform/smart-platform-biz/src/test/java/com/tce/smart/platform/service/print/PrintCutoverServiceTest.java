package com.tce.smart.platform.service.print;

import com.tce.smart.platform.core.entity.print.PrintPrinter;
import com.tce.smart.platform.core.mapper.PrintJobMapper;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 切换只公布通道状态；执行候选不得要求设备维护权限或暴露完整档案。 */
public class PrintCutoverServiceTest {
    @Test public void disabledDefaultsKeepLegacyButSelectedParkNeverFallsBackDuringRollback() {
        PrintFeatureProperties feature=new PrintFeatureProperties();PrintExecutionProperties execution=new PrintExecutionProperties();
        PrintCutoverProperties properties=new PrintCutoverProperties();PrintCutoverService service=new PrintCutoverService(feature,execution,properties);
        assertEquals("LEGACY",service.status("1").get("visitorMode"));
        assertEquals(false,service.status("1").get("newJobCreationEnabled"));
        properties.setTemplateParkIds(new HashSet<>(Collections.singletonList("1")));
        assertEquals("PAUSED",service.status("1").get("visitorMode"));
        feature.setEnabled(true);execution.setEnabled(true);
        assertEquals("TEMPLATE",service.status("1").get("visitorMode"));
        execution.setEnabled(false);
        assertEquals("PAUSED",service.status("1").get("visitorMode"));
        assertEquals(false,service.status("1").get("legacyVisitorAllowed"));
        assertEquals("LEGACY",service.status("2").get("visitorMode"));
    }
    @Test public void invalidParkIsRejectedInsteadOfPickingAnotherPark() {
        PrintCutoverService service=new PrintCutoverService(new PrintFeatureProperties(),new PrintExecutionProperties(),new PrintCutoverProperties());
        for(String park:Arrays.asList("", "1/2", "-1", "abc", "999999999999999999")) {
            try {service.status(park);fail("非法园区应拒绝");}catch(PrintApiException expected){assertEquals(422,expected.getStatus());}
        }
    }
    @Test public void printOperatorCanReadOnlyPickerFieldsWithoutDeviceAdministrationPermission() {
        PrintJobMapper db=mock(PrintJobMapper.class);PrintAccessPolicy access=mock(PrintAccessPolicy.class);
        when(access.resolvePark("1")).thenReturn("1");
        PrintPrinter printer=new PrintPrinter();printer.setPrinterProfileId(UUID.randomUUID().toString());printer.setStatus("ENABLED");printer.setActiveJobId(UUID.randomUUID().toString());
        printer.setConfigJson("{\"displayName\":\"访客台\",\"deviceIdentity\":\"secret-workstation\",\"deviceType\":\"LABEL_PRINTER\",\"allowedPrintModes\":[\"SINGLE\"],\"defaultPrintMode\":\"SINGLE\",\"driverVersion\":\"private-version\",\"capabilityEvidence\":[{\"verifiedBy\":\"private-person\"}]}");
        when(db.listPrinters(eq("1"),any(RowBounds.class))).thenReturn(Collections.singletonList(printer));when(db.countPrinters("1")).thenReturn(1L);
        Map<String,Object> result=new PrintPrinterService(db,access,null).options("1",1,20);
        verify(access).require("execute","1");verify(access,never()).require(eq("device"),anyString());
        String json=PrintJson.canonical(result);assertTrue(json.contains("访客台"));assertTrue(json.contains("busy"));
        assertFalse(json.contains("secret-workstation"));assertFalse(json.contains("private-person"));assertFalse(json.contains(printer.getActiveJobId()));
    }
}
