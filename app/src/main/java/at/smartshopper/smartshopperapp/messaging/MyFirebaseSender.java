package at.smartshopper.smartshopperapp.messaging;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopperapp.shoppinglist.Member;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class MyFirebaseSender {

    private List<String> messageIds;

    public MyFirebaseSender(List<Member> members) {
        ArrayList<String> tmp = new ArrayList<>();
        for (Member m : members) {
            tmp.add(m.getMsid());
        }
        messageIds = tmp;
    }

    public void addMember(Member member) {
        if (!messageIds.contains(member.getMsid())) {
            messageIds.add(member.getMsid());
        }
    }


    /**
     * Sendet die Firebase Messages zum server
     *
     * @param message Push Nachricht
     * @param title   Push title
     */
    public void sendMessage(String message, String title) {
        message = message.replace(" ", "%20");
        title = title.replace(" ", "%20");
        //FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        for (int i = 0; messageIds.size() > i; i++) {

            try {

                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("https://www.smartshopper.cf/push/" + messageIds.get(i) + "/" + message + "/" + title);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httpGet);
                Log.d("SmartShopper", "Message ID: " + messageIds.get(i));
                Log.d("SmartShopper", "Response Push Post: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
