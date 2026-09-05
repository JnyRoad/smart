using System.Reflection;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
namespace Smart.Printing.Adapters;

public sealed class BpacPrintDriver : IBpacPrintDriver
{
    public SubmissionResult Submit(LocalPrinterProfile profile,string jobName,byte[] template,byte[] image)
    {
        if(!profile.CalibrationVerified || profile.BpacMediaId<=0 || template.Length==0 || image.Length==0 || !OperatingSystem.IsWindows())
            throw new PrintNotSubmittedException("b-PAC 工作站或介质未完成校准");
        return InWindowsApartment(profile,jobName,template,image);
    }
    [SupportedOSPlatform("windows")]
    private static SubmissionResult InWindowsApartment(LocalPrinterProfile profile,string jobName,byte[] template,byte[] image)
    {
        SubmissionResult? result=null; Exception? failure=null;
        var thread=new Thread(()=> {try {result=SubmitWindows(profile,jobName,template,image);}catch(Exception error){failure=error;}});
        thread.SetApartmentState(ApartmentState.STA);thread.Start();thread.Join();
        if(failure!=null) System.Runtime.ExceptionServices.ExceptionDispatchInfo.Capture(failure).Throw();
        return result??throw new IOException("b-PAC 未返回提交结果");
    }
    [SupportedOSPlatform("windows")]
    private static SubmissionResult SubmitWindows(LocalPrinterProfile profile,string jobName,byte[] template,byte[] image)
    {
        object? document=null,printer=null,picture=null; var started=false; string? working=null;
        try
        {
            WindowsPrinterIdentity.Require(profile);
            if(string.IsNullOrWhiteSpace(profile.TempDirectory) || !Path.IsPathFullyQualified(profile.TempDirectory)) throw new PrintNotSubmittedException("未配置受控临时目录");
            working=PrivateDirectory.Ensure(Path.Combine(PrivateDirectory.Ensure(profile.TempDirectory),Guid.NewGuid().ToString()));
            var templatePath=Path.Combine(working,"page.lbx");var imagePath=Path.Combine(working,"page.png");
            File.WriteAllBytes(templatePath,template);File.WriteAllBytes(imagePath,image);
            var type=Type.GetTypeFromProgID("bpac.Document")??throw new PrintNotSubmittedException("未安装与进程位数匹配的 b-PAC");
            document=Activator.CreateInstance(type)??throw new PrintNotSubmittedException("无法建立 b-PAC 文档");
            dynamic doc=document;
            if(!(bool)doc.Open(templatePath) || !(bool)doc.SetPrinter(profile.WindowsPrinterName,false)) throw new PrintNotSubmittedException("b-PAC 无法打开受控模板或选择指定设备");
            printer=ReadMember(document,"Printer");
            if(printer==null || !(bool)((dynamic)printer).IsPrinterOnline(profile.WindowsPrinterName)) throw new PrintNotSubmittedException("Brother 打印机未在线");
            if(Convert.ToInt32(ReadMember(printer,"MediaId"))!=profile.BpacMediaId || Convert.ToInt32(ReadMember(document,"MediaId"))!=profile.BpacMediaId)
                throw new PrintNotSubmittedException("实际纸卷与已校准 LBX 介质不匹配");
            picture=doc.GetObject(profile.LbxObjectName);
            if(picture==null || !(bool)((dynamic)picture).SetData(0,imagePath,4)) throw new PrintNotSubmittedException("b-PAC 图像对象替换失败");
            // 从 StartPrint 起保守视为可能已经提交。PrintOut 第二参数按官方FAQ固定0。
            started=true;
            if(!(bool)doc.StartPrint(jobName,1) || !(bool)doc.PrintOut(1,0) || !(bool)doc.EndPrint()) throw new IOException("b-PAC 提交结果不明");
            return new SubmissionResult(true);
        }
        catch(PrintNotSubmittedException) when(!started) {throw;}
        catch(Exception error) when(!started) {throw new PrintNotSubmittedException("b-PAC 预检失败，尚未提交",error);}
        finally
        {
            if(document!=null) {try {((dynamic)document).Close();}catch(Exception) {}}
            foreach(var item in new[] {picture,printer,document}) if(item!=null&&Marshal.IsComObject(item)) {try {Marshal.FinalReleaseComObject(item);}catch(Exception) {}}
            if(working!=null) {try {Directory.Delete(working,true);}catch(IOException) {}}
        }
    }
    [SupportedOSPlatform("windows")]
    private static object? ReadMember(object target,string property)
    {
        try {return target.GetType().InvokeMember(property,BindingFlags.GetProperty,null,target,null);}
        catch(COMException error) when(error.HResult==unchecked((int)0x80020003)||error.HResult==unchecked((int)0x80020006))
        {return target.GetType().InvokeMember("Get"+property,BindingFlags.InvokeMethod,null,target,null);}
        catch(MissingMethodException) {return target.GetType().InvokeMember("Get"+property,BindingFlags.InvokeMethod,null,target,null);}
    }
}
