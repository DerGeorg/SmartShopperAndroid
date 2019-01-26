package at.smartshopper.smartshopper;

import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection conect;
    final String HOST = "188.166.124.80";
    final String DB_NAME = "smartshopperdb";
    final String USERNAME = "smartshopper-user";
    final String PASSWORD = "jW^v#&LjNY_b3-k*jYj!U4Xz?T??m_D6249XAeWZ#7C^FRbKm!c_Dt+qj@4&a-Hs";
    final int PORT = 5432;


    public  Database(){};
    public void  connectDatabase() throws SQLException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        DriverManager.registerDriver(new org.postgresql.Driver());
        conect = DriverManager.getConnection("jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME, USERNAME, PASSWORD);

        System.out.println("Database connected!");
    }

    public List<Shoppinglist> getMyShoppinglists(String uid) throws JSONException {
        try {
            connectDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String SQL = "SELECT row_to_json(\"Shoppinglist\") AS obj FROM \"Shoppinglist\" JOIN \"Shoppinglist_admin\" USING (sl_id) WHERE username = ?";
        ArrayList<Shoppinglist> shoppinglistsList = null;
        try (
                Statement stmt = conect.createStatement();
                PreparedStatement pstmt = conect.prepareStatement(SQL)) {
            pstmt.setString(1, uid);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(uid);
                shoppinglistsList = new ArrayList<Shoppinglist>();
                while (rs.next()) {
                    String shoppinglist = rs.getString(1);
                    JSONObject jsonObject = new JSONObject(shoppinglist);

                    shoppinglistsList.add(new Shoppinglist(jsonObject.getString("sl_id"), jsonObject.getString("name"), jsonObject.getString("description"), jsonObject.getString("invitelink"), jsonObject.getString("color")));
                }
                Log.d("DATABASE SHOPPINGLISTS", shoppinglistsList.toString());

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return (List<Shoppinglist>) shoppinglistsList;

    }
}
