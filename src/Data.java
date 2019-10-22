
import com.mysql.cj.protocol.Resultset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ilham Andrian
 */
public class Data {
    static ResultSet rs;
    
    public static int getUniqueUser() throws SQLException{
        String query = "SELECT COUNT(user_id) FROM user;";
        rs = MYSQLConnection.getData(query);
        rs.next();
        return rs.getInt(1);
    }
    
    public void getAllUserData(){
        String query = "SELECT * FROM user";
        MYSQLConnection.getData(query);
    }
    
    public static int getMaxLengthUserFilm() throws SQLException{
        String query = "SELECT MAX(watched) as 'max' FROM( SELECT COUNT(movie_id) as watched FROM usermovie GROUP BY user_id ) AS count";
        rs = MYSQLConnection.getData(query);
        rs.next();
        return rs.getInt(1);
    }
    
    public static List<Integer> getUsers() throws SQLException{
        List<Integer> user_list = new ArrayList<>();
        String query = "SELECT DISTINCT user_id FROM usermovie;";
        rs = MYSQLConnection.getData(query);
        while (rs.next()) {            
            int user_id = rs.getInt(1);
            user_list.add(user_id);
        }
        return user_list;
    }
    
    public static List<Integer> getMovies() throws SQLException{
        List<Integer> movie_list = new ArrayList<>();
        String query = "SELECT DISTINCT movie_id FROM usermovie;";
        rs = MYSQLConnection.getData(query);
        while (rs.next()) {            
            int user_id = rs.getInt(1);
            movie_list.add(user_id);
        }
        return movie_list;
    }
    
    
    
}
