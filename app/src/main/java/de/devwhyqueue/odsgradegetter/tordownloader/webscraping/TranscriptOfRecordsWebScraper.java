package de.devwhyqueue.odsgradegetter.tordownloader.webscraping;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import de.devwhyqueue.odsgradegetter.tordownloader.model.Module;
import de.devwhyqueue.odsgradegetter.tordownloader.model.TranscriptOfRecords;

public class TranscriptOfRecordsWebScraper {
    private static final String BASE_URL = "https://ods.fh-dortmund.de/ods";

    public TranscriptOfRecords getTranscriptOfRecords(String sidd) throws TranscriptOfRecordsWebScraperException {
        Document doc = loadTranscriptOfRecords(sidd);
        return parseTranscriptOfRecords(doc);
    }

    private Document loadTranscriptOfRecords(String sidd) throws TranscriptOfRecordsWebScraperException {
        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .data("Sicht", "ExcS")
                    .data("ExcSicht", "Notenspiegel")
                    .data("m", "1")
                    .data("SIDD", sidd)
                    .get();
            return doc;
        } catch (IOException e) {
            throw new TranscriptOfRecordsWebScraperException("Could not load transcript of records!", e);
        }
    }

    private TranscriptOfRecords parseTranscriptOfRecords(Document doc) {
        Element table = doc.selectFirst("table[class=small]");
        Elements rows = table.getElementsByTag("tr");

        TranscriptOfRecords tor = new TranscriptOfRecords();
        for (int i = 1; i < rows.size(); i++) {
            String grade = rows.get(i).child(5).text();
            if (!grade.isEmpty()) {
                String name = rows.get(i).child(1).text();
                // parse date
                String dateStr = rows.get(i).child(3).text();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
                LocalDate date = LocalDate.parse(dateStr, formatter);
                grade = grade.replace(',', '.');
                String credits = rows.get(i).child(6).text();
                credits = credits.replace(',', '.');
                Module m = new Module(name, Double.parseDouble(grade), Double.parseDouble(credits), date);
                tor.addModule(m);
            }
        }
        return tor;
    }


}
