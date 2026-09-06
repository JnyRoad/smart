namespace Smart.Printing;

/** 原生驱动无法可靠取消时，超时后记录结果不明并让工作站退出，禁止继续领取下一任务。 */
public sealed class PrintSubmissionTimeoutException(string message) : IOException(message);
public static class NativeSubmission
{
    public static SubmissionResult Run(LocalPrinterProfile profile, Func<SubmissionResult> action, ApartmentState apartment=ApartmentState.MTA)
    {
        var seconds=profile.SubmissionTimeoutSeconds==0?120:profile.SubmissionTimeoutSeconds;
        if(seconds<10||seconds>300)throw new PrintNotSubmittedException("原生提交超时配置无效");
        SubmissionResult? result=null; Exception? failure=null;
        var thread=new Thread(()=>{try{result=action();}catch(Exception error){failure=error;}}) {IsBackground=true};
        // 单元测试在非 Windows 运行；真实 COM 提交只会在 Windows 调用此处。
        if(OperatingSystem.IsWindows())thread.SetApartmentState(apartment);
        thread.Start();
        if(!thread.Join(TimeSpan.FromSeconds(seconds)))throw new PrintSubmissionTimeoutException("原生打印提交超过受控时限，输出状态未知");
        if(failure!=null) System.Runtime.ExceptionServices.ExceptionDispatchInfo.Capture(failure).Throw();
        return result??throw new IOException("原生打印驱动未返回提交结果");
    }
}
