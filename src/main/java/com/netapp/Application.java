package com.netapp;

import com.netapp.controllers.ScannerController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Application {

    public void start() throws NullPointerException {
        Javalin app = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH)).start(80);
        app.routes(() -> {
            path("/", () -> {
                get(ScannerController::get);
                post(ScannerController::scan);
            });

        });
    }
}