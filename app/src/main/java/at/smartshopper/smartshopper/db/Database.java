package at.smartshopper.smartshopper.db;

import android.os.StrictMode;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.smartshopper.smartshopper.shoppinglist.Member;
import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.group.Group;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;

public class Database {

    private transient Connection conect;
    final private String HOST = "188.166.124.80";
    final private String DB_NAME = "smartshopperdb";
    final private String USERNAME = "smartshopper-user";
    final private String PASSWORD = "jW^v#&LjNY_b3-k*jYj!U4Xz?T??m_D6249XAeWZ#7C^FRbKm!c_Dt+qj@4&a-Hs";
    final private int PORT = 5432;
    final private int sl_idLength = 10;
    final private int groupIdLength = 10;
    final private int itemIdLength = 10;
    final private int inviteLength = 50;


    /**
     * Macht nix
     */
    public Database() {

        try {
            connectDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verbindet Sich mit der Datenbank. Auf der Konsole wird "Database connected!" angezeigt, bei erfolgreicher verbindung
     *
     * @throws SQLException Bei einem error bei der Verbindung, können details über diese Exception abgerufen werden
     */
    private void connectDatabase() throws SQLException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        DriverManager.registerDriver(new org.postgresql.Driver());
        conect = DriverManager.getConnection("jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME, USERNAME, PASSWORD);

        System.out.println("Database connected!");
    }

    /**
     * Setzt ein Item auf erledigt indem es in Done items verschoben wird
     *
     * @param uid   Die User id, von dem das item geändert werden soll
     * @param name  Der name des Items
     * @param count Die Anzahl des Items
     * @throws SQLException
     */
    public void setDoneItem(String uid, String name, String item_id, String groupId, String sl_id, int count) throws SQLException, JSONException {
        java.sql.Date date = new java.sql.Date(new java.util.Date().getDate());
        List<Member> members = getMembers(sl_id);
        Member admin = getAdmin(sl_id);
        sqlUpdate5ParamLastIsDateAndInt("INSERT INTO \"Done_Purchase\" (purchased_item_id, username, name, count, date) VALUES(?,?,?,?,?)", generateItemId(), admin.getUid(), name, count, date);
        for (int i = 0; i < members.size(); i++) {
            sqlUpdate5ParamLastIsDateAndInt("INSERT INTO \"Done_Purchase\" (purchased_item_id, username, name, count, date) VALUES(?,?,?,?,?)", generateItemId(), members.get(i).getUid(), name, count, date);
        }
        sqlUpdate3Param("DELETE FROM \"Item\" WHERE item_id = ? AND group_id = ? AND sl_id = ?", item_id, groupId, sl_id);
    }


    /**
     * Holt den Admin einer Shoppingliste
     *
     * @param sl_id Die Shoppingliste von welcher der Admin gewünscht ist
     * @return Member Objekt das mit den Daten des Admins gefüllt ist
     * @throws SQLException
     * @throws JSONException
     */
    public Member getAdmin(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"User\") as obj FROM \"User\" JOIN \"Shoppinglist_member\" USING (username) WHERE sl_id = ?";
        JSONObject jsonObject = new JSONObject(executeQuery(SQL, sl_id));
        return new Member(jsonObject.getString("username"), jsonObject.getString("message_id"));
    }

    /**
     * Holt alle mitglieder einer Shoppingliste
     *
     * @param sl_id Die Shoppingliste von der die Mitglieder gefragt sind
     * @throws SQLException
     */
    public List<Member> getMembers(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"User\") as obj FROM \"User\" JOIN \"Shoppinglist_member\" USING (username) WHERE sl_id = ?";
        ArrayList<Member> members = new ArrayList();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, sl_id);
        for(int i = 0; i < jsonObjects.size(); i++){
            JSONObject jsonObject = jsonObjects.get(i);
            members.add(new Member(jsonObject.getString("username"), jsonObject.getString("message_id")));
        }
        return members;
    }

    /**
     * Entfernt einen invitelink anhand des invitelinks
     *
     * @param invitelink Löscht den invitelink aus der ganzen db
     * @throws SQLException
     * @throws JSONException
     */
    public void deleteInvite(String invitelink) throws SQLException, JSONException {
        String sl_id = getSlIdFromInvite(invitelink);
        sqlUpdate("DELETE FROM \"Shoppinglist_member\" WHERE sl_id = ?", sl_id);
        sqlUpdate("Update \"Shoppinglist\" set invitelink=null where sl_id=?", sl_id);
    }

    /**
     * Stopt eine Einladung, indem der Member die liste nichtmehr sehen kann
     * @param invitelink Der invitelink
     * @throws SQLException
     * @throws JSONException
     */
    public void stopInvite(String invitelink, String uid) throws SQLException, JSONException {
        String sl_id = getSlIdFromInvite(invitelink);
        sqlUpdate2Param("DELETE FROM \"Shoppinglist_member\" WHERE sl_id = ? AND username = ?", sl_id, uid);
    }

    /**
     * Gibt den Invite link einer Shoppingliste zurück, wenn keiner vorhanden ist --> null
     *
     * @param sl_id Die shoppinglist von der der invitelimnk gefragt ist
     * @return Der invite link
     * @throws SQLException
     * @throws JSONException
     */
    public String getInviteLink(String sl_id) throws SQLException, JSONException {
        String SQL = "Select invitelink from \"Shoppinglist\" WHERE sl_id = ?";
        String returnLink = executeQuery(SQL, sl_id);
        return returnLink;
    }

    /**
     * Sucht anhand des invitelinks eine Shoppingliste und gibt dessen sl_id zurück
     *
     * @param invitelink Der invitelink nach dem gesucht werden soll
     * @return Die sl_id die dem invitelink zugeordnet ist
     * @throws SQLException
     * @throws JSONException
     */
    private String getSlIdFromInvite(String invitelink) throws SQLException, JSONException {
        String SQL = "Select sl_id from \"Shoppinglist\" WHERE invitelink = ?";
        String returnSl_id = executeQuery(SQL, invitelink);
        return returnSl_id;
    }


    /**
     * Fügt einen invite link zu den shoppinglisten hinzu
     *
     * @param invitelink Der invite link der hinzugefügt werden soll
     * @param uid        Der user zu dem der invitelink hinzugefügt werden soll
     * @throws SQLException
     * @throws JSONException
     */
    public void addInviteLink(String invitelink, String uid) throws SQLException, JSONException {
        String sl_id = getSlIdFromInvite(invitelink);
        if (!sl_id.equals("null")) {
            sqlUpdate2Param("INSERT INTO \"Shoppinglist_member\" (username, sl_id) VALUES (?, ?)", uid, sl_id);
        }
    }

    /**
     * Erstellt einen neuen InviteLink
     *
     * @param sl_id
     * @return Der neue InviteLink
     * @throws SQLException
     */
    public String createInviteLink(String sl_id) throws SQLException {
        String invitelink = generateInviteLink();
        sqlUpdate2Param("UPDATE \"Shoppinglist\" SET invitelink = ? WHERE sl_id = ?", invitelink, sl_id);
        return invitelink;
    }

    /**
     * Wenn die Shoppingliste bereits geshared ist wird true zurückgegeben
     *
     * @param sl_id Die Liste die geprüft werden soll
     * @return True wenn die liste bereits geshared ist
     * @throws SQLException
     * @throws JSONException
     */
    public boolean isShared(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" WHERE sl_id = ?";
        boolean returnBoolean = false;
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, sl_id);
        for(int i = 0; i < jsonObjects.size(); i++){
            JSONObject jsonObject = jsonObjects.get(i);
            if (jsonObject.getString("invitelink").equals("null")) {
                returnBoolean = false;
            } else {
                returnBoolean = true;
            }
        }
        return returnBoolean;
    }

