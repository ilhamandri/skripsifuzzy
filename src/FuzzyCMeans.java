
import static java.lang.Double.NaN;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
import java.sql.*;
import java.util.List;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ilhamandrian
 */
public class FuzzyCMeans {

    private int number_of_cluster; //jumlah cluster yang diinginkan
    private int m = 2; //fuzzification parameter yang bernilai 2
    private int max_iteration;
    private double[][] init_member;
    private double[][] centroid;
    private double[][] distance; //cluster x user
    private double[] total_quadrat_membership;
    private boolean convergence_status = true;

    public FuzzyCMeans(int number_of_cluster, int max_iteration) throws SQLException {
        this.max_iteration = max_iteration;
        this.number_of_cluster = number_of_cluster;
        init_membership_matrix();
    }

    public void setInitMember(double[][] new_val) {
        this.init_member = new_val;

        //PRINT VALUE
        for (int i = 0; i < this.init_member.length; i++) {
            for (int j = 0; j < this.init_member[0].length; j++) {
                System.out.print(this.init_member[i][j] + "    ");
            }
            System.out.println("");
        }

        System.out.println("");
    }

    /**
     * Method untuk menginisialisasi nilai membership awal setiap cluster yang
     * jika di jumlahkan bernilai 1
     *
     * @return
     * @throws SQLException
     */
    public void init_membership_matrix() throws SQLException {
        MYSQLConnection.executeQuery("DELETE FROM user_cluster;");
        MYSQLConnection.executeQuery("DELETE FROM cluster_user;");

        Random r = new Random();
        int unikUser = Data.getUniqueUser();
        this.init_member = new double[unikUser][this.number_of_cluster];
        for (int i = 0; i < unikUser; i++) {
            int total = 0;
            for (int j = 0; j < this.number_of_cluster; j++) {
                int x = r.nextInt(999) + 1;
                this.init_member[i][j] = x;
                total += x;
            }

            for (int j = 0; j < number_of_cluster; j++) {
                double val = this.init_member[i][j] / total;
                this.init_member[i][j] = val;
                String query = "INSERT INTO user_cluster(user_id, cluster, value) VALUES (" + (i + 1) + "," + (j + 1) + "," + val + ")";
                MYSQLConnection.executeQuery(query);
            }
        }

//        //PRINT MEMBERSHIP
//        for (int i = 0; i < unikUser; i++) {
//            for (int j = 0; j < number_of_cluster; j++) {
//                System.out.println(this.init_member[i][j] + " ");
//            }
//            System.out.println();
//        }
    }

    public void kuadratCluster() throws SQLException {
        int unik = Data.getUniqueUser();
        this.total_quadrat_membership = new double[number_of_cluster];
        for (int i = 0; i < number_of_cluster; i++) {
            double total = 0.0;
            for (int j = 0; j < unik; j++) {
                total += Math.pow(this.init_member[j][i], 2);
            }
            this.total_quadrat_membership[i] = total;
        }

        //PRINT VALUE
        for (int i = 0; i < number_of_cluster; i++) {
            System.out.println("Kuadrat c" + (i + 1) + " " + this.total_quadrat_membership[i] + " ");
        }
        System.out.println("");
    }

    public void calculate_centroid() throws SQLException {
        ResultSet rs;

        List<Integer> movies = Data.getMovies();
        List<Integer> users = Data.getUsers();

        centroid = new double[number_of_cluster][movies.size()];
        for (int i = 0; i < number_of_cluster; i++) {
//            double divider = 0.0;
            for (int j = 0; j < movies.size(); j++) {
                int movie_id = movies.get(j);
                double c_temp = 0.0;
                for (int k = 0; k < users.size(); k++) {
                    int user_id = users.get(k);
                    rs = MYSQLConnection.getData("SELECT rating FROM usermovie WHERE user_id =" + user_id + " AND movie_id=" + movie_id);
                    if (rs.next()) {
                        int rating = rs.getInt(1);
//                        System.out.println("Rating : " + rating);
                        double pengali = Math.pow(init_member[k][i], 2);
//                        System.out.println("value c" + i + " : " + init_member[k][i]);
//                        System.out.println("kuadrat : " + pengali);
                        c_temp += rating * pengali;
//                        System.out.println("cluster temp : " + c_temp);
//                        divider += pengali;
//                        System.out.println("divider : " + divider);
//                        System.out.println("---------------------");
//                        System.out.println("");
                    }
                }
                if (this.total_quadrat_membership[i] == 0) {
                    centroid[i][j] = 0;
                } 
                else {
                    centroid[i][j] = c_temp / this.total_quadrat_membership[i];
                }
//                System.out.println("-------> " + "C" + i + " movies : " + j + " : " + centroid[i][j]);
//                System.out.println("");
            }
        }

        //PRINT CENTROID VALUE
        for (int i = 0; i < centroid.length; i++) {
            System.out.println("CENTROID " + (i + 1) + " : ");
            for (int j = 0; j < centroid[i].length; j++) {
                System.out.println(centroid[i][j] + " ");
            }
            System.out.println("");
        }
        distance();
    }

