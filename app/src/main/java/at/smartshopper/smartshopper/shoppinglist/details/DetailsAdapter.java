package at.smartshopper.smartshopper.shoppinglist.details;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;
import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.customViews.RoundCornersTransformation;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemAdapter;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.MyViewHolder> {

    private List<Details> details;

    public DetailsAdapter(List<Details> details) {
        this.details = details;
    }

    /**
     * Erstellt einen Neuen view holder mit aktueller view
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardviewgroup, viewGroup, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }


    /**
     * Setzt alle Daten in die View elemente
     * @param viewHolder Das View Holder Objekt mit allen elementen
     * @param i Der Index welcher aus der data list genommen werden soll
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        TextView groupName = viewHolder.groupName;
        ImageButton deleteGroup = viewHolder.deleteGroup;
        RecyclerView itemsrecycle = viewHolder.itemsrecycle;
        TextView ownerName = viewHolder.ownerName;
        ImageView ownerImage = viewHolder.ownerImage;
        CardView cardViewGroup = viewHolder.cardViewGroups;

        Database db = new Database();

        groupName.setText(details.get(i).getGroup().getName());
        ownerName.setText("Kein SQL");
        cardViewGroup.setCardBackgroundColor(Color.parseColor("#" + details.get(i).getGroup().getColor()));
        Picasso.get().load(R.drawable.delete).into(deleteGroup);
        Picasso.get().load(R.drawable.user).resize(250,250).transform(new RoundCornersTransformation(30, 30, true, true)).into(ownerImage);

        itemsrecycle.setHasFixedSize(true);
        itemsrecycle.setLayoutManager(new LinearLayoutManager(new Activity()));
        List<at.smartshopper.smartshopper.shoppinglist.details.item.Item> itemsList = details.get(i).getItems();
        ItemAdapter itemAdapter = new ItemAdapter(itemsList);

        itemsrecycle.setAdapter(itemAdapter);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView groupName, ownerName;
        ImageButton deleteGroup;
        RecyclerView itemsrecycle;
        ImageView ownerImage;
        CardView cardViewGroups;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.groupName = (TextView) itemView.findViewById(R.id.groupName);
            this.deleteGroup = (ImageButton) itemView.findViewById(R.id.deleteGroup);
            this.itemsrecycle = (RecyclerView) itemView.findViewById(R.id.itemsRecycle);
            this.ownerName = (TextView)itemView.findViewById(R.id.ownerName);
            this.ownerImage = (ImageView)itemView.findViewById(R.id.ownerImage);
            this.cardViewGroups = (CardView)itemView.findViewById(R.id.cardViewGroup);

        }

    }
}