    /**
     * Löscht eine Gruppe von der Tabelle Group und alle items dieser group, desswegen wird aucj die tabelle item geleert
     *
     * @param group_id Die group id welche gelöscht werden soll
     * @param sl_id    Die Shoppingliste auf der sich die group befindet
     * @throws SQLException
     */
    public void deleteGroup(String group_id, String sl_id) throws SQLException {
        sqlUpdate2Param("DELETE FROM \"Item\" WHERE group_id = ? AND sl_id = ?", group_id, sl_id);
        sqlUpdate2Param("DELETE FROM \"Group\" WHERE group_id = ? AND sl_id = ?", group_id, sl_id);
    }

    /**
     * Gibt den Besitzer einer Shoppingliste zurück
     *
     * @param sl_id Shoppingliste von der der Besitzer gefunden werden soll
     * @return Die uid des Besitzers
     */
    public String getShoppinglistOwner(String sl_id) throws SQLException {
        String SQL = "Select username from \"Shoppinglist_admin\" WHERE sl_id = ?";
        String owner = executeQuery(SQL, sl_id);
        return owner;
    }

    /**
     * Bearbeitet ein Item in der Datenbank
     *
     * @param item_id  Daqs zu bearbeitende item
     * @param group_id Die gruppe in dem da sitem ist
     * @param sl_id    die shoppinglist in dem das item ist
     * @param newname  der neue name
     * @param newcount die neue anzahl
     * @throws SQLException
     * @throws JSONException
     */
    public void editItem(String item_id, String group_id, String sl_id, String newname, int newcount) throws SQLException, JSONException {
        Item olditem = getItem(item_id);

        if (!olditem.getName().equals(newname) && newname != null) {
            sqlUpdate4Param("UPDATE \"Item\" SET name = ? WHERE item_id = ? AND group_id = ? AND sl_id = ?", newname, item_id, group_id, sl_id);
        }

        /*
        if (!oldgroup.getHidden().equals(newhidden) && newhidden != null) {
            sqlUpdate3Param("UPDATE \"Group\" SET hidden = ? WHERE group_id = ? AND sl_id = ?", newhidden, group_id, sl_id);
        }
*/
        if (Integer.parseInt(olditem.getCount()) != newcount) {
            sqlUpdate4ParamFirstInt("UPDATE \"Item\" SET count = ? WHERE item_id = ? AND group_id = ? AND sl_id = ?", newcount, item_id, group_id, sl_id);
        }
    }


