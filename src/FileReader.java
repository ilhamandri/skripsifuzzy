
import java.io.IOException;
import java.util.HashMap;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.sql.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ilhamandrian
 */
public class FileReader {

//    private HashMap<Integer, HashMap<Integer, Integer>> data;
    private MYSQLConnection db;
    private ResultSet rs;

    public FileReader(MYSQLConnection db) {
        this.db = db;
        db.executeQuery("DELETE FROM user;");
        db.executeQuery("DELETE FROM movie;");
        db.executeQuery("DELETE FROM usermovie;");
    }

    public void insertData(String filename, String file_type) throws SQLException {
        try {
            List<String> AllLine = Files.readAllLines(Paths.get(filename));
            if (file_type.equalsIgnoreCase("UserMovie")) {
                for (String line : AllLine) {
                    insertDataUserMovie(line);
                }
            } else if (file_type.equalsIgnoreCase("User")) {
                for (String line : AllLine) {
                    insertDataUser(line);
                }
            } else if (file_type.equalsIgnoreCase("Movie")) {
                for (String line : AllLine) {
                    insertDataMovie(line);
                }
            }
            calculateAverageRating();
        } catch (IOException e) {
            System.out.println("ERROR READ FILE");
            e.printStackTrace();
        }
    }
    
    private void insertDataUser(String line){
        String[] data = line.split(";");
        int user_id = Integer.parseInt(data[0]);
        int age = Integer.parseInt(data[2]);
        String query = "INSERT INTO user(user_id, gender, age) VALUES("+user_id+",'"+data[1]+"',"+age+")";
        db.insertData(query);
    }
    
    private void insertDataMovie(String line){
        String[] data = line.split(";");
        int movie_id = Integer.parseInt(data[0]);
        String title = data[1];
        String query = "INSERT INTO movie (movie_id,title) VALUES("+movie_id+" , '"+title+"') ";
//        System.out.println(query);
        db.insertData(query);
    }

    private void insertDataUserMovie(String line){
        String[] data = line.split(";");
        int user = Integer.parseInt(data[0]);
        int movie = Integer.parseInt(data[1]);
        int rating = Integer.parseInt(data[2]);
        String query = "INSERT INTO usermovie (user_id, movie_id, rating) VALUES("+user+","+movie+","+rating+")";
//        System.out.println(query);
        db.insertData(query);
    }
    
    public void calculateAverageRating() throws SQLException{
        String query = "SELECT user_id, AVG(rating) FROM usermovie GROUP BY user_id";
        rs = db.getData(query);
        while(rs.next()){
            double avg_rating = rs.getDouble(2);
            int user_id = rs.getInt(1);
            String update = "UPDATE user SET avg_rating = " + avg_rating + " WHERE user_id = " + user_id; 
            db.executeQuery(update);
        }
    }
}
