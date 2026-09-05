using System.Globalization;
using System.IO.Compression;
using System.Xml;
using System.Xml.Linq;
namespace Smart.Printing.Adapters;

public interface IBpacPrintDriver { SubmissionResult Submit(LocalPrinterProfile profile,string jobName,byte[] template,byte[] image); }
public sealed class BrotherPrintAdapter(LocalPrinterProfile profile,PdfPageRenderer renderer,IBpacPrintDriver driver) : IPrintAdapter
{
    public Task<SubmissionResult> SubmitAsync(PrintCommand command,byte[] pdf,CancellationToken token)
    {
        profile.ValidateCommand(command);
        if(profile.Manufacturer!="Brother" || profile.Model!="QL-800" || command.PrintMode!="SINGLE" || command.Face!="FRONT" || command.PageCount!=1
            || profile.MediaWidthMm>58 || profile.ColorMode is not ("MONO" or "BLACK_RED") || profile.ColorMode=="BLACK_RED"&&!profile.BlackRedVerified
            || profile.FrontRotation!=0 || profile.OffsetXMm!=0 || profile.OffsetYMm!=0)
            throw new PrintNotSubmittedException("访客介质、颜色或单面模式未通过校验");
        var template=ReadTemplate(profile);
        var pages=renderer.Render(pdf,1,command.PageWidthMm,command.PageHeightMm,profile.Dpi);
        token.ThrowIfCancellationRequested();
        return Task.FromResult(driver.Submit(profile,"smart-"+command.CommandId,template,pages[0].Png));
    }
    private static byte[] ReadTemplate(LocalPrinterProfile profile)
    {
        try
        {
            if(!Path.IsPathFullyQualified(profile.LbxPath) || !Hashing.IsHash(profile.LbxHash)) throw new PrintNotSubmittedException("未配置受控 LBX 文件");
            var info=new FileInfo(profile.LbxPath);
            if(!info.Exists || info.Length>1024*1024 || (info.Attributes&FileAttributes.ReparsePoint)!=0) throw new PrintNotSubmittedException("LBX 文件不可用");
            var bytes=File.ReadAllBytes(profile.LbxPath);
            if(Hashing.Sha256(bytes)!=profile.LbxHash) throw new PrintNotSubmittedException("LBX 内容已改变，需要重新校准");
            using var archive=new ZipArchive(new MemoryStream(bytes),ZipArchiveMode.Read);
            if(archive.Entries.Count>8 || archive.Entries.Sum(e=>e.Length)>4*1024*1024
                || archive.Entries.Any(e=>e.FullName!=Path.GetFileName(e.FullName) || e.FullName.Contains('\\')))
                throw new PrintNotSubmittedException("LBX 包结构不合规");
            var entry=archive.GetEntry("label.xml")??throw new PrintNotSubmittedException("LBX 缺少版面");
            using var input=entry.Open(); using var xml=XmlReader.Create(input,new XmlReaderSettings {DtdProcessing=DtdProcessing.Prohibit,XmlResolver=null,MaxCharactersInDocument=512*1024});
            var doc=XDocument.Load(xml);
            XNamespace pt="http://schemas.brother.info/ptouch/2007/lbx/main",style="http://schemas.brother.info/ptouch/2007/lbx/style",image="http://schemas.brother.info/ptouch/2007/lbx/image";
            var sheets=doc.Descendants(style+"sheet").ToArray(); var papers=doc.Descendants(style+"paper").ToArray();
            var objects=doc.Descendants(pt+"objects").ToArray();
            if(sheets.Length!=1 || papers.Length!=1 || objects.Length!=1 || objects[0].Elements().Count()!=1 || objects[0].Elements().Single().Name!=image+"image")
                throw new PrintNotSubmittedException("LBX 必须只有一页和一个图像对象");
            var paper=papers[0]; var item=objects[0].Elements().Single(); var position=item.Element(pt+"objectStyle");
            if(position==null || position.Element(pt+"expanded")?.Attribute("objectName")?.Value!=profile.LbxObjectName
                || paper.Attribute("printerName")?.Value!="Brother QL-800" || paper.Attribute("orientation")?.Value!="portrait"
                || paper.Attribute("autoLength")?.Value!="false" || (paper.Attribute("printColorDisplay")?.Value=="true")!=(profile.ColorMode=="BLACK_RED"))
                throw new PrintNotSubmittedException("LBX 对象、颜色或设备配置不匹配");
            var width=Millimeters(position,"width"); var height=Millimeters(position,"height");
            if(Math.Abs(width-profile.MediaWidthMm)>0.01 || Math.Abs(height-profile.MediaHeightMm)>0.01
                || width>58.000001 || Millimeters(position,"x")+width>Millimeters(paper,"width")-Millimeters(paper,"marginRight")+0.01
                || Millimeters(position,"y")+height>Millimeters(paper,"height")-Millimeters(paper,"marginBottom")+0.01)
                throw new PrintNotSubmittedException("LBX 可打印区域与 PDF 不一致");
            foreach(var attribute in doc.Descendants().Attributes())
                if(attribute.Value.Contains("://",StringComparison.Ordinal) || attribute.Value.Contains("..",StringComparison.Ordinal) || attribute.Value.Contains('\\'))
                    if(!attribute.IsNamespaceDeclaration) throw new PrintNotSubmittedException("LBX 包含外部引用");
            return bytes;
        }
        catch(PrintNotSubmittedException) {throw;}
        catch(Exception error) {throw new PrintNotSubmittedException("LBX 校验失败，未提交打印机",error);}
    }
    private static double Millimeters(XElement element,string name)
    {
        var raw=element.Attribute(name)?.Value;
        if(raw is null || !raw.EndsWith("pt",StringComparison.Ordinal) || !double.TryParse(raw[..^2],NumberStyles.Float,CultureInfo.InvariantCulture,out var value)
            || !double.IsFinite(value) || value<0) throw new PrintNotSubmittedException("LBX 尺寸格式无效");
        return value*25.4/72;
    }
}
