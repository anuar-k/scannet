package com.netapp.controllers;

import com.netapp.Application;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import static com.netapp.utils.CertificateUtil.clearFile;
import static com.netapp.utils.CertificateUtil.scanIPAddresses;

@Data
@AllArgsConstructor
public class ScannerController extends Application {

    public static void get(@NonNull Context ctx) {
        ctx.render("public/index.html");
    }

    public static void scan(@NonNull Context ctx) {
        String mask = ctx.formParam("mask");
        int threadCount = Integer.parseInt(ctx.formParam("threadCount"));

        if (threadCount <= 0) {
            threadCount = 1;
        }
        clearFile();
        ctx.redirect("/");
        scanIPAddresses(mask, threadCount);
    }
}