package at.smartshopper.smartshopper;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ShoppinglistAdapter extends RecyclerView.Adapter<ShoppinglistAdapter.ShoppinglistViewHolder> {


    //this context we will use to inflate the layout
    private Context mCtx;

    //we are storing all the products in a list
    private List<Shoppinglist> shoppinglist;

    //getting the context and product list with constructor
    public ShoppinglistAdapter(Context mCtx, List<Shoppinglist> shoppinglist) {
        this.mCtx = mCtx;
        this.shoppinglist = shoppinglist;
    }

    @Override
    public ShoppinglistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.cardview, null);
        return new ShoppinglistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ShoppinglistViewHolder holder, int position) {
        //getting the product of the specified position
        Shoppinglist shoppinglist = this.shoppinglist.get(position);

        //binding the data with the viewholder views
        holder.textViewTitle.setText(shoppinglist.getname());
        System.out.println(shoppinglist.getname());
        holder.textViewBeschreibung.setText(shoppinglist.getdescription());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();
            holder.imageView.setImageURI(photoUrl);

            // Check if user's email is verified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
        }



    }


    @Override
    public int getItemCount() {
        return shoppinglist.size();
    }


    class ShoppinglistViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewBeschreibung;
        ImageView imageView;

        public ShoppinglistViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.shoppinglistName);
            textViewBeschreibung = itemView.findViewById(R.id.shoppinglistBeschreibung);
            imageView = itemView.findViewById(R.id.shoppinglistOwner);
        }
    }
}