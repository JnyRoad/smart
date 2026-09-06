using Smart.Printing;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class JournalOutboxTests : IDisposable
{
    private readonly string directory=Path.Combine(Path.GetTempPath(),"smart-outbox-"+Guid.NewGuid());
    [Fact]
    public void UnacknowledgedEventsAndSequenceSurviveRestartAndRetirementNeverReopensCommand()
    {
        var command=PrintAdapterTests.Command(TestPdf.Create()); PrintClientEvent first;
        using(var journal=new PrintCommandJournal(directory)) {
            journal.RecordIntent(command); journal.MarkSubmissionStarted(command.CommandId);
            first=journal.RecordResult(command.CommandId,"OUTPUT_UNKNOWN"); Assert.Single(journal.PendingEvents()); Assert.True(first.ClientSequence>0);
        }
        using(var journal=new PrintCommandJournal(directory)) {
            Assert.Equal(first.EventId,journal.PendingEvents().Single().EventId);
            journal.Acknowledge(first.CommandId,first.EventId); Assert.Empty(journal.PendingEvents());
            var retired=journal.Retire(command.CommandId,true,true); Assert.True(retired.ClientSequence>first.ClientSequence);
            Assert.Equal(retired.EventId,journal.Retire(command.CommandId,true,true).EventId); Assert.Equal("RETIRED",journal.Find(command.CommandId)!.State);
        }
        using(var journal=new PrintCommandJournal(directory)) {Assert.Equal("COMMAND_RETIRED",journal.PendingEvents().Single().EventType);Assert.Equal("RETIRED",journal.Find(command.CommandId)!.State);}
    }
    [Fact]
    public void RetirementNeedsPhysicalEvidenceAndAckCannotInventEvent()
    {
        var command=PrintAdapterTests.Command(TestPdf.Create()); using var journal=new PrintCommandJournal(directory);journal.RecordIntent(command);
        Assert.Throws<InvalidDataException>(()=>journal.Retire(command.CommandId,false,true));
        Assert.Throws<InvalidDataException>(()=>journal.Retire(command.CommandId,true,false));
        Assert.Throws<InvalidDataException>(()=>journal.Acknowledge(command.CommandId,Guid.NewGuid().ToString()));
        Assert.Equal("INTENT_RECORDED",journal.Find(command.CommandId)!.State);
    }
    [Fact]
    public void AcknowledgedRecordsAreArchivedAndCorruptSiblingDoesNotBlockPendingEvents()
    {
        var archived=PrintAdapterTests.Command(TestPdf.Create());var pending=PrintAdapterTests.Command(TestPdf.Create());
        using var journal=new PrintCommandJournal(directory);
        journal.RecordIntent(archived);journal.MarkSubmissionStarted(archived.CommandId);var result=journal.RecordResult(archived.CommandId,"DEVICE_ACCEPTED");journal.Acknowledge(archived.CommandId,result.EventId);
        Assert.True(File.Exists(Path.Combine(directory,"archive",archived.CommandId+".json")));Assert.NotNull(journal.Find(archived.CommandId));
        journal.RecordIntent(pending);journal.MarkSubmissionStarted(pending.CommandId);var waiting=journal.RecordResult(pending.CommandId,"OUTPUT_UNKNOWN");
        File.WriteAllText(Path.Combine(directory,Guid.NewGuid()+".json"),"损坏的其他记录");
        Assert.Equal(waiting.EventId,journal.PendingEvents().Single().EventId);
    }
    [Fact]
    public void RestartArchivesAcknowledgedRecordLeftByInterruptedCompaction()
    {
        var command=PrintAdapterTests.Command(TestPdf.Create());var active=Path.Combine(directory,command.CommandId+".json");var archived=Path.Combine(directory,"archive",command.CommandId+".json");
        using(var journal=new PrintCommandJournal(directory)) {
            journal.RecordIntent(command);journal.MarkSubmissionStarted(command.CommandId);var result=journal.RecordResult(command.CommandId,"DEVICE_ACCEPTED");journal.Acknowledge(command.CommandId,result.EventId);
            File.Move(archived,active); // 模拟 ACK 已落盘、归档移动前工作站异常退出。
        }
        using var reopened=new PrintCommandJournal(directory);
        Assert.False(File.Exists(active));Assert.True(File.Exists(archived));Assert.NotNull(reopened.Find(command.CommandId));
    }
    [Fact]
    public void PrivateDirectoryRejectsSymlinkAndProtectsLogsOnUnix()
    {
        if(OperatingSystem.IsWindows()) return;
        PrivateDirectory.Ensure(directory);
        Assert.Equal(UnixFileMode.UserRead|UnixFileMode.UserWrite|UnixFileMode.UserExecute,File.GetUnixFileMode(directory));
        var link=directory+"-link";
        try {Directory.CreateSymbolicLink(link,directory);Assert.Throws<IOException>(()=>PrivateDirectory.Ensure(Path.Combine(link,"child")));}
        finally {if(Directory.Exists(link)) Directory.Delete(link);}
    }
    public void Dispose(){if(Directory.Exists(directory))Directory.Delete(directory,true);}
}
