package emp.fri.si.instarun;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Helper class for showing and canceling recording
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class RecordingNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Recording";

    public static Notification build(final Context context){
        final Resources res = context.getResources();

        final String ticker = "Recording";
        final String title = "Instarun";
        final String text = TrackRecorderService.isTracking()
                ? "Recording new run"
                : "Ready to start recording";

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.FLAG_ONGOING_EVENT)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_run)
                .setLargeIcon(BitmapFactory.decodeResource(InstarunApp.getContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(text)

                .setTicker(ticker)

                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, RecordActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT));

        if (TrackRecorderService.isTracking())
            builder.setUsesChronometer(true);

        return builder.build();
    }

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     */
    public static void notify(final Context context) {
        notify(context, build(context));
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}
