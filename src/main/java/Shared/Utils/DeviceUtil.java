package Shared.Utils;

public class DeviceUtil {
    public static String getDeviceInfo() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        String userName = System.getProperty("user.name");
        return "OS: " + osName + " " + osVersion + " (" + osArch + "), User: "+userName;
    }
}
