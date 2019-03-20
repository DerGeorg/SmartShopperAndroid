package at.smartshopper.smartshopperapp.shoppinglist.details.group;

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
        String colorstring;
        if (color.contains("#")) {
            colorstring = color;
        } else {
            colorstring = "#" + color;
        }
        this.color = colorstring;
        this.hidden = hidden;
    }

    public String getGroup_id() {
        return this.group_id;
    }

    public String getSl_idd() {
        return this.sl_id;
    }

    public String getGroupName() {
        return this.name;
    }

    public String getColor() {
        if (color.isEmpty()) {
            return "#FFFFFF";
        } else if (!color.contains("#")) {
            return "#" + this.color;
        } else {
            return this.color;
        }
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
