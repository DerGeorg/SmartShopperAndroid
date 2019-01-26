package at.smartshopper.smartshopper;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;

import java.util.List;
import java.util.jar.JarInputStream;


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
                showOwnShoppingList(uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ownswiperefresh = (SwipeRefreshLayout) findViewById(R.id.ownSwipe);

            ownswiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshOwnShoppinglist(uid);

                }
            });


        }
    }

    /**
     * Logt den User aus und geht zur Login Activity
     */
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Refreshed die eigene shoppinglist und veranlasst das das refreshen beendet wird
     *
     * @param uid Von dem benutzer von welchem die Shoppinglists angezeigt werden sollen
     */
    private void refreshOwnShoppinglist(String uid) {
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
    private void showOwnShoppingList(String uid) throws JSONException {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_menu, menu);
        return true;
    }


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


}
