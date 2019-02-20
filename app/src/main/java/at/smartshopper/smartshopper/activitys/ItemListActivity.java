package at.smartshopper.smartshopper.activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemAdapter;

public class ItemListActivity extends Activity implements ItemAdapter.OnItemEditClicked, ItemAdapter.OnItemDelClicked, ItemAdapter.OnItemCheckClicked {
    private String group_id, groupNameString;
    private String sl_id;
    private PopupWindow popupWindowItem;
    private FloatingActionButton fabAddItem;
    private TextView groupName;
    private String colorString;
    private Database db;

    private View colorView;
    private SwipeRefreshLayout swipeRefreshLayoutItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Intent myIntent = getIntent(); // gets the previously created intent
        this.group_id = myIntent.getStringExtra("group_id"); // will return "FirstKeyValue"
        this.sl_id = myIntent.getStringExtra("sl_id"); // will return "SecondKeyValue"
        this.groupNameString = myIntent.getStringExtra("groupNameString"); // will return "SecondKeyValue"
        this.db = new Database();

        this.groupName = (TextView) findViewById(R.id.groupViewName);
        this.groupName.setText(groupNameString);

        this.colorView = (View) findViewById(R.id.itemListColorView);

        try {
            this.colorView.setBackgroundColor(Color.parseColor(db.getGroup(group_id, sl_id).getColor()));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.swipeRefreshLayoutItem = (SwipeRefreshLayout) findViewById(R.id.itemListRefresh);
        this.swipeRefreshLayoutItem.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    showItems(group_id, sl_id);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayoutItem.setRefreshing(false);
            }
        });

        this.fabAddItem = (FloatingActionButton) findViewById(R.id.fabItemAdd);

        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showPopupItemEdit(false, sl_id, group_id, null, "Item erstellen", v);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


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
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        RecyclerView.ItemDecoration itemDecoration;

        while (itemsListRecycler.getItemDecorationCount() > 0
                && (itemDecoration = itemsListRecycler.getItemDecorationAt(0)) != null) {
            itemsListRecycler.removeItemDecoration(itemDecoration);
        }
        itemsListRecycler.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        itemsListRecycler.setHasFixedSize(true);
        itemsListRecycler.setLayoutManager(new LinearLayoutManager(this));
        List<Item> itemList = db.getItemsOfGroup(group_id, sl_id);

        ItemAdapter itemAdapter = new ItemAdapter(itemList);
        itemAdapter.setOnItemEditClick(this);
        itemAdapter.setItemDelClick(this);
        itemAdapter.setOnItemCheckClick(this);

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
        try {
            swipeRefreshLayoutItem.setRefreshing(true);
            db.deleteItem(item_id, group_id, sl_id);
            showItems(group_id, sl_id);
            swipeRefreshLayoutItem.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemEditClicked(String item_id, String group_id, String sl_id, View v) {
        try {
            showPopupItemEdit(true, sl_id, group_id, item_id, "Item bearbeiten", v);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPopupItemEdit(final boolean fromDB, final String sl_id, final String group_id, final String item_id, String title, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);


        View customView = inflater.inflate(R.layout.add_item_dialog, null);

        TextView addGroupTitle = (TextView) customView.findViewById(R.id.addItemTitle);
        addGroupTitle.setText(title);

        ImageButton close = (ImageButton) customView.findViewById(R.id.itemClose);
        final EditText name = (EditText) customView.findViewById(R.id.itemName);
        final EditText count = (EditText) customView.findViewById(R.id.itemAnzahl);
        final Button finish = (Button) customView.findViewById(R.id.itemFinish);


        Picasso.get().load(R.drawable.close).into(close);

        if (fromDB) {
            Item dbitem = db.getItem(item_id);


            name.setText(dbitem.getName());
            count.setText(dbitem.getCount());
        } else {
            colorString = "ffffff";
        }

        if (!name.getText().toString().isEmpty()) {
            finish.setEnabled(true);
        } else {
            finish.setEnabled(false);
        }
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!name.getText().toString().isEmpty() && !count.getText().toString().isEmpty()) {
                    finish.setEnabled(true);
                } else {
                    finish.setEnabled(false);
                }
            }
        };
        count.addTextChangedListener(tw);
        name.addTextChangedListener(tw);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDB) {
                    try {
                        db.editItem(item_id, group_id, sl_id, name.getText().toString(), Integer.parseInt(count.getText().toString()));
                        showItems(group_id, sl_id);
                        popupWindowItem.dismiss();
                        colorString = "ffffff";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        db.addItem(group_id, sl_id, name.getText().toString(), Integer.parseInt(count.getText().toString()));
                        showItems(group_id, sl_id);
                        popupWindowItem.dismiss();
                        colorString = "ffffff";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowItem.dismiss();
            }
        });

        popupWindowItem = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupWindowItem.setElevation(5.0f);
        }

        popupWindowItem.setOutsideTouchable(false);
        popupWindowItem.setFocusable(true);

        popupWindowItem.setAnimationStyle(R.style.popup_window_animation_phone);


        popupWindowItem.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupWindowItem.update();
    }

    @Override
    public void onItemCheckClicked(String uid, String name, String itemId, String groupId, String sl_id, int count) {
        try {
            swipeRefreshLayoutItem.setRefreshing(true);
            db.setDoneItem(uid, name, itemId, groupId, sl_id, count);
            showItems(group_id, sl_id);
            swipeRefreshLayoutItem.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
