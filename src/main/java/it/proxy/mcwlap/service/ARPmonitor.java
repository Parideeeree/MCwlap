package it.proxy.mcwlap.service;

import it.proxy.mcwlap.HelloController;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ARPmonitor extends Thread {

    private final TextArea logArea;
    private boolean running = true;
    private String gatewayIP;
    private String originalMac;


    public ARPmonitor(TextArea logArea) {
        this.logArea = logArea;
    }

    public void stopMonitoring() {
        this.running = false;
    }



    @Override
    public void run() {
        updateLog("[INIT] Identificazione Gateway...");
        gatewayIP = fetchGatewayIP();

        if (gatewayIP == null) {
            updateLog("[ERRORE] Impossibile trovare il Gateway. Protezione interrotta.");
            return;
        }

        originalMac = fetchMacFromIP(gatewayIP);
        if (originalMac == null) {
            updateLog("[ERRORE] Gateway non risponde nella tabella ARP. Prova a navigare su internet e riavvia.");
            return;
        }

        updateLog("[SAFE] Gateway: " + gatewayIP + " | MAC: " + originalMac);
        updateLog("[LIVE] Monitoraggio attivo... Sto cercando anomalie.");
        while (running) {

            try {
                Thread.sleep(3000);
                String currentMac = fetchMacFromIP(gatewayIP);

                if (currentMac != null && !currentMac.equalsIgnoreCase(originalMac)) {
                    updateLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    updateLog("[ALERT] SOSPETTO ARP SPOOFING RILEVATO!");
                    updateLog("[ALERT] Il Gateway " + gatewayIP + " ha cambiato MAC!");
                    updateLog("[ALERT] Originale: " + originalMac);
                    updateLog("[ALERT] ATTUALE: " + currentMac + " (Attaccante?)");
                    updateLog("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Platform.runLater(() -> {
                        HelloController controller = new HelloController();
                        controller.showNotification("ATTACCO RILEVATO", "Il Gateway ha cambiato MAC! Possibile intruso.");
                    });
                }

            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private String fetchGatewayIP() {
        try {
            Process p = Runtime.getRuntime().exec("ipconfig");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("Gateway predefinito") || line.contains("Default Gateway")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private String fetchMacFromIP(String targetIP) {
        try {
            Process p = Runtime.getRuntime().exec("arp -a");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(targetIP)) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) return parts[1].replace("-", ":").toUpperCase();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void updateLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }
}