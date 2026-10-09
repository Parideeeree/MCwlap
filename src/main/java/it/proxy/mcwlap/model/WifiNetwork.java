package it.proxy.mcwlap.model;

public class WifiNetwork {
    private String ssid;
    private String bssid;
    private String security;
    private String wps;

    public WifiNetwork(String ssid, String bssid, String security, String wps) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.security = security;
        this.wps = wps;
    }

    public String getSsid() { return ssid; }
    public String getBssid() { return bssid; }
    public String getSecurity() { return security; }
    public String getWps() { return wps; }
}