package at.smartshopper.smartshopper.shoppinglist.details;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import at.smartshopper.smartshopper.R;
import at.smartshopper.smartshopper.activitys.ItemListActivity;
import at.smartshopper.smartshopper.customViews.SpaceItemDecoration;
import at.smartshopper.smartshopper.db.Database;
import at.smartshopper.smartshopper.shoppinglist.details.item.ItemShoppinglistDetailsAdapter;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.MyViewHolder> implements ItemShoppinglistDetailsAdapter.OnItemEditClicked {

    private List<Details> details;
    private OnGroupEditClicked onGroupEditClicked;
    private OnGroupDeleteClicked onGroupDeleteClicked;
    private OnCardClicked onCardClicked;
    private Database db;

    public DetailsAdapter(List<Details> details, Database db) {
        this.details = details;
        this.db = db;
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
        final TextView groupName = viewHolder.groupName;
        ImageButton deleteGroup = viewHolder.deleteGroup;
        RecyclerView itemsrecycle = viewHolder.itemsrecycle;
        View groupColor = viewHolder.grouoColor;
        ImageButton editGroup = viewHolder.editGroup;

        int spacingInPixels = viewHolder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.item_spacing);
        itemsrecycle.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        itemsrecycle.setHasFixedSize(true);
        itemsrecycle.setLayoutManager(new LinearLayoutManager(new Activity()));
        List<at.smartshopper.smartshopper.shoppinglist.details.item.Item> itemsList = details.get(i).getItems();
        ItemShoppinglistDetailsAdapter itemAdapter = new ItemShoppinglistDetailsAdapter(itemsList);
        itemAdapter.setOnItemEditClick(this);
        itemsrecycle.setAdapter(itemAdapter);


        int cardcolor;
        try {
            cardcolor = Color.parseColor(details.get(i).getGroup().getColor());
        } catch (Exception e) {
            cardcolor = Color.parseColor("#FFFFFF");
        }
        groupColor.setBackgroundColor(cardcolor);

        groupName.setText(details.get(i).getGroup().getGroupName());
        Picasso.get().load(R.drawable.delete).into(deleteGroup);
        Picasso.get().load(R.drawable.bearbeiten).into(editGroup);

        viewHolder.groupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardClicked.onCardClick(details.get(i).getGroup().getGroup_id(), details.get(i).getGroup().getSl_idd(), details.get(i).getGroup().getGroupName(), v);
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

    @Override
    public void onItemEditClicked(String item_id, String group_id, String sl_id, String groupName, View v) {
        //v.getContext().finish();
        Intent intent = new Intent(v.getContext(), ItemListActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("sl_id", sl_id);
        intent.putExtra("groupNameString", groupName);
        v.getContext().startActivity(intent);
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onCardClicked Der Click event Listener
     */
    public void setCardClick(OnCardClicked onCardClicked) {
        this.onCardClicked = onCardClicked;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onGroupDeleteClicked Der Click event Listener
     */
    public void setGroupDeleteClick(OnGroupDeleteClicked onGroupDeleteClicked) {
        this.onGroupDeleteClicked = onGroupDeleteClicked;
    }

    /**
     * Setzt das OnChangeItemClick event
     *
     * @param onGroupEditClicked Der Click event Listener
     */
    public void setGroupEditClick(OnGroupEditClicked onGroupEditClicked) {
        this.onGroupEditClicked = onGroupEditClicked;
    }

    @Override
    public int getItemCount() {
        return details.size();
    }


    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnCardClicked {
        void onCardClick(String group_id, String sl_id, String groupName, View v);
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnGroupDeleteClicked {
        void onGroupDeleteClick(String sl_id, String group_id, View v);
    }

    /**
     * Interface damit onoclick in der Shoppinglistdetails activity ausgeführt werden kann
     */
    public interface OnGroupEditClicked {
        void onGroupEditClick(String sl_id, String group_id, View v);
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        ImageButton deleteGroup, editGroup;
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
            this.groupCard = (CardView) itemView.findViewById(R.id.cardViewGroup);

        }

    }
}
