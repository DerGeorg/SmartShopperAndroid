package at.smartshopper.smartshopper.activitys;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistSharedAdapter;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemShoppinglistDetailsAdapter;

public class DoneItemActivity extends AppCompatActivity {

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_item);

        db = new Database();

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.doneItemListRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                try {
                    showDoneItems();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        try {
            showDoneItems();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zeigt alle erledigten Items an
     * @throws SQLException
     * @throws JSONException
     */
    private void showDoneItems() throws SQLException, JSONException {
        RecyclerView doneRecycle = (RecyclerView) findViewById(R.id.doneitemsrecycle);
        doneRecycle.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        doneRecycle.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        doneRecycle.setLayoutManager(new LinearLayoutManager(this));
        List<Item> doneItems = db.getDoneItems();
        ItemShoppinglistDetailsAdapter islAdapter = new ItemShoppinglistDetailsAdapter(doneItems);
        doneRecycle.setAdapter(islAdapter);
    }
}
