# MCwlap — Wireless & Network Analysis Tool

**MCwlap** (modulo della suite *MC Vads*) è un'applicazione desktop orientata all'analisi di rete, alla diagnosi delle connessioni locali e al monitoraggio delle infrastrutture wireless.

Progettato con un'interfaccia grafica intuitiva e reattiva, il tool fornisce a sistemisti e appassionati di cybersecurity un insieme di strumenti essenziali per l'ispezione della rete locale.

---

## 🚀 Funzionalità Principali

* 📡 **Wi-Fi Scanner:**
    * Scansione in tempo reale delle reti wireless nell'area di copertura.
    * Identificazione dettagliata di **SSID**, **BSSID (Indirizzo MAC)**, tipo di **Sicurezza** (WPA2, WPA3, ecc.) e presenza di **WPS**.
* 🛡️ **ARP Monitor:**
    * Monitoraggio della tabella ARP e del traffico di rete locale.
    * Utile per la diagnosi di problemi di instradamento e l'individuazione di potenziali anomale (es. ARP Spoofing/Poisoning).
* 🔍 **Vendor Lookup:**
    * Riconoscimento automatico del produttore hardware dei dispositivi connessi tramite analisi del prefisso OUI del MAC address.

---

## 🛠️ Architettura e Tecnologie

* **Ambiente:** Applicazione Desktop (Windows)
* **Funzionalità di Rete:** Ispezione di interfaccia di rete, Socket, comandi ARP e parsing dei beacon Wi-Fi.

---

## 📋 Requisiti ed Esecuzione

### Prerequisiti
* Scheda di rete wireless abilitata per la funzionalità *Wi-Fi Scanner*.
* Permessi di amministratore per accedere al monitoraggio delle tabelle ARP e dei socket di rete.

### Installazione
1. Clonare la repository:
   ```bash
   git clone [https://github.com/Parideeeree/MCwlap.git](https://github.com/Parideeeree/MCwlap.git)