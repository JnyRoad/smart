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

/** 仅提供合成Mapper资料，不启动应用或外部连接。 */
public class SupplierAccessTestFixture {
    static final String BADGE = "9223372036854775806";
    static final Instant NOW = Instant.parse("2026-09-05T04:00:00Z");
    SmtAdmittanceFellowMapper fellows;
    SmtAdmittanceApplyMapper applies;
    SmtAdmittanceFellow fellow;
    SmtAdmittanceApply apply;
    SupplierAccessProperties properties;
    SupplierAdmissionSource source;

    @Before public void prepare() {
        fellows = mock(SmtAdmittanceFellowMapper.class);
        applies = mock(SmtAdmittanceApplyMapper.class);
        properties = properties();
        fellow = new SmtAdmittanceFellow();
        fellow.setId(Long.valueOf(BADGE)); fellow.setVisitorId(8000001L);
        fellow.setFellowName("合成访客甲"); fellow.setCertType(0); fellow.setCertNo("synthetic-x"); fellow.setIsMain(1);
        apply = new SmtAdmittanceApply(); apply.setId(8000001L); apply.setParkId(1); apply.setCompany("合成单位");
        apply.setVisitorName("合成访客甲"); apply.setCertNo("SYNTHETIC-X"); apply.setVisitorPhone("synthetic-phone");
        apply.setStatus(0); apply.setApplyType(1); apply.setAreaType("0,11");
        apply.setStartTime(LocalDateTime.parse("2026-09-05T11:00:00")); apply.setEndTime(LocalDateTime.parse("2026-09-05T13:00:00"));
        when(fellows.selectById(Long.valueOf(BADGE))).thenReturn(fellow);
        when(applies.selectById(8000001L)).thenReturn(apply);
        source = new SupplierAdmissionSource(fellows, applies, mock(ImageService.class), properties);
    }

    static SupplierAccessProperties properties() {
        SupplierAccessProperties p = new SupplierAccessProperties(); p.setEnabled(true);
        SupplierAccessProperties.Post post = new SupplierAccessProperties.Post();
        post.setId("gate"); post.setName("合成岗位"); post.setParkId(1); post.setParkName("合成园区");
        post.setAreaId("area"); post.setAreaName("合成区域"); post.setAdmittanceAreaTypeCode("0");
        p.setPosts(Collections.singletonList(post)); p.validate(); return p;
    }

}
