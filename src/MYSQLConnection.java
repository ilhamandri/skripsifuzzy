import com.mysql.cj.jdbc.Driver;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ilham Andrian
 */
public class MYSQLConnection {
    private String url;
    private String user;
    private String password;
    private String driver;
    static Connection conn;
    static Statement stmt;
    static ResultSet rs;
        
    public MYSQLConnection(){
        this.driver = "com.mysql.cj.jdbc.Driver";
        this.url = "jdbc:mysql://localhost/skripsifuzzy?serverTimezone=UTC";
        this.user = "root";
        this.password = "";
        connect();
    }
    
    public Connection connect(){
//        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("BERHASIL terhubung ke basis data");
        }
        catch(SQLException e){
            System.out.println("GAGAL terhubung ke basis data");
            System.out.println(e.getMessage());
        }
        
        return conn;
    }
    
    public static ResultSet getData(String query){
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
    
    public static void insertData(String query){
         try {
            stmt = conn.createStatement();
            stmt.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void executeQuery(String query){
         try {
            stmt = conn.createStatement();
            stmt.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
