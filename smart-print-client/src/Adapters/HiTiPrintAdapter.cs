namespace Smart.Printing.Adapters;

public sealed class HiTiPrintAdapter(LocalPrinterProfile profile, PdfPageRenderer renderer, IWindowsPrintDriver driver) : IPrintAdapter
{
    public Task<SubmissionResult> SubmitAsync(PrintCommand command, byte[] pdf, CancellationToken token)
    {
        profile.ValidateCommand(command);
        if(profile.Manufacturer!="HiTi" || profile.Model is not ("CS220" or "CS-220" or "CS220e" or "CS-220e"))
            throw new PrintNotSubmittedException("设备不属于已适配的 HiTi 档案");
        var automatic=command.PrintMode=="AUTO_DUPLEX";
        if(automatic ? (!profile.AutoDuplexVerified || profile.DuplexEdge is not ("LONG" or "SHORT") || command.Face!="BOTH" || command.PageCount!=2)
            : (command.PrintMode!="MANUAL_DUPLEX" || command.Face is not ("FRONT" or "BACK") || command.PageCount!=1))
            throw new PrintNotSubmittedException("翻面能力或当前打印面不匹配");
        var pages=renderer.Render(pdf,automatic?2:1,command.PageWidthMm,command.PageHeightMm,profile.Dpi);
        token.ThrowIfCancellationRequested();
        // 每个命令只有一次驱动提交；不携带卡号写入、芯片编码或权限操作。
        return Task.FromResult(NativeSubmission.Run(profile,()=>driver.Submit(profile,new PrintBatch("smart-"+command.CommandId,command.Face,automatic?profile.DuplexEdge:"SIMPLEX",pages))));
    }
}