    /**
     * Löscht ein item
     *
     * @param item_id  Item id
     * @param group_id group id
     * @param sl_id    shoppoinglist id
     */
    public void deleteItem(String item_id, String group_id, String sl_id) throws SQLException {
        sqlUpdate3Param("DELETE FROM \"Item\" WHERE item_id = ? AND group_id = ? AND sl_id = ?", item_id, group_id, sl_id);
    }

    /**
     * Fügt ein neues Item der Datenbank hinzu
     *
     * @param group_id Die group id in der das neue item angezeigt werden soll
     * @param sl_id    Die Shoppingliste in der das neue item nagezeigt werden soll
     * @param name     Der name des Items
     * @param count    Die anzahl des Items
     * @throws SQLException
     */
    public void addItem(String group_id, String sl_id, String name, int count) throws SQLException {
        sqlUpdate5Param("INSERT INTO \"Item\" VALUES (?,?,?,?,?)", generateItemId(), group_id, sl_id, name, count);
    }

    /**
     * Erstellt eine neue Gruppe
     *
     * @param sl_id  Shoppinglist id in welcher die Gruppe ist
     * @param name
     * @param color
     * @param hidden
     */
    public void addGroup(String sl_id, String name, String color, String hidden) throws SQLException {
        sqlUpdate4Param("INSERT INTO \"Group\" (group_id, sl_id, name, color, hidden) VALUES (?, ?,?,?, false)", generateGroupId(), sl_id, name, color);
    }

    /**
     * Bearbeitet eine Gruppe
     *
     * @param sl_id     Die Shoppinglist oid in welcher die gruppe ist
     * @param group_id  Die Group id der gruppe
     * @param newname   Der neue Name
     * @param newcolor  Die neue Farbe
     * @param newhidden Der neue hidden boolean
     * @throws SQLException
     * @throws JSONException
     */
    public void editGroup(String sl_id, String group_id, String newname, String newcolor, String newhidden) throws SQLException, JSONException {
        Group oldgroup = getGroup(group_id, sl_id);

        if (!oldgroup.getGroupName().equals(newname) && newname != null) {
            sqlUpdate3Param("UPDATE \"Group\" SET name = ? WHERE group_id = ? AND sl_id = ?", newname, group_id, sl_id);
        }

        /*
        if (!oldgroup.getHidden().equals(newhidden) && newhidden != null) {
            sqlUpdate3Param("UPDATE \"Group\" SET hidden = ? WHERE group_id = ? AND sl_id = ?", newhidden, group_id, sl_id);
        }
*/
        if (!oldgroup.getColor().equals(newcolor) && newcolor != null) {
            sqlUpdate3Param("UPDATE \"Group\" SET color = ? WHERE group_id = ? AND sl_id = ?", newcolor, group_id, sl_id);
        }

    }

