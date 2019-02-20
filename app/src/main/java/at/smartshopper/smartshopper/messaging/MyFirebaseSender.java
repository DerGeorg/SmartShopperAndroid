package at.smartshopper.smartshopper.messaging;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.shoppinglist.Member;

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

            firebaseMessaging.send(new RemoteMessage.Builder(R.string.firebase_sender_id + "@fcm.googleapis.com/fcm/send")
                    .setMessageId(messageIds.get(i))
                    .addData("my_message", message)
                    .addData("my_action", action)
                    .build());
        }
    }

}
