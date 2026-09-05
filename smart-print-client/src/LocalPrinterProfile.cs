namespace Smart.Printing;

// 本机白名单必须与平台冻结档案 hash 对应；修改校准后应重新登记平台档案并重新验收。
public sealed record LocalPrinterProfile
{
    public string PrinterProfileId {get;init;}="";
    public string DeviceIdentity {get;init;}="";
    public string PrinterSnapshotHash {get;init;}="";
    public string Manufacturer {get;init;}="";
    public string Model {get;init;}="";
    public string WindowsPrinterName {get;init;}="";
    public string WindowsDriverName {get;init;}="";
    public string WindowsPortName {get;init;}="";
    public string[] AllowedPrintModes {get;init;}=[];
    public double MediaWidthMm {get;init;}
    public double MediaHeightMm {get;init;}
    public int Dpi {get;init;}
    public bool CalibrationVerified {get;init;}
    public bool AutoDuplexVerified {get;init;}
    public string DuplexEdge {get;init;}="";
    public int PaperRawKind {get;init;}
    public string PaperName {get;init;}="";
    public bool Landscape {get;init;}
    public double OffsetXMm {get;init;}
    public double OffsetYMm {get;init;}
    public int FrontRotation {get;init;}
    public int BackRotation {get;init;}
    public string ColorMode {get;init;}="MONO";
    public bool BlackRedVerified {get;init;}
    public int BpacMediaId {get;init;}
    public string LbxPath {get;init;}="";
    public string LbxHash {get;init;}="";
    public string LbxObjectName {get;init;}="PageImage";
    public string TempDirectory {get;init;}="";
    public void ValidateCommand(PrintCommand command)
    {
        if(command.PrinterProfileId!=PrinterProfileId || command.DeviceIdentity!=DeviceIdentity || command.PrinterSnapshotHash!=PrinterSnapshotHash
            || !AllowedPrintModes.Contains(command.PrintMode) || !Hashing.IsHash(PrinterSnapshotHash) || !CalibrationVerified
            || string.IsNullOrWhiteSpace(WindowsPrinterName) || string.IsNullOrWhiteSpace(WindowsDriverName) || string.IsNullOrWhiteSpace(WindowsPortName)
            || !double.IsFinite(MediaWidthMm) || !double.IsFinite(MediaHeightMm) || MediaWidthMm<=0 || MediaHeightMm<=0
            || Math.Abs(command.PageWidthMm-MediaWidthMm)>0.01 || Math.Abs(command.PageHeightMm-MediaHeightMm)>0.01
            || !double.IsFinite(OffsetXMm) || !double.IsFinite(OffsetYMm) || Math.Abs(OffsetXMm)>5 || Math.Abs(OffsetYMm)>5
            || FrontRotation is not (0 or 180) || BackRotation is not (0 or 180) || Dpi is < 150 or > 600)
            throw new PrintNotSubmittedException("设备身份、介质或校准未通过校验");
    }
}
public sealed record PrintBatch(string JobName, string Face, string DuplexEdge, IReadOnlyList<RasterPage> Pages);
public interface IWindowsPrintDriver { SubmissionResult Submit(LocalPrinterProfile profile, PrintBatch batch); }
