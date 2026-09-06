using System.Text.Json;
using System.Text.Json.Serialization;
using System.Text.RegularExpressions;
namespace Smart.Printing;

public sealed record RuntimeOptions
{
    public bool ExecutionEnabled {get;init;}
    public string Endpoint {get;init;}="";
    public string DeviceIdentity {get;init;}="";
    public string TokenEnvironmentVariable {get;init;}="SMART_PRINT_DEVICE_TOKEN";
    public string JournalDirectory {get;init;}="";
    public int PollIntervalSeconds {get;init;}=3;
    public LocalPrinterProfile[] Printers {get;init;}=[];
    public static RuntimeOptions Load(string path)
    {
        if(new FileInfo(path).Length>1024*1024)throw new InvalidDataException("工作站配置过大");
        var options=JsonSerializer.Deserialize<RuntimeOptions>(File.ReadAllBytes(path),new JsonSerializerOptions(Hashing.Json){UnmappedMemberHandling=JsonUnmappedMemberHandling.Disallow})
            ??throw new InvalidDataException("工作站配置无效");options.Validate();return options;
    }
    public void Validate()
    {
        if(!Uri.TryCreate(Endpoint,UriKind.Absolute,out var address)||address.Scheme!="https"||address.UserInfo!=""||address.Query!=""||address.Fragment!=""
            || !Regex.IsMatch(DeviceIdentity,"^[A-Za-z0-9_-]{1,64}$") || !Regex.IsMatch(TokenEnvironmentVariable,"^[A-Z][A-Z0-9_]{1,100}$")
            || PollIntervalSeconds is < 1 or > 60 || Printers.Length is < 1 or > 16)
            throw new InvalidDataException("工作站地址、身份或轮询配置无效");
        if(Printers.Select(p=>p.PrinterProfileId).Distinct().Count()!=Printers.Length
            || Printers.Select(p=>p.WindowsPrinterName).Distinct(StringComparer.OrdinalIgnoreCase).Count()!=Printers.Length
            || Printers.Select(p=>p.WindowsPortName).Distinct(StringComparer.OrdinalIgnoreCase).Count()!=Printers.Length)
            throw new InvalidDataException("同一物理打印队列不能配置多个可领取档案");
        foreach(var printer in Printers)
            if(printer.DeviceIdentity!=DeviceIdentity || !Guid.TryParseExact(printer.PrinterProfileId,"D",out _) || !Hashing.IsHash(printer.PrinterSnapshotHash)
                || printer.Manufacturer is not ("HiTi" or "Brother") || printer.AllowedPrintModes.Length==0
                || printer.AllowedPrintModes.Any(mode=>mode is not ("SINGLE" or "MANUAL_DUPLEX" or "AUTO_DUPLEX"))
                || ExecutionEnabled&&!printer.CalibrationVerified)
                throw new InvalidDataException("打印机档案未通过身份、模式或验收校验");
        if(JournalDirectory.Length>0&&!Path.IsPathFullyQualified(JournalDirectory))throw new InvalidDataException("日志目录必须是本机绝对路径");
    }
    public string ResolvedJournalDirectory()=>JournalDirectory.Length>0?JournalDirectory:Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData),"SmartPrintClient",DeviceIdentity);
}
