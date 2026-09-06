using System.Text.Json;
using System.Text.Json.Nodes;
namespace Smart.Printing;

public sealed record ClaimState(string PrinterProfileId,string ClaimId,string JobId,string LeaseExpiresAt,string ClientInstanceId="",string DeviceIdentity="",string PrinterSnapshotHash="",bool Released=false);
public sealed class TaskPoller(PrintApiClient api,PrintCommandJournal journal,IReadOnlyList<LocalPrinterProfile> profiles,Func<LocalPrinterProfile,IPrintAdapter> adapterFactory)
{
    private readonly string instanceId=Guid.NewGuid().ToString();
    private readonly HashSet<string> claimedThisInstance=[];
    private readonly SemaphoreSlim coordination=new(1,1);
    public async Task StepAsync(CancellationToken token)
    {
        await SendPendingEvents(token);
        foreach(var profile in profiles)
        {
            JsonObject? remote;await coordination.WaitAsync(token);
            try {
            var state=journal.LoadClaim(profile.PrinterProfileId);
            if(state?.Released==true){journal.ReleaseClaim(state);state=null;claimedThisInstance.Remove(profile.PrinterProfileId);}
            // 重领之前先结束上次未确认的续租；不能让新的租约覆盖持久请求。
            if(state!=null && !claimedThisInstance.Contains(profile.PrinterProfileId) && !journal.HasPendingOperation(profile.PrinterProfileId,"claim"))await RenewClaim(profile,state,token);
            PendingOperation? pending=null;var claimInstance=state?.ClientInstanceId??instanceId;
            if(state!=null && claimedThisInstance.Contains(profile.PrinterProfileId))
                remote=await api.JsonAsync(HttpMethod.Get,$"/api/print-client/v1/claims/{state.ClaimId}/current",null,token);
            else {
                var body=new JsonObject { ["deviceIdentity"]=profile.DeviceIdentity,["printerProfileId"]=profile.PrinterProfileId,["clientInstanceId"]=instanceId,
                    ["clientVersion"]="0.1.0",["supportedPrintModes"]=JsonSerializer.SerializeToNode(profile.AllowedPrintModes,Hashing.Json),["capabilitySnapshotHash"]=profile.PrinterSnapshotHash };
                if(state!=null)body["resumeJobId"]=state.JobId;
                pending=journal.BeginOperation(profile.PrinterProfileId,"claim","/api/print-client/v1/claim",body);
                claimInstance=Value(pending.Body,"clientInstanceId");
                remote=await api.JsonAsync(HttpMethod.Post,pending.Path,pending.Body,token,pending.Key);
            }
            if(remote==null || string.IsNullOrEmpty(Value(remote,"jobId"))) {journal.ClearClaim(profile.PrinterProfileId);claimedThisInstance.Remove(profile.PrinterProfileId);if(pending!=null)journal.CompleteOperation(profile.PrinterProfileId,"claim",pending.Key);continue;}
            if(Value(remote,"printerProfileId")!=profile.PrinterProfileId || Value(remote,"deviceIdentity")!=profile.DeviceIdentity)
                throw new InvalidDataException("领取结果不属于已登记设备");
            var claimId=Value(remote,"claimId");var jobId=Value(remote,"jobId");
            if(!Guid.TryParseExact(claimId,"D",out _) || !Guid.TryParseExact(jobId,"D",out _))throw new InvalidDataException("领取结果标识无效");
            // GET /current 只能返回本实例已领取的同一任务；切换任务必须先完成显式设备清空。
            if(state!=null && claimedThisInstance.Contains(profile.PrinterProfileId) && (claimId!=state.ClaimId || jobId!=state.JobId))
                throw new InvalidDataException("当前领取结果不属于原任务");
            if(Value(remote,"printerSnapshotHash")!=profile.PrinterSnapshotHash)throw new InvalidDataException("领取结果与本机冻结档案不一致");
            journal.SaveClaim(new ClaimState(profile.PrinterProfileId,claimId,jobId,Value(remote,"leaseExpiresAt"),claimInstance,profile.DeviceIdentity,Value(remote,"printerSnapshotHash")));claimedThisInstance.Add(profile.PrinterProfileId);
            if(pending!=null)journal.CompleteOperation(profile.PrinterProfileId,"claim",pending.Key);
            } finally {coordination.Release();}
            var currentJobId=Value(remote!,"jobId");
            var action=remote["action"] as JsonObject??throw new InvalidDataException("领取结果缺少动作");var kind=Value(action,"type");
            if(new[] {"WAIT_FRONT_CHECK","WAIT_FLIP","WAIT_OUTPUT_CHECK","RESULT_UNKNOWN","NONE"}.Contains(kind))continue;
            if(kind is not ("PRINT_FRONT" or "PRINT_BACK" or "PRINT_BOTH"))throw new InvalidDataException("服务端返回未允许动作");
            var artifact=action["artifact"] as JsonObject??throw new InvalidDataException("当前命令缺少制品");
            var command=new PrintCommand(currentJobId,Value(action,"attemptId"),Value(action,"commandId"),profile.PrinterProfileId,profile.DeviceIdentity,
                Value(remote,"printMode"),Value(action,"face"),Value(artifact,"sha256"),Value(remote,"templateSnapshotHash"),Value(remote,"printerSnapshotHash"),
                artifact["pageWidthMm"]?.GetValue<double>()??0,artifact["pageHeightMm"]?.GetValue<double>()??0,artifact["pageCount"]?.GetValue<int>()??0);
            if(command.Face!=(kind=="PRINT_BOTH"?"BOTH":kind=="PRINT_BACK"?"BACK":"FRONT"))throw new InvalidDataException("动作与业务面不一致");
            var bytes=await api.ArtifactAsync(command,Value(artifact,"downloadPath"),token);
            var processor=new PrintCommandProcessor(journal,adapterFactory(profile),new PrinterBinding(profile.PrinterProfileId,profile.DeviceIdentity,profile.PrinterSnapshotHash,profile.AllowedPrintModes));
            await processor.ExecuteAsync(command,bytes,token);
            await SendPendingEvents(token);
        }
    }
    public async Task RenewAsync(CancellationToken token)
    {
        await coordination.WaitAsync(token);
        try {foreach(var profile in profiles) {
            var claim=journal.LoadClaim(profile.PrinterProfileId);
            if(claim!=null && claimedThisInstance.Contains(profile.PrinterProfileId))await RenewClaim(profile,claim,token);
        }} finally {coordination.Release();}
    }
    private async Task RenewClaim(LocalPrinterProfile profile,ClaimState claim,CancellationToken token)
    {
        if(!Guid.TryParseExact(claim.ClientInstanceId,"D",out _))throw new InvalidDataException("领取日志缺少原客户端身份");
        var pending=journal.BeginOperation(profile.PrinterProfileId,"renew",$"/api/print-client/v1/claims/{claim.ClaimId}/renew",new JsonObject {
            ["deviceIdentity"]=profile.DeviceIdentity,["clientInstanceId"]=claim.ClientInstanceId,["leaseExpiresAt"]=claim.LeaseExpiresAt});
        var data=await api.JsonAsync(HttpMethod.Post,pending.Path,pending.Body,token,pending.Key);
        if(data==null || Value(data,"claimId")!=claim.ClaimId || Value(data,"jobId")!=claim.JobId || Value(data,"printerProfileId")!=profile.PrinterProfileId || Value(data,"deviceIdentity")!=profile.DeviceIdentity)
            throw new InvalidDataException("续租结果不属于原任务");
        journal.SaveClaim(claim with {LeaseExpiresAt=Value(data,"leaseExpiresAt")});
        journal.CompleteOperation(profile.PrinterProfileId,"renew",pending.Key);
    }
    private async Task SendPendingEvents(CancellationToken token)
    {
        foreach(var pending in journal.PendingEvents())
        {
            var body=JsonSerializer.SerializeToNode(pending,Hashing.Json)!.AsObject();body.Remove("jobId");
            var result=await api.JsonAsync(HttpMethod.Post,$"/api/print-client/v1/jobs/{pending.JobId}/events",body,token,pending.EventId);
            if(result?["eventAccepted"]?.GetValue<bool>()!=true)throw new InvalidDataException("服务端未确认打印回执");
            journal.Acknowledge(pending.CommandId,pending.EventId);
        }
    }
    private static string Value(JsonObject value,string field)=>value[field]?.GetValue<string>()??"";
}