    public void distance() throws SQLException {
        ResultSet rs;
        List<Integer> users = Data.getUsers();
        List<Integer> movies = Data.getMovies();

        this.distance = new double[number_of_cluster][users.size()];
        for (int i = 0; i < number_of_cluster; i++) {
            for (int j = 0; j < users.size(); j++) {
                double dist_temp = 0.0;
                int user_id = users.get(j);
                for (int k = 0; k < movies.size(); k++) {
                    int movie_id = movies.get(k);
//                    System.out.println("user_id " + user_id);
//                    System.out.println("movie_id : " + movie_id);
                    rs = MYSQLConnection.getData("SELECT rating FROM usermovie WHERE user_id=" + user_id + " AND movie_id=" + movie_id);
                    if (rs.next()) {
//                        System.out.println("centroid value : " + centroid[i][k]);
//                        System.out.println("rating : " + rs.getInt(1));
                        dist_temp += Math.pow(centroid[i][k] - rs.getInt(1), 2);
                    } else {
                        dist_temp += Math.pow(centroid[i][k] - 0, 2);
                    }
//                    System.out.println("dist_temp : " + dist_temp);
//                    System.out.println("");
                }
                double distance_to_centroid = Math.sqrt(dist_temp);
                this.distance[i][j] = distance_to_centroid;
//                System.out.println("distance to centroid C"+i+" user " + i + " : " + distance_to_centroid);
//                System.out.println("----------------------------------");
//                System.out.println("");
            }
//            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        }

        //PRINT VALUE
        for (int i = 0; i < number_of_cluster; i++) {
            System.out.println("DISTANCE " + (i + 1) + " : ");
            for (int j = 0; j < users.size(); j++) {
                System.out.println(this.distance[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("====================================================");
        renewCluster();
    }

    public void renewCluster() throws SQLException {
        ResultSet rs;
        List<Integer> users = Data.getUsers();
        for (int i = 0; i < users.size(); i++) {
            System.out.println("NEW MEMBERSHIP VALUE USER " + (i + 1));
            for (int j = 0; j < number_of_cluster; j++) {
                System.out.println("CLUSTER " + (j + 1));
                double divider = getDivider(i, j);
                double val = 1.0 / divider;
                if (val == NaN || divider == 0) {
                    val = 0;
                }
                double hasil = Math.abs(val - init_member[i][j]);
                System.out.println("OLD VALUE : " + init_member[i][j]);
                System.out.println("NEW VALUE : " + val);
                System.out.println("HASIL : " + hasil);
                System.out.println("");
                if (hasil > 0.0001) {
                    convergence_status = false;
                }
                init_member[i][j] = val;

                String query = "UPDATE user_cluster SET value = " + val + " WHERE user_id = " + (i + 1) + " AND cluster = " + (j + 1) + ";";
                MYSQLConnection.executeQuery(query);
            }
            System.out.println(" ");
        }

        // PRINT NEW INIT MEMBERSHIP
//        this.init_member = init_membership_matrix();
        for (int i = 0; i < Data.getUniqueUser(); i++) {

            double totals = 0;
            for (int j = 0; j < this.number_of_cluster; j++) {
                System.out.print(init_member[i][j] + " ");
                totals += init_member[i][j];
            }
            System.out.println("");
            System.out.println("TOTAL = " + totals);
            System.out.println("");
        }
    }

    private double getDivider(int user_idx, int cluster) {
        double divider = 0;

        double x = distance[cluster][user_idx];
        if (x == 0) {
            return 0;
        }
//        System.out.println(" X :  " + x);
        for (int i = 0; i < number_of_cluster; i++) {
//            System.out.println("x / distance = " + x + " / " + distance[i][user_idx]);
            divider += Math.pow((x / distance[i][user_idx]), 2 / (this.m - 1));
        }
//        System.out.println("DIVIDER : " + divider);
        return divider;
    }

    public void checkConvergence() throws SQLException {
        for (int i = 0; i < this.max_iteration; i++) {
            if (i == 0) {
                this.convergence_status = false;
            }
            System.out.println("====== ITERASI " + (i + 1) + " ======");
            kuadratCluster();
            calculate_centroid();
            System.out.println("===============================");
//            this.convergence_status = false;
            if (this.convergence_status == true) {
                System.out.println("++++++++++++++++++++++++++ LAST ITERATION : " + (i + 1) + " +++++++++++++++++++++++++++++++++++++");
                break;
            }
            this.convergence_status = true;
        }

        setClusterUser();
    }

    public void setClusterUser() throws SQLException {
        String query = "SELECT user_id FROM user;";
        ResultSet users, clusters, maxs;
        users = MYSQLConnection.getData(query);
        while (users.next()) {
            int user_id = users.getInt(1);
            query = "SELECT MAX(value) FROM user_cluster WHERE user_id = " + user_id + ";";
            maxs = MYSQLConnection.getData(query);
            while (maxs.next()) {
                double max = maxs.getDouble(1);
                query = "SELECT cluster FROM user_cluster WHERE user_id = " + user_id + " AND value = " + max + ";";
                clusters = MYSQLConnection.getData(query);
                while (clusters.next()) {
                    int cluster = clusters.getInt(1);
                    query = "INSERT INTO cluster_user(cluster, user_id) VALUES(" + cluster + "," + user_id + ")";
                    MYSQLConnection.executeQuery(query);
                }
            }
        }
    }

}
