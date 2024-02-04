package space.itoncek.wal.net;

import io.javalin.Javalin;

public class Main {
    static Javalin app;

    public static void main(String[] args) {
        app = Javalin.create(ctx -> {
                    ctx.bundledPlugins.enableRedirectToLowercasePaths();
                    ctx.showJavalinBanner = true;
                    ctx.useVirtualThreads = true;
                    ctx.http.brotliAndGzipCompression();
                    ctx.http.prefer405over404 = true;
                })
                .get("/", ctx -> ctx.result("Hello there!"))
                .post("/login/", ctx -> {

                })
                .start();
    }
}