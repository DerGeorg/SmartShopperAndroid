package at.smartshopper.smartshopper.activitys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistAdapter;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistSharedAdapter;


public class Dash extends AppCompatActivity implements ShoppinglistAdapter.OnItemClicked, ShoppinglistAdapter.OnShoppinglistClick, ShoppinglistAdapter.OnChangeItemClick, ShoppinglistAdapter.OnShareClick, ShoppinglistSharedAdapter.SharedOnItemClicked, ShoppinglistSharedAdapter.SharedOnChangeItemClick, ShoppinglistSharedAdapter.SharedOnShareClick, ShoppinglistSharedAdapter.SharedOnShoppinglistClick {

    private final Database db = new Database();
    private SwipeRefreshLayout ownswiperefresh, sharedswiperefresh;
    private FloatingActionButton addShoppinglistFab;
    private PopupWindow popupWindowAdd, popupShare, popupAddShare, popupEditShare;
    private String color;
    private Button colorBtn;
    //Für Double Back press to exit
    private boolean doubleBackToExitPressedOnce = false;

    /**
     * Convertiert eine int farbe in eine hexa dezimale Farbe
     *
     * @param color Farbe zum umwandeln in int
     * @return farbe als hex im string
     */
    private static String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFFFF & color);
    }

    /**
     * Setzt das atribut color wenn die activity colorpicker beendet wird
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int color = Integer.parseInt(data.getData().toString());
                this.color = colorToHexString(color);
                colorBtn.setBackgroundColor(Color.parseColor(this.color));
            }
        }
    }

    /**
     * Holt den msg token
     * <p>
     * SETZT IHN NOCH NED
     * <p>
     * <p>
     * WEITER PROGRAMMIERN
     * <p>
     * MIR FEHLT NOCH DIE DB VON LUKAS
     */
    private void setMsgId() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("SmartShopper", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("SmartShopper MSG", token);

                        /* Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                        */
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);
        color = "ffffff";


        setMsgId();
        // Erstellt die Tabs
        tabHoster();


        /*
        Get userinformations and show them
         */
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();


            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            final String uid = user.getUid();


            try {
                try {
                    showOwnShoppingList(uid);
                    showSharedShoppingList(uid);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sharedswiperefresh = (SwipeRefreshLayout) findViewById(R.id.sharedSwipe);

            sharedswiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    try {
                        showSharedShoppingList(uid);
                        sharedswiperefresh.setRefreshing(false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            ownswiperefresh = (SwipeRefreshLayout) findViewById(R.id.ownSwipe);

            ownswiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    try {
                        refreshOwnShoppinglist(uid);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            });

            addShoppinglistFab = (FloatingActionButton) findViewById(R.id.addShoppinglistFab);

            addShoppinglistFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        showShoppinglistEditView(false, null, "Shoppingliste erstellen", v);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            });


        }
    }

    /**
     * Zeigt ein Popup das zum Bearbeiten/Erstellen einer Shoppingliste dient.
     * Wenn eine Shoppingliste bearbeitet werden soll, muss fromDB true sein und sl_id mit einer id gefüllt
     * Wenn erstellt werden soll muss fromDB false sein und sl_id null
     *
     * @param fromDB True wenn daten von der DB kommen sollen, wenn false dann muss die sl_id null sein
     * @param sl_id  Muss nur eine sl_id drinnen sein wenn fromDB true ist
     * @param v      der View auf dem das Popup sein soll
     */
    private void showShoppinglistEditView(final boolean fromDB, String sl_id, String title, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        final String username = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View customView = inflater.inflate(R.layout.add_shoppinglist_dialog, null);

        TextView fensterTitle = (TextView) customView.findViewById(R.id.shoppinglisteAddTitle);
        fensterTitle.setText(title);

        ImageButton addClose = (ImageButton) customView.findViewById(R.id.addClose);
        colorBtn = (Button) customView.findViewById(R.id.addColor);
        final Button addFertig = (Button) customView.findViewById(R.id.addFertig);
        final EditText name = (EditText) customView.findViewById(R.id.addName);
        final EditText description = (EditText) customView.findViewById(R.id.addDescription);

        Picasso.get().load(R.drawable.close).into(addClose);

        if (!name.getText().toString().isEmpty()) {
            addFertig.setEnabled(true);
        } else {
            addFertig.setEnabled(false);
        }
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!name.getText().toString().isEmpty()) {
                    addFertig.setEnabled(true);
                } else {
                    addFertig.setEnabled(false);
                }
            }
        });

        if (fromDB) {
            Shoppinglist dbShoppinglist = db.getShoppinglist(sl_id);
            String colorstring;
            if (dbShoppinglist.getcolor().contains("#")) {
                colorstring = dbShoppinglist.getcolor();
            } else {
                colorstring = "#" + dbShoppinglist.getcolor();
            }
            this.color = colorstring;
            colorBtn.setBackgroundColor(Color.parseColor(colorstring));
            name.setText(dbShoppinglist.getname());
            description.setText(dbShoppinglist.getdescription());
        } else {
            color = "ffffff";
        }

        final String sl_idString = sl_id;
        addFertig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fromDB) {
                    try {
                        db.editShoppinglist(sl_idString, name.getText().toString(), description.getText().toString(), color);
                        color = "ffffff";
                        popupWindowAdd.dismiss();
                        showOwnShoppingList(username);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        db.addShoppinglist(name.getText().toString(), description.getText().toString(), username, color);
                        color = "ffffff";
                        popupWindowAdd.dismiss();
                        showOwnShoppingList(username);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        });

        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dash.this, Colorpicker.class);
                startActivityForResult(intent, 1);

            }
        });

        addClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowAdd.dismiss();
            }
        });

        popupWindowAdd = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupWindowAdd.setElevation(5.0f);
        }

        popupWindowAdd.setOutsideTouchable(false);
        popupWindowAdd.setFocusable(true);
        popupWindowAdd.setAnimationStyle(R.style.popup_window_animation_phone);


        popupWindowAdd.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupWindowAdd.update();
    }

    /**
     * Logt den User aus und geht zur Login Activity
     */
    private void logout() {
        finish();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Refreshed die eigene shoppinglist und veranlasst das das refreshen beendet wird
     *
     * @param uid Von dem benutzer von welchem die Shoppinglists angezeigt werden sollen
     */
    private void refreshOwnShoppinglist(String uid) throws SQLException {
        try {
            showOwnShoppingList(uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshOwnShoppinglistFinish();
    }

    /**
     * Stoppt das refreshen der OwnShoppinglist
     */
    private void refreshOwnShoppinglistFinish() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        ownswiperefresh.setRefreshing(false);
    }

    /**
     * Macht eine Datenbankverbindung und holt alle Shoppinglists die mit dem User geteilt werden, diese werden auf dem recycled view angezeigt
     *
     * @param uid Die UserId damit von diesem user die shoppinglisten angezeigt werden
     */
    private void showSharedShoppingList(String uid) throws JSONException, SQLException {
        RecyclerView sharedRecycler = (RecyclerView) findViewById(R.id.sharedrecycler);
        sharedRecycler.setHasFixedSize(true);
        sharedRecycler.setLayoutManager(new LinearLayoutManager(this));
        List<Shoppinglist> ownListsList = db.getSharedShoppinglists(uid);
        ShoppinglistSharedAdapter shpAdapter = new ShoppinglistSharedAdapter(Dash.this, ownListsList, db);
        shpAdapter.setOnDelClick(Dash.this);
        shpAdapter.setOnChangeClick(Dash.this);
        shpAdapter.setOnShareClick(Dash.this);
        shpAdapter.setOnShoppinglistClick(Dash.this);
        sharedRecycler.setAdapter(shpAdapter);

    }

    /**
     * Macht eine Datenbankverbindung und holt alle Shoppinglists die dem User gehören, diese werden auf dem recycled view angezeigt
     *
     * @param uid Die UserId damit von diesem user die shoppinglisten angezeigt werden
     */
    private void showOwnShoppingList(String uid) throws JSONException, SQLException {
        RecyclerView ownRecycleView = (RecyclerView) findViewById(R.id.ownrecycler);
        ownRecycleView.setHasFixedSize(true);
        ownRecycleView.setLayoutManager(new LinearLayoutManager(this));
        List<Shoppinglist> ownListsList = db.getMyShoppinglists(uid);
        ShoppinglistAdapter shpAdapter = new ShoppinglistAdapter(Dash.this, ownListsList, db);
        shpAdapter.setOnDelClick(Dash.this);
        shpAdapter.setOnChangeClick(Dash.this);
        shpAdapter.setOnShareClick(Dash.this);
        shpAdapter.setOnShoppinglistClick(Dash.this);

        ownRecycleView.setAdapter(shpAdapter);

    }

    /**
     * Ist dafür Zuständig das es Tabs in der App gibt. Ohne dieser Funktion werden die Tabs nichtmehr Angezeigt.
     * Hier wird auch der Name der Tabs gesetzt
     */
    private void tabHoster() {
        TabHost host = (TabHost) findViewById(R.id.tabHost1);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Eigene Einkaufslisten");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Eigene Einkaufslisten");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Geteilte Einkaufslisten");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Geteilte Einkaufslisten");
        host.addTab(spec);
    }

    /**
     * Schickt an die Login Activity einen intend mit dem extra EXIT. Um die app zu schließen
     */
    private void exit() {
        Intent intent = new Intent(Dash.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_menu, menu);
        return true;
    }

    /**
     * Menu item Action listener
     *
     * @param item Action Item
     * @return True wenn erfolgreich
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutBtn:
                logout();
                return true;

            case R.id.addInvite:
                popupaddInvite();
                return true;
            case R.id.doneEinkauf:
                Intent intent = new Intent(Dash.this, DoneItemActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Öffnet ein popup in dem ein invite link eingegeben werden kann. Diese Shoppingliste wird dann hinzugefügt
     */
    private void popupaddInvite() {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
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


                try {
                    TabHost tabhost = (TabHost) findViewById(R.id.tabHost1);
                    tabhost.setCurrentTab(1);
                    sharedswiperefresh.setRefreshing(true);
                    showSharedShoppingList(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    sharedswiperefresh.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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


        popupAddShare.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        popupAddShare.update();
    }

    /**
     * 2 Mal Zurück Drücken um die App zu schließen
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            exit();

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * Das ist der Onclick für die einzelnen shoppinglists. Löscht eine shoppinglist und refreshed alle anderen
     *
     * @param sl_id Die Shoppingliste dieser Id wird gelöscht
     */
    private void onItemClickContainer(String sl_id) {
        try {
            db.delShoppinglist(sl_id);
            refreshOwnShoppinglist(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Das ist der oncklick für eine einzelen Shoppinglist. Bearbeitet eine Shoppinglist
     *
     * @param sl_id Die Shoppinglist die bearbeitet werden soll
     */
    private void onChangeItemClickContainer(String sl_id, View v) {
        try {
            showShoppinglistEditView(true, sl_id, "Shoppingliste bearbeiten", v);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onShoppinglistClickContainer(String sl_id, View v) {
        Intent intent = new Intent(this, ShoppinglistDetails.class);
        intent.putExtra("sl_id", sl_id);

        startActivity(intent);
    }

    /**
     * Das ist der Onclick für die einzelnen shoppinglists. Löscht eine shoppinglist und refreshed alle anderen
     *
     * @param sl_id Die Shoppingliste dieser Id wird gelöscht
     */
    @Override
    public void onItemClick(String sl_id) {
        onItemClickContainer(sl_id);
    }

    /**
     * Das ist der oncklick für eine einzelen Shoppinglist. Bearbeitet eine Shoppinglist
     *
     * @param sl_id Die Shoppinglist die bearbeitet werden soll
     */
    @Override
    public void onChangeItemClick(String sl_id, View v) {
        onChangeItemClickContainer(sl_id, v);
    }

    /**
     * Holt den Invitelink einer Shoppingliste
     *
     * @param sl_id Die Shoppingliste von der der invitelink gewünscht ist
     * @return
     */
    private String getInviteLink(String sl_id) {
        String link = null;
        try {
            if (db.isShared(sl_id)) {
                link = db.getInviteLink(sl_id);
            } else {

                link = db.createInviteLink(sl_id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return link;
    }

    @Override
    public void onShareClick(String sl_id, View v) {
        final String link = getInviteLink(sl_id);

        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupContentView = inflater.inflate(R.layout.add_share, null);

        final TextView linkausgabe = (TextView) popupContentView.findViewById(R.id.shareLink);
        linkausgabe.setText("www.smartshopper.cf/invite/" + link);

        ImageButton exitButton = (ImageButton) popupContentView.findViewById(R.id.shareExit);
        Picasso.get().load(R.drawable.close).into(exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupShare.dismiss();
            }
        });

        final Button copyButton = (Button) popupContentView.findViewById(R.id.shareCopy);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(linkausgabe.getText().toString());
                popupShare.dismiss();
            }
        });

        Button delShare = (Button) popupContentView.findViewById(R.id.delShare);

        final String finalLink = link;
        delShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    db.deleteInvite(finalLink);


                    TabHost tabhost = (TabHost) findViewById(R.id.tabHost1);
                    tabhost.setCurrentTab(0);
                    sharedswiperefresh.setRefreshing(true);

                    showSharedShoppingList(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    sharedswiperefresh.setRefreshing(false);
                    popupShare.dismiss();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        popupShare = new PopupWindow(popupContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupShare.setOutsideTouchable(false);
        popupShare.setFocusable(true);
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupShare.setElevation(5.0f);
        }
        popupShare.setAnimationStyle(R.style.popup_window_animation_phone);


        popupShare.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupShare.update();
    }

    /**
     * Kopiert einen Text in die Zwischenablage
     *
     * @param text Der Text, welcher zu kopieren ist
     */
    private void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("SmartShopper", text);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onShoppinglistClick(String sl_id, View v) {
        onShoppinglistClickContainer(sl_id, v);
    }

    @Override
    public void sharedOnItemClick(String sl_id) {
        onItemClickContainer(sl_id);
    }

    @Override
    public void sharedOnChangeItemClick(String sl_id, View v) {
        onChangeItemClickContainer(sl_id, v);
    }

    @Override
    public void sharedOnShareClick(String sl_id, View v) {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupContentView = inflater.inflate(R.layout.edit_share_member, null);

        ImageButton exitBtn = popupContentView.findViewById(R.id.exitButton);
        Picasso.get().load(R.drawable.close).into(exitBtn);
        final TextView linkAusgabe = popupContentView.findViewById(R.id.linkausgabe);
        Button copyBtn = popupContentView.findViewById(R.id.copyButton);
        Button stopShareBtn = popupContentView.findViewById(R.id.delShare);


        linkAusgabe.setText("www.smartshopper.cf/invite/" + getInviteLink(sl_id));
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupEditShare.dismiss();
            }
        });
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(linkAusgabe.getText().toString());
                popupEditShare.dismiss();
            }
        });
        stopShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ownswiperefresh.setRefreshing(true);
                    db.stopInvite(linkAusgabe.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                    popupEditShare.dismiss();
                    showSharedShoppingList(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ownswiperefresh.setRefreshing(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        popupEditShare = new PopupWindow(popupContentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupEditShare.setOutsideTouchable(false);
        popupEditShare.setFocusable(true);
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupEditShare.setElevation(5.0f);
        }
        popupEditShare.setAnimationStyle(R.style.popup_window_animation_phone);


        popupEditShare.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupEditShare.update();
    }

    @Override
    public void sharedOnShoppinglistClick(String sl_id, View v) {
        onShoppinglistClickContainer(sl_id, v);
    }
}
