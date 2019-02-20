package at.smartshopper.smartshopper.shoppinglist.details.item;

public class Item {

    private String item_id;
    private String group_id;
    private String sl_id;
    private String name;
    private String count;

    public Item(String item_id, String group_id, String sl_id, String name, String count) {
        this.item_id = item_id;
        this.group_id = group_id;
        this.sl_id = sl_id;
        this.name = name;
        this.count = count;
    }

    public String getItem_id() {
        return item_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public String getSl_id() {
        return sl_id;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Item{" +
                "item_id='" + item_id + '\'' +
                ", group_id='" + group_id + '\'' +
                ", sl_id='" + sl_id + '\'' +
                ", name='" + name + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
