
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ilham Andrian
 */
public class CollaborativeFIltering {

    public CollaborativeFIltering() {
        MYSQLConnection.executeQuery("DELETE FROM similarity;");
        MYSQLConnection.executeQuery("DELETE FROM prediction_rating;");
    }

    public void calculateSimilarity() throws SQLException {
        String query = "SELECT user_id, avg_rating FROM user;";
        ResultSet users, clusters, users_in_cluster, user2;
        users = MYSQLConnection.getData(query);
        while (users.next()) {
            int user_1 = users.getInt(1);
            double avg_rating_user_1 = users.getDouble(2);
            String query2 = "SELECT cluster FROM cluster_user WHERE user_id = " + user_1 + ";";
            clusters = MYSQLConnection.getData(query2);
            while (clusters.next()) {
                int cluster = clusters.getInt(1);
                String query3 = "SELECT user_id FROM cluster_user WHERE cluster=" + cluster + " AND user_id != " + user_1 + ";";
                users_in_cluster = MYSQLConnection.getData(query3);
                while (users_in_cluster.next()) {
                    int user_2 = users_in_cluster.getInt(1);
                    double avg_rating_user_2 = getAvgRating(user_2);
                    List<int[]> same_movie = getSameMovie(user_1, user_2);
                    System.out.println("USER 1 : " + user_1);
                    System.out.println("USER 2 : " + user_2);
                    double numerator = 0;
                    double denominator = 0;
                    double denominatorA = 0;
                    double denominatorB = 0;
                    for (int i = 0; i < same_movie.size(); i++) {
                        int rating_user_1 = same_movie.get(i)[1];
                        int rating_user_2 = same_movie.get(i)[2];
                        double userA = (rating_user_1 - avg_rating_user_1);
                        double userB = (rating_user_2 - avg_rating_user_2);
                        numerator += userA * userB;
                        denominatorA += (Math.pow(userA, 2));
                        denominatorB += (Math.pow(userB, 2));
                        System.out.println(rating_user_1 + " : " + avg_rating_user_1 + "    -    " + rating_user_2 + " : " + avg_rating_user_2);
                    }
//                        System.out.println("BAWAH A : " + Math.sqrt(bawahA));
//                        System.out.println("BAWAH B : " + Math.sqrt(bawahB));
                    denominator = Math.sqrt(denominatorA) * Math.sqrt(denominatorB);
                    double hasil;
                    if (denominator == 0) {
                        hasil = -1.0;
                    } else {
                        hasil = numerator / denominator;
                    }
//                        System.out.println("ATAS : " + atas);
//                        System.out.println("BAWAH : " + bawah);
                    System.out.println("SIMILARITY A dan B : " + hasil);
                    String insertSimilarity = "INSERT INTO similarity(cur_user,target_user,value) VALUES(" + user_1 + "," + user_2 + "," + hasil + ")";
                    MYSQLConnection.insertData(insertSimilarity);
                }
                System.out.println("===============================");
            }
            System.out.println("++++++++++++++++++++++++++++++++");
        }
    }

    public void calculatePrediction() throws SQLException {
        String userQuery = "SELECT user_id FROM user";
        ResultSet users, unrated;
        users = MYSQLConnection.getData(userQuery);
        while (users.next()) {
            int user = users.getInt(1);
            double user_avg_rating = getAvgRating(user);
            List<Integer> unrated_movie = getUnratedMovie(user);
            for (int i = 0; i < unrated_movie.size(); i++) {
                int movie = unrated_movie.get(i);
                double prediction = 0;
                double divider = 0;
                List<Integer> compare_users = getCompareUser(user);
//                System.out.println("NEIGHBOUR : "+compare_users.size()+"(user: "+compare_users.get(0)+")");
                for (int j = 0; j < compare_users.size(); j++) {
                    int user_compare_id = compare_users.get(j);
                    double user_compare_avg_rating = getAvgRating(user_compare_id);
                    double user_compare_rating = getRating(user_compare_id, movie);
//                    System.out.println("USER COMPARE RATING  : "+user_compare_rating + "("+user_compare_id+" , "+movie+")");
                    if (user_compare_rating > 0) {
                        double similarity_value = getSimilarity(user, user_compare_id);
//                        System.out.println(similarity_value + " * ( " + user_compare_rating + " - " + user_compare_avg_rating + " )");
                        prediction += similarity_value * (user_compare_rating - user_compare_avg_rating);
                        divider += similarity_value;
                    }
                }

                if (divider != 0) {
                    double prediction_rating = user_avg_rating + (prediction / divider);
//                    System.out.println(user_avg_rating+" + ( " + prediction + " / " + divider + " )");
                    System.out.println("RATING USER: " + user + " | MOVIE:" + movie + "  --> " + prediction_rating);
                    MYSQLConnection.executeQuery("INSERT INTO prediction_rating(user_id, movie_id, rating) VALUES(" + user + "," + movie + "," + prediction_rating + ")");

                }
            }
        }
    }
    
    public void getUserRecommendation(int user_id) throws SQLException{
        String query = "SELECT movie_id , rating FROM prediction_rating WHERE user_id = " + user_id + " AND rating >= 3";
        ResultSet userRecommendation, movie_title;
        userRecommendation = MYSQLConnection.getData(query);
        while(userRecommendation.next()){
            int movie_id = userRecommendation.getInt(1);
            double rating = userRecommendation.getDouble(2);
            String movie = "SELECT title FROM movie WHERE movie_id = "+ movie_id;
            movie_title = MYSQLConnection.getData(movie);
            while(movie_title.next()){
                String getTtitle = movie_title.getString(1);
                System.out.println("title = " + getTtitle + " , " + "rating " + rating);
            }
        }
    }

    private List<int[]> getSameMovie(int user_1, int user_2) throws SQLException {
        List<int[]> movies = new ArrayList<>();
        String query = "SELECT d1.movie_id, d1.rating, d2.rating FROM (SELECT * FROM `usermovie` WHERE user_id = " + user_1 + " ) AS d1 ";
        query += "INNER JOIN (SELECT * FROM `usermovie` WHERE user_id = " + user_2 + " ) AS d2 ON d1.movie_id = d2.movie_id";

        ResultSet movies_id = MYSQLConnection.getData(query);
        while (movies_id.next()) {
            int[] datas = new int[3];
            datas[0] = movies_id.getInt(1);
            datas[1] = movies_id.getInt(2);
            datas[2] = movies_id.getInt(3);
            movies.add(datas);
        }

        return movies;
    }

    private List<Integer> getUnratedMovie(int user) throws SQLException {
        List<Integer> movies = new ArrayList<>();
        String query = "SELECT movie_id FROM movie WHERE movie_id NOT IN (" + getRatedMovie(user) + ")";
        ResultSet movies_id = MYSQLConnection.getData(query);
        while (movies_id.next()) {
            movies.add(movies_id.getInt(1));
        }

        return movies;
    }

    private double getRating(int user_id, int movie_id) throws SQLException {
        double rating = 0;
        String query = "SELECT rating FROM usermovie WHERE user_id = " + user_id + " AND movie_id = " + movie_id;
        ResultSet rs = MYSQLConnection.getData(query);
        while (rs.next()) {
            rating = rs.getDouble(1);
        }
        return rating;
    }

    private double getSimilarity(int curr_user, int target_user) throws SQLException {
        double similarity = Double.MIN_VALUE;
        String query = "SELECT value FROM similarity WHERE cur_user = " + curr_user + " AND target_user = " + target_user;
        ResultSet rs = MYSQLConnection.getData(query);
        while (rs.next()) {
            similarity = rs.getDouble(1);
        }
        return similarity;
    }

    private double getAvgRating(int user_id) throws SQLException {
        double avgRating = Double.MIN_VALUE;
        String query = "SELECT avg_rating FROM user WHERE user_id = " + user_id;
        ResultSet rs = MYSQLConnection.getData(query);
        while (rs.next()) {
            avgRating = rs.getDouble(1);
        }
        return avgRating;
    }

    private String getRatedMovie(int user_id) throws SQLException {
        String result = "";
        String query = "SELECT movie_id FROM usermovie WHERE user_id = " + user_id;
        ResultSet rs = MYSQLConnection.getData(query);
        while (rs.next()) {
            if (rs.isLast()) {
                result += rs.getString(1);
            } else {
                result += rs.getString(1) + ", ";
            }
        }
        return result;
    }

    private List<Integer> getCompareUser(int user) throws SQLException {
        List<Integer> compareUser = new ArrayList<>();
        String query = "SELECT target_user FROM similarity WHERE cur_user = " + user + " AND value > 0";
        ResultSet target_users_id = MYSQLConnection.getData(query);
        while (target_users_id.next()) {
            compareUser.add(target_users_id.getInt(1));
        }
        return compareUser;
    }
}
