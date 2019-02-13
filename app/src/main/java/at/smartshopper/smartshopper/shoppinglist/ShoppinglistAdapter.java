package at.smartshopper.smartshopper.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.activitys.ShoppinglistDetails;
import at.smartshopper.smartshopper.customViews.RoundCornersTransformation;
import at.smartshopper.smartshopper.db.Database;

public class ShoppinglistAdapter extends RecyclerView.Adapter<ShoppinglistAdapter.ShoppinglistViewHolder> {

    private OnChangeItemClick onChangeClick;
    private OnItemClicked onClick;
    private at.smartshopper.smartshopper.db.Database db;

    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the products in a list
    private List<Shoppinglist> shoppinglist;

    //getting the context and product list with constructor
    public ShoppinglistAdapter(Context mCtx, List<Shoppinglist> shoppinglist) {
        this.mCtx = mCtx;
        this.shoppinglist = shoppinglist;
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

        db = new Database();


        //binding the data with the viewholder views
        holder.textViewTitle.setText(shoppinglist.getname());
        System.out.println(shoppinglist.getname());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ShoppinglistDetails.class);
                Bundle bundle = new Bundle();
                bundle.putString("sl_id", shoppinglist.getSlId());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);


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

        int cardcolor;
        try {
            cardcolor = Color.parseColor(shoppinglist.getcolor());
        } catch (Exception e) {
            cardcolor = Color.parseColor("#FFFFFF");
        }

        holder.shoppinglistColor.setBackgroundColor(cardcolor);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();
            holder.ownerName.setText(name);
            Picasso.get().load(photoUrl).resize(250, 250).transform(new RoundCornersTransformation(30, 30, true, true)).into(holder.imageView);
            // holder.imageView.setImageDrawable(Drawable.createFromPath("@drawable/common_google_signin_btn_icon_dark"));

            // Check if user's email is verified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
        }


    }


    /**
     * Holt die anzahl der items in dem Adapter
     * @return Anzahl der Items in dem Adapter
     */
    @Override
    public int getItemCount() {
        return shoppinglist.size();
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
    public interface OnChangeItemClick{
        void onChangeItemClick(String sl_id, View v);
    }

    /**
     * Setzt das OnChangeItemClick event
     * @param onChangeClick Der Click event Listener
     */
    public void setOnChangeClick(OnChangeItemClick onChangeClick){
        this.onChangeClick = onChangeClick;
    }

    /**
     * Setzt das OnItemClicked event
     * @param onClick Der Click Listener
     */
    public void setOnDelClick(OnItemClicked onClick)
    {
        this.onClick=onClick;
    }


    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    class ShoppinglistViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewBeschreibung, ownerName;
        ImageView imageView;
        CardView ownList;
        ImageButton bearbeiten, del;
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


        }


    }
}