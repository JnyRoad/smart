using System.Text.Json;
using System.Text.Json.Nodes;
namespace Smart.Printing;

public sealed record JournalEntry(PrintCommand Command, string Signature, string State, PrintClientEvent? Result)
{
    public List<PrintClientEvent> Events {get;init;}=[];
    public List<string> AcknowledgedEventIds {get;init;}=[];
}
public sealed record PendingOperation(string Key,string Path,JsonObject Body);

// 独占工作站日志目录；每次提交前强制刷新磁盘，原子替换记录，损坏日志绝不当作新命令。
public sealed class PrintCommandJournal : IDisposable
{
    private readonly string directory;
    private readonly FileStream processLock;
    private readonly object gate = new();
    public PrintCommandJournal(string directory)
    {
        this.directory = PrivateDirectory.Ensure(directory);
        Directory.CreateDirectory(this.directory);
        if ((File.GetAttributes(this.directory) & FileAttributes.ReparsePoint) != 0) throw new IOException("打印日志目录不能是符号链接");
        if (!OperatingSystem.IsWindows()) File.SetUnixFileMode(this.directory, UnixFileMode.UserRead | UnixFileMode.UserWrite | UnixFileMode.UserExecute);
        processLock = new FileStream(Path.Combine(this.directory, "client.lock"), FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None);
    }
    private string RecordPath(string commandId)
    {
        if (!Guid.TryParseExact(commandId, "D", out _)) throw new InvalidDataException("命令标识格式无效");
        return Path.Combine(directory, commandId + ".json");
    }
    public JournalEntry? Find(string commandId)
    {
        lock(gate)
        {
            var path = RecordPath(commandId);
            if (!File.Exists(path)) return null;
            if ((File.GetAttributes(path) & FileAttributes.ReparsePoint) != 0) throw new InvalidDataException("日志记录不能是符号链接");
            try
            {
                var info = new FileInfo(path);
                if(info.Length > 65536) throw new InvalidDataException("日志记录大小异常");
                var entry = JsonSerializer.Deserialize<JournalEntry>(File.ReadAllBytes(path), Hashing.Json);
                if (entry is null || entry.Command.CommandId != commandId || entry.Signature != Hashing.Signature(entry.Command)
                    || !new[] { "INTENT_RECORDED", "SUBMISSION_STARTED", "RESULT_RECORDED", "RETIRED" }.Contains(entry.State))
                    throw new InvalidDataException("打印日志校验失败，需要人工核对");
                if(entry.Events.Any(e=>e.CommandId!=commandId || e.JobId!=entry.Command.JobId || e.AttemptId!=entry.Command.AttemptId || e.ArtifactHash!=entry.Command.ArtifactHash || e.ClientSequence<=0 || !Guid.TryParseExact(e.EventId,"D",out _))
                    || entry.Events.Select(e=>e.EventId).Distinct().Count()!=entry.Events.Count || entry.AcknowledgedEventIds.Any(id=>!entry.Events.Any(e=>e.EventId==id)))
                    throw new InvalidDataException("事件日志校验失败");
                if(entry.State == "RESULT_RECORDED" && (entry.Result is null || !entry.Events.Any(e=>e.EventId==entry.Result.EventId))) throw new InvalidDataException("打印回执缺失");
                return entry;
            }
            catch(JsonException error) { throw new InvalidDataException("打印日志损坏，需要人工核对", error); }
        }
    }
    public JournalEntry RecordIntent(PrintCommand command)
    {
        lock(gate)
        {
            var existing = Find(command.CommandId);
            if(existing is not null)
            {
                if(existing.Signature != Hashing.Signature(command)) throw new InvalidDataException("同一命令内容发生改变");
                return existing;
            }
            var entry = new JournalEntry(command, Hashing.Signature(command), "INTENT_RECORDED", null);
            Save(entry); return entry;
        }
    }
    public void MarkSubmissionStarted(string commandId)
    {
        lock(gate)
        {
            var entry = Find(commandId) ?? throw new InvalidDataException("缺少提交意图");
            if(entry.State != "INTENT_RECORDED") throw new InvalidDataException("命令不能重复提交");
            Save(entry with { State = "SUBMISSION_STARTED" });
        }
    }
    public PrintClientEvent RecordResult(string commandId, string eventType, string? driverJobKey = null)
    {
        lock(gate)
        {
            var entry = Find(commandId) ?? throw new InvalidDataException("缺少提交意图");
            if(entry.Result is not null) return entry.Result;
            if(entry.State is not ("SUBMISSION_STARTED" or "RETIRED")) throw new InvalidDataException("命令状态不允许记录回执");
            if(!new[] { "DEVICE_ACCEPTED", "DRIVER_REJECTED", "OUTPUT_UNKNOWN", "COMMAND_RETIRED" }.Contains(eventType)) throw new InvalidDataException("无效打印事件");
            if(driverJobKey?.Length > 128) throw new InvalidDataException("驱动标识过长");
            var payload = new Dictionary<string,object>();
            if(eventType == "DEVICE_ACCEPTED") payload["queueAccepted"] = true;
            if(eventType == "DRIVER_REJECTED") payload["submissionAccepted"] = false;
            var result = new PrintClientEvent(Guid.NewGuid().ToString(), entry.Command.JobId, entry.Command.AttemptId, commandId,
                eventType, DateTimeOffset.UtcNow, entry.Command.ArtifactHash, driverJobKey, payload, NextSequence());
            Save(entry with { State = "RESULT_RECORDED", Result = result, Events=[..entry.Events,result] }); return result;
        }
    }
    public IReadOnlyList<PrintClientEvent> PendingEvents()
    {
        lock(gate) return Entries().SelectMany(entry=>entry.Events.Where(e=>!entry.AcknowledgedEventIds.Contains(e.EventId))).OrderBy(e=>e.ClientSequence).ToArray();
    }
    public void Acknowledge(string commandId,string eventId)
    {
        lock(gate) {
            var entry=Find(commandId)??throw new InvalidDataException("未找到回执命令");
            if(!entry.Events.Any(e=>e.EventId==eventId)) throw new InvalidDataException("回执事件不属于该命令");
            if(!entry.AcknowledgedEventIds.Contains(eventId)) Save(entry with {AcknowledgedEventIds=[..entry.AcknowledgedEventIds,eventId]});
        }
    }
    public PrintClientEvent Retire(string commandId,bool queueCleared,bool sameCardFaceVerified)
    {
        lock(gate) {
            if(!queueCleared || !sameCardFaceVerified) throw new InvalidDataException("终止命令前必须确认同一卡面并清空原驱动队列");
            var entry=Find(commandId)??throw new InvalidDataException("缺少原命令记录");
            if(entry.Command.PrintMode!="MANUAL_DUPLEX") throw new InvalidDataException("仅手动翻面允许同卡核对后的终止续打");
            var existing=entry.Events.SingleOrDefault(e=>e.EventType=="COMMAND_RETIRED"); if(existing!=null) return existing;
            var result=new PrintClientEvent(Guid.NewGuid().ToString(),entry.Command.JobId,entry.Command.AttemptId,commandId,"COMMAND_RETIRED",DateTimeOffset.UtcNow,
                entry.Command.ArtifactHash,entry.Result?.DriverJobKey,new() {{"queueCleared",true},{"localCommandRetired",true},{"sameCardFaceVerified",true}},NextSequence());
            Save(entry with {State="RETIRED",Events=[..entry.Events,result]});return result;
        }
    }
    private IEnumerable<JournalEntry> Entries()
    {
        foreach(var file in Directory.EnumerateFiles(directory,"*.json"))
            if(Guid.TryParseExact(Path.GetFileNameWithoutExtension(file),"D",out _)) yield return Find(Path.GetFileNameWithoutExtension(file))!;
    }
    private long NextSequence() => checked(Entries().SelectMany(entry=>entry.Events).Select(e=>e.ClientSequence).DefaultIfEmpty(0).Max()+1);
    private string ClaimPath(string profileId)
    {
        if(!Guid.TryParseExact(profileId,"D",out _))throw new InvalidDataException("设备档案标识格式无效");
        return Path.Combine(directory,profileId+".claim");
    }
    public ClaimState? LoadClaim(string profileId)
    {
        lock(gate) {
            var path=ClaimPath(profileId);if(!File.Exists(path))return null;
            if(new FileInfo(path).Length>65536 || (File.GetAttributes(path)&FileAttributes.ReparsePoint)!=0)throw new InvalidDataException("领取日志文件异常");
            var result=JsonSerializer.Deserialize<ClaimState>(File.ReadAllBytes(path),Hashing.Json);
            if(result==null || result.PrinterProfileId!=profileId || !Guid.TryParseExact(result.JobId,"D",out _) || !Guid.TryParseExact(result.ClaimId,"D",out _))throw new InvalidDataException("领取日志内容损坏");
            return result;
        }
    }
    public void SaveClaim(ClaimState claim)
    {
        lock(gate) {
            if(!Guid.TryParseExact(claim.JobId,"D",out _) || !Guid.TryParseExact(claim.ClaimId,"D",out _) || !DateTimeOffset.TryParse(claim.LeaseExpiresAt,out _))throw new InvalidDataException("领取结果格式无效");
            SaveBytes(ClaimPath(claim.PrinterProfileId),JsonSerializer.SerializeToUtf8Bytes(claim,Hashing.Json));
        }
    }
    public void ClearClaim(string profileId) {lock(gate) File.Delete(ClaimPath(profileId));}
    public bool HasPendingOperation(string profileId,string kind) {lock(gate) return File.Exists(OperationPath(profileId,kind));}
    public void ReleaseClaim(ClaimState claim)
    {
        lock(gate) {
            var current=LoadClaim(claim.PrinterProfileId);
            if(current?.JobId!=claim.JobId || current.ClaimId!=claim.ClaimId)throw new InvalidDataException("清空目标与领取日志不一致");
            // 墓碑先刷盘，崩溃后可继续清理；不能先删领取记录再遗留会重放旧任务的请求。
            SaveClaim(current with {Released=true});
            foreach(var kind in new[]{"claim","renew","clear"})File.Delete(OperationPath(claim.PrinterProfileId,kind));
            ClearClaim(claim.PrinterProfileId);
        }
    }
    // 请求意图先落盘；服务端提交后响应丢失，重启仍重放同一键和原始请求体。
    public PendingOperation BeginOperation(string profileId,string kind,string path,JsonObject body)
    {
        lock(gate) {
            var file=OperationPath(profileId,kind);
            if(File.Exists(file)) {
                if(new FileInfo(file).Length>65536 || (File.GetAttributes(file)&FileAttributes.ReparsePoint)!=0)throw new InvalidDataException("请求日志异常");
                var old=JsonSerializer.Deserialize<PendingOperation>(File.ReadAllBytes(file),Hashing.Json);
                if(old==null||!Guid.TryParseExact(old.Key,"D",out _)||old.Path!=path)throw new InvalidDataException("未完成操作与当前目标冲突");
                return old;
            }
            var operation=new PendingOperation(Guid.NewGuid().ToString(),path,(JsonObject)body.DeepClone());
            SaveBytes(file,JsonSerializer.SerializeToUtf8Bytes(operation,Hashing.Json));return operation;
        }
    }
    public void CompleteOperation(string profileId,string kind,string key)
    {
        lock(gate) {
            var file=OperationPath(profileId,kind);if(!File.Exists(file))return;
            var old=JsonSerializer.Deserialize<PendingOperation>(File.ReadAllBytes(file),Hashing.Json);
            if(old?.Key!=key)throw new InvalidDataException("未完成请求标识不一致");File.Delete(file);
        }
    }
    private string OperationPath(string profileId,string kind)
    {
        if(kind is not ("claim" or "renew" or "clear"))throw new InvalidDataException("请求日志类别无效");
        return ClaimPath(profileId)+"."+kind+".operation";
    }
    private void Save(JournalEntry entry)
    {
        SaveBytes(RecordPath(entry.Command.CommandId),JsonSerializer.SerializeToUtf8Bytes(entry,Hashing.Json));
    }
    private void SaveBytes(string path,byte[] bytes)
    {
        var temporary = path + ".pending-" + Guid.NewGuid();
        try
        {
            using(var stream = new FileStream(temporary, FileMode.CreateNew, FileAccess.Write, FileShare.None, 4096, FileOptions.WriteThrough))
            {
                if(!OperatingSystem.IsWindows()) File.SetUnixFileMode(temporary, UnixFileMode.UserRead | UnixFileMode.UserWrite);
                stream.Write(bytes); stream.Flush(true);
            }
            File.Move(temporary, path, true);
        }
        finally { if(File.Exists(temporary)) File.Delete(temporary); }
    }
    public void Dispose() => processLock.Dispose();
}
