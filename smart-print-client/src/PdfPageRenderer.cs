using PDFtoImage;
using SkiaSharp;
namespace Smart.Printing;

// 此异常只允许在任何驱动提交发生之前使用，区分确定未提交与提交结果不明。
public sealed class PrintNotSubmittedException(string message, Exception? cause = null) : Exception(message, cause);
public sealed record RasterPage(byte[] Png, int PixelWidth, int PixelHeight, double WidthMm, double HeightMm);

public sealed class PdfPageRenderer
{
    private static readonly object RenderGate = new();
    public IReadOnlyList<RasterPage> Render(byte[] pdf, int expectedPages, double widthMm, double heightMm, int dpi)
    {
        if(pdf.Length == 0 || pdf.Length > 32*1024*1024 || !pdf.AsSpan().StartsWith("%PDF-"u8)
            || expectedPages is < 1 or > 2 || dpi is < 150 or > 600 || !double.IsFinite(widthMm) || !double.IsFinite(heightMm)
            || widthMm <= 0 || heightMm <= 0 || widthMm*dpi/25.4*heightMm*dpi/25.4 > 16_000_000)
            throw new PrintNotSubmittedException("PDF 制品或渲染尺寸不合规");
        if(!OperatingSystem.IsWindows() && !OperatingSystem.IsLinux() && !OperatingSystem.IsMacOS())
            throw new PrintNotSubmittedException("当前操作系统不支持 PDF 渲染");
        lock(RenderGate)
        {
            try
            {
                if(Conversion.GetPageCount(pdf) != expectedPages) throw new PrintNotSubmittedException("PDF 页数与当前命令不匹配");
                var sizes = Conversion.GetPageSizes(pdf).ToArray();
                if(sizes.Any(size => Math.Abs(size.Width*25.4/72-widthMm)>0.01 || Math.Abs(size.Height*25.4/72-heightMm)>0.01))
                    throw new PrintNotSubmittedException("PDF 物理尺寸与冻结档案不匹配");
                var result = new List<RasterPage>();
                for(var index=0; index<expectedPages; index++)
                {
                    using var bitmap = Conversion.ToImage(pdf, page:index, options:new RenderOptions(Dpi:dpi, WithAnnotations:false, WithFormFill:false, BackgroundColor:SKColors.White));
                    if(bitmap.Width <= 0 || bitmap.Height <= 0 || (long)bitmap.Width*bitmap.Height > 16_000_000)
                        throw new PrintNotSubmittedException("页面像素超限");
                    using var data = bitmap.Encode(SKEncodedImageFormat.Png,100);
                    result.Add(new RasterPage(data.ToArray(),bitmap.Width,bitmap.Height,widthMm,heightMm));
                }
                return result;
            }
            catch(PrintNotSubmittedException) { throw; }
            catch(Exception error) { throw new PrintNotSubmittedException("PDF 解析失败，未提交打印机",error); }
        }
    }
}
