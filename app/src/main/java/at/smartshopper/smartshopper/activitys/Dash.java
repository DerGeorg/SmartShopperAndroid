package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistAdapter;


public class Dash extends AppCompatActivity {

    private Database db = new Database();
    private SwipeRefreshLayout ownswiperefresh;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);


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


        }
    }

    /**
     * Logt den User aus und geht zur Login Activity
     */
    private void logout(){
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
    private void exit(){
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
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}
