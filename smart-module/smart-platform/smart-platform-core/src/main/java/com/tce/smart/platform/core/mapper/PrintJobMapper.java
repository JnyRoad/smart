package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.print.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import java.util.List;
/** 所有设备竞争通过数据库事务和行锁仲裁。 */
public interface PrintJobMapper {
    int insertPrinter(PrintPrinter value);
    PrintPrinter findPrinter(@Param("id") String id);
    PrintPrinter lockPrinter(@Param("id") String id);
    int updatePrinter(PrintPrinter value);
    int insertJob(PrintJob value);
    PrintJob findJob(@Param("id") String id);
    PrintJob lockJob(@Param("id") String id);
    int updateJob(PrintJob value);
    int insertJobAttempt(PrintJobAttempt value);
    PrintJobAttempt findJobAttempt(@Param("id") String id);
    PrintJobAttempt lockJobAttempt(@Param("id") String id);
    int updateJobAttempt(PrintJobAttempt value);
    int insertJobEvent(PrintJobEvent value);
    PrintJobEvent findJobEvent(@Param("id") String id);
    int insertJobArtifact(PrintJobArtifact value);
    PrintJobArtifact findJobArtifact(@Param("id") String id);
    int insertJobPreview(PrintJobPreview value);
    PrintJobPreview findJobPreview(@Param("id") String id);
    List<PrintJob> queryJobs(java.util.Map<String,Object> query, RowBounds bounds);
    long countQueryJobs(java.util.Map<String,Object> query);
    List<String> jobAudit(@Param("id") String id, RowBounds bounds);
    long countPrinters(@Param("park") String park);
    long countJobs(@Param("park") String park,@Param("owner") String owner,@Param("printer") String printer,@Param("status") String status);
    List<PrintPrinter> listPrinters(@Param("park") String park, RowBounds bounds);
    List<PrintJob> listJobs(@Param("park") String park,@Param("owner") String owner,@Param("printer") String printer,@Param("status") String status, RowBounds bounds);
    List<PrintJob> queuedJobs(RowBounds bounds);
    PrintJob findClaim(@Param("id") String id);
    List<PrintJobAttempt> attempts(@Param("id") String jobId);
    List<PrintJobEvent> events(@Param("id") String jobId, RowBounds bounds);
}
