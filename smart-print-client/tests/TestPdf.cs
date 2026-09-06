using System.Globalization;
using System.Text;
namespace Smart.Printing.Tests;

// 生成不含真实人员信息的有效 PDF；每页为不同纯色，独立验证页序和物理尺寸。
internal static class TestPdf
{
    public static byte[] Create(int pages = 1, double widthMm = 85.6, double heightMm = 53.98)
    {
        var width = (widthMm * 72 / 25.4).ToString("0.########", CultureInfo.InvariantCulture);
        var height = (heightMm * 72 / 25.4).ToString("0.########", CultureInfo.InvariantCulture);
        var objects = new List<string> { "<< /Type /Catalog /Pages 2 0 R >>", "" };
        var kids = new List<string>();
        for(var i=0; i<pages; i++)
        {
            var id = objects.Count+1; kids.Add($"{id} 0 R");
            var content = $"{(i==0 ? "1 0 0" : "0 0 1")} rg 0 0 {width} {height} re f\n";
            objects.Add($"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 {width} {height}] /Resources << >> /Contents {id+1} 0 R >>");
            objects.Add($"<< /Length {Encoding.ASCII.GetByteCount(content)} >>\nstream\n{content}endstream");
        }
        objects[1] = $"<< /Type /Pages /Count {pages} /Kids [{string.Join(" ",kids)}] >>";
        var body = new StringBuilder("%PDF-1.7\n"); var offsets = new List<int> { 0 };
        for(var i=0; i<objects.Count; i++) { offsets.Add(body.Length); body.Append($"{i+1} 0 obj\n{objects[i]}\nendobj\n"); }
        var xref = body.Length; body.Append($"xref\n0 {objects.Count+1}\n0000000000 65535 f \n");
        foreach(var offset in offsets.Skip(1)) body.Append(offset.ToString("D10", CultureInfo.InvariantCulture)+" 00000 n \n");
        body.Append($"trailer\n<< /Size {objects.Count+1} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n");
        return Encoding.ASCII.GetBytes(body.ToString());
    }
}
