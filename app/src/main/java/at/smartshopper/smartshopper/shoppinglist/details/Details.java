package at.smartshopper.smartshopper.shoppinglist.details;

import java.util.ArrayList;

import at.smartshopper.smartshopper.shoppinglist.details.group.Group;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;

public class Details {

    private Group group;
    private ArrayList<Item> items;

    public Details(Group group) {
        this.group = group;
        this.items = new ArrayList<Item>();
    }

    public Group getGroup() {
        return group;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void addItem(Item item){
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Details{" +
                "group=" + group.toString() +
                ", items=" + items.toString() +
                '}';
    }
}
