package at.smartshopper.smartshopperapp.shoppinglist.details.item;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;

import java.sql.SQLException;
import java.util.List;

import at.smartshopper.smartshopperapp.R;
import at.smartshopper.smartshopperapp.db.Database;

public class ItemShoppinglistDetailsAdapter extends RecyclerView.Adapter<ItemShoppinglistDetailsAdapter.MyViewHolder> {

    private List<at.smartshopper.smartshopperapp.shoppinglist.details.item.Item> data;
    private OnItemEditClicked onItemEditClick;

    public ItemShoppinglistDetailsAdapter(List<at.smartshopper.smartshopperapp.shoppinglist.details.item.Item> data) {
        this.data = data;
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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardviewitemshoppinglistdetails, viewGroup, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    /**
     * Setzt alle Daten in die View elemente
     *
     * @param myViewHolder Das View Holder Objekt mit allen elementen
     * @param i            Der Index welcher aus der data list genommen werden soll
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        final TextView itemName = myViewHolder.itemName;
        final TextView itemAnzahl = myViewHolder.itemAnzahl;
        CardView itemCardView = myViewHolder.itemCardView;

        itemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onItemEditClick.onItemEditClicked(data.get(i).getItem_id(), data.get(i).getGroup_id(), data.get(i).getSl_id(), new Database().getGroup(data.get(i).getGroup_id(), data.get(i).getSl_id()).getGroupName(), v);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        itemName.setText(data.get(i).getName());
        itemAnzahl.setText(data.get(i).getCount());
    }

    public void setOnItemEditClick(OnItemEditClicked onItemEditClick) {
        this.onItemEditClick = onItemEditClick;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemEditClicked {
        void onItemEditClicked(String item_id, String group_id, String sl_id, String groupName, View v);
    }

    /**
     * Haltet alle elemente. Durch ein Objekt von dem kann jedes Element welches hier drinnen angeführt ist verwendet werden
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView itemName, itemAnzahl;
        CardView itemCardView;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.itemName = (TextView) itemView.findViewById(R.id.nameItem);
            this.itemAnzahl = (TextView) itemView.findViewById(R.id.anzahlItem);
            this.itemCardView = (CardView) itemView.findViewById(R.id.itemCardView);
        }
    }
}