    /**
     * Hollt ein bestimtes item
     *
     * @param item_id Die sl_id in der das item ist
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    public Item getItem(String item_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Item\") AS obj FROM \"Item\" JOIN \"Group\" USING (group_id) WHERE item_id = ?";
        JSONObject jsonObject = new JSONObject(executeQuery(SQL, item_id));
        return new Item(generateItemId(), jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("count"));
    }

    /**
     * Hollt alle daten einer Bestimmten group und erstellt damit ein Group object
     *
     * @param group_id Group id die zu holen ist
     * @param sl_id    Shoppingliste der group
     * @throws SQLException
     * @throws JSONException
     */
    public Group getGroup(String group_id, String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Group\") AS obj FROM \"Group\" WHERE group_id = ? AND sl_id = ?";
        JSONObject jsonObject = new JSONObject(executeQuery2Param(SQL, group_id, sl_id));
        return new Group(jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("color"), jsonObject.getString("hidden"));
    }

    /**
     * Löscht eine Shoppingliste aus der Tabelle:
     * Shoppinglist / - member / -admin
     *
     * @param sl_id Shoppinglist Id welche gelöscht werden soll
     * @throws SQLException
     */
    public void delShoppinglist(String sl_id) throws SQLException {
        sqlUpdate("DELETE FROM \"Shoppinglist_admin\" WHERE sl_id = ?", sl_id);
        sqlUpdate("DELETE FROM \"Shoppinglist_member\" WHERE sl_id = ?", sl_id);
        sqlUpdate("DELETE FROM \"Shoppinglist\" WHERE sl_id = ?", sl_id);
    }


    /**
     * Erstellt eine neue Shoppingliste mit den dazugehörigen Usern
     *
     * @param name        Name der Shoppingliste
     * @param description Beschreibung der Shoppingliste
     * @param username    Username des erstellers der Shoppingliste
     * @param color       Farbe der Shoppingliste
     * @throws SQLException
     */
    public void addShoppinglist(String name, String description, String username, String color) throws SQLException, JSONException {
        String sl_id = generateSL_Id(sl_idLength);
        if (!checkIfUserExists(username)) {
            createUser(username);
        }
        createShoppinglist(sl_id, name, description, color);
        createAdmin(sl_id, username);

    }

    /**
     * Erstellt einen neuen Admin in der Tabelle Shoppinglist_admin
     *
     * @param sl_id    Die Shopppinglist Id
     * @param username Der username des Admins
     * @throws SQLException
     */
    private void createAdmin(String sl_id, String username) throws SQLException {
        String SQL = "INSERT INTO \"Shoppinglist_admin\" (username, sl_id) VALUES (?, ?)";
        sqlUpdate2Param(SQL, username, sl_id);
    }

    /**
     * Erstellt einen neue Shoppingliste in der Tabelle Shoppinglist
     *
     * @param sl_id       Shopppinglist Id
     * @param name        Shoppinglist name
     * @param description Shoppinglist beschriebung
     * @param color       Shoppinglist Farbe
     * @throws SQLException
     */
    private void createShoppinglist(String sl_id, String name, String description, String color) throws SQLException {
        String SQL = "INSERT INTO \"Shoppinglist\" (sl_id, name, description, color) VALUES (?, ?, ?, ?)";
        sqlUpdate4Param(SQL, sl_id, name, description, color);
    }

    /**
     * Erstellt einen neuen User, wenn keiner existiert
     *
     * @param username Der Username des neuen Users
     * @throws SQLException
     */
    private void createUser(String username) throws SQLException {
        String SQL = "INSERT INTO \"User\" (username) VALUES (?)";
        sqlUpdate(SQL, username);
    }

