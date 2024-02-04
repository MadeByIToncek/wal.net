package space.itoncek.wal.net;

import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class PasswordTools {
    public static String hash(@NotNull Connection dbcon,
                              @NotNull String password) throws NoSuchAlgorithmException, SQLException, InvalidKeySpecException {
        ResultSet rs = dbcon.createStatement().executeQuery("SELECT * FROM walnet_cache WHERE key='salt'");
        byte[] salt;
        if(rs.next()) {
            salt = Base64.getDecoder().decode(rs.getString("value"));
        } else {
            salt = generateSalt(2048);
            byte[] encode = Base64.getEncoder().encode(salt);
            dbcon.createStatement().executeUpdate("INSERT INTO walnet_cache VALUES ('%s', '%s'); ".formatted("salt",new String(encode)));
        }
        rs.close();

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        return new String(Base64.getEncoder().encode(factory.generateSecret(spec).getEncoded()));
    }

    public static boolean validate(@NotNull String username,
                                   @NotNull String b64Passwordhash,
                                   @NotNull Connection dbcon) throws SQLException, AuthenticationException {
        ResultSet rs = dbcon.createStatement().executeQuery("SELECT * FROM walnet_users WHERE `user`='%s' LIMIT 1;".formatted(username));
        if(rs.next()) if (rs.getString("password").equals(b64Passwordhash)) return true;
        else throw new AuthenticationException("Invalid password");
        else throw new AuthenticationException("This user does not exist!");
    }

    private static byte[] generateSalt(int saltLenght) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltLenght];
        random.nextBytes(salt);
        return salt;
    }


}
