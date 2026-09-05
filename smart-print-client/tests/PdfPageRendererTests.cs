using Smart.Printing;
using SkiaSharp;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class PdfPageRendererTests
{
    [Fact]
    public void RasterizesRealPdfAtPhysicalSizeAndPreservesFrontBackOrder()
    {
        var pages = new PdfPageRenderer().Render(TestPdf.Create(2), 2, 85.6, 53.98, 300);
        Assert.Equal(2, pages.Count);
        using var front = SKBitmap.Decode(pages[0].Png);
        using var back = SKBitmap.Decode(pages[1].Png);
        Assert.InRange(front.Width,1010,1012); Assert.InRange(front.Height,637,639);
        Assert.Equal(SKColors.Red,front.GetPixel(100,100)); Assert.Equal(SKColors.Blue,back.GetPixel(100,100));
    }
    [Fact]
    public void RejectsExtraPagesWrongSizeMalformedPdfAndUnboundedRaster()
    {
        var renderer = new PdfPageRenderer();
        Assert.Throws<PrintNotSubmittedException>(() => renderer.Render(TestPdf.Create(2),1,85.6,53.98,300));
        Assert.Throws<PrintNotSubmittedException>(() => renderer.Render(TestPdf.Create(),1,54,86,300));
        Assert.Throws<PrintNotSubmittedException>(() => renderer.Render("%PDF-broken"u8.ToArray(),1,85.6,53.98,300));
        Assert.Throws<PrintNotSubmittedException>(() => renderer.Render(TestPdf.Create(1,2000,2000),1,2000,2000,600));
        Assert.Throws<PrintNotSubmittedException>(() => renderer.Render(TestPdf.Create(),1,85.6,53.98,100000));
    }
}
