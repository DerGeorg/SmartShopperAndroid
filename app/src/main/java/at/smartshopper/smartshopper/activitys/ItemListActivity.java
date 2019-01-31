package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.DetailsAdapter;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemAdapter;

public class ItemListActivity extends Activity implements ItemAdapter.OnItemEditClicked, ItemAdapter.OnItemDelClicked {
    private String group_id;
    private String sl_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Intent myIntent = getIntent(); // gets the previously created intent
        this.group_id = myIntent.getStringExtra("group_id"); // will return "FirstKeyValue"
        this.sl_id = myIntent.getStringExtra("sl_id"); // will return "SecondKeyValue"


        try {
            showItems(group_id, sl_id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void showItems(String group_id, String sl_id) throws SQLException, JSONException {
        RecyclerView itemsListRecycler = findViewById(R.id.itemsListRecycler);
        itemsListRecycler.setHasFixedSize(true);
        itemsListRecycler.setLayoutManager(new LinearLayoutManager(this));
        List<Item> itemList = new Database().getItemsOfGroup(group_id, sl_id);


        ItemAdapter itemAdapter = new ItemAdapter(itemList);
        itemAdapter.setOnItemEditClick(this);
        itemAdapter.setItemDelClick(this);

        itemsListRecycler.setAdapter(itemAdapter);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(this, ShoppinglistDetails.class);
        intent.putExtra("sl_id", sl_id);
        startActivity(intent);
    }

    @Override
    public void onItemDelClicked(String item_id, String group_id, String sl_id) {

    }

    @Override
    public void onItemEditClicked(String item_id, String group_id, String sl_id, String newname, int newcount) {

    }
}
