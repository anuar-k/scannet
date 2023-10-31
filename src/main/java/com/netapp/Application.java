package com.netapp;

import com.netapp.controllers.ScannerController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Application {

    public void start() throws NullPointerException {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public", Location.CLASSPATH);
        }).start(80);

        app.get("/", ScannerController::get);
        app.post("/scan", ScannerController::scan);

    }
}