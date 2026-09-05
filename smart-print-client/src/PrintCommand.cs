using System.Security.Cryptography;
using System.Text.Json;
using System.Text.RegularExpressions;
namespace Smart.Printing;

// 命令只保存不可变标识与hash；人员内容仅在已经授权下载的PDF中短暂存在。
public sealed record PrintCommand(string JobId, string AttemptId, string CommandId, string PrinterProfileId,
    string DeviceIdentity, string PrintMode, string Face, string ArtifactHash, string TemplateSnapshotHash,
    string PrinterSnapshotHash, double PageWidthMm, double PageHeightMm, int PageCount);
public sealed record PrinterBinding(string PrinterProfileId, string DeviceIdentity, string PrinterSnapshotHash, string[] AllowedPrintModes);
public sealed record SubmissionResult(bool Accepted, string? DriverJobKey = null);
public sealed record PrintClientEvent(string EventId, string JobId, string AttemptId, string CommandId,
    string EventType, DateTimeOffset OccurredAt, string ArtifactHash, string? DriverJobKey, Dictionary<string, object> Payload, long ClientSequence = 0);
public interface IPrintAdapter
{
    Task<SubmissionResult> SubmitAsync(PrintCommand command, byte[] pdf, CancellationToken token);
}
public static class Hashing
{
    public static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);
    public static string Sha256(byte[] bytes) => "sha256:" + Convert.ToHexStringLower(SHA256.HashData(bytes));
    public static string Signature(PrintCommand command) => Sha256(JsonSerializer.SerializeToUtf8Bytes(command, Json));
    public static bool IsHash(string value) => value is not null && Regex.IsMatch(value, "^sha256:[a-f0-9]{64}$");
}
