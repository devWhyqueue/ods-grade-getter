package de.devwhyqueue.odsgradegetter.tordownloader.session;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import de.devwhyqueue.odsgradegetter.tordownloader.model.Credentials;

public class SessionHandler implements AutoCloseable {
    private static final String BASE_URL = "https://ods.fh-dortmund.de/ods";

    private String sidd;

    public String open(Credentials credentials) throws SessionException {
        if (this.sidd == null) {
            try {
                Document doc = Jsoup.connect(BASE_URL)
                        .data("User", credentials.getUsername())
                        .data("PWD", credentials.getPassword())
                        .post();
                String sidd = doc.getElementsByTag("meta").first().attr("content");
                if (!sidd.contains("SIDD=")) {
                    throw new IOException();
                }
                this.sidd = sidd.substring(sidd.indexOf("SIDD=") + 5);
            } catch (IOException e) {
                throw new SessionException("Login failed!", e);
            }
        }
        return this.sidd;
    }

    @Override
    public void close() throws SessionException {
        if (this.sidd != null) {
            try {
                Jsoup.connect(BASE_URL)
                        .data("Sicht", "ExcS")
                        .data("ExcSicht", "Menue")
                        .data("SIDD", this.sidd)
                        .data("Tuwas", "abmelden")
                        .get();
                this.sidd = null;
            } catch (IOException e) {
                throw new SessionException("Logout failed!", e);
            }
        }
    }
}
