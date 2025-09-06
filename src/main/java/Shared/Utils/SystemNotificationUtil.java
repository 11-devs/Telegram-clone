package Shared.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * A utility class to display system tray notifications.
 * It uses java.awt.SystemTray for native OS integration.
 */
public class SystemNotificationUtil {

    private static TrayIcon trayIcon;
    private static boolean isInitialized = false;

    /**
     * Initializes the SystemTray icon. This is done lazily on the first notification request.
     */
    private static void initialize() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported on this platform!");
            isInitialized = false;
            return;
        }
        if (isInitialized) {
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            // Load the application's logo for the tray icon
            Image image = ImageIO.read(Objects.requireNonNull(SystemNotificationUtil.class.getResource("/Client/images/TelegramLogo.png")));

            trayIcon = new TrayIcon(image, "Telegram");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Telegram");
            tray.add(trayIcon);
            isInitialized = true;
        } catch (IOException | AWTException e) {
            System.err.println("TrayIcon could not be added or tray icon image could not be loaded.");
            e.printStackTrace();
            isInitialized = false;
        }
    }

    /**
     * Displays a system notification.
     *
     * @param title   The title of the notification (e.g., sender's name).
     * @param message The content of the notification (e.g., message text).
     */
    public static void showNotification(String title, String message) {
        // Ensure initialization is done on the first call
        if (!isInitialized) {
            initialize();
        }

        if (isInitialized && trayIcon != null) {
            // Display the notification via the AWT tray icon
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } else {
            // Fallback for systems where tray is not supported or failed to initialize
            System.out.println("SYSTEM NOTIFICATION: [" + title + "] " + message);
        }
    }
}