package at.smartshopper.smartshopper.messaging;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;


import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.shoppinglist.Member;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class MyFirebaseSender {

    private List<String> messageIds;

    public MyFirebaseSender(List<Member> members) {
        ArrayList<String> tmp = new ArrayList<>();
        for (Member m : members) {
            tmp.add(m.getMsid());
        }
        messageIds = tmp;
    }


    /**
     * Sendet die Firebase Messages zum server
     *
     * @param message Push Nachricht
     * @param action  Push action
     */
    public void sendMessage(String message, String action) {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        for (int i = 0; messageIds.size() > i; i++) {

            try {
                firebaseMessaging.send(new RemoteMessage.Builder(R.string.firebase_sender_id + "@fcm.googleapis.com/fcm/")
                        .setMessageId(messageIds.get(i))
                        .addData("my_message", message)
                        .addData("LoginActivity", action)
                        .build());

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("https://www.smartshopper.cf/push/" + messageIds.get(i));

                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    Log.d(R.string.StringTag + "", "Response Push Post" + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
