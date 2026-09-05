using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Smart.Printing;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class TaskPollerTests : IDisposable
{
    private readonly string directory=Path.Combine(Path.GetTempPath(),"smart-poller-"+Guid.NewGuid());
    [Fact]
    public async Task RestartResumesSameJobAndRetransmitsLostEventWithoutPrintingAgain()
    {
        var server=new Server();var driver=new Adapter();
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
            server.LoseEventReply=true;
            await Assert.ThrowsAsync<HttpRequestException>(()=>poller.StepAsync(CancellationToken.None));
            Assert.Equal(1,driver.Calls);Assert.Single(journal.PendingEvents());
        }
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
            server.LoseEventReply=false;await poller.StepAsync(CancellationToken.None);
            Assert.Empty(journal.PendingEvents());Assert.Equal(server.JobId,server.LastResumeJobId);
        }
        Assert.Equal(1,driver.Calls);Assert.Equal(2,server.EventIds.Count);Assert.Equal(server.EventIds[0],server.EventIds[1]);
        Assert.All(server.AuthorizationValues,value=>Assert.Equal("Bearer "+new string('t',40),value));
    }
    [Fact]
    public async Task ForeignArtifactUrlAndTamperedBytesNeverReachAdapter()
    {
        var server=new Server();var driver=new Adapter();using var journal=new PrintCommandJournal(directory);
        var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
        server.ForeignUrl=true;await Assert.ThrowsAsync<InvalidDataException>(()=>poller.StepAsync(CancellationToken.None));
        Assert.Equal(0,server.Downloads);Assert.Equal(0,driver.Calls);
        server.ForeignUrl=false;server.Tamper=true;await Assert.ThrowsAsync<InvalidDataException>(()=>poller.StepAsync(CancellationToken.None));
        Assert.Equal(1,server.Downloads);Assert.Equal(0,driver.Calls);
    }
    [Fact]
    public void ApiRejectsPlaintextRemoteAndCredentialsInBaseUrl()
    {
        Assert.Throws<InvalidDataException>(()=>new PrintApiClient(new HttpClient(new Server()),new Uri("http://print.example/"),new string('t',40)));
        Assert.Throws<InvalidDataException>(()=>new PrintApiClient(new HttpClient(new Server()),new Uri("https://user:pass@print.example/"),new string('t',40)));
    }
    [Fact]
    public async Task LostClaimResponseReplaysExactDurableRequestAfterRestartAndRenewHasKey()
    {
        var server=new Server {LoseClaimReply=true};var driver=new Adapter();
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
            await Assert.ThrowsAsync<HttpRequestException>(()=>poller.StepAsync(CancellationToken.None));
            Assert.Equal(0,driver.Calls);
        }
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
            await poller.StepAsync(CancellationToken.None);await poller.RenewAsync(CancellationToken.None);
        }
        Assert.Equal(2,server.ClaimKeys.Count);Assert.Equal(server.ClaimKeys[0],server.ClaimKeys[1]);
        Assert.Equal(server.ClaimBodies[0],server.ClaimBodies[1]);Assert.Equal(1,driver.Calls);Assert.Equal(1,server.Renewals);
    }
    [Fact]
    public async Task AutoDuplexAcceptsOnlyCombinedArtifactHeader()
    {
        var server=new Server {AutoDuplex=true};var driver=new Adapter();using var journal=new PrintCommandJournal(directory);
        await new TaskPoller(Api(server),journal,[server.Profile],_=>driver).StepAsync(CancellationToken.None);
        Assert.Equal(1,driver.Calls);Assert.Equal(1,server.Downloads);
    }
    [Fact]
    public async Task TwoJobsNeedExplicitDeviceReleaseAndRestartStartsFreshClaim()
    {
        var server=new Server();var driver=new Adapter();
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);
            await poller.StepAsync(CancellationToken.None);server.Completed=true;
            await poller.StepAsync(CancellationToken.None);Assert.NotNull(journal.LoadClaim(server.Profile.PrinterProfileId));Assert.Equal(1,driver.Calls);
            await new DeviceRecovery(Api(server),journal,_=>{}).ClearAsync(server.Profile,server.JobId,Guid.NewGuid().ToString(),true,CancellationToken.None);
            Assert.Null(journal.LoadClaim(server.Profile.PrinterProfileId));
        }
        server.JobId=Guid.NewGuid().ToString();server.CommandId=Guid.NewGuid().ToString();server.Completed=false;
        using(var journal=new PrintCommandJournal(directory)) {
            await new TaskPoller(Api(server),journal,[server.Profile],_=>driver).StepAsync(CancellationToken.None);
            Assert.Null(server.LastResumeJobId);Assert.Equal(server.JobId,journal.LoadClaim(server.Profile.PrinterProfileId)!.JobId);
        }
        Assert.Equal(2,driver.Calls);
    }
    [Fact]
    public async Task LostRenewResponseReplaysOriginalLeaseAfterRestart()
    {
        var server=new Server();var driver=new Adapter();
        using(var journal=new PrintCommandJournal(directory)) {
            var poller=new TaskPoller(Api(server),journal,[server.Profile],_=>driver);await poller.StepAsync(CancellationToken.None);
            server.LoseRenewReply=true;await Assert.ThrowsAsync<HttpRequestException>(()=>poller.RenewAsync(CancellationToken.None));
        }
        using(var journal=new PrintCommandJournal(directory))await new TaskPoller(Api(server),journal,[server.Profile],_=>driver).StepAsync(CancellationToken.None);
        Assert.Equal(server.RenewKeys[0],server.RenewKeys[1]);Assert.Equal(server.RenewBodies[0],server.RenewBodies[1]);Assert.Equal(1,driver.Calls);
    }
    private static PrintApiClient Api(Server server)=>new(new HttpClient(server),new Uri("https://print.example/platform/"),new string('t',40));
    public void Dispose(){if(Directory.Exists(directory))Directory.Delete(directory,true);}
    private sealed class Adapter:IPrintAdapter
    {public int Calls;public Task<SubmissionResult> SubmitAsync(PrintCommand command,byte[] pdf,CancellationToken token){Calls++;return Task.FromResult(new SubmissionResult(true));}}
    private sealed class Server:HttpMessageHandler
    {
        public string JobId=Guid.NewGuid().ToString(),ClaimId=Guid.NewGuid().ToString(),AttemptId=Guid.NewGuid().ToString(),CommandId=Guid.NewGuid().ToString();
        public readonly LocalPrinterProfile Profile=PrintAdapterTests.Profile() with {PrinterProfileId=Guid.NewGuid().ToString()};
        public readonly byte[] Pdf=TestPdf.Create();public bool LoseEventReply,ForeignUrl,Tamper,LoseClaimReply,AutoDuplex,Completed,LoseRenewReply;public List<string> RenewKeys=[],RenewBodies=[];public List<string> ClaimKeys=[],ClaimBodies=[];public int Renewals;public int Downloads;
        public string? LastResumeJobId;public List<string> EventIds=[];public List<string> AuthorizationValues=[];
        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request,CancellationToken token)
        {
            Assert.Equal("print.example",request.RequestUri!.Host);AuthorizationValues.Add(request.Headers.Authorization!.ToString());
            var path=request.RequestUri.AbsolutePath;
            JsonElement? body=request.Content==null?null:JsonSerializer.Deserialize<JsonElement>(await request.Content.ReadAsStringAsync(token));
            if(path.EndsWith("/claim")){
                Assert.True(request.Headers.Contains("Idempotency-Key"));
                ClaimKeys.Add(request.Headers.GetValues("Idempotency-Key").Single());ClaimBodies.Add(body!.Value.GetRawText());
                LastResumeJobId=body.Value.TryGetProperty("resumeJobId",out var resume)?resume.GetString():null;
                if(LoseClaimReply){LoseClaimReply=false;throw new HttpRequestException("模拟领取已提交但响应丢失");}return Claim();}
            if(path.EndsWith("/renew")){Assert.True(request.Headers.Contains("Idempotency-Key"));Renewals++;RenewKeys.Add(request.Headers.GetValues("Idempotency-Key").Single());RenewBodies.Add(body!.Value.GetRawText());if(LoseRenewReply){LoseRenewReply=false;throw new HttpRequestException("模拟续租响应丢失");}return Claim();}
            if(path.EndsWith("/current"))return Claim();
            if(path.EndsWith("/device-cleared"))return Json(new{cleared=true});
            if(path.EndsWith("/events")){
                var id=body!.Value.GetProperty("eventId").GetString()!;EventIds.Add(id);Assert.Equal(id,request.Headers.GetValues("Idempotency-Key").Single());
                if(LoseEventReply)throw new HttpRequestException("模拟响应丢失");return Json(new{eventAccepted=true,replayed=EventIds.Count>1});
            }
            if(path.EndsWith("/download")){
                Downloads++;var response=new HttpResponseMessage(HttpStatusCode.OK){Content=new ByteArrayContent(Tamper?"changed"u8.ToArray():Pdf)};
                response.Content.Headers.ContentType=new MediaTypeHeaderValue("application/pdf");response.Headers.Add("X-Artifact-Sha256",Hashing.Sha256(Pdf));response.Headers.Add("X-Job-Id",JobId);response.Headers.Add("X-Face",AutoDuplex?"combined":"FRONT");return response;
            }
            throw new InvalidDataException("未允许的测试请求路径");
        }
        private HttpResponseMessage Claim()=>Json(new{claimId=ClaimId,leaseExpiresAt="2050-01-01T00:00:00Z",jobId=JobId,status=Completed?"COMPLETED":"FRONT_IN_PROGRESS",deviceIdentity=Profile.DeviceIdentity,printerProfileId=Profile.PrinterProfileId,printMode=AutoDuplex?"AUTO_DUPLEX":"MANUAL_DUPLEX",templateSnapshotHash="sha256:"+new string('a',64),printerSnapshotHash=Profile.PrinterSnapshotHash,
            action=new{type=Completed?"NONE":AutoDuplex?"PRINT_BOTH":"PRINT_FRONT",face=AutoDuplex?"BOTH":"FRONT",attemptId=AttemptId,commandId=CommandId,artifact=new{downloadPath=ForeignUrl?"https://evil.example/pdf":$"/api/print-client/v1/jobs/{JobId}/artifacts/{(AutoDuplex?"combined":"FRONT")}/download",sha256=Hashing.Sha256(Pdf),pageWidthMm=85.6,pageHeightMm=53.98,pageCount=AutoDuplex?2:1}}});
        private static HttpResponseMessage Json(object data)=>new(HttpStatusCode.OK){Content=new StringContent(JsonSerializer.Serialize(new{data},Hashing.Json),Encoding.UTF8,"application/json")};
    }
}
