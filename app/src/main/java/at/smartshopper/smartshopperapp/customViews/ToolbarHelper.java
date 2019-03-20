package at.smartshopper.smartshopperapp.customViews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.activitys.Dash;
import at.smartshopper.smartshopperapp.activitys.DoneItemActivity;
import at.smartshopper.smartshopperapp.activitys.LoginActivity;
import at.smartshopper.smartshopperapp.db.Database;

public class ToolbarHelper extends Activity {

    private PopupWindow popupAddShare;
    private Database db = new Database();
    private Context context;
    private View decor;

    public ToolbarHelper(Context context, View decor) {
        this.context = context;
        this.decor = decor;
    }

    /**
     * Logt den User aus und geht zur Login Activity
     */
    public void logout() {
        finish();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void doneEinkauf(String from, String sl_id,String group_id, String groupname) {
        finish();
        Intent intent = new Intent(context, DoneItemActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("sl_id", sl_id);
        intent.putExtra("groupNameString", groupname);
        intent.putExtra("group_id", group_id);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Öffnet ein popup in dem ein invite link eingegeben werden kann. Diese Shoppingliste wird dann hinzugefügt
     */
    public void popupaddInvite() {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupContentView = inflater.inflate(R.layout.add_share_link, null);

        final TextView linkEingabe = (TextView) popupContentView.findViewById(R.id.addShareLinkInput);

        ImageButton exitButton = (ImageButton) popupContentView.findViewById(R.id.addShareExit);
        Picasso.get().load(R.drawable.close).into(exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddShare.dismiss();
            }
        });
        final Button finish = (Button) popupContentView.findViewById(R.id.shareAddFinish);

        if (!linkEingabe.getText().toString().isEmpty()) {
            finish.setEnabled(true);
        } else {
            finish.setEnabled(false);
        }
        linkEingabe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!linkEingabe.getText().toString().isEmpty()) {
                    finish.setEnabled(true);
                } else {
                    finish.setEnabled(false);
                }
            }
        });


        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invite = linkEingabe.getText().toString();


                try {
                    db.addInviteLink(invite, FirebaseAuth.getInstance().getCurrentUser().getUid());
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                popupAddShare.dismiss();


                finish();
                Intent intent = new Intent(context, Dash.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("tab2", "true");
                context.startActivity(intent);
            }
        });

        popupAddShare = new PopupWindow(popupContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupAddShare.setOutsideTouchable(false);
        popupAddShare.setFocusable(true);
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupAddShare.setElevation(5.0f);
        }
        popupAddShare.setAnimationStyle(R.style.popup_window_animation_phone);


        popupAddShare.showAtLocation(decor.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupAddShare.update();
    }
}
