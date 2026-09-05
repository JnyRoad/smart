using System.Drawing;
using System.Drawing.Printing;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
namespace Smart.Printing.Adapters;

public sealed class WindowsPrintDriver : IWindowsPrintDriver
{
    public SubmissionResult Submit(LocalPrinterProfile profile, PrintBatch batch)
    {
        if(!profile.CalibrationVerified || batch.Pages.Count is < 1 or > 2 || !OperatingSystem.IsWindows())
            throw new PrintNotSubmittedException("Windows 打印驱动或校准不可用");
        return SubmitWindows(profile,batch);
    }
    [SupportedOSPlatform("windows")]
    private static SubmissionResult SubmitWindows(LocalPrinterProfile profile, PrintBatch batch)
    {
        using var document=new PrintDocument();
        try
        {
            WindowsPrinterIdentity.Require(profile);
            document.PrinterSettings.PrinterName=profile.WindowsPrinterName;
            if(!document.PrinterSettings.IsValid) throw new PrintNotSubmittedException("已登记打印队列不存在");
            if(batch.DuplexEdge!="SIMPLEX" && (!document.PrinterSettings.CanDuplex || !profile.AutoDuplexVerified))
                throw new PrintNotSubmittedException("驱动未提供已验收的自动翻面能力");
            if(batch.DuplexEdge is not ("SIMPLEX" or "LONG" or "SHORT") || batch.Pages.Count!=(batch.DuplexEdge=="SIMPLEX"?1:2))
                throw new PrintNotSubmittedException("驱动任务面数错误");
            var papers=document.PrinterSettings.PaperSizes.Cast<PaperSize>().Where(p=>p.RawKind==profile.PaperRawKind && p.PaperName==profile.PaperName).ToArray();
            if(papers.Length!=1) throw new PrintNotSubmittedException("已校准介质未在当前驱动中找到");
            var paper=papers[0];
            var actualWidth=(profile.Landscape?paper.Height:paper.Width)*25.4/100;
            var actualHeight=(profile.Landscape?paper.Width:paper.Height)*25.4/100;
            if(Math.Abs(actualWidth-profile.MediaWidthMm)>0.3 || Math.Abs(actualHeight-profile.MediaHeightMm)>0.3)
                throw new PrintNotSubmittedException("当前驱动介质尺寸与档案不符");
            var resolutions=document.PrinterSettings.PrinterResolutions.Cast<PrinterResolution>().Where(r=>r.X==profile.Dpi && r.Y==profile.Dpi).ToArray();
            if(resolutions.Length==0) throw new PrintNotSubmittedException("驱动不支持已校准的打印分辨率");
            document.PrinterSettings.Copies=1; document.PrinterSettings.Collate=false;
            document.PrinterSettings.PrintToFile=false; document.PrinterSettings.PrintRange=PrintRange.AllPages;
            document.PrinterSettings.Duplex=batch.DuplexEdge switch { "LONG"=>Duplex.Vertical, "SHORT"=>Duplex.Horizontal, _=>Duplex.Simplex };
            document.DefaultPageSettings.PaperSize=paper; document.DefaultPageSettings.Landscape=profile.Landscape;
            document.DefaultPageSettings.PrinterResolution=resolutions[0]; document.DefaultPageSettings.Margins=new Margins(0,0,0,0);
            document.DefaultPageSettings.Color=profile.ColorMode=="COLOR";
            document.DocumentName=batch.JobName; document.PrintController=new StandardPrintController(); document.OriginAtMargins=false;
        }
        catch(PrintNotSubmittedException) {throw;}
        catch(Exception error) {throw new PrintNotSubmittedException("驱动预检失败，尚未提交",error);}
        var index=0;
        document.PrintPage+=(_,args)=> {
            var page=batch.Pages[index];
            using var stream=new MemoryStream(page.Png,false); using var picture=Image.FromStream(stream);
            var rotation=(batch.Face=="BACK" || index==1)?profile.BackRotation:profile.FrontRotation;
            if(rotation==180) picture.RotateFlip(RotateFlipType.Rotate180FlipNone);
            var graphics=args.Graphics??throw new IOException("驱动没有提供绘图表面");
            graphics.PageUnit=GraphicsUnit.Millimeter;
            graphics.TranslateTransform((float)(profile.OffsetXMm-args.PageSettings.HardMarginX*25.4/100),
                (float)(profile.OffsetYMm-args.PageSettings.HardMarginY*25.4/100));
            graphics.DrawImage(picture,new RectangleF(0,0,(float)page.WidthMm,(float)page.HeightMm));
            args.HasMorePages=++index<batch.Pages.Count;
        };
        // Print 开始后的任何异常都交由处理器记录为结果不明，不再归类为确定未提交。
        document.Print();
        return new SubmissionResult(true);
    }
}

