namespace Smart.Printing;

// 仅把已经验证的当前命令交给适配器；持久化提交标记之后的异常统一按结果不明处理。
public sealed class PrintCommandProcessor(PrintCommandJournal journal, IPrintAdapter adapter, PrinterBinding binding)
{
    private readonly SemaphoreSlim gate = new(1,1);
    public async Task<PrintClientEvent> ExecuteAsync(PrintCommand command, byte[] pdf, CancellationToken token)
    {
        await gate.WaitAsync(token);
        try
        {
            Validate(command, pdf);
            var entry = journal.RecordIntent(command);
            if(entry.State == "RETIRED") throw new InvalidDataException("已终止命令不得再次执行");
            if(entry.Result is not null) return entry.Result;
            if(entry.State == "SUBMISSION_STARTED") return journal.RecordResult(command.CommandId, "OUTPUT_UNKNOWN");
            token.ThrowIfCancellationRequested();
            journal.MarkSubmissionStarted(command.CommandId);
            SubmissionResult result;
            try { result = await adapter.SubmitAsync(command, pdf, token); }
            catch(PrintNotSubmittedException) { return journal.RecordResult(command.CommandId, "DRIVER_REJECTED"); }
            catch(Exception) { return journal.RecordResult(command.CommandId, "OUTPUT_UNKNOWN"); }
            return journal.RecordResult(command.CommandId, result.Accepted ? "DEVICE_ACCEPTED" : "DRIVER_REJECTED", result.DriverJobKey);
        }
        finally { gate.Release(); }
    }
    private void Validate(PrintCommand command, byte[] pdf)
    {
        if(!Guid.TryParseExact(command.JobId,"D",out _) || !Guid.TryParseExact(command.AttemptId,"D",out _) || !Guid.TryParseExact(command.CommandId,"D",out _)) throw new InvalidDataException("任务命令标识无效");
        if(command.PrinterProfileId != binding.PrinterProfileId || command.DeviceIdentity != binding.DeviceIdentity
            || command.PrinterSnapshotHash != binding.PrinterSnapshotHash || !binding.AllowedPrintModes.Contains(command.PrintMode)) throw new InvalidDataException("设备身份、配置或模式不匹配");
        if(!Hashing.IsHash(command.ArtifactHash) || !Hashing.IsHash(command.PrinterSnapshotHash) || !Hashing.IsHash(command.TemplateSnapshotHash)
            || pdf.Length > 32 * 1024 * 1024 || !pdf.AsSpan().StartsWith("%PDF-"u8) || Hashing.Sha256(pdf) != command.ArtifactHash) throw new InvalidDataException("打印制品校验失败");
        if(!double.IsFinite(command.PageWidthMm) || !double.IsFinite(command.PageHeightMm) || command.PageWidthMm <= 0 || command.PageHeightMm <= 0 || command.PageWidthMm > 2000 || command.PageHeightMm > 2000) throw new InvalidDataException("纸张尺寸无效");
        bool allowed = command.PrintMode switch {
            "SINGLE" => command.Face == "FRONT" && command.PageCount == 1,
            "MANUAL_DUPLEX" => (command.Face is "FRONT" or "BACK") && command.PageCount == 1,
            "AUTO_DUPLEX" => command.Face == "BOTH" && command.PageCount == 2,
            _ => false
        };
        if(!allowed) throw new InvalidDataException("页面与翻面模式不匹配");
    }
}
