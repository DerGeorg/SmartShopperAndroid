package at.smartshopper.smartshopper.shoppinglist;

public class Shoppinglist {



    private String sl_id, name, description, invitelink, color;

    /**
     * Erstellt ein Object von Shoppinglist.
     * @param sl_id Shoppinglist ID
     * @param name Shoppinglist Name
     * @param description Shoppinglist Beschreibung
     * @param invitelink Shoppinglist Einladungslink
     * @param color Shoppinglist Farbe
     */
    public Shoppinglist(String sl_id, String name, String description, String invitelink, String color){
        this.sl_id = sl_id;
        this.name = name;
        this.description = description;
        this.invitelink = invitelink;
        this.color = color;
    }

    public String getSlId(){
        return this.sl_id;
    }

    public String getname(){
        return this.name;
    }

    public String getdescription(){return this.description;}

    public String getInvitelink(){return this.invitelink;}

    public String getcolor(){ return this.color;}

    @Override
    public String toString(){
        return "SL_ID: " + sl_id + " name: " + name + " description: " + description + " invitelink: " + invitelink + " color: "+ color;
    }

}
