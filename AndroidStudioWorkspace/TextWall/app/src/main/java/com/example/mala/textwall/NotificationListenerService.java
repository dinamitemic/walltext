package com.example.mala.textwall;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

/**
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Notification notif = sbn.getNotification();
        String notif_cat = notif.category;
        Bundle notif_bundle = notif.extras;
        String notif_title = notif_bundle.getString("android.title");
        String notif_message = notif_bundle.getString("android.text");
        String notif_key = sbn.getKey();
        Intent intent = new Intent("com.github.chagall.notificationlistenerexample");
        Bundle n = new Bundle();
        n.putString("Category", notif_cat);
        n.putString("Title", notif_title);
        n.putString("Message", notif_message);
        n.putString("Key", notif_key);
        intent.putExtras(n);
        sendBroadcast(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Intent intent = new Intent("com.github.chagall.notificationlistenerexample");
        sendBroadcast(intent);
    }

}
