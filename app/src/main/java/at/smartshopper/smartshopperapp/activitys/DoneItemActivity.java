package at.smartshopper.smartshopperapp.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopperapp.customViews.ToolbarHelper;
import at.smartshopper.smartshopperapp.db.Database;
import at.smartshopper.smartshopperapp.shoppinglist.details.item.Item;
import at.smartshopper.smartshopperapp.shoppinglist.details.item.ItemShoppinglistDetailsAdapter;

public class DoneItemActivity extends AppCompatActivity {

    private Database db;
    private String from, sl_id;
    private String groupname;
    private String group_id;

    /**
     * Menu item Action listener
     *
     * @param item Action Item
     * @return True wenn erfolgreich
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ToolbarHelper th = new ToolbarHelper(getApplicationContext(), getWindow().getDecorView());
        switch (item.getItemId()) {
            case R.id.logoutBtn:
                th.logout();
                return true;

            case R.id.addInvite:
                th.popupaddInvite();
                return true;
            case R.id.editUser:
                finish();
                Intent intent2 = new Intent(this, EditUser.class);
                startActivity(intent2);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.done_items_menu, menu);

        return true;

    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, ShoppinglistDetails.class);
        if (from != null) {
            switch (from) {
                case "shpdetails":
                    intent = new Intent(this, ShoppinglistDetails.class);
                    intent.putExtra("sl_id", sl_id);

                    break;
                case "itemlist":
                    intent = new Intent(this, ItemListActivity.class);
                    intent.putExtra("sl_id", sl_id);
                    intent.putExtra("groupNameString", groupname);
                    intent.putExtra("group_id", group_id);
                    break;
            }
        } else {
            intent = new Intent(this, Dash.class);
        }


        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_item);

        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        sl_id = intent.getStringExtra("sl_id");
        groupname = intent.getStringExtra("groupNameString");
        group_id = intent.getStringExtra("group_id");


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
     *
     * @throws SQLException
     * @throws JSONException
     */
    private void showDoneItems() throws SQLException, JSONException {
        RecyclerView doneRecycle = (RecyclerView) findViewById(R.id.doneitemsrecycle);
        doneRecycle.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        RecyclerView.ItemDecoration itemDecoration;

        while (doneRecycle.getItemDecorationCount() > 0
                && (itemDecoration = doneRecycle.getItemDecorationAt(0)) != null) {
            doneRecycle.removeItemDecoration(itemDecoration);
        }
        doneRecycle.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        doneRecycle.setLayoutManager(new LinearLayoutManager(this));
        List<Item> doneItems = db.getDoneItems();
        ItemShoppinglistDetailsAdapter islAdapter = new ItemShoppinglistDetailsAdapter(doneItems);
        doneRecycle.setAdapter(islAdapter);
    }
}