    /**
     * Prüft ob ein User bereits in der DB vorhanden ist. Wenn ja dann wird true returned
     *
     * @param username Der username nach dem geprüft werden soll
     * @return True wenn User existiert, False wenn nicht
     * @throws SQLException
     */
    private boolean checkIfUserExists(String username) throws SQLException, JSONException {
        String SQL = "SELECT username FROM \"User\"";

        ArrayList<String> outUserList = new ArrayList<String>();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, username);
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            outUserList.add(jsonObject.getString("username"));
        }

        if (outUserList.contains(username)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verbindet sich mit dem Server
     * Holt die eigenen Shoppinglisten vom Server. Und speichert diese in eine List mit Shoppinglist Objekten
     *
     * @param uid Die UID auf welche die Abfrage ausgeführt werden soll
     * @return Das Ergebnis der eigenen Shoppinglisten in einer List gefüllt mit Shoppinglist Objekten
     * @throws JSONException Ein JSON Umwandlungsfehler
     * @throws SQLException  Ein PostgreSQL Fehler
     */
    public List<Shoppinglist> getMyShoppinglists(String uid) throws JSONException, SQLException {
        final String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_admin\" USING (sl_id) WHERE username = ?";

        ArrayList<Shoppinglist> shoppinglistsList = new ArrayList<Shoppinglist>();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, uid);
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            shoppinglistsList.add(new Shoppinglist(jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color")));
        }
        return shoppinglistsList;
    }

    /**
     * Verbindet sich mit dem server
     * Holt alle shared shoppinglists des users
     *
     * @param uid User von dem die Shared Shoppinglists geholt werden sollen
     * @return Die Shared Shoppinglisten des Users
     * @throws SQLException
     * @throws JSONException
     */
    public List<Shoppinglist> getSharedShoppinglists(String uid) throws SQLException, JSONException {
        final String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_member\" USING (sl_id) WHERE username = ?";

        ArrayList<Shoppinglist> shoppinglistArrayList = new ArrayList<Shoppinglist>();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, uid);
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            shoppinglistArrayList.add(new Shoppinglist(jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color")));
        }
        return shoppinglistArrayList;
    }


    /**
     * Hoolt alle groups und items der list und erstelt ein Detail objekt von jeder group. Die detail objekte kommen in eine List
     *
     * @param sl_id Shoppinglist Id mit der gearbeitet wird
     * @return Eine List mit Details über jede Shoppinglist
     * @throws SQLException
     * @throws JSONException
     */
    public List<Details> getListDetails(String sl_id) throws SQLException, JSONException {

        List<Group> groups = getGroups(sl_id);

        List<Item> items = getItems(sl_id);

        ArrayList<Details> detailsArrayList = new ArrayList<Details>();
        for (Group group : groups) {
            Details detailsTmp = new Details(group);
            for (Item item : items) {
                if (group.getGroup_id().equals(item.getGroup_id())) {
                    detailsTmp.addItem(item);
                }
            }
            detailsArrayList.add(detailsTmp);
        }

        return detailsArrayList;


    }

    /**
     * Holt alle Items einer bestimmten gruppe
     *
     * @param group_id Gruppe welche geholt werden soll
     * @param sl_id    Die Shoppinglist in der sich die gruppe befindet
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    public List<Item> getItemsOfGroup(String group_id, String sl_id) throws SQLException, JSONException {
        List<Details> details = getListDetails(sl_id);
        ArrayList<Item> result = new ArrayList<Item>();

        for (Details d : details) {
            String group_idtmp = d.getGroup().getGroup_id();
            if (group_idtmp.equals(group_id)) {
                result = d.getItems();
            }
        }

        return result;
    }

    /**
     * Generiert eine neue 8 stellige sl_id
     *
     * @return Neue Sl_id
     */
    private String generateSL_Id(int length) {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String output = "";

        for (int i = 0; i < length; i++) {
            output += possible.charAt((int) Math.floor(Math.random() * possible.length()));
        }

        System.out.println("Generate SL_ID: " + output);

        return output;
    }

    /**
     * Generiert eine neue 8 stellige group_id
     *
     * @return Neue group_id
     */
    private String generateGroupId() {
        return generateSL_Id(groupIdLength);
    }

    /**
     * Generiert eine neue 8 stellige item_id
     *
     * @return Neue item_id
     */
    public String generateItemId() {
        return generateSL_Id(itemIdLength);
    }

    /**
     * Generiert eine neue 8 stellige inviteLink
     *
     * @return Neue intielink
     */
    public String generateInviteLink() {
        return generateSL_Id(inviteLength);
    }


    /**
     * Holt alle erledigten Items eines Users
     *
     * @return Die erledigten Items in eines Users
     * @throws SQLException
     * @throws JSONException
     */
    public List<Item> getDoneItems() throws SQLException, JSONException {
        final String SQL = "SELECT row_to_json(\"Done_Purchase\") AS obj FROM \"Done_Purchase\" WHERE username = ?";
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ArrayList<Item> listItems = new ArrayList<Item>();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, uid);
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            String itemId = jsonObject.getString("purchased_item_id");
            String name = jsonObject.getString("name");
            String count = jsonObject.getInt("count") + "";
            listItems.add(new Item(itemId, null, null, name, count));
        }
        return listItems;
    }

    /**
     * Holt alle Items einer bestimmten shoppingliste, angegeben durch die shoppinglist id
     *
     * @param sl_id
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    public List<Item> getItems(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Item\") AS obj FROM \"Item\" JOIN \"Group\" USING (group_id) WHERE \"Group\".sl_id = ?";

        ArrayList<Item> listItems = new ArrayList<Item>();
        List<JSONObject> jsonObjects = executeQueryJSONObject(SQL, sl_id);
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            listItems.add(new Item(jsonObject.getString("item_id"), jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("count")));
        }

        return listItems;

    }

    /**
     * Holt alle gruppen einer bestimmten Shoppinglist id
     *
     * @param sl_id Holt alle goups dieser Shoppinglist id
     * @return Gibt alle groups der Abgefragten Shoppinglist id zurück
     * @throws SQLException
     * @throws JSONException
     */
    private List<Group> getGroups(String sl_id) throws SQLException, JSONException {
        String SQLGroups = "SELECT row_to_json(\"Group\") AS obj FROM \"Group\" JOIN \"Shoppinglist\" USING (sl_id) WHERE sl_id = ?";

        List<JSONObject> jsonObjects = executeQueryJSONObject(SQLGroups, sl_id);
        ArrayList<Group> listGroup = new ArrayList<Group>();
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            listGroup.add(new Group(jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("color"), jsonObject.getString("hidden")));
        }
        return listGroup;
    }

    /**
     * Hollt eine Shoppingliste vom server
     *
     * @param sl_id Shoppingliste welche heruntergelanden werden soll
     * @return Ein Shoppinglist Objekt
     * @throws SQLException
     * @throws JSONException
     */
    public Shoppinglist getShoppinglist(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_admin\" USING (sl_id) WHERE sl_id = ?";
        JSONObject jsonObject = new JSONObject(executeQuery(SQL, sl_id));

        return new Shoppinglist(sl_id, jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color"));
    }

    /**
     * Führt ein SQL Befehl aus und gibt die antwort in ein JSONObject List
     *
     * @param SQL   Der SQL der auszuführen ist
     * @param param 1. Param
     * @param param 2. Param
     * @return Das ergebnis als JSONObject
     * @throws SQLException
     * @throws JSONException
     */
    public List<JSONObject> executeQueryJSONObject2Param(String SQL, String param, String param2) throws SQLException, JSONException {
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        ResultSet rsgroups = pstmt.executeQuery();
        System.out.println(param);
        while (rsgroups.next()) {
            String groupString = rsgroups.getString(1);
            JSONObject jsonObject = new JSONObject(groupString);
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    /**
     * Führt ein SQL Befehl aus und gibt die antwort in ein JSONObject List
     *
     * @param SQL   Der SQL der auszuführen ist
     * @param param 1. Param
     * @return Das ergebnis als JSONObject
     * @throws SQLException
     * @throws JSONException
     */
    public List<JSONObject> executeQueryJSONObject(String SQL, String param) throws SQLException, JSONException {
        ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        ResultSet rsgroups = pstmt.executeQuery();
        System.out.println(param);
        while (rsgroups.next()) {
            String groupString = rsgroups.getString(1);
            JSONObject jsonObject = new JSONObject(groupString);
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    /**
     * Führt ein SQL mit einem Parameter aus und liefert den ersten String
     *
     * @param SQL   SQL Befehl
     * @param param 1. Param
     * @param param 2. Param
     * @return Erster result String
     * @throws SQLException
     */
    private String executeQuery2Param(String SQL, String param, String param2) throws SQLException {
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        ResultSet rs = pstmt.executeQuery();

        rs.next();
        return rs.getString(1);
    }

    /**
     * Führt ein SQL mit einem Parameter aus und liefert den ersten String
     *
     * @param SQL   SQL Befehl
     * @param param 1. Param
     * @return Erster result String
     * @throws SQLException
     */
    private String executeQuery(String SQL, String param) throws SQLException {
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        ResultSet rs = pstmt.executeQuery();

        rs.next();
        return rs.getString(1);
    }

    /**
     * Bearbeitet die Eigenschaften einer Shoppingliste
     *
     * @param sl_id          Shoppinglist Id welche zu bearbeiten ist
     * @param newname        Neuer Shoppinglistname
     * @param newdescription Neue Shoppinglist Beschreibung
     * @param newColor       Neue Shoppinglist Farbe
     * @throws SQLException
     * @throws JSONException
     */
    public void editShoppinglist(String sl_id, String newname, String newdescription, String newColor) throws SQLException, JSONException {
        Shoppinglist oldShoppinglist = getShoppinglist(sl_id);

        if (!oldShoppinglist.getname().equals(newname) && newname != null) {
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET name = ? WHERE sl_id = ?", newname, sl_id);
        }

        if (!oldShoppinglist.getdescription().equals(newdescription) && newdescription != null) {
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET description = ? WHERE sl_id = ?", newdescription, sl_id);
        }

        if (!oldShoppinglist.getcolor().equals(newColor) && newColor != null) {
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET color = ? WHERE sl_id = ?", newColor, sl_id);
        }
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @param param3 ein 3. parameter
     * @param param4 ein 4. Parameter
     * @throws SQLException
     */
    private void sqlUpdate4ParamFirstInt(String SQL, int param, String param2, String param3, String param4) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setInt(1, param);
        pstmt.setString(2, param2);
        pstmt.setString(3, param3);
        pstmt.setString(4, param4);
        pstmt.executeUpdate();
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @param param3 ein 3. parameter
     * @param param4 ein 4. Parameter
     * @param param5 Ein datum des Typen java.sql.Date
     * @throws SQLException
     */
    private void sqlUpdate5ParamLastIsDateAndInt(String SQL, String param, String param2, String param3, int param4, java.sql.Date param5) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        pstmt.setString(3, param3);
        pstmt.setInt(4, param4);
        pstmt.setDate(5, param5);
        pstmt.executeUpdate();
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @param param3 ein 3. parameter
     * @param param4 ein 4. Parameter
     * @param param5 ein 5. Parameter
     * @throws SQLException
     */
    private void sqlUpdate5Param(String SQL, String param, String param2, String param3, String param4, int param5) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        pstmt.setString(3, param3);
        pstmt.setString(4, param4);
        pstmt.setInt(5, param5);
        pstmt.executeUpdate();
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @param param3 ein 3. parameter
     * @param param4 ein 4. Parameter
     * @throws SQLException
     */
    private void sqlUpdate4Param(String SQL, String param, String param2, String param3, String param4) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        pstmt.setString(3, param3);
        pstmt.setString(4, param4);
        pstmt.executeUpdate();
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @param param3 ein 3. parameter
     * @throws SQLException
     */
    private void sqlUpdate3Param(String SQL, String param, String param2, String param3) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        pstmt.setString(3, param3);

        pstmt.executeUpdate();
    }


    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL    Der SQL befehl
     * @param param  ein Parameter
     * @param param2 ein 2. Parameter
     * @throws SQLException
     */
    private void sqlUpdate2Param(String SQL, String param, String param2) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.setString(2, param2);
        pstmt.executeUpdate();
    }

    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL   Der SQL befehl
     * @param param ein Parameter
     * @throws SQLException
     */
    private void sqlUpdate(String SQL, String param) throws SQLException {
        //connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.executeUpdate();
    }

}
