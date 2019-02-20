package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.DetailsAdapter;
import at.smartshopper.smartshopper.shoppinglist.details.group.Group;

public class ShoppinglistDetails extends Activity implements DetailsAdapter.OnGroupEditClicked, DetailsAdapter.OnGroupDeleteClicked, DetailsAdapter.OnCardClicked {

    private Database db;
    private FloatingActionButton fab;
    private String colorString;
    private PopupWindow popupWindow;
    private Button colorBtn;
    private SwipeRefreshLayout detailsSwiperefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist_details);
        fab = findViewById(R.id.addGroupFab);
        db = new Database();
        colorBtn = (Button) findViewById(R.id.groupColor);
        Intent intent = getIntent();
        String sl_id = intent.getStringExtra("sl_id");


        try {
            Shoppinglist shoppinglist = db.getShoppinglist(sl_id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String finalSl_id = sl_id;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    detailsSwiperefresh.setRefreshing(true);
                    showPupupGroupEdit(false, null, finalSl_id, "Gruppe erstellen", v);
                    detailsSwiperefresh.setRefreshing(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            showDetails(sl_id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        detailsSwiperefresh = (SwipeRefreshLayout) findViewById(R.id.detailsRefreshSwipe);
        final String finalSl_id1 = sl_id;
        detailsSwiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    showDetails(finalSl_id1);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                detailsSwiperefresh.setRefreshing(false);
            }
        });

    }


    /**
     * Zeigt ein Popup zum bearbeiten und erstellen von groups
     * Wenn from db true ist wird die groupid benötigt
     *
     * @param fromDB  Wenn true ist das popup im bearbeiten modus, wenn false wird die groupid nicht benötigt
     * @param groupid Wenn fromDb true ist wird diese id benötigt um das richtige element zu bearbeiten
     * @param v       Der view auf dem das popup platziert werden soll
     */
    private void showPupupGroupEdit(final boolean fromDB, final String groupid, final String sl_id, String title, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        final String username = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View customView = inflater.inflate(R.layout.add_group_dialog, null);

        TextView addGroupTitle = (TextView) customView.findViewById(R.id.addgruppetitle);
        addGroupTitle.setText(title);
        ImageButton close = (ImageButton) customView.findViewById(R.id.groupClose);
        final EditText name = (EditText) customView.findViewById(R.id.groupName);
        Button color = (Button) customView.findViewById(R.id.groupColor);
        final Button finish = (Button) customView.findViewById(R.id.groupFinish);

        this.colorBtn = color;

        Picasso.get().load(R.drawable.close).into(close);

        if (fromDB) {
            Group dbgroup = db.getGroup(groupid, sl_id);
            String colorstring;
            if (dbgroup.getColor().contains("#")) {
                colorstring = dbgroup.getColor();
            } else {
                colorstring = "#" + dbgroup.getColor();
            }
            color.setBackgroundColor(Color.parseColor(colorstring));
            name.setText(dbgroup.getGroupName());
        } else {
            colorString = "ffffff";
        }

        if(!name.getText().toString().isEmpty()){
            finish.setEnabled(true);
        }else{
            finish.setEnabled(false);
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
                if(!name.getText().toString().isEmpty()){
                    finish.setEnabled(true);
                }else{
                    finish.setEnabled(false);
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDB) {
                    try {
                        db.editGroup(sl_id, groupid, name.getText().toString(), colorString, "");
                        showDetails(sl_id);
                        popupWindow.dismiss();
                        colorString = "ffffff";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        db.addGroup(sl_id, name.getText().toString(), colorString, "");
                        showDetails(sl_id);
                        popupWindow.dismiss();
                        colorString = "ffffff";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShoppinglistDetails.this, Colorpicker.class);
                startActivityForResult(intent, 1);

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            popupWindow.setElevation(5.0f);
        }

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);

        popupWindow.setAnimationStyle(R.style.popup_window_animation_phone);


        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupWindow.update();
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
                this.colorString = colorToHexString(color);
                String colorstring;
                if (this.colorString.contains("#")) {
                    colorstring = this.colorString;
                } else {
                    colorstring = "#" + this.colorString;
                }
                int colorint = Color.parseColor(colorstring);
                colorBtn.setBackgroundColor(colorint);
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

    /**
     * Zeigt das Card View der Shoppinglist Details an
     *
     * @param sl_id Shoppinglist welche angezeigt werden soll
     * @throws SQLException
     * @throws JSONException
     */
    private void showDetails(String sl_id) throws SQLException, JSONException {
        RecyclerView detailsRecycleView = (RecyclerView) findViewById(R.id.groupRecycle);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        RecyclerView.ItemDecoration itemDecoration;

        while (detailsRecycleView.getItemDecorationCount() > 0
                &&(itemDecoration = detailsRecycleView.getItemDecorationAt(0)) != null) {
            detailsRecycleView.removeItemDecoration(itemDecoration);
        }
        detailsRecycleView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
        detailsRecycleView.setHasFixedSize(true);
        detailsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        List<Details> detailsList = db.getListDetails(sl_id);


        DetailsAdapter detailsAdapter = new DetailsAdapter(detailsList, db);
        detailsAdapter.setGroupEditClick(this);
        detailsAdapter.setGroupDeleteClick(this);
        detailsAdapter.setCardClick(this);

        detailsRecycleView.setAdapter(detailsAdapter);
    }

    @Override
    public void onGroupEditClick(String sl_id, String group_id, View v) {
        try {
            detailsSwiperefresh.setRefreshing(true);
            showPupupGroupEdit(true, group_id, sl_id, "Gruppe bearbeiten", v);
            detailsSwiperefresh.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGroupDeleteClick(String sl_id, String group_id, View v) {
        try {
            detailsSwiperefresh.setRefreshing(true);
            db.deleteGroup(group_id, sl_id);
            showDetails(sl_id);
            detailsSwiperefresh.setRefreshing(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCardClick(String group_id, String sl_id, String groupName, View v) {
        finish();
        Intent intent = new Intent(this, ItemListActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("sl_id", sl_id);
        intent.putExtra("groupNameString", groupName);
        startActivity(intent);
    }
}
