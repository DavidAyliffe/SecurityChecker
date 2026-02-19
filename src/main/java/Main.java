import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

/**
 * SecurityChecker — command-line login verifier.
 *
 * Reads database credentials from a .env file (falling back to real
 * environment variables if the file is absent), then prompts the operator
 * for a username and password, looks up the stored BCrypt hash in MySQL,
 * and reports whether authentication succeeded.
 *
 * Setup:
 *   1. Run schema.sql against your MySQL server to create the database and
 *      users table.
 *   2. Use HashPassword to generate BCrypt hashes for each user and insert
 *      them via schema.sql.
 *   3. Fill in .env with DB_URL, DB_USER, and DB_PASSWORD.
 *   4. mvn package
 *   5. java -jar target/SecurityChecker-1.0-SNAPSHOT.jar
 */
public class Main {

    public static void main(String[] args) {

        // ---------------------------------------------------------------------------
        // 1. Load configuration
        // ---------------------------------------------------------------------------

        // Dotenv looks for a .env file in the working directory.
        // ignoreIfMissing() means the app still works when credentials are
        // supplied as real environment variables instead (e.g. in CI/CD).
        Dotenv dotenv = Dotenv.configure()
                              .ignoreIfMissing()
                              .load();

        // Pull the three required values.  dotenv.get() first checks the .env
        // file, then falls back to the actual process environment, and returns
        // null if the key is absent in both places.
        String url        = dotenv.get("DB_URL");
        String dbUser     = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");

        // All three values are mandatory — abort early with a clear message if
        // any are missing so the operator knows exactly what to fix.
        if (url == null || dbUser == null || dbPassword == null) {
            System.err.println("Error: DB_URL, DB_USER, and DB_PASSWORD must be set in .env or the environment.");
            System.exit(1);
        }

        // ---------------------------------------------------------------------------
        // 2. Collect credentials from the operator
        // ---------------------------------------------------------------------------

        // Wrap Scanner in try-with-resources so it is always closed when we
        // are done reading input, avoiding a resource leak.
        String inputUsername;
        String inputPassword;
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Username: ");
            inputUsername = scanner.nextLine();  // read the full line, including spaces

            System.out.print("Password: ");
            inputPassword = scanner.nextLine();  // plain-text; compared to the stored hash below
        }

        // ---------------------------------------------------------------------------
        // 3. Query the database
        // ---------------------------------------------------------------------------

        // A parameterised query prevents SQL injection: the username supplied
        // by the operator is always treated as a data value, never as SQL syntax.
        String query = "SELECT password, role FROM users WHERE username = ?";

        // try-with-resources guarantees Connection and PreparedStatement are
        // closed (and the connection returned to the driver pool) even if an
        // exception is thrown.
        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement statement = conn.prepareStatement(query)) {

            // Bind the operator-supplied username to the ? placeholder.
            statement.setString(1, inputUsername);

            // Execute the SELECT and keep the result set open inside a nested
            // try-with-resources so it too is closed automatically.
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    // A row was found — retrieve the stored BCrypt hash and the
                    // user's role (e.g. "admin" or "user").
                    String storedHash = resultSet.getString("password");
                    String role       = resultSet.getString("role");

                    // BCrypt.checkpw() re-hashes the plain-text password with
                    // the salt embedded in storedHash and compares the result.
                    // This is timing-safe by design and works even if the cost
                    // factor used when the hash was created differs from the
                    // current default.
                    if (BCrypt.checkpw(inputPassword, storedHash)) {
                        System.out.println("Login successful. Role: " + role);
                    } else {
                        // Wrong password — use the same message as the
                        // "user not found" branch to avoid leaking which
                        // part of the check failed (username enumeration).
                        System.out.println("Invalid username or password.");
                    }
                } else {
                    // No row matched the supplied username.  Return the same
                    // message as a password mismatch so an attacker cannot tell
                    // whether a given username exists in the database.
                    System.out.println("Invalid username or password.");
                }
            }

        } catch (SQLException e) {
            // Surface database-level errors (wrong URL, bad credentials,
            // network timeout, etc.) without exposing internal stack traces
            // to a potential attacker.
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
