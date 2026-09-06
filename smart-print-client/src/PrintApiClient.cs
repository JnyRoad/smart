using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json.Nodes;
namespace Smart.Printing;

// 所有请求固定在一个已配置服务下，设备凭据不用于浏览器登录或任意返回地址。
public sealed class PrintApiClient
{
    private readonly HttpClient http;
    private readonly Uri endpoint;
    private readonly string token;
    public PrintApiClient(HttpClient http,Uri endpoint,string token)
    {
        if(!endpoint.IsAbsoluteUri || endpoint.Scheme!="https" || endpoint.UserInfo.Length!=0 || endpoint.Query.Length!=0 || endpoint.Fragment.Length!=0
            || token.Length<32 || token.Any(char.IsWhiteSpace)) throw new InvalidDataException("需要明确的 HTTPS 服务地址与独立设备凭据");
        this.http=http;this.endpoint=new Uri(endpoint.AbsoluteUri.TrimEnd('/')+"/");this.token=token;
    }
    public static HttpClient CreateTransport()=>new(new HttpClientHandler {AllowAutoRedirect=false,UseCookies=false}) {Timeout=TimeSpan.FromSeconds(30)};
    public async Task<JsonObject?> JsonAsync(HttpMethod method,string path,JsonNode? data,CancellationToken cancellation,string? idempotencyKey=null)
    {
        using var request=Request(method,path);
        if(data!=null)request.Content=new StringContent(data.ToJsonString(Hashing.Json),Encoding.UTF8,"application/json");
        if(idempotencyKey!=null)request.Headers.Add("Idempotency-Key",idempotencyKey);
        using var response=await http.SendAsync(request,HttpCompletionOption.ResponseHeadersRead,cancellation);
        RequireSuccess(response);
        if(response.StatusCode==HttpStatusCode.NoContent)return null;
        if(response.Content.Headers.ContentType?.MediaType!="application/json")throw new InvalidDataException("打印服务响应类型错误");
        var bytes=await ReadLimited(response.Content,2*1024*1024,cancellation);
        var result=JsonNode.Parse(bytes) as JsonObject??throw new InvalidDataException("打印服务响应格式错误");
        return result["data"] as JsonObject??throw new InvalidDataException("打印服务响应缺少数据");
    }
    public async Task<byte[]> ArtifactAsync(PrintCommand command,string suppliedPath,CancellationToken cancellation)
    {
        if(!Guid.TryParseExact(command.JobId,"D",out _) || command.Face is not ("FRONT" or "BACK" or "BOTH"))throw new InvalidDataException("制品任务标识无效");
        var face=command.Face=="BOTH"?"combined":command.Face;
        var expected=$"/api/print-client/v1/jobs/{command.JobId}/artifacts/{face}/download";
        if(suppliedPath!=expected)throw new InvalidDataException("制品地址不属于当前冻结任务");
        using var request=Request(HttpMethod.Get,expected);using var response=await http.SendAsync(request,HttpCompletionOption.ResponseHeadersRead,cancellation);
        RequireSuccess(response);
        if(response.Content.Headers.ContentType?.MediaType!="application/pdf" || Header(response,"X-Artifact-Sha256")!=command.ArtifactHash
            || Header(response,"X-Job-Id")!=command.JobId || Header(response,"X-Face")!=face)throw new InvalidDataException("制品响应身份校验失败");
        var bytes=await ReadLimited(response.Content,32*1024*1024,cancellation);
        if(Hashing.Sha256(bytes)!=command.ArtifactHash)throw new InvalidDataException("制品内容hash不匹配");
        return bytes;
    }
    private HttpRequestMessage Request(HttpMethod method,string path)
    {
        if(!path.StartsWith("/api/print-client/v1/",StringComparison.Ordinal) || path.Contains("..") || path.Contains('%') || path.Contains('?') || path.Contains('#') || path.Contains('\\'))
            throw new InvalidDataException("客户端请求路径不合规");
        var request=new HttpRequestMessage(method,new Uri(endpoint,path.TrimStart('/')));
        request.Headers.Authorization=new AuthenticationHeaderValue("Bearer",token);request.Headers.Add("X-Request-Id",Guid.NewGuid().ToString());return request;
    }
    private static string? Header(HttpResponseMessage response,string name)=>response.Headers.TryGetValues(name,out var values)?values.SingleOrDefault():null;
    private static void RequireSuccess(HttpResponseMessage response)
    {
        if(!response.IsSuccessStatusCode)throw new HttpRequestException("打印服务请求未成功，HTTP "+(int)response.StatusCode,null,response.StatusCode);
    }
    private static async Task<byte[]> ReadLimited(HttpContent content,int limit,CancellationToken token)
    {
        if(content.Headers.ContentLength>limit)throw new InvalidDataException("响应文件过大");
        using var input=await content.ReadAsStreamAsync(token);using var output=new MemoryStream();var buffer=new byte[8192];int read;
        while((read=await input.ReadAsync(buffer,token))>0){if(output.Length+read>limit)throw new InvalidDataException("响应文件超限");await output.WriteAsync(buffer.AsMemory(0,read),token);}
        if(content.Headers.ContentLength.HasValue && content.Headers.ContentLength!=output.Length)throw new InvalidDataException("响应文件长度不匹配");
        return output.ToArray();
    }
}
