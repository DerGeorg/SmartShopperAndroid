package at.smartshopper.smartshopperapp.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopperapp.customViews.ToolbarHelper;
import at.smartshopper.smartshopperapp.db.Database;
import at.smartshopper.smartshopperapp.messaging.MyFirebaseSender;
import at.smartshopper.smartshopperapp.shoppinglist.details.item.Item;
import at.smartshopper.smartshopperapp.shoppinglist.details.item.ItemAdapter;

public class ItemListActivity extends AppCompatActivity implements ItemAdapter.OnItemEditClicked, ItemAdapter.OnItemDelClicked, ItemAdapter.OnItemCheckClicked {
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
        String colorToolbar = null;
        try {
            colorToolbar = db.getGroup(group_id, sl_id).getColor();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.itemToolbar);
        toolbar.setTitle("Gruppe: " + this.groupNameString);
        String colorstring;
        if (colorToolbar.contains("#")) {
            colorstring = colorToolbar;
        } else {
            colorstring = "#" + colorToolbar;
        }
        toolbar.setBackgroundColor(Color.parseColor(colorstring));
        setSupportActionBar(toolbar);

        this.groupName = (TextView) findViewById(R.id.groupViewName);
        this.groupName.setText(groupNameString);

        this.colorView = (View) findViewById(R.id.itemListColorView);

        this.colorView.setBackgroundColor(Color.parseColor(colorstring));


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
            case R.id.doneEinkauf:
                th.doneEinkauf("itemlist", sl_id, group_id, groupNameString);
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

        inflater.inflate(R.menu.dash_menu, menu);

        return true;

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

        ArrayList<Item> itemArrayListTmp = new ArrayList<>();
        List itemListTmp;
        View pfeil = findViewById(R.id.pfeilnachunten2);
        if (itemList.isEmpty()) {
            itemArrayListTmp.add(new Item("empty", "empty", "empty", "Bitte ein Item Hinzufügen!", "1"));
            itemListTmp = itemArrayListTmp;
            pfeil.setVisibility(View.VISIBLE);
        } else {
            itemListTmp = itemList;
            pfeil.setVisibility(View.GONE);
        }

        ItemAdapter itemAdapter = new ItemAdapter(itemListTmp);
        if (itemList.isEmpty()) {
            itemAdapter.setOnItemEditClick(new ItemAdapter.OnItemEditClicked() {
                @Override
                public void onItemEditClicked(String item_id, String group_id, String sl_id, View v) {

                }
            });
            itemAdapter.setItemDelClick(new ItemAdapter.OnItemDelClicked() {
                @Override
                public void onItemDelClicked(String item_id, String group_id, String sl_id) {

                }
            });
            itemAdapter.setOnItemCheckClick(new ItemAdapter.OnItemCheckClicked() {
                @Override
                public void onItemCheckClicked(String uid, String name, String itemId, String groupId, String sl_id, int count) {

                }
            });
        } else {
            itemAdapter.setOnItemEditClick(this);
            itemAdapter.setItemDelClick(this);
            itemAdapter.setOnItemCheckClick(this);
        }
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
        Item item = null;
        try {
            item = db.getItem(item_id);
            swipeRefreshLayoutItem.setRefreshing(true);
            db.deleteItem(item_id, group_id, sl_id);
            showItems(group_id, sl_id);
            swipeRefreshLayoutItem.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            MyFirebaseSender myFirebaseSender = new MyFirebaseSender(db.getMembers(sl_id));
            myFirebaseSender.addMember(db.getAdmin(sl_id));
            myFirebaseSender.sendMessage(item.getName() + " wurde von " + db.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).getName() + " gelöscht!", "Item: " + item.getName() + " wurde gelöscht!");
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
                String pushEndSting;
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
                    pushEndSting = " wurde geändert!";
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
                    pushEndSting = " wurde erstellt!";
                }
                try {
                    MyFirebaseSender myFirebaseSender = new MyFirebaseSender(db.getMembers(sl_id));
                    myFirebaseSender.addMember(db.getAdmin(sl_id));
                    myFirebaseSender.sendMessage(name.getText().toString() + pushEndSting + " Von: " + db.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).getName(), "Item: " + name.getText().toString() + pushEndSting);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
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
        Item item = null;
        try {
            item = db.getItem(itemId);
            swipeRefreshLayoutItem.setRefreshing(true);
            db.setDoneItem(uid, name, itemId, groupId, sl_id, count);
            showItems(group_id, sl_id);
            swipeRefreshLayoutItem.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            MyFirebaseSender myFirebaseSender = new MyFirebaseSender(db.getMembers(sl_id));
            myFirebaseSender.addMember(db.getAdmin(sl_id));
            myFirebaseSender.sendMessage(item.getName() + " wurde von " + db.getUser(FirebaseAuth.getInstance().getCurrentUser().getUid()).getName() + " gekauft!", "Item Erledigt: " + item.getName() + "!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
