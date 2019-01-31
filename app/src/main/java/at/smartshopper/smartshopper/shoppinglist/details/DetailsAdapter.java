package at.smartshopper.smartshopper.shoppinglist.details;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
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
import at.smartshopper.smartshopper.activitys.ShoppinglistDetails;
import at.smartshopper.smartshopper.customViews.RoundCornersTransformation;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.ShoppinglistAdapter;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemAdapter;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.MyViewHolder> {

    private List<Details> details;
    private OnGroupEditClicked onGroupEditClicked;
    private OnItemAddClicked onItemAddClicked;
    private OnGroupDeleteClicked onGroupDeleteClicked;
    private OnCardClicked onCardClicked;

    public DetailsAdapter(List<Details> details) {
        this.details = details;
    }

    /**
     * Erstellt einen Neuen view holder mit aktueller view
     *
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
     *
     * @param viewHolder Das View Holder Objekt mit allen elementen
     * @param i          Der Index welcher aus der data list genommen werden soll
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, final int i) {
        TextView groupName = viewHolder.groupName;
        ImageButton deleteGroup = viewHolder.deleteGroup;
        RecyclerView itemsrecycle = viewHolder.itemsrecycle;
        View groupColor = viewHolder.grouoColor;
        ImageButton editGroup = viewHolder.editGroup;
        ImageButton addItem = viewHolder.addItem;


        itemsrecycle.setHasFixedSize(true);
        itemsrecycle.setLayoutManager(new LinearLayoutManager(new Activity()));
        List<at.smartshopper.smartshopper.shoppinglist.details.item.Item> itemsList = details.get(i).getItems();
        ItemAdapter itemAdapter = new ItemAdapter(itemsList);
        itemsrecycle.setAdapter(itemAdapter);


        final Database db = new Database();


        int cardcolor;
        try {
            cardcolor = Color.parseColor(details.get(i).getGroup().getColor());
        } catch (Exception e) {
            cardcolor = Color.parseColor("#FFFFFF");
        }
        groupColor.setBackgroundColor(cardcolor);

        groupName.setText(details.get(i).getGroup().getName());
        Picasso.get().load(R.drawable.delete).into(deleteGroup);
        Picasso.get().load(R.drawable.add).into(addItem);
        Picasso.get().load(R.drawable.bearbeiten).into(editGroup);

        viewHolder.groupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardClicked.onCardClick(details.get(i).getGroup().getGroup_id(), details.get(i).getGroup().getSl_idd(), v);
            }
        });


        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemAddClicked.onItemAddClick(details.get(i).getGroup().getSl_idd(), details.get(i).getGroup().getGroup_id(), db.generateItemId(), v);
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGroupEditClicked.onGroupEditClick(details.get(i).getGroup().getSl_idd(), details.get(i).getGroup().getGroup_id(), v);
            }
        });

        deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGroupDeleteClicked.onGroupDeleteClick(details.get(i).getGroup().getSl_idd(), details.get(i).getGroup().getGroup_id(), v);
            }
        });
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnCardClicked {
        void onCardClick(String group_id, String sl_id, View v);
    }

    /**
     * Setzt das OnChangeItemClick event
     * @param onCardClicked Der Click event Listener
     */
    public void setCardClick(OnCardClicked onCardClicked){
        this.onCardClicked = onCardClicked;
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnItemAddClicked {
        void onItemAddClick(String sl_id, String group_id, String item_id, View v);
    }

    /**
     * Setzt das OnChangeItemClick event
     * @param onItemAddClicked Der Click event Listener
     */
    public void setItemAddClick(OnItemAddClicked onItemAddClicked){
        this.onItemAddClicked = onItemAddClicked;
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnGroupDeleteClicked {
        void onGroupDeleteClick(String sl_id, String group_id, View v);
    }

    /**
     * Setzt das OnChangeItemClick event
     * @param onGroupDeleteClicked Der Click event Listener
     */
    public void setGroupDeleteClick(OnGroupDeleteClicked onGroupDeleteClicked){
        this.onGroupDeleteClicked = onGroupDeleteClicked;
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnGroupEditClicked {
        void onGroupEditClick(String sl_id, String group_id, View v);
    }

    /**
     * Setzt das OnChangeItemClick event
     * @param onGroupEditClicked Der Click event Listener
     */
    public void setGroupEditClick(OnGroupEditClicked onGroupEditClicked){
        this.onGroupEditClicked = onGroupEditClicked;
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        ImageButton deleteGroup, editGroup, addItem;
        RecyclerView itemsrecycle;
        View grouoColor;
        CardView groupCard;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.groupName = (TextView) itemView.findViewById(R.id.groupName);
            this.deleteGroup = (ImageButton) itemView.findViewById(R.id.deleteGroup);
            this.itemsrecycle = (RecyclerView) itemView.findViewById(R.id.itemsRecycle);
            this.grouoColor = (View) itemView.findViewById(R.id.groupColorView);
            this.editGroup = (ImageButton) itemView.findViewById(R.id.editGroup);
            this.addItem = (ImageButton) itemView.findViewById(R.id.addItem);
            this.groupCard = (CardView) itemView.findViewById(R.id.cardViewGroup);

        }

    }
}
