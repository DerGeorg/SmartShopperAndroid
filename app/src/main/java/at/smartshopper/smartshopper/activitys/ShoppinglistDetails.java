package at.smartshopper.smartshopper.activitys;

import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.DetailsAdapter;

public class ShoppinglistDetails extends Activity {

    private Database db = new Database();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist_details);

        Bundle bundle = getIntent().getExtras();
        int position = -1; // or other values
        if (bundle != null)
            position = bundle.getInt("pos");

        Toast.makeText(this, "Click detected on item " + position, Toast.LENGTH_LONG).show();

        List<Shoppinglist> shoppinglists = null;
        try {
            shoppinglists = db.getMyShoppinglists(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Shoppinglist aktuelleShopinglist = shoppinglists.get(position);

        try {
            showDetails(aktuelleShopinglist.getSlId());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Zeigt das Card View der Shoppinglist Details an
     * @param sl_id Shoppinglist welche angezeigt werden soll
     * @throws SQLException
     * @throws JSONException
     */
    private void showDetails(String sl_id) throws SQLException, JSONException {
        RecyclerView detailsRecycleView = (RecyclerView) findViewById(R.id.groupRecycle);
        detailsRecycleView.setHasFixedSize(true);
        detailsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        List<Details> detailsList = db.getListDetails(sl_id);
        DetailsAdapter detailsAdapter = new DetailsAdapter(detailsList);
        detailsRecycleView.setAdapter(detailsAdapter);
    }

}
