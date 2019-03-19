package at.smartshopper.smartshopper.shoppinglist;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.customViews.RoundCornersTransformation;
import at.smartshopper.smartshopper.db.Database;

public class ShoppinglistAdapter extends RecyclerView.Adapter<ShoppinglistAdapter.ShoppinglistViewHolder> {

    private OnChangeItemClick onChangeClick;
    private OnItemClicked onClick;
    private OnShareClick onShareClick;
    private Database db;

    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the products in a list
    private List<Shoppinglist> shoppinglist;
    private OnShoppinglistClick onShoppinglistClick;

    //getting the context and product list with constructor
    public ShoppinglistAdapter(Context mCtx, List<Shoppinglist> shoppinglist, Database db) {
        this.mCtx = mCtx;
        this.shoppinglist = shoppinglist;
        this.db = db;
    }

    /**
     * Erstellt einen Neuen view holder mit aktueller view
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ShoppinglistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.cardviewshoppinglist, parent, false);
        return new ShoppinglistViewHolder(view);
    }

    /**
     * Setzt alle Daten in die View elemente
     *
     * @param holder   Das View Holder Objekt mit allen elementen
     * @param position Der Index welcher aus der data list genommen werden soll
     */
    @Override
    public void onBindViewHolder(ShoppinglistViewHolder holder, final int position) {
        //getting the product of the specified position,
        final Shoppinglist shoppinglist = this.shoppinglist.get(position);
        final ImageButton shareButton = holder.share;
        TextView beschreibung = holder.textViewBeschreibung;
        beschreibung.setText(shoppinglist.getdescription());
        Picasso.get().load(R.drawable.share).into(shareButton);

        //binding the data with the viewholder views
        holder.textViewTitle.setText(shoppinglist.getname());
        System.out.println(shoppinglist.getname());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShoppinglistClick.onShoppinglistClick(shoppinglist.getSlId(), v);
            }
        });
        holder.bearbeiten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeClick.onChangeItemClick(shoppinglist.getSlId(), v);
            }
        });
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onItemClick(shoppinglist.getSlId());
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sl_id = shoppinglist.getSlId();
                Toast.makeText(v.getContext(), "LISTENER im ADAPTER geht: " + sl_id, Toast.LENGTH_LONG);
                onShareClick.onShareClick(sl_id, v);
            }
        });

        int cardcolor;
        try {
            cardcolor = Color.parseColor(shoppinglist.getcolor());
        } catch (Exception e) {
            cardcolor = Color.parseColor("#FFFFFF");
        }

        holder.shoppinglistColor.setBackgroundColor(cardcolor);


String uid = FirebaseAuth.getInstance().getUid();



            try {
                Member user = db.getUser(uid);
                holder.ownerName.setText(user.getName());
                Picasso.get().load(user.getPic()).resize(250, 250).transform(new RoundCornersTransformation(30, 30, true, true)).into(holder.imageView);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // holder.imageView.setImageDrawable(Drawable.createFromPath("@drawable/common_google_signin_btn_icon_dark"));

            // Check if user's email is verified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
        }





    /**
     * Holt die anzahl der items in dem Adapter
     *
     * @return Anzahl der Items in dem Adapter
     */
    @Override
    public int getItemCount() {
        return shoppinglist.size();
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onChangeClick Der Click event Listener
     */
    public void setOnChangeClick(OnChangeItemClick onChangeClick) {
        this.onChangeClick = onChangeClick;
    }

    /**
     * Setzt das OnItemClicked event
     *
     * @param onClick Der Click Listener
     */
    public void setOnDelClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onShareClick Der Click event Listener
     */
    public void setOnShareClick(OnShareClick onShareClick) {
        this.onShareClick = onShareClick;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onShoppinglistClick Der Click event Listener
     */
    public void setOnShoppinglistClick(OnShoppinglistClick onShoppinglistClick) {
        this.onShoppinglistClick = onShoppinglistClick;
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface OnItemClicked {
        void onItemClick(String sl_id);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface OnChangeItemClick {
        void onChangeItemClick(String sl_id, View v);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface OnShareClick {
        void onShareClick(String sl_id, View v);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface OnShoppinglistClick {
        void onShoppinglistClick(String sl_id, View v);
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    class ShoppinglistViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewBeschreibung, ownerName;
        ImageView imageView;
        CardView ownList;
        ImageButton bearbeiten, del, share;
        View shoppinglistColor;

        public ShoppinglistViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.shoppinglistName);
            textViewBeschreibung = itemView.findViewById(R.id.shoppinglistBeschreibung);
            imageView = itemView.findViewById(R.id.shoppinglistOwner);
            ownerName = itemView.findViewById(R.id.ownerName);
            ownList = itemView.findViewById(R.id.ownLists);
            bearbeiten = itemView.findViewById(R.id.bearbeiteShoppinglist);
            del = itemView.findViewById(R.id.deleteShoppinglist);
            shoppinglistColor = itemView.findViewById(R.id.shoppinglistColor);
            share = itemView.findViewById(R.id.shareButton);

        }


    }
}