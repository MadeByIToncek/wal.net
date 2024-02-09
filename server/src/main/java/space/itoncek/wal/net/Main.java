package space.itoncek.wal.net;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.Javalin;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    static Javalin app;
    static Connection conn;

    public static void main(String[] args) throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/walnet","root", "root");

        Algorithm algo = Algorithm.HMAC512(PasswordTools.getKey(conn));

        JWTGenerator<WalnetUser> generator = (user, alg) -> {
            JWTCreator.Builder token = JWT.create()
                    .withClaim("user", user.user())
                    .withClaim("passwordhash", user.passwordhash());
            return token.sign(alg);
        };

        JWTVerifier verifier = JWT.require(algo).build();

        JWTProvider<WalnetUser> provider = new JWTProvider<>(algo, generator, verifier);

        app = Javalin.create(ctx -> {
                    ctx.bundledPlugins.enableRedirectToLowercasePaths();
                    ctx.showJavalinBanner = true;
                    ctx.useVirtualThreads = true;
                    ctx.http.brotliAndGzipCompression();
                    ctx.http.prefer405over404 = true;
                })
                .before(ctx -> {
                    if(!ctx.path().equals("/login")) {
                        DecodedJWT token = verifier.verify(new JSONObject(ctx.body()).getString("token"));
                        ctx.header("activeUser", token.getClaim("user").asString());
                    }
                })
                .get("/", ctx -> ctx.result("Hello there!"))
                .post("/login/", ctx -> {
                    JSONObject j = new JSONObject(ctx.body());
                    WalnetUser mockUser = new WalnetUser(j.getString("user"), j.getString("pwd"));
                    String token = provider.generateToken(mockUser);
                    ctx.result(new JSONObject().put("token", token).toString());
                })
                .exception(JSONException.class, (e, ctx) ->  {
                    ctx.status(402).result("Unable to parse JSON");
                })
                .start();
    }
}