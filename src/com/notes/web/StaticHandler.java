package com.notes.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StaticHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String filePart = path.startsWith("/static/") ? path.substring("/static/".length()) : path.replaceAll("^/+", "");
        if ("favicon.ico".equals(filePart)) {
            filePart = "favicon.svg";
        }

        File file = new File("static", filePart);
        if (!file.exists() || file.isDirectory()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String mime = "text/plain";
        if (filePart.endsWith(".css")) {
            mime = "text/css; charset=UTF-8";
        } else if (filePart.endsWith(".svg")) {
            mime = "image/svg+xml";
        } else if (filePart.endsWith(".ico")) {
            mime = "image/x-icon";
        } else if (filePart.endsWith(".png")) {
            mime = "image/png";
        }

        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, file.length());

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = exchange.getResponseBody()) {
            byte[] buf = new byte[2048];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
    }
}
