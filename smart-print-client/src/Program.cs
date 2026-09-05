using System.Text.Json.Nodes;
using Smart.Printing.Adapters;
namespace Smart.Printing;

public static class Program
{
    public static async Task<int> Main(string[] args)
    {
        if(args.Length<3||args[1]!="--config") {
            Console.WriteLine("用法：Smart.PrintClient run|validate|retire|clear-device --config <本机配置.json>");
            Console.WriteLine("retire 另带 --command-id <原命令> --queue-cleared --same-card-face-verified；clear-device 另带 --printer-profile-id <档案> --job-id <任务> --operator-check-id <核对记录> --no-card");return args.Length==0?0:2;
        }
        try
        {
            var options=RuntimeOptions.Load(args[2]);
            if(args[0]=="validate"){Console.WriteLine("配置格式通过；尚未验证 Windows 驱动、设备和实际介质。");return 0;}
            if(!OperatingSystem.IsWindows())throw new InvalidDataException("打印工作站执行仅支持 Windows");
            if(args[0]=="run"&&!options.ExecutionEnabled){Console.WriteLine("本机执行开关已关闭，未领取新任务。");return 0;}
            using var journal=new PrintCommandJournal(options.ResolvedJournalDirectory());
            if(args[0]=="retire")
            {
                var commandId=Argument(args,"--command-id");var entry=journal.Find(commandId)??throw new InvalidDataException("没有原命令持久记录");
                var profile=options.Printers.Single(p=>p.PrinterProfileId==entry.Command.PrinterProfileId);
                if(profile.PrinterSnapshotHash!=entry.Command.PrinterSnapshotHash || !args.Contains("--queue-cleared") || !args.Contains("--same-card-face-verified"))throw new InvalidDataException("请使用原档案，并确认原队列清空且同一卡面已核对");
                WindowsPrinterIdentity.RequireEmptyQueue(profile);
                journal.Retire(commandId,true,true);Console.WriteLine("原命令已在本机永久终止；恢复运行后上报证据，由平台人工决定是否续打。");return 0;
            }
            var token=Environment.GetEnvironmentVariable(options.TokenEnvironmentVariable)??throw new InvalidDataException("未配置独立设备凭据环境变量");
            using var transport=PrintApiClient.CreateTransport();var api=new PrintApiClient(transport,new Uri(options.Endpoint),token);
            if(args[0]=="clear-device")
            {
                var profileId=Argument(args,"--printer-profile-id");var jobId=Argument(args,"--job-id");var checkId=Argument(args,"--operator-check-id");
                if(!args.Contains("--no-card")||!Guid.TryParseExact(jobId,"D",out _)||!Guid.TryParseExact(checkId,"D",out _))throw new InvalidDataException("需要现场无卡确认及平台有效输出核对记录");
                var profile=options.Printers.Single(p=>p.PrinterProfileId==profileId);
                await new DeviceRecovery(api,journal,WindowsPrinterIdentity.RequireEmptyQueue).ClearAsync(profile,jobId,checkId,true,CancellationToken.None);
                Console.WriteLine("设备清空证据已提交；是否释放占用由平台任务状态判定。");return 0;
            }
            if(args[0]!="run")throw new InvalidDataException("未知工作站命令");
            var poller=new TaskPoller(api,journal,options.Printers,profile=>profile.Manufacturer=="Brother"
                ?new BrotherPrintAdapter(profile,new PdfPageRenderer(),new BpacPrintDriver()):new HiTiPrintAdapter(profile,new PdfPageRenderer(),new WindowsPrintDriver()));
            using var stop=new CancellationTokenSource();Console.CancelKeyPress+=(_,eventArgs)=>{eventArgs.Cancel=true;stop.Cancel();};
            var heartbeat=Heartbeat(poller,stop.Token);Console.WriteLine("工作站已启动；回执或设备状态异常时保留原任务，等待平台核对。");
            try {
                while(!stop.IsCancellationRequested) {
                    try {await poller.StepAsync(stop.Token);}
                    catch(OperationCanceledException) when(stop.IsCancellationRequested){break;}
                    catch(HttpRequestException){Console.Error.WriteLine("打印服务连接或请求未成功，保留命令与回执，稍后重新查询。");}
                    catch(InvalidDataException){Console.Error.WriteLine("打印任务或本机记录校验失败，已停止领取，请核对平台与工作站配置。");return 3;}
                    await Task.Delay(TimeSpan.FromSeconds(options.PollIntervalSeconds),stop.Token);
                }
            }
            finally {stop.Cancel();try{await heartbeat;}catch(OperationCanceledException){}}
            return 0;
        }
        catch(OperationCanceledException){return 0;}
        catch(Exception){Console.Error.WriteLine("工作站未完成操作。请核对配置、设备身份、权限、原任务和持久日志；未自动重放打印命令。");return 2;}
    }
    private static string Argument(string[] args,string name)
    {
        var index=Array.IndexOf(args,name);if(index<0||index+1>=args.Length)throw new InvalidDataException("缺少参数 "+name);return args[index+1];
    }
    private static async Task Heartbeat(TaskPoller poller,CancellationToken token)
    {
        while(!token.IsCancellationRequested){await Task.Delay(TimeSpan.FromSeconds(15),token);try{await poller.RenewAsync(token);}catch(HttpRequestException){Console.Error.WriteLine("连接租约续期失败，物理任务占用保持。");}}
    }
}
