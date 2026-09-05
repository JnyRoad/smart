using System.Net;
using System.Text;
using System.Text.Json;
using Smart.Printing;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class DeviceRecoveryTests : IDisposable
{
    private readonly string directory=Path.Combine(Path.GetTempPath(),"smart-clear-"+Guid.NewGuid());
    [Fact]
    public async Task OtherQueueCannotClearOriginalJobAndLostReplyKeepsOriginalBinding()
    {
        var a=PrintAdapterTests.Profile() with {PrinterProfileId=Guid.NewGuid().ToString()};
        var b=a with {PrinterProfileId=Guid.NewGuid().ToString(),WindowsPrinterName="Other queue",WindowsPortName="USB002"};
        var job=Guid.NewGuid().ToString();var check=Guid.NewGuid().ToString();var server=new Server {LoseReply=true};var queues=new List<string>();
        using(var journal=new PrintCommandJournal(directory)) {
            journal.SaveClaim(new ClaimState(a.PrinterProfileId,Guid.NewGuid().ToString(),job,"2050-01-01T00:00:00Z",Guid.NewGuid().ToString(),a.DeviceIdentity,a.PrinterSnapshotHash));
            var recovery=new DeviceRecovery(Api(server),journal,p=>queues.Add(p.WindowsPrinterName));
            await Assert.ThrowsAsync<InvalidDataException>(()=>recovery.ClearAsync(b,job,check,true,CancellationToken.None));
            Assert.Empty(queues);Assert.Empty(server.Keys);
            await Assert.ThrowsAsync<HttpRequestException>(()=>recovery.ClearAsync(a,job,check,true,CancellationToken.None));
            Assert.NotNull(journal.LoadClaim(a.PrinterProfileId));
        }
        using(var journal=new PrintCommandJournal(directory)) {
            await new DeviceRecovery(Api(server),journal,p=>queues.Add(p.WindowsPrinterName)).ClearAsync(a,job,check,true,CancellationToken.None);
            Assert.Null(journal.LoadClaim(a.PrinterProfileId));
        }
        Assert.All(queues,name=>Assert.Equal(a.WindowsPrinterName,name));Assert.Equal(server.Keys[0],server.Keys[1]);Assert.Equal(server.Bodies[0],server.Bodies[1]);
    }
    [Fact]
    public async Task NonReleasedReplyKeepsClaimAndDoesNotPermitNewJob()
    {
        var profile=PrintAdapterTests.Profile() with {PrinterProfileId=Guid.NewGuid().ToString()};var job=Guid.NewGuid().ToString();
        using var journal=new PrintCommandJournal(directory);
        journal.SaveClaim(new ClaimState(profile.PrinterProfileId,Guid.NewGuid().ToString(),job,"2050-01-01T00:00:00Z",Guid.NewGuid().ToString(),profile.DeviceIdentity,profile.PrinterSnapshotHash));
        var server=new Server {Cleared=false};
        await Assert.ThrowsAsync<InvalidDataException>(()=>new DeviceRecovery(Api(server),journal,_=>{}).ClearAsync(profile,job,Guid.NewGuid().ToString(),true,CancellationToken.None));
        Assert.Equal(job,journal.LoadClaim(profile.PrinterProfileId)!.JobId);
        server.Cleared=true;
        await new DeviceRecovery(Api(server),journal,_=>{}).ClearAsync(profile,job,Guid.NewGuid().ToString(),true,CancellationToken.None);
        Assert.Null(journal.LoadClaim(profile.PrinterProfileId));Assert.NotEqual(server.Keys[0],server.Keys[1]);Assert.NotEqual(server.Bodies[0],server.Bodies[1]);
    }
    private static PrintApiClient Api(Server server)=>new(new HttpClient(server),new Uri("https://print.example/platform/"),new string('t',40));
    private sealed class Server:HttpMessageHandler
    {
        public bool LoseReply,Cleared=true;public List<string> Keys=[],Bodies=[];
        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request,CancellationToken token)
        {
            Keys.Add(request.Headers.GetValues("Idempotency-Key").Single());Bodies.Add(await request.Content!.ReadAsStringAsync(token));
            if(LoseReply){LoseReply=false;throw new HttpRequestException("模拟清空已提交但响应丢失");}
            return new(HttpStatusCode.OK){Content=new StringContent(JsonSerializer.Serialize(new {data=new {cleared=Cleared}}),Encoding.UTF8,"application/json")};
        }
    }
    public void Dispose(){if(Directory.Exists(directory))Directory.Delete(directory,true);}
}
