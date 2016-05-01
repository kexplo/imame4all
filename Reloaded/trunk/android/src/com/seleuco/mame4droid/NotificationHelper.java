package com.seleuco.mame4droid;

import android.app.NotificationManager;
import android.content.Context;
import android.app.Notification;
import android.content.Intent;
import android.app.PendingIntent;

import com.kexplo.mame4droid.R;

final class NotificationHelper
{
	private static NotificationManager notificationManager = null;

	public static void addNotification(Context ctx, String onShow, String title, String message)
	{
		if(notificationManager == null)
		{
			notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		int icon = R.drawable.icon_sb; // TODO: don't hard-code
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, /*onShow*/null, when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
		CharSequence contentTitle = title;
		CharSequence contentText = message;
		Intent notificationIntent = new Intent(ctx, MAME4droid.class);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);

		//notification.setLatestEventInfo(ctx, contentTitle, contentText, contentIntent);
		notificationManager.notify(1, notification);
	}

	public static void removeNotification()
	{
		if(notificationManager != null)
			notificationManager.cancel(1);
	}
}
