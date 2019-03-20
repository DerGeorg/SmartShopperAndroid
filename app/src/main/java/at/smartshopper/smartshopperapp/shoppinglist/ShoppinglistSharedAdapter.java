package at.smartshopper.smartshopperapp.shoppinglist;

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

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.customViews.RoundCornersTransformation;
import at.smartshopper.smartshopperapp.db.Database;

public class ShoppinglistSharedAdapter extends RecyclerView.Adapter<ShoppinglistSharedAdapter.ShoppinglistViewHolder> {

    private SharedOnChangeItemClick sharedOnChangeClick;
    private SharedOnItemClicked sharedOnClick;
    private SharedOnShareClick sharedOnShareClick;
    private Database db;

    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the products in a list
    private List<Shoppinglist> shoppinglist;
    private SharedOnShoppinglistClick sharedOnShoppinglistClick;

    //getting the context and product list with constructor
    public ShoppinglistSharedAdapter(Context mCtx, List<Shoppinglist> shoppinglist, Database db) {
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
        View view = inflater.inflate(R.layout.cardviewshoppinglistshared, parent, false);
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
                sharedOnShoppinglistClick.sharedOnShoppinglistClick(shoppinglist.getSlId(), v);
            }
        });
        holder.bearbeiten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedOnChangeClick.sharedOnChangeItemClick(shoppinglist.getSlId(), v);
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sl_id = shoppinglist.getSlId();
                Toast.makeText(v.getContext(), "LISTENER im ADAPTER geht: " + sl_id, Toast.LENGTH_LONG);
                sharedOnShareClick.sharedOnShareClick(sl_id, v);
            }
        });

        int cardcolor;
        try {
            cardcolor = Color.parseColor(shoppinglist.getcolor());
        } catch (Exception e) {
            cardcolor = Color.parseColor("#FFFFFF");
        }

        holder.shoppinglistColor.setBackgroundColor(cardcolor);

        if(shoppinglist.getSlId().equals("empty")){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Name, email address, and profile photo Url
                String name = user.getDisplayName();
                Uri photoUrl = user.getPhotoUrl();
                holder.ownerName.setText(name);
                Picasso.get().load(photoUrl).resize(250, 250).transform(new RoundCornersTransformation(15, 15, true, true)).into(holder.imageView);
                // holder.imageView.setImageDrawable(Drawable.createFromPath("@drawable/common_google_signin_btn_icon_dark"));

                // Check if user's email is verified

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getIdToken() instead.
            }
        }
        try {
            if(!shoppinglist.getSlId().equals("empty")){
                Member admin = db.getAdmin(shoppinglist.getSlId());
                Picasso.get().load(admin.getPic()).resize(250, 250).transform(new RoundCornersTransformation(15, 15, true, true)).into(holder.imageView);
                holder.ownerName.setText(admin.getName());
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


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
    public void setOnChangeClick(SharedOnChangeItemClick onChangeClick) {
        this.sharedOnChangeClick = onChangeClick;
    }

    /**
     * Setzt das OnItemClicked event
     *
     * @param onClick Der Click Listener
     */
    public void setOnDelClick(SharedOnItemClicked onClick) {
        this.sharedOnClick = onClick;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onShareClick Der Click event Listener
     */
    public void setOnShareClick(SharedOnShareClick onShareClick) {
        this.sharedOnShareClick = onShareClick;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onShoppinglistClick Der Click event Listener
     */
    public void setOnShoppinglistClick(SharedOnShoppinglistClick onShoppinglistClick) {
        this.sharedOnShoppinglistClick = onShoppinglistClick;
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface SharedOnItemClicked {
        void sharedOnItemClick(String sl_id);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface SharedOnChangeItemClick {
        void sharedOnChangeItemClick(String sl_id, View v);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface SharedOnShareClick {
        void sharedOnShareClick(String sl_id, View v);
    }

    /**
     * Interface damit onoclick in der dash activity ausgeführt werden kann
     */
    public interface SharedOnShoppinglistClick {
        void sharedOnShoppinglistClick(String sl_id, View v);
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    class ShoppinglistViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewBeschreibung, ownerName;
        ImageView imageView;
        CardView ownList;
        ImageButton bearbeiten, share;
        View shoppinglistColor;

        public ShoppinglistViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.shoppinglistName);
            textViewBeschreibung = itemView.findViewById(R.id.shoppinglistBeschreibung);
            imageView = itemView.findViewById(R.id.shoppinglistOwner);
            ownerName = itemView.findViewById(R.id.ownerName);
            ownList = itemView.findViewById(R.id.ownLists);
            bearbeiten = itemView.findViewById(R.id.bearbeiteShoppinglist);
            shoppinglistColor = itemView.findViewById(R.id.shoppinglistColor);
            share = itemView.findViewById(R.id.shareEditButton);

        }


    }
}