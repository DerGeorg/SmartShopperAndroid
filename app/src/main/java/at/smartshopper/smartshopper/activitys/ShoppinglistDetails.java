package at.smartshopper.smartshopper.activitys;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.DetailsAdapter;
import at.smartshopper.smartshopper.shoppinglist.details.group.Group;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;

public class ShoppinglistDetails extends Activity implements DetailsAdapter.OnGroupEditClicked, DetailsAdapter.OnGroupDeleteClicked, DetailsAdapter.OnItemAddClicked, DetailsAdapter.OnCardClicked {

    private Database db = new Database();
    private FloatingActionButton fab;
    private String colorString;
    private PopupWindow popupWindow;
    private PopupWindow popupWindowItem;
    private Button colorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoppinglist_details);
        fab = findViewById(R.id.addGroupFab);

        colorBtn = (Button) findViewById(R.id.groupColor);
        Bundle bundle = getIntent().getExtras();
        String sl_id = null; // or other values
        if (bundle != null)
            sl_id = bundle.getString("sl_id");

        //Toast.makeText(this, "Click detected on item " + position, Toast.LENGTH_LONG).show();

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
                    showPupupGroupEdit(false, null, finalSl_id, v);
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

    }

    /**
     * Zeigt ein Popup zum bearbeiten und erstellen von groups
     * Wenn from db true ist wird die groupid benötigt
     *
     * @param fromDB  Wenn true ist das popup im bearbeiten modus, wenn false wird die groupid nicht benötigt
     * @param groupid Wenn fromDb true ist wird diese id benötigt um das richtige element zu bearbeiten
     * @param v       Der view auf dem das popup platziert werden soll
     */
    private void showPupupGroupEdit(final boolean fromDB, final String groupid, final String sl_id, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        final String username = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View customView = inflater.inflate(R.layout.add_group_dialog, null);

        ImageButton close = (ImageButton) customView.findViewById(R.id.groupClose);
        final EditText name = (EditText) customView.findViewById(R.id.groupName);
        Button color = (Button) customView.findViewById(R.id.groupColor);
        Button finish = (Button) customView.findViewById(R.id.groupFinish);

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
            name.setText(dbgroup.getName());
        } else {
            colorString = "ffffff";
        }

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
        detailsRecycleView.setHasFixedSize(true);
        detailsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        List<Details> detailsList =  db.getListDetails(sl_id);


        DetailsAdapter detailsAdapter = new DetailsAdapter(detailsList);
        detailsAdapter.setGroupEditClick(this);
        detailsAdapter.setGroupDeleteClick(this);
        detailsAdapter.setItemAddClick(this);
        detailsAdapter.setCardClick(this);

        detailsRecycleView.setAdapter(detailsAdapter);
    }


    private void showPopupItemEdit(final boolean fromDB, final String sl_id, final String group_id, String item_id, View v) throws SQLException, JSONException {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);


        View customView = inflater.inflate(R.layout.add_item_dialog, null);

        ImageButton close = (ImageButton) customView.findViewById(R.id.itemClose);
        final EditText name = (EditText) customView.findViewById(R.id.itemName);
        final EditText count = (EditText) customView.findViewById(R.id.itemAnzahl);
        Button finish = (Button) customView.findViewById(R.id.itemFinish);


        Picasso.get().load(R.drawable.close).into(close);

        if (fromDB) {
            Item dbitem = db.getItem(item_id);


            name.setText(dbitem.getName());
        } else {
            colorString = "ffffff";
        }

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDB) {
                    try {

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
                        db.addItem(group_id, sl_id, name.getText().toString(), Integer.parseInt(count.getText().toString()));
                        showDetails(sl_id);
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


        popupWindowItem.showAtLocation(v, Gravity.CENTER, 0, 0);
        popupWindowItem.update();
    }

    @Override
    public void onGroupEditClick(String sl_id, String group_id, View v) {
        try {
            showPupupGroupEdit(true, group_id, sl_id, v);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGroupDeleteClick(String sl_id, String group_id, View v) {
        try {
            db.deleteGroup(group_id, sl_id);
            showDetails(sl_id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemAddClick(String sl_id, String group_id, String item_id, View v) {

        try {
            showPopupItemEdit(false, sl_id, group_id, item_id, v);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCardClick(String group_id, String sl_id, View v) {
        finish();
        Intent intent = new Intent(this, ItemListActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("sl_id", sl_id);
        startActivity(intent);
    }
}
