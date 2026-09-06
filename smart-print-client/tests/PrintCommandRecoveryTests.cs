using Smart.Printing;
using Xunit;
namespace Smart.Printing.Tests;

// 使用真实本地持久日志，驱动替身用于统计实际提交次数及制造断线。
public sealed class PrintCommandRecoveryTests : IDisposable
{
    private readonly string directory = Path.Combine(Path.GetTempPath(), "smart-print-recovery-" + Guid.NewGuid());
    private static readonly byte[] Pdf = "%PDF-1.7\nsynthetic-test"u8.ToArray();
    private static PrintCommand Command() => new(Guid.NewGuid().ToString(), Guid.NewGuid().ToString(), Guid.NewGuid().ToString(), "printer-1", "device-1", "MANUAL_DUPLEX", "FRONT", Hashing.Sha256(Pdf), "sha256:" + new string('a',64), "sha256:" + new string('b',64), 85.6, 53.98, 1);
    private static PrinterBinding Binding() => new("printer-1", "device-1", "sha256:" + new string('b',64), ["MANUAL_DUPLEX"]);
    [Fact]
    public async Task DuplicateCommandAfterReopenDoesNotSubmitAgain()
    {
        var command = Command(); var adapter = new Adapter();
        using(var journal = new PrintCommandJournal(directory))
        {
            var runner = new PrintCommandProcessor(journal, adapter, Binding());
            var first = await runner.ExecuteAsync(command, Pdf, CancellationToken.None);
            Assert.Equal("DEVICE_ACCEPTED", first.EventType);
        }
        using(var journal = new PrintCommandJournal(directory))
        {
            var replay = await new PrintCommandProcessor(journal, adapter, Binding()).ExecuteAsync(command, Pdf, CancellationToken.None);
            Assert.Equal("DEVICE_ACCEPTED", replay.EventType);
        }
        Assert.Equal(1, adapter.Submissions);
    }
    [Fact]
    public async Task CrashAfterSubmissionMarkerBecomesUnknownAndNeverReplays()
    {
        var command = Command();
        using(var journal = new PrintCommandJournal(directory)) { journal.RecordIntent(command); journal.MarkSubmissionStarted(command.CommandId); }
        var adapter = new Adapter();
        using var recovered = new PrintCommandJournal(directory);
        var runner = new PrintCommandProcessor(recovered, adapter, Binding());
        var first = await runner.ExecuteAsync(command, Pdf, CancellationToken.None);
        var again = await runner.ExecuteAsync(command, Pdf, CancellationToken.None);
        Assert.Equal("OUTPUT_UNKNOWN", first.EventType); Assert.Equal(first.EventId, again.EventId); Assert.Equal(0, adapter.Submissions);
    }
    [Fact]
    public async Task SubmittedButReplyLostStaysUnknownAcrossRestart()
    {
        var command = Command(); var adapter = new Adapter { ThrowAfterSubmission = true };
        using(var journal = new PrintCommandJournal(directory))
            Assert.Equal("OUTPUT_UNKNOWN", (await new PrintCommandProcessor(journal, adapter, Binding()).ExecuteAsync(command, Pdf, CancellationToken.None)).EventType);
        using(var journal = new PrintCommandJournal(directory))
            Assert.Equal("OUTPUT_UNKNOWN", (await new PrintCommandProcessor(journal, adapter, Binding()).ExecuteAsync(command, Pdf, CancellationToken.None)).EventType);
        Assert.Equal(1, adapter.Submissions);
    }
    [Fact]
    public async Task NativeSubmissionTimeoutRecordsUnknownThenStopsTheCommand()
    {
        var command=Command();using var journal=new PrintCommandJournal(directory);
        await Assert.ThrowsAsync<PrintSubmissionTimeoutException>(()=>new PrintCommandProcessor(journal,new TimeoutAdapter(),Binding()).ExecuteAsync(command,Pdf,CancellationToken.None));
        Assert.Equal("OUTPUT_UNKNOWN",journal.Find(command.CommandId)!.Result!.EventType);
    }
    [Fact]
    public async Task ChangedCommandBodyOrArtifactCannotReuseSubmission()
    {
        var command = Command(); var adapter = new Adapter(); using var journal = new PrintCommandJournal(directory);
        var runner = new PrintCommandProcessor(journal, adapter, Binding());
        await runner.ExecuteAsync(command, Pdf, CancellationToken.None);
        await Assert.ThrowsAsync<InvalidDataException>(() => runner.ExecuteAsync(command with { Face = "BACK" }, Pdf, CancellationToken.None));
        await Assert.ThrowsAsync<InvalidDataException>(() => runner.ExecuteAsync(Command(), "tampered"u8.ToArray(), CancellationToken.None));
        Assert.Equal(1, adapter.Submissions);
    }
    [Fact]
    public async Task WrongDeviceProfileOrDuplexNeverReachesAdapter()
    {
        var command = Command(); var adapter = new Adapter(); using var journal = new PrintCommandJournal(directory);
        var runner = new PrintCommandProcessor(journal, adapter, Binding());
        await Assert.ThrowsAsync<InvalidDataException>(() => runner.ExecuteAsync(command with { DeviceIdentity = "other" }, Pdf, CancellationToken.None));
        await Assert.ThrowsAsync<InvalidDataException>(() => runner.ExecuteAsync(command with { PrinterSnapshotHash = "sha256:" + new string('c',64) }, Pdf, CancellationToken.None));
        await Assert.ThrowsAsync<InvalidDataException>(() => runner.ExecuteAsync(command with { PrintMode = "AUTO_DUPLEX", Face = "BOTH", PageCount = 2 }, Pdf, CancellationToken.None));
        Assert.Equal(0, adapter.Submissions);
    }
    [Fact]
    public void SecondProcessCannotUseSameJournalAndCorruptionIsNotReset()
    {
        var command = Command(); using(var journal = new PrintCommandJournal(directory))
        {
            Assert.Throws<IOException>(() => new PrintCommandJournal(directory)); journal.RecordIntent(command);
        }
        File.WriteAllText(Path.Combine(directory, command.CommandId + ".json"), "broken");
        using var reopened = new PrintCommandJournal(directory);
        Assert.Throws<InvalidDataException>(() => reopened.Find(command.CommandId));
    }
    [Fact]
    public async Task ConcurrentDuplicatesSubmitOnlyOnceAndMarkerExistsBeforeDriverCall()
    {
        var command = Command(); using var journal = new PrintCommandJournal(directory); var adapter = new Adapter();
        adapter.BeforeSubmit = () => Assert.Equal("SUBMISSION_STARTED", journal.Find(command.CommandId)!.State);
        var runner = new PrintCommandProcessor(journal, adapter, Binding());
        await Task.WhenAll(Enumerable.Range(0,8).Select(_ => runner.ExecuteAsync(command, Pdf, CancellationToken.None)));
        Assert.Equal(1, adapter.Submissions);
    }
    public void Dispose() { if(Directory.Exists(directory)) Directory.Delete(directory, true); }
    private sealed class Adapter : IPrintAdapter
    {
        public int Submissions; public bool ThrowAfterSubmission; public Action? BeforeSubmit;
        public Task<SubmissionResult> SubmitAsync(PrintCommand command, byte[] pdf, CancellationToken token)
        {
            BeforeSubmit?.Invoke(); Interlocked.Increment(ref Submissions);
            if(ThrowAfterSubmission) throw new IOException("模拟驱动回执丢失");
            return Task.FromResult(new SubmissionResult(true, "synthetic-driver-job"));
        }
    }
    private sealed class TimeoutAdapter : IPrintAdapter
    {
        public Task<SubmissionResult> SubmitAsync(PrintCommand command, byte[] pdf, CancellationToken token) => Task.FromException<SubmissionResult>(new PrintSubmissionTimeoutException("模拟原生提交超时"));
    }
}
