// database import statements
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args)
    {

        // ask for username and password


        //create database usernamepassword;
        //use usernamepassword;
        //
        //create table users
        //(
        // username varchar(20),
        // password varchar(20),
        // role varchar(20)
        //);
        //
        //insert into users (username, password, role) values ('test', 'password1234', 'all');
        //insert into users (username, password, role) values ('admin', 'itsasecret', 'all');
        //insert into users (username, password, role) values ('person1', 'hello', 'all');

        String url = "jdbc:mysql://192.168.98.249:3306/usernamepassword"; // Change database name
        String user = "students"; // Change username if necessary
        String password = "students"; // Change your MySQL password

        // Establish the connection
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection successful!");
            try {
                // 3. Prepare an SQL query to retrieve user credentials:
                String query = "SELECT * FROM RockPaperScissors.users";
                PreparedStatement statement = conn.prepareStatement(query);

                // 4. Execute the query and retrieve the result set:
                ResultSet resultSet = statement.executeQuery();

                // 5. Check if the result set has any rows, indicating a successful match of username and password:
                while (resultSet.next()) {
                    String databaseUsername = resultSet.getString("username");
                    String databasePassword = resultSet.getString("password");

                    System.out.printf( "Username is %s\t password is %s\n", databaseUsername, databasePassword);
                }
            }
            catch ( SQLException e)
            {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}