package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import at.smartshopper.smartshopper.R;

import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;


import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistAdapter;


public class Dash extends AppCompatActivity implements ShoppinglistAdapter.OnItemClicked, ShoppinglistAdapter.OnChangeItemClick {

    private Database db = new Database();
    private SwipeRefreshLayout ownswiperefresh;
    private FloatingActionButton addShoppinglistFab;
    private PopupWindow popupWindowAdd;
    private String color;
    private Button colorBtn;

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
     * Convertiert eine int farbe in eine hexa dezimale Farbe
     *
     * @param color Farbe zum umwandeln in int
     * @return farbe als hex im string
     */
    private static String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFFFF & color);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);
        color = "ffffff";


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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        showShoppinglistEditView(false, null, v);
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
    private void showShoppinglistEditView(final boolean fromDB, String sl_id, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        final String username = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View customView = inflater.inflate(R.layout.add_shoppinglist_dialog, null);

        ImageButton addClose = (ImageButton) customView.findViewById(R.id.addClose);
        colorBtn = (Button) customView.findViewById(R.id.addColor);
        Button addFertig = (Button) customView.findViewById(R.id.addFertig);
        final EditText name = (EditText) customView.findViewById(R.id.addName);
        final EditText description = (EditText) customView.findViewById(R.id.addDescription);

        Picasso.get().load(R.drawable.close).into(addClose);

        if (fromDB) {
            Shoppinglist dbShoppinglist = db.getShoppinglist(sl_id);
            String colorstring;
            if(dbShoppinglist.getcolor().contains("#")){
                colorstring = dbShoppinglist.getcolor();
            }else{
                colorstring = "#" + dbShoppinglist.getcolor();
            }
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

                if(fromDB){
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
                }else {
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
     * Macht eine Datenbankverbindung und holt alle Shoppinglists die dem User gehören, diese werden auf dem recycled view angezeigt
     *
     * @param uid Die UserId damit von diesem user die shoppinglisten angezeigt werden
     */
    private void showOwnShoppingList(String uid) throws JSONException, SQLException {
        RecyclerView ownRecycleView = (RecyclerView) findViewById(R.id.ownrecycler);
        ownRecycleView.setHasFixedSize(true);
        ownRecycleView.setLayoutManager(new LinearLayoutManager(this));
        List<Shoppinglist> ownListsList = db.getMyShoppinglists(uid);
        ShoppinglistAdapter shpAdapter = new ShoppinglistAdapter(Dash.this, ownListsList);
        shpAdapter.setOnDelClick(Dash.this);
        shpAdapter.setOnChangeClick(Dash.this);
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

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    //Für Double Back press to exit
    private boolean doubleBackToExitPressedOnce = false;

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
    @Override
    public void onItemClick(String sl_id) {
        try {
            db.delShoppinglist(sl_id);
            showOwnShoppingList(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Das ist der oncklick für eine einzelen Shoppinglist. Bearbeitet eine Shoppinglist
     *
     * @param sl_id Die Shoppinglist die bearbeitet werden soll
     */
    @Override
    public void onChangeItemClick(String sl_id, View v) {
        try {
            showShoppinglistEditView(true, sl_id, v);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
