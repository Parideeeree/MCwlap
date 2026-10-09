package it.proxy.mcwlap.service;

import it.proxy.mcwlap.model.WifiNetwork;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScanService {

    public String OSdetection() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "WINDOWS";
        } else if (os.contains("nux") || os.contains("nix")) {
            return "LINUX";
        } else {
            return "UNKNOWN";
        }
    }

    public List<WifiNetwork> scan() {

        List<WifiNetwork> networkList = new ArrayList<>();
        String os = OSdetection();
        String command = "";

        // Scegliamo il comando con un if normale
        if (os.equals("WINDOWS")) {
            command = "netsh wlan show networks mode=bssid";
        } else if (os.equals("LINUX")) {
            command = "nmcli -f SSID,BSSID,SECURITY,WPS,BARS dev wifi";
        } else {
            return networkList;
        }

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            if (os.equals("LINUX")) {
                // --- PARSING LINUX ---
                reader.readLine(); // Salta l'intestazione
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() == false) {
                        String[] parts = line.split("\\s{2,}");
                        if (parts.length >= 5) {
                            WifiNetwork net = new WifiNetwork(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                            networkList.add(net);
                        }
                    }
                }
            } else if (os.equals("WINDOWS")) {
                // --- PARSING WINDOWS ---
                String ssid = "";
                String bssid = "";
                String security = "";
                String signal = "";

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.startsWith("SSID")) {

                        if (ssid.isEmpty() == false) {
                            WifiNetwork net = new WifiNetwork(ssid, bssid, security, "N/D");
                            networkList.add(net);
                            // Resettiamo le variabili per la nuova rete
                            bssid = "";
                            security = "";
                            signal = "";
                        }

                        String[] split = line.split(":");
                        if (split.length > 1) {
                            ssid = split[1].trim();
                        } else {
                            ssid = "[Hidden]";
                        }

                        if (ssid.isEmpty()) {
                            ssid = "[Hidden]";
                        }
                    } else if (line.startsWith("Autenticazione") || line.startsWith("Authentication")) {
                        String[] split = line.split(":");
                        if (split.length > 1) {
                            security = split[1].trim();
                        }
                    } else if (line.startsWith("BSSID")) {

                        int index = line.indexOf(":");
                        if (index != -1) {
                            bssid = line.substring(index + 1).trim();
                        }
                    }
                }

                if (ssid.isEmpty() == false) {
                    WifiNetwork net = new WifiNetwork(ssid, bssid, security, "N/D");
                    networkList.add(net);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return networkList;
    }
}