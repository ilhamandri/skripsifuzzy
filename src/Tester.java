
import java.sql.*;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ilham Andrian
 */
public class Tester {

    static Connection conn;
    static Statement stmt;
    static ResultSet rs;

    public static void main(String[] args) throws SQLException {
        MYSQLConnection db = new MYSQLConnection();
        FileReader file_reader = new FileReader(db);
        file_reader.insertData("Dummy/user.txt", "User");
        file_reader.insertData("Dummy/movie.txt", "Movie");
        file_reader.insertData("Dummy/user_rating.txt", "UserMovie");
        Scanner sc = new Scanner(System.in);
        Data d = new Data();
        int uniq_users = d.getUniqueUser();
        System.out.print("JUMLAH CLUSTER : ");
        int n_cluster = sc.nextInt();
        System.out.print("BANYAK ITERASI : ");
        int n_times = sc.nextInt();

        if (n_cluster < uniq_users) {
            FuzzyCMeans fcm = new FuzzyCMeans(n_cluster, n_times);

            System.out.print("PAKAI INPUT MANUAL ? ( Y / N ) ");
            String res = sc.next();
            if (res.compareToIgnoreCase("Y") == 0) {
                double[][] initMember = new double[uniq_users][n_cluster];
                for (int i = 0; i < uniq_users; i++) {
                    for (int j = 0; j < n_cluster; j++) {
                        initMember[i][j] = sc.nextDouble();
                    }
                }
                fcm.setInitMember(initMember);
            }

            fcm.checkConvergence();

            CollaborativeFIltering cf = new CollaborativeFIltering();
            cf.calculateSimilarity();
//            cf.calculatePrediction();
//            System.out.println();
//            System.out.println("Masukkan userID untuk melihat rekomendasi : ");
//            int user_id = sc.nextInt();
//            cf.getUserRecommendation(user_id);
        }
        else{
            System.out.println("JUMLAH CLUSTER YANG DIMASUKKAN TIDAK BOLEH SAMA ATAU LEBIH BESAR DARI JUMLAH USER");
        }

//        System.out.println(fcm.print_init_membership_matrix());          
//        fcm.init_membership_matrix();
//        fcm.kuadratCluster();
//        fcm.calculate_centroid();
////        fcm.kuadratCluster();
//        file_reader.insertData("Dummy/user.txt", "User");
//        file_reader.insertData("Dummy/movie.txt", "Movie");
//        file_reader.insertData("Dummy/user_rating.txt", "UserMovie");
//        file_reader.getAllUserData();
    }
}
