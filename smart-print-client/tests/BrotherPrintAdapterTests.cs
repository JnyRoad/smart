using Smart.Printing;
using Smart.Printing.Adapters;
using SkiaSharp;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class BrotherPrintAdapterTests
{
    private static LocalPrinterProfile Profile(string filename="visitor-image.lbx")
    {
        var path=Path.Combine(AppContext.BaseDirectory,"resources","brother",filename);
        return PrintAdapterTests.Profile() with {Manufacturer="Brother",Model="QL-800",WindowsPrinterName="Brother QL-800",AllowedPrintModes=["SINGLE"],
            MediaWidthMm=58,MediaHeightMm=76,ColorMode="MONO",AutoDuplexVerified=false,LbxPath=path,LbxHash=Hashing.Sha256(File.ReadAllBytes(path))};
    }
    private static PrintCommand Command(byte[] pdf)=>PrintAdapterTests.Command(pdf,"SINGLE") with {PageWidthMm=58,PageHeightMm=76};
    [Fact]
    public async Task SubmitsSingleRasterImageThroughVerifiedFixedMediaTemplate()
    {
        var driver=new Driver(); var pdf=TestPdf.Create(1,58,76);
        var result=await new BrotherPrintAdapter(Profile(),new(),driver).SubmitAsync(Command(pdf),pdf,CancellationToken.None);
        Assert.True(result.Accepted); Assert.Equal(1,driver.Calls);
        using var image=SKBitmap.Decode(driver.Image); Assert.InRange(image.Width,684,686); Assert.InRange(image.Height,897,899);
        Assert.Equal("PageImage",driver.ObjectName); Assert.True(driver.Template.AsSpan().StartsWith("PK"u8));
    }
    [Fact]
    public async Task RejectsBackPageExtraPageWidthOverflowUnverifiedRedAndTemplateTampering()
    {
        var driver=new Driver(); var pdf=TestPdf.Create(1,58,76); var command=Command(pdf); var profile=Profile();
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(profile,new(),driver).SubmitAsync(command with {Face="BACK"},pdf,CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(profile,new(),driver).SubmitAsync(command,TestPdf.Create(2,58,76),CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(profile with {MediaWidthMm=62},new(),driver).SubmitAsync(command with {PageWidthMm=62},pdf,CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(profile with {ColorMode="BLACK_RED"},new(),driver).SubmitAsync(command,pdf,CancellationToken.None));
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(profile with {LbxHash="sha256:"+new string('f',64)},new(),driver).SubmitAsync(command,pdf,CancellationToken.None));
        Assert.Equal(0,driver.Calls);
    }
    [Fact]
    public async Task BlackRedRequiresMatchingVerifiedTemplateAndLostReplyRemainsUnknown()
    {
        var driver=new Driver(); var pdf=TestPdf.Create(1,58,76); var command=Command(pdf);
        await Assert.ThrowsAsync<PrintNotSubmittedException>(()=>new BrotherPrintAdapter(Profile() with {ColorMode="BLACK_RED",BlackRedVerified=true},new(),driver).SubmitAsync(command,pdf,CancellationToken.None));
        var profile=Profile("visitor-image-black-red.lbx") with {ColorMode="BLACK_RED",BlackRedVerified=true};
        await new BrotherPrintAdapter(profile,new(),driver).SubmitAsync(command,pdf,CancellationToken.None); Assert.Equal(1,driver.Calls);
        driver.LoseReply=true; await Assert.ThrowsAsync<IOException>(()=>new BrotherPrintAdapter(profile,new(),driver).SubmitAsync(command,pdf,CancellationToken.None));
        Assert.Equal(2,driver.Calls);
    }
    private sealed class Driver:IBpacPrintDriver
    {
        public int Calls; public bool LoseReply; public byte[] Image=[]; public byte[] Template=[]; public string ObjectName="";
        public SubmissionResult Submit(LocalPrinterProfile profile,string jobName,byte[] template,byte[] image)
        {Calls++;Image=image;Template=template;ObjectName=profile.LbxObjectName;if(LoseReply)throw new IOException("受理后断线");return new(true);}
    }
}
