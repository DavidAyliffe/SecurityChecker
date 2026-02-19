import org.mindrot.jbcrypt.BCrypt;

/**
 * HashPassword — one-shot utility for generating BCrypt password hashes.
 *
 * BCrypt is a deliberately slow hashing algorithm designed for passwords.
 * It embeds a random salt and a cost factor into the output string, so the
 * hash is self-contained: you never need to store the salt separately, and
 * you can increase the cost factor in future without invalidating old hashes.
 *
 * Usage (from the project root):
 *   mvn compile exec:java -Dexec.mainClass=HashPassword -Dexec.args="yourpassword"
 *
 * Copy the 60-character hash printed to stdout into the INSERT statement in
 * schema.sql, then load that file into MySQL.
 *
 * This utility is intentionally kept separate from Main so that it can be
 * run offline — it requires no database connection.
 */
public class HashPassword {

    public static void main(String[] args) {

        // Require exactly one argument (the plain-text password).
        // Printing usage to stderr and exiting with a non-zero code lets shell
        // scripts and CI pipelines detect the error automatically.
        if (args.length == 0) {
            System.err.println("Usage: HashPassword <password>");
            System.exit(1);
        }

        // BCrypt.gensalt(10) generates a random salt with a cost factor of 10
        // (2^10 = 1 024 rounds).  Higher values are slower to compute, which
        // makes brute-force attacks more expensive — 10 is a widely accepted
        // default that balances security and login latency on modern hardware.
        //
        // BCrypt.hashpw() applies the algorithm and returns a 60-character
        // string in Modular Crypt Format:
        //   $2a$<cost>$<22-char salt><31-char hash>
        // This entire string is stored in the database and later passed
        // verbatim to BCrypt.checkpw() in Main.java for verification.
        String hash = BCrypt.hashpw(args[0], BCrypt.gensalt(10));

        // Print only the hash so the output can be piped or copy-pasted
        // directly into schema.sql without any extra formatting to strip out.
        System.out.println(hash);
    }
}
