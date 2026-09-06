using Smart.Printing;
using Smart.Printing.Adapters;
using SkiaSharp;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class PrintAdapterTests
{
    internal static LocalPrinterProfile Profile() => new() {
        PrinterProfileId="printer-1",DeviceIdentity="device-1",PrinterSnapshotHash="sha256:"+new string('b',64),
        Manufacturer="HiTi",Model="CS220",WindowsPrinterName="HiTi USB",WindowsDriverName="HiTi CS220 Series",WindowsPortName="USB001",
        AllowedPrintModes=["MANUAL_DUPLEX","AUTO_DUPLEX"],MediaWidthMm=85.6,MediaHeightMm=53.98,Dpi=300,
        CalibrationVerified=true,AutoDuplexVerified=true,DuplexEdge="LONG",PaperRawKind=256,
        ColorMode="COLOR",PaperName="CR80",LbxPath="",LbxHash="",LbxObjectName="PageImage"
    };
    internal static PrintCommand Command(byte[] pdf,string mode="MANUAL_DUPLEX",string face="FRONT") => new(
        Guid.NewGuid().ToString(),Guid.NewGuid().ToString(),Guid.NewGuid().ToString(),"printer-1","device-1",mode,face,
        Hashing.Sha256(pdf),"sha256:"+new string('a',64),"sha256:"+new string('b',64),85.6,53.98,mode=="AUTO_DUPLEX"?2:1);
    [Fact]
    public async Task ManualSubmitsExactlyOnePageWithoutDuplexAndAutoSubmitsOrderedPairOnce()
    {
        var driver=new Driver(); var adapter=new HiTiPrintAdapter(Profile(),new PdfPageRenderer(),driver);
        var pdf=TestPdf.Create(); await adapter.SubmitAsync(Command(pdf),pdf,CancellationToken.None);
        Assert.Single(driver.Batches); Assert.Single(driver.Batches[0].Pages); Assert.Equal("SIMPLEX",driver.Batches[0].DuplexEdge);
        var pair=TestPdf.Create(2); await adapter.SubmitAsync(Command(pair,"AUTO_DUPLEX","BOTH"),pair,CancellationToken.None);
        Assert.Equal(2,driver.Batches.Count); Assert.Equal("LONG",driver.Batches[1].DuplexEdge); Assert.Equal(2,driver.Batches[1].Pages.Count);
        using var front=SKBitmap.Decode(driver.Batches[1].Pages[0].Png); using var back=SKBitmap.Decode(driver.Batches[1].Pages[1].Png);
        Assert.Equal(SKColors.Red,front.GetPixel(100,100)); Assert.Equal(SKColors.Blue,back.GetPixel(100,100));
    }
    [Fact]
    public async Task UnverifiedFlipperWrongPrinterAndWrongMediaNeverSubmit()
    {
        var driver=new Driver(); var pdf=TestPdf.Create(2); var profile=Profile() with {AutoDuplexVerified=false};
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new HiTiPrintAdapter(profile,new(),driver).SubmitAsync(Command(pdf,"AUTO_DUPLEX","BOTH"),pdf,CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new HiTiPrintAdapter(Profile(),new(),driver).SubmitAsync(Command(pdf,"AUTO_DUPLEX","BOTH") with {DeviceIdentity="other"},pdf,CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new HiTiPrintAdapter(Profile() with {MediaWidthMm=54},new(),driver).SubmitAsync(Command(pdf,"AUTO_DUPLEX","BOTH"),pdf,CancellationToken.None));
        Assert.Empty(driver.Batches);
    }
    [Fact]
    public async Task KnownPreSubmissionRejectionIsDifferentFromLostSpoolReply()
    {
        var directory=Path.Combine(Path.GetTempPath(),"smart-adapter-"+Guid.NewGuid()); var pdf=TestPdf.Create();
        try {
            using var journal=new PrintCommandJournal(directory);
            var binding=new PrinterBinding("printer-1","device-1","sha256:"+new string('b',64),["MANUAL_DUPLEX"]);
            var rejected=new PrintCommandProcessor(journal,new HiTiPrintAdapter(Profile() with {CalibrationVerified=false},new(),new Driver()),binding);
            Assert.Equal("DRIVER_REJECTED",(await rejected.ExecuteAsync(Command(pdf),pdf,CancellationToken.None)).EventType);
            var unknown=new PrintCommandProcessor(journal,new HiTiPrintAdapter(Profile(),new(),new Driver {LoseReply=true}),binding);
            Assert.Equal("OUTPUT_UNKNOWN",(await unknown.ExecuteAsync(Command(pdf),pdf,CancellationToken.None)).EventType);
        } finally {if(Directory.Exists(directory)) Directory.Delete(directory,true);}
    }
    private sealed class Driver : IWindowsPrintDriver
    {
        public readonly List<PrintBatch> Batches=[]; public bool LoseReply;
        public SubmissionResult Submit(LocalPrinterProfile profile,PrintBatch batch)
        {
            Batches.Add(batch); if(LoseReply) throw new IOException("驱动已收到但响应丢失"); return new(true);
        }
    }
}
