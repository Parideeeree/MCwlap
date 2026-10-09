package it.proxy.mcwlap;

import it.proxy.mcwlap.model.WifiNetwork;
import it.proxy.mcwlap.service.ARPmonitor;
import it.proxy.mcwlap.service.ScanService;
import it.proxy.mcwlap.service.VendorService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class HelloController {

    /* ── FXML — Table ─────────────────────────────── */
    @FXML private TableView<WifiNetwork>        tableScanner;
    @FXML private TableColumn<WifiNetwork, String> colSsid;
    @FXML private TableColumn<WifiNetwork, String> colMac;
    @FXML private TableColumn<WifiNetwork, String> colSecurity;
    @FXML private TableColumn<WifiNetwork, String> colWps;

    /* ── FXML — Controls ──────────────────────────── */
    @FXML private Button btnStartScan;
    @FXML private Button  btnStopScan;
    @FXML private Button  btnRefreshArp;
    @FXML private Button  btnLookup;
    @FXML private TextField txtMacLookup;
    @FXML private TextArea arpLogArea;

    /* ── FXML — Labels / status ───────────────────── */
    @FXML private Label lblScanStatus;
    @FXML private Label lbarpstatus;
    @FXML private Label lblVendorResult;
    @FXML private Label lblBottomStatus;

    /* ── FXML — Pills (status indicators) ────────── */
    @FXML private HBox   scanPill;
    @FXML private Circle scanDot;
    @FXML private HBox   arpPill;
    @FXML private Circle arpDot;
    @FXML private HBox   statusBadge;
    @FXML private Circle statusDot;

    /* ── FXML — Result box (Vendor Lookup) ────────── */
    @FXML private HBox  resultBox;

    /* ── FXML — Views (one per section) ──────────── */
    @FXML private VBox viewWifi;
    @FXML private VBox viewArp;
    @FXML private VBox viewVendor;

    /* ── FXML — Nav items ─────────────────────────── */
    @FXML private HBox navWifi;
    @FXML private HBox navArp;
    @FXML private HBox navVendor;

    /* ── Services ─────────────────────────────────── */
    private final ScanService   scanService   = new ScanService();
    private final VendorService vendorService = new VendorService();
    private final ObservableList<WifiNetwork> obsList = FXCollections.observableArrayList();
    private ARPmonitor monitor;

    // ════════════════════════════════════════════════
    //  Init
    // ════════════════════════════════════════════════
    @FXML
    public void initialize() {
        colSsid.setCellValueFactory(new PropertyValueFactory<>("ssid"));
        colMac.setCellValueFactory(new PropertyValueFactory<>("bssid"));
        colSecurity.setCellValueFactory(new PropertyValueFactory<>("security"));
        colWps.setCellValueFactory(new PropertyValueFactory<>("wps"));
        tableScanner.setItems(obsList);

        showView(viewWifi);
        setNavActive(navWifi);
        setScanStatus("IDLE", "idle");
        setArpStatus("STOPPED", "idle");
    }

    // ════════════════════════════════════════════════
    //  Navigation
    // ════════════════════════════════════════════════
    @FXML public void showWifi()   { showView(viewWifi);   setNavActive(navWifi); }
    @FXML public void showArp()    { showView(viewArp);    setNavActive(navArp); }
    @FXML public void showVendor() { showView(viewVendor); setNavActive(navVendor); }

    private void showView(VBox target) {
        for (VBox v : new VBox[]{viewWifi, viewArp, viewVendor}) {
            boolean active = v == target;
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    private void setNavActive(HBox target) {
        for (HBox nav : new HBox[]{navWifi, navArp, navVendor}) {
            nav.getStyleClass().remove("nav-active");
        }
        if (!target.getStyleClass().contains("nav-active")) {
            target.getStyleClass().add("nav-active");
        }
    }

    // ════════════════════════════════════════════════
    //  WiFi Scanner
    // ════════════════════════════════════════════════
    @FXML
    protected void onClickScan() {
        setScanStatus("SCANNING", "scanning");
        btnStartScan.setDisable(true);
        obsList.clear();

        try {
            Runtime.getRuntime().exec("cmd /c start /b netsh wlan show networks");
        } catch (IOException e) {
            System.err.println("netsh error: " + e.getMessage());
        }

        Thread t = new Thread(() -> {
            List<WifiNetwork> results = scanService.scan();
            Platform.runLater(() -> {
                obsList.clear();
                obsList.addAll(results);
                setScanStatus("IDLE", "idle");
                btnStartScan.setDisable(false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void stopScan() {
        obsList.clear();
        setScanStatus("STOPPED", "stopped");
        btnStartScan.setDisable(false);
    }

    /** state: "idle" | "scanning" | "stopped" | "active" */
    private void setScanStatus(String text, String state) {
        lblScanStatus.setText(text);
        setPill(scanPill, scanDot, lblScanStatus, state);
    }

    // ════════════════════════════════════════════════
    //  ARP Monitor
    // ════════════════════════════════════════════════
    protected void onEnableArp() {
        if (monitor == null || !monitor.isAlive()) {
            monitor = new ARPmonitor(arpLogArea);
            monitor.setDaemon(true);
            monitor.start();
            setArpStatus("ACTIVE", "active");
            btnRefreshArp.setText("Stop Monitor");
        }
    }

    @FXML
    protected void onRefreshTable() {
        if (monitor != null && monitor.isAlive()) {
            monitor.stopMonitoring();
            arpLogArea.clear();
            setArpStatus("STOPPED", "idle");
            btnRefreshArp.setText("Start Monitor");
        } else {
            arpLogArea.clear();
            onEnableArp();
        }
    }

    private void setArpStatus(String text, String state) {
        lbarpstatus.setText(text);
        setPill(arpPill, arpDot, lbarpstatus, state);
    }

    // ════════════════════════════════════════════════
    //  Vendor Lookup
    // ════════════════════════════════════════════════
    @FXML
    protected void FindVendorClick() {
        String mac = txtMacLookup.getText().trim();
        if (mac.isEmpty()) {
            setResult("Please enter a MAC address.", "warning");
            return;
        }

        setResult("Searching…", "idle");
        btnLookup.setDisable(true);

        Thread t = new Thread(() -> {
            String result = vendorService.FindMacVendor(mac);
            Platform.runLater(() -> {
                btnLookup.setDisable(false);
                if (result == null || result.isBlank() || result.equalsIgnoreCase("unknown")) {
                    setResult("Vendor not found for: " + mac, "warning");
                } else {
                    setResult(result, "success");
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ════════════════════════════════════════════════
    //  Network status (sidebar badge)
    // ════════════════════════════════════════════════
    public void setNetworkConnected(boolean connected) {
        Platform.runLater(() -> {
            statusBadge.getStyleClass().removeAll("status-badge-connected", "status-badge-disconnected");
            statusDot.getStyleClass().removeAll("dot-connected", "dot-disconnected");
            lblBottomStatus.getStyleClass().removeAll("lbl-connected", "lbl-disconnected");

            if (connected) {
                statusBadge.getStyleClass().add("status-badge-connected");
                statusDot.getStyleClass().add("dot-connected");
                lblBottomStatus.getStyleClass().add("lbl-connected");
                lblBottomStatus.setText("Connected");
            } else {
                statusBadge.getStyleClass().add("status-badge-disconnected");
                statusDot.getStyleClass().add("dot-disconnected");
                lblBottomStatus.getStyleClass().add("lbl-disconnected");
                lblBottomStatus.setText("Disconnected");
            }
        });
    }




    // ════════════════════════════════════════════════
    //  System tray notification
    // ════════════════════════════════════════════════
    public void showNotification(String title, String message) {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image img = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            TrayIcon icon = new TrayIcon(img, "MCwlap");
            icon.setImageAutoSize(true);
            tray.add(icon);
            icon.displayMessage(title, message, TrayIcon.MessageType.WARNING);

            Thread cleanup = new Thread(() -> {
                try { Thread.sleep(5000); tray.remove(icon); }
                catch (Exception ignored) {}
            });
            cleanup.setDaemon(true);
            cleanup.start();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════

    /** Updates pill + dot + label style to one of: idle | scanning | stopped | active */
    private void setPill(HBox pill, Circle dot, Label label, String state) {
        pill.getStyleClass().removeAll("pill-idle", "pill-scanning", "pill-stopped", "pill-active");
        dot.getStyleClass().removeAll("dot-idle", "dot-scanning", "dot-stopped", "dot-active");
        label.getStyleClass().removeAll("lbl-idle", "lbl-scanning", "lbl-stopped", "lbl-active");
        pill.getStyleClass().add("pill-" + state);
        dot.getStyleClass().add("dot-" + state);
        label.getStyleClass().add("lbl-" + state);
    }

    /** Updates result box and label style to: idle | success | warning */
    private void setResult(String text, String state) {
        lblVendorResult.setText(text);
        resultBox.getStyleClass().removeAll("result-box-idle", "result-box-success", "result-box-warning");
        lblVendorResult.getStyleClass().removeAll("result-text-idle", "result-text-success", "result-text-warning");
        resultBox.getStyleClass().add("result-box-" + state);
        lblVendorResult.getStyleClass().add("result-text-" + state);
    }
}