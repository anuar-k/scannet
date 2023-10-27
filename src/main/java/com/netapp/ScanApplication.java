package com.netapp;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.apache.commons.net.util.SubnetUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ScanApplication {
    public static String fileName = "found_domains.txt";

    public void start() throws NullPointerException {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public", Location.CLASSPATH);
        }).start(80);

        app.post("/scan", ctx -> {
            String mask = ctx.formParam("mask");
            int threadCount = Integer.parseInt(ctx.formParam("threadCount"));

            if (threadCount <= 0) {
                threadCount = 1;
            }
            clearFile();
            ctx.redirect("/");
            scanIPAddresses(mask, threadCount);
        });
    }

    public void scanIPAddresses(String mask, int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        SubnetUtils subnetUtils = new SubnetUtils(mask);
        String[] addresses = subnetUtils.getInfo().getAllAddresses();

        for (String ip : addresses) {
            executor.submit(() -> scanIpAddress(ip));
        }
        executor.shutdown();

    }

    public X509Certificate getSSLCertificate(String ipAddress) {
        System.out.println(ipAddress);
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null);

            SSLSocketFactory factory = context.getSocketFactory();
            SSLSocket socket = (SSLSocket) factory.createSocket(ipAddress, 443);
            socket.startHandshake();

            SSLSession session = socket.getSession();
            return (X509Certificate) session.getPeerCertificates()[0];
        } catch (Exception e) {
            return null;
        }
    }

    public void scanIpAddress(String ip) {
        try {
            X509Certificate certificates = getSSLCertificate(ip);
            List<List<?>> certificateNames = certificates
                    .getSubjectAlternativeNames()
                    .stream().collect(Collectors.toList());
            List<String> domains = new ArrayList<>();
            for (List<?> certificateName : certificateNames) {
                domains.add((String) certificateName.get(1));
            }

            saveDomainToFile(ip + " - " + domains);
        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized public void saveDomainToFile(String domain) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName, true);
            writer.write(domain + "\n");
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void clearFile() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}