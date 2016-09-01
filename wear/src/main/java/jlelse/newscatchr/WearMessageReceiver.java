/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import jlelse.readit.R;

public class WearMessageReceiver extends WearableListenerService {

    private String title = "";
    private String content = "";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        System.out.println("Message received");
        if (messageEvent.getPath().contains("newscatchr")) {
            String message = new String(messageEvent.getData());
            try {
                title = message.split("x_x_x")[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                content = message.split("x_x_x")[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            Notification.Builder b = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.icon)
                    .setLocalOnly(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, b.build());
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}