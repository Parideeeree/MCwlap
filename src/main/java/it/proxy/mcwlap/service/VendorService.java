package it.proxy.mcwlap.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class VendorService {

    public String FindMacVendor(String mac) {
        try {
            String mac_f = mac.trim().replaceAll("\\s+", "");
            URL url = new URL("https://api.macvendors.com/" + mac_f);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);


            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int r = httpURLConnection.getResponseCode();

            if (r == 200) {
                try (Scanner s = new Scanner(httpURLConnection.getInputStream())) {
                    return s.useDelimiter("\\A").hasNext() ? s.next() : "Sconosciuto";
                }
            } else if (r == 429) {
                return "Errore: Troppe richieste (Rate Limit)";
            } else if (r == 404) {
                return "Produttore non trovato";
            } else {
                return "Errore Server: " + r;
            }

        } catch (Exception e) {
            return "Errore: " + e.getMessage();
        }
    }
}
