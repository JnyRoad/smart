using System.Security.AccessControl;
using System.Security.Principal;
using System.Runtime.Versioning;
namespace Smart.Printing;

public static class PrivateDirectory
{
    public static string Ensure(string path)
    {
        var full=Path.GetFullPath(path);
        // macOS 的系统 /var、/tmp 是固定别名；先收敛系统别名，再拒绝应用路径中的链接。
        if(OperatingSystem.IsMacOS())
            foreach(var alias in new[] {"/var/","/tmp/"})
                if(full.StartsWith(alias,StringComparison.Ordinal)) full="/private"+full;
        if(full==Path.GetPathRoot(full)) throw new IOException("不能使用磁盘根目录存放打印记录");
        for(var parent=new DirectoryInfo(full);parent!=null;parent=parent.Parent)
            if(parent.Exists&&(parent.Attributes&FileAttributes.ReparsePoint)!=0) throw new IOException("受控目录路径不能包含符号链接");
        Directory.CreateDirectory(full);
        if(OperatingSystem.IsWindows()) SecureWindows(full);
        else File.SetUnixFileMode(full,UnixFileMode.UserRead|UnixFileMode.UserWrite|UnixFileMode.UserExecute);
        return full;
    }
    [SupportedOSPlatform("windows")]
    private static void SecureWindows(string path)
    {
        var identity=WindowsIdentity.GetCurrent().User??throw new IOException("无法取得打印工作站运行身份");
        var acl=new DirectorySecurity();acl.SetAccessRuleProtection(true,false);acl.SetOwner(identity);
        foreach(var sid in new[] {identity,new SecurityIdentifier(WellKnownSidType.LocalSystemSid,null),new SecurityIdentifier(WellKnownSidType.BuiltinAdministratorsSid,null)})
            acl.AddAccessRule(new FileSystemAccessRule(sid,FileSystemRights.FullControl,InheritanceFlags.ContainerInherit|InheritanceFlags.ObjectInherit,PropagationFlags.None,AccessControlType.Allow));
        new DirectoryInfo(path).SetAccessControl(acl);
    }
}
