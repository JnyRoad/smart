package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.SupplierQualificationSnapshot;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 入厂主键、资格和隐私边界；Mapper替身不连接业务库。 */
public class SupplierAdmissionSourceTest extends SupplierAccessTestFixture {
    @Test public void preservesLongIdAndShanghaiTimeWithoutExposingDocumentAsPersonId() {
        SupplierQualificationSnapshot q = load();
        assertEquals(BADGE, q.getBadgeId()); assertEquals("8000001", q.getAdmissionId());
        assertEquals("2026-09-05T03:00:00Z", q.getValidFrom().toString());
        assertEquals("synthetic-phone", q.getPersonPhone());
        assertTrue(q.getCompanyId().startsWith("admittance-company:"));
        assertFalse(q.getPersonId().contains("synthetic"));
    }

    @Test public void historicalH5NullTypeAndExplicitIdCardShareStableIdentity() {
        fellow.setCertNo("990000200001010012"); fellow.setCertType(null);
        String legacy = load().getPersonId();
        fellow.setCertType(0); assertEquals(legacy, load().getPersonId());
        fellow.setCertType(1); assertNotEquals(legacy, load().getPersonId());
    }

    @Test public void unknownTypesAndUnrecognizedHistoricalDocumentsFailClosed() {
        fellow.setCertType(999); denied(403);
        fellow.setCertType(6); denied(403);
        fellow.setCertType(-1); denied(403);
        fellow.setCertType(null);
        for (String document : new String[]{"synthetic-passport", "990000200001010011", "123", ""}) {
            fellow.setCertNo(document); denied(403);
        }
    }

    @Test public void shortDecimalIdsAreStillOnlyFellowPrimaryKeys() {
        for (long id : new long[]{1L, 123456L}) {
            fellow.setId(id); when(fellows.selectById(id)).thenReturn(fellow);
            assertEquals(Long.toString(id), source.load(Long.toString(id), properties.getPosts().get(0), NOW).getBadgeId());
        }
    }

    @Test public void sameCertificateAcrossPersonRowsSharesPresenceButNotBadge() {
        String first = load().getPersonId(); fellow.setId(9223372036854775805L); fellow.setCertNo(" SYNTHETIC-X ");
        when(fellows.selectById(9223372036854775805L)).thenReturn(fellow);
        SupplierQualificationSnapshot other = source.load("9223372036854775805", properties.getPosts().get(0), NOW);
        assertEquals(first, other.getPersonId()); assertEquals("9223372036854775805", other.getBadgeId());
    }

    @Test public void neverUsesApplicantPhoneForCompanionOrMismatchedMain() {
        fellow.setIsMain(0); assertEquals("", load().getPersonPhone());
        fellow.setIsMain(1); fellow.setCertNo("SYNTHETIC-Y"); assertEquals("", load().getPersonPhone());
    }

    @Test public void rejectsReservationUrlsControlsLeadingZerosAndOverflowBeforeQuery() {
        for (String code : new String[]{"", "0", "01234567", "https://example/1234567", "1234567\n", "9223372036854775808"}) {
            try { source.load(code, properties.getPosts().get(0), NOW); fail(code); }
            catch (SupplierAccessHttpException expected) { assertEquals(400, expected.getStatus()); }
        }
        verifyZeroInteractions(fellows, applies);
    }

    @Test public void rejectsMissingOrMismatchedRows() {
        fellow.setId(1234567L); denied(404); fellow.setId(Long.valueOf(BADGE));
        apply.setId(8000002L); denied(404); apply.setId(8000001L);
        fellow.setVisitorId(null); denied(404);
    }

    @Test public void rejectsVehicleAppointmentsAndUnknownApplicationTypes() {
        apply.setApplyType(2); denied(403); apply.setApplyType(null); denied(403);
    }

    @Test public void rejectsUnapprovedWrongParkExpiredAndAbsentCompany() {
        apply.setStatus(7); denied(403); apply.setStatus(3); assertNotNull(load());
        apply.setParkId(2); denied(403); apply.setParkId(1);
        apply.setEndTime(LocalDateTime.parse("2026-09-05T12:00:00")); denied(403);
        apply.setEndTime(LocalDateTime.parse("2026-09-05T13:00:00")); apply.setCompany(" "); denied(403);
    }

    @Test public void matchesWholeAreaNumbersIncludingZeroAndRejectsMalformedSets() {
        assertTrue(load().getAuthorizedAreaIds().contains("area"));
        properties.getPosts().get(0).setAdmittanceAreaTypeCode("1"); denied(403);
        apply.setAreaType("1,11"); assertNotNull(load());
        for (String areas : new String[]{"1,text", "1,", "01", "1,,11"}) { apply.setAreaType(areas); denied(403); }
    }

    @Test public void configurationFailsClosedOnMissingDuplicateOrConflictingMappings() {
        SupplierAccessProperties p = new SupplierAccessProperties(); assertFalse(p.isEnabled()); p.validate();
        p.setEnabled(true); invalidConfig(p);
        p = properties(); p.setPosts(java.util.Arrays.asList(p.getPosts().get(0), p.getPosts().get(0))); invalidConfig(p);
        p = properties(); p.getPosts().get(0).setAdmittanceAreaTypeCode(null); invalidConfig(p);
        p = properties(); p.setBusinessTimezone("UTC"); invalidConfig(p);
    }
    private void invalidConfig(SupplierAccessProperties p) { try { p.validate(); fail(); } catch (IllegalArgumentException expected) { } }
    private SupplierQualificationSnapshot load() { return source.load(BADGE, properties.getPosts().get(0), NOW); }
    private void denied(int status) { try { load(); fail(); } catch (SupplierAccessHttpException expected) { assertEquals(status, expected.getStatus()); } }
}
