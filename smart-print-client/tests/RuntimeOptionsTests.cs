using Smart.Printing;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class RuntimeOptionsTests
{
    [Fact]
    public void OnePhysicalQueueCannotBeClaimedAsTwoPrinterProfiles()
    {
        var first=PrintAdapterTests.Profile() with {PrinterProfileId=Guid.NewGuid().ToString()};
        var options=new RuntimeOptions {Endpoint="https://print.example/platform/",DeviceIdentity=first.DeviceIdentity,ExecutionEnabled=true,Printers=[first,first with {PrinterProfileId=Guid.NewGuid().ToString()}]};
        Assert.Throws<InvalidDataException>(()=>options.Validate());
    }
    [Fact]
    public void EnabledRuntimeRequiresVerifiedProfileAndMatchingDeviceIdentity()
    {
        var first=PrintAdapterTests.Profile() with {PrinterProfileId=Guid.NewGuid().ToString(),CalibrationVerified=false};
        var options=new RuntimeOptions {Endpoint="https://print.example/",DeviceIdentity=first.DeviceIdentity,ExecutionEnabled=true,Printers=[first]};
        Assert.Throws<InvalidDataException>(()=>options.Validate());
        options=options with {ExecutionEnabled=false};options.Validate();
        Assert.Throws<InvalidDataException>(()=>(options with {DeviceIdentity="different"}).Validate());
    }
}
