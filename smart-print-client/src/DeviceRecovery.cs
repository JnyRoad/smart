using System.Text.Json.Nodes;
namespace Smart.Printing;

// 清空操作必须绑定原任务日志及冻结档案；参数只能确认目标，不能替换被检查的队列。
public sealed class DeviceRecovery(PrintApiClient api,PrintCommandJournal journal,Action<LocalPrinterProfile> requireEmptyQueue)
{
    public async Task ClearAsync(LocalPrinterProfile profile,string jobId,string checkId,bool noCard,CancellationToken token)
    {
        if(!noCard || !Guid.TryParseExact(jobId,"D",out _) || !Guid.TryParseExact(checkId,"D",out _))throw new InvalidDataException("需要现场无卡确认及平台核对记录");
        var claim=journal.LoadClaim(profile.PrinterProfileId)??throw new InvalidDataException("缺少原打印机领取记录");
        if(claim.JobId!=jobId || claim.DeviceIdentity!=profile.DeviceIdentity || claim.PrinterSnapshotHash!=profile.PrinterSnapshotHash)
            throw new InvalidDataException("目标任务不属于该打印机原始档案");
        requireEmptyQueue(profile);
        var operation=journal.BeginOperation(profile.PrinterProfileId,"clear",$"/api/print-client/v1/jobs/{jobId}/device-cleared",new JsonObject {
            ["deviceIdentity"]=profile.DeviceIdentity,["physicalState"]="NO_CARD_IN_DEVICE",["operatorCheckId"]=checkId,["reason"]="工作站现场确认无卡且原驱动队列为空"});
        if(operation.Body["operatorCheckId"]?.GetValue<string>()!=checkId)
            throw new InvalidDataException("存在同一任务的未完成清空请求，请使用原核对记录标识重试");
        var result=await api.JsonAsync(HttpMethod.Post,operation.Path,operation.Body,token,operation.Key);
        if(result?["cleared"]?.GetValue<bool>()==false) {
            // 明确拒绝释放也是已结束的请求；保留领取记录，让后续新检查使用新请求键。
            journal.CompleteOperation(profile.PrinterProfileId,"clear",operation.Key);
            throw new InvalidDataException("平台尚未确认释放原任务占用");
        }
        if(result?["cleared"]?.GetValue<bool>()!=true)throw new InvalidDataException("平台释放响应缺少明确结果");
        journal.ReleaseClaim(claim);
    }
}
