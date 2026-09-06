using Smart.Printing;
using Smart.Printing.Adapters;
using Xunit;
namespace Smart.Printing.Tests;

public sealed class WindowsDriverBoundaryTests
{
    [Fact]
    public void NativeDriverRefusesUnverifiedProfileBeforeAnyWindowsCall()
    {
        Assert.Throws<PrintNotSubmittedException>(()=>new BpacPrintDriver().Submit(PrintAdapterTests.Profile() with {CalibrationVerified=false},"smart-test",[],[]));
        var driver=new WindowsPrintDriver();
        Assert.Throws<PrintNotSubmittedException>(()=>driver.Submit(PrintAdapterTests.Profile() with {CalibrationVerified=false},
            new PrintBatch("smart-test","FRONT","SIMPLEX",[])));
    }
}