[SupportedOSPlatform("windows")]
internal static class WindowsPrinterIdentity
{
    [DllImport("winspool.drv", CharSet=CharSet.Unicode, SetLastError=true)]
    [return:MarshalAs(UnmanagedType.Bool)] private static extern bool OpenPrinter(string name,out IntPtr handle,IntPtr defaults);
    [DllImport("winspool.drv", CharSet=CharSet.Unicode, SetLastError=true)]
    [return:MarshalAs(UnmanagedType.Bool)] private static extern bool GetPrinter(IntPtr handle,uint level,IntPtr buffer,uint size,out uint needed);
    [DllImport("winspool.drv",SetLastError=true)]
    [return:MarshalAs(UnmanagedType.Bool)] private static extern bool ClosePrinter(IntPtr handle);
    public static void RequireEmptyQueue(LocalPrinterProfile profile)
    {
        Require(profile);
        if(!OpenPrinter(profile.WindowsPrinterName,out var handle,IntPtr.Zero))throw new PrintNotSubmittedException("无法核对原打印队列");
        IntPtr buffer=IntPtr.Zero;
        try {
            GetPrinter(handle,2,IntPtr.Zero,0,out var needed);
            if(needed<13*IntPtr.Size+32 || needed>1024*1024)throw new PrintNotSubmittedException("队列信息不可用");
            buffer=Marshal.AllocHGlobal((int)needed);
            if(!GetPrinter(handle,2,buffer,needed,out _)||Marshal.ReadInt32(buffer,13*IntPtr.Size+24)!=0)throw new PrintNotSubmittedException("原驱动仍有任务，不允许确认清空");
        } finally {if(buffer!=IntPtr.Zero)Marshal.FreeHGlobal(buffer);ClosePrinter(handle);}
    }
    public static void Require(LocalPrinterProfile profile)
    {
        if(!OpenPrinter(profile.WindowsPrinterName,out var handle,IntPtr.Zero)) throw new PrintNotSubmittedException("无法打开指定打印队列");
        IntPtr buffer=IntPtr.Zero;
        try
        {
            GetPrinter(handle,2,IntPtr.Zero,0,out var needed);
            if(needed<13*IntPtr.Size || needed>1024*1024) throw new PrintNotSubmittedException("驱动身份信息异常");
            buffer=Marshal.AllocHGlobal((int)needed);
            if(!GetPrinter(handle,2,buffer,needed,out _)) throw new PrintNotSubmittedException("无法读取驱动身份");
            // PRINTER_INFO_2 的第4、5个指针依次为端口名、驱动名；固定匹配本机白名单。
            var port=Marshal.PtrToStringUni(Marshal.ReadIntPtr(buffer,3*IntPtr.Size));
            var driver=Marshal.PtrToStringUni(Marshal.ReadIntPtr(buffer,4*IntPtr.Size));
            if(port!=profile.WindowsPortName || driver!=profile.WindowsDriverName)
                throw new PrintNotSubmittedException("打印队列对应的端口或驱动已改变");
        }
        finally {if(buffer!=IntPtr.Zero) Marshal.FreeHGlobal(buffer); ClosePrinter(handle);}
    }
}
