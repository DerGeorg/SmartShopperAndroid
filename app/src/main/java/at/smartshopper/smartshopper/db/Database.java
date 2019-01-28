package at.smartshopper.smartshopper.db;

import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.smartshopper.smartshopper.shoppinglist.Shoppinglist;
import at.smartshopper.smartshopper.shoppinglist.details.Details;
import at.smartshopper.smartshopper.shoppinglist.details.group.Group;
import at.smartshopper.smartshopper.shoppinglist.details.item.Item;

public class Database {

    private Connection conect;
    final String HOST = "188.166.124.80";
    final String DB_NAME = "smartshopperdb";
    final String USERNAME = "smartshopper-user";
    final String PASSWORD = "jW^v#&LjNY_b3-k*jYj!U4Xz?T??m_D6249XAeWZ#7C^FRbKm!c_Dt+qj@4&a-Hs";
    final int PORT = 5432;


    /**
     * Macht nix
     */
    public Database() {
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
    public void addShoppinglist(String name, String description, String username, String color) throws SQLException {
        String sl_id = generateSL_Id();
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
        connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, username);
        pstmt.setString(2, sl_id);
        pstmt.executeUpdate();
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
        connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, sl_id);
        pstmt.setString(2, name);
        pstmt.setString(3, description);
        pstmt.setString(4, color);
        pstmt.executeUpdate();
    }

    /**
     * Erstellt einen neuen User, wenn keiner existiert
     *
     * @param username Der Username des neuen Users
     * @throws SQLException
     */
    private void createUser(String username) throws SQLException {
        String SQL = "INSERT INTO \"User\" (username) VALUES (?)";
        connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, username);
        pstmt.executeUpdate();

    }

    /**
     * Prüft ob ein User bereits in der DB vorhanden ist. Wenn ja dann wird true returned
     *
     * @param username Der username nach dem geprüft werden soll
     * @return True wenn User existiert, False wenn nicht
     * @throws SQLException
     */
    private boolean checkIfUserExists(String username) throws SQLException {
        String SQL = "SELECT username FROM \"User\"";
        connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        ResultSet rs = pstmt.executeQuery();
        ArrayList<String> outUserList = new ArrayList<String>();
        while (rs.next()) {
            outUserList.add(rs.getString(1));
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
        String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_admin\" USING (sl_id) WHERE username = ?";

        connectDatabase();

        ArrayList<Shoppinglist> shoppinglistsList = new ArrayList<Shoppinglist>();

        ResultSet rs = null;
        try (PreparedStatement pstmt = conect.prepareStatement(SQL)) {
            pstmt.setString(1, uid);
            rs = pstmt.executeQuery();
            System.out.println(uid);


            while (rs.next()) {
                String shoppinglist = rs.getString(1);
                JSONObject jsonObject = new JSONObject(shoppinglist);

                shoppinglistsList.add(new Shoppinglist(jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }


        Log.d("DATABASE SHOPPINGLISTS", shoppinglistsList.toString());

        return (List<Shoppinglist>) shoppinglistsList;

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

        return (List<Details>) detailsArrayList;


    }

    /**
     * Generiert eine neue 8 stellige sl_id
     *
     * @return Neue Sl_id
     */
    private String generateSL_Id() {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String output = "";

        for (int i = 0; i < 8; i++) {
            output += possible.charAt((int) Math.floor(Math.random() * possible.length()));
        }

        System.out.println("Generate SL_ID: " + output);

        return output;
    }

    /**
     * Holt alle Items einer bestimmten shoppingliste, angegeben durch die shoppinglist id
     *
     * @param sl_id
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    private List<Item> getItems(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Item\") AS obj FROM \"Item\" JOIN \"Group\" USING (group_id) WHERE \"Group\".sl_id = ?";
        connectDatabase();

        ArrayList<Item> listItems = new ArrayList<Item>();
        ResultSet rsitems = null;
        try (PreparedStatement pstmt = conect.prepareStatement(SQL)) {
            pstmt.setString(1, sl_id);
            rsitems = pstmt.executeQuery();
            System.out.println(sl_id);
            while (rsitems.next()) {
                String itemsString = rsitems.getString(1);
                JSONObject jsonObject = new JSONObject(itemsString);

                listItems.add(new Item(jsonObject.getString("item_id"), jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("count")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return (List<Item>) listItems;

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
        connectDatabase();

        ResultSet rsgroups = null;
        ArrayList<Group> listGroup = new ArrayList<Group>();
        try (PreparedStatement pstmt = conect.prepareStatement(SQLGroups)) {
            pstmt.setString(1, sl_id);
            rsgroups = pstmt.executeQuery();
            System.out.println(sl_id);
            while (rsgroups.next()) {
                String groupString = rsgroups.getString(1);
                JSONObject jsonObject = new JSONObject(groupString);


                listGroup.add(new Group(jsonObject.getString("group_id"), jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("color"), jsonObject.getString("hidden")));

            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return (List<Group>) listGroup;


    }


    /**
     * Bearbeitet die Eigenschaften einer Shoppingliste
     * @param sl_id Shoppinglist Id welche zu bearbeiten ist
     * @param newname Neuer Shoppinglistname
     * @param newdescription Neue Shoppinglist Beschreibung
     * @param newColor Neue Shoppinglist Farbe
     * @throws SQLException
     * @throws JSONException
     */
    public void editShoppinglist(String sl_id, String newname, String newdescription, String newColor) throws SQLException, JSONException {
        Shoppinglist oldShoppinglist = getShoppinglist(sl_id);

        if(!oldShoppinglist.getname().equals(newname) && newname != null){
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET name = ? WHERE sl_id = ?", newname, sl_id);
        }

        if(!oldShoppinglist.getdescription().equals(newdescription) && newdescription != null){
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET description = ? WHERE sl_id = ?", newdescription, sl_id);
        }

        if(!oldShoppinglist.getcolor().equals(newColor) && newColor != null){
            sqlUpdate2Param("UPDATE \"Shoppinglist\" SET color = ? WHERE sl_id = ?", newColor, sl_id);
        }
    }


    /**
     * Führt einen SQL Befehl durch der keine rückgabe hat.
     *
     * @param SQL   Der SQL befehl
     * @param param ein Parameter
     * @param param2 ein 2. Parameter
     * @throws SQLException
     */
    private void sqlUpdate2Param(String SQL, String param, String param2) throws SQLException {
        connectDatabase();
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
        connectDatabase();
        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, param);
        pstmt.executeUpdate();
    }

    /**
     * Hollt eine Shoppingliste vom server
     * @param sl_id Shoppingliste welche heruntergelanden werden soll
     * @return Ein Shoppinglist Objekt
     * @throws SQLException
     * @throws JSONException
     */
    public Shoppinglist getShoppinglist(String sl_id) throws SQLException, JSONException {
        String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_admin\" USING (sl_id) WHERE sl_id = ?";
        connectDatabase();

        PreparedStatement pstmt = conect.prepareStatement(SQL);
        System.out.println(sl_id);
        pstmt.setString(1, sl_id);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        String resultString = rs.getString(1);
        JSONObject jsonObject = new JSONObject(resultString);

        return new Shoppinglist(sl_id, jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color"));
    }


    /**
     * NICHT VERWENDEN FUNKTIONIERT NICHT!!
     * <p>
     * <p>
     * NUR EIN KOPIE SAMPLE
     * <p>
     * Beim Start wird die Verbindung zum Server hergesetellt. Dann wird das resultSet von dem SQL reqest zurückgegeben
     *
     * @param SQL Der zumachende SQL Request
     * @param uid Die UID des Benutzers, für den die Abfrage gemacht wird
     * @return Das entstandene Result set, mit der Antwort des Servers
     */
    private ResultSet databaseRequest(String SQL, String uid) throws SQLException {
        connectDatabase();

        PreparedStatement pstmt = conect.prepareStatement(SQL);
        pstmt.setString(1, uid);
        ResultSet rs = pstmt.executeQuery();
        System.out.println(uid);

        //HIER
        //WEITER
        //PROGRAMMIEREN

        return rs;
    }
}
