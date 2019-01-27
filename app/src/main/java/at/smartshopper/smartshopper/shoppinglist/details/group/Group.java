package at.smartshopper.smartshopper.shoppinglist.details.group;

public class Group {

    private String group_id;
    private String sl_id;
    private String name;
    private String color;
    private String hidden;


    public Group(String group_id, String sl_id, String name, String color, String hidden) {
        this.group_id = group_id;
        this.sl_id = sl_id;
        this.name = name;
        this.color = color;
        this.hidden = hidden;
    }

    public String getGroup_id() {
        return this.group_id;
    }

    public String getSl_idd() {
        return this.sl_id;
    }

    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }

    public String getHidden() {
        return this.hidden;
    }

    @Override
    public String toString() {
        return "Group{" +
                "group_id='" + group_id + '\'' +
                ", sl_id='" + sl_id + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", hidden='" + hidden + '\'' +
                '}';
    }
}
