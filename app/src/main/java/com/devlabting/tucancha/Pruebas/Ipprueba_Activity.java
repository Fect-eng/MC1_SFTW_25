package com.devlabting.tucancha.Pruebas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devlabting.tucancha.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class Ipprueba_Activity extends AppCompatActivity {

    private TextView tvDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ipprueba);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDatos = findViewById(R.id.tvDatos);

        StringBuilder sb = new StringBuilder();

        // === IP LOCAL ===
        String ip = getLocalIp(this);
        sb.append("📡 IP Local: ").append(ip).append("\n\n");

        // === MAC ORIGINAL DEL WIFI ===
        String mac = getWifiMacAddress();
        sb.append("🔖 MAC Física (Wi-Fi): ").append(mac).append("\n\n");

        // === DNS REALES ===
        List<String> dnsList = getRealDnsServers(this);
        sb.append("🌐 Servidores DNS detectados:\n");
        if (dnsList.isEmpty()) {
            sb.append("No se detectaron DNS.\n");
        } else {
            for (String dns : dnsList) {
                sb.append(" • ").append(dns).append("\n");
            }
        }

        tvDatos.setText(sb.toString());
    }

    // ================== MÉTODOS ==================

    /** IP local del adaptador activo (Wi-Fi o datos móviles) */
    private String getLocalIp(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm != null) {
                int ipInt = wm.getConnectionInfo().getIpAddress();
                if (ipInt != 0) {
                    return Formatter.formatIpAddress(ipInt);
                }
            }
            // Si no está en Wi-Fi, buscar por interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface nif : Collections.list(interfaces)) {
                Enumeration<InetAddress> addrs = nif.getInetAddresses();
                for (InetAddress addr : Collections.list(addrs)) {
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No disponible";
    }

    /** MAC real del adaptador Wi-Fi (si el sistema la permite) */
    private String getWifiMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) { // interfaz Wi-Fi
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) return "No disponible";
                    StringBuilder mac = new StringBuilder();
                    for (byte b : macBytes) {
                        mac.append(String.format("%02X:", b));
                    }
                    if (mac.length() > 0) mac.deleteCharAt(mac.length() - 1);
                    return mac.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No disponible";
    }

    /** DNS reales mediante ConnectivityManager (Android 9+) o DHCP (fallback) */
    private List<String> getRealDnsServers(Context context) {
        List<String> dnsList = new ArrayList<>();
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network active = cm.getActiveNetwork();
                if (active != null) {
                    LinkProperties lp = cm.getLinkProperties(active);
                    if (lp != null) {
                        for (InetAddress dns : lp.getDnsServers()) {
                            dnsList.add(dns.getHostAddress());
                        }
                    }
                }
            }
            // Si no encuentra por LinkProperties, usa DHCP info
            if (dnsList.isEmpty()) {
                WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                if (wm != null) {
                    DhcpInfo dhcp = wm.getDhcpInfo();
                    if (dhcp != null) {
                        String dns1 = Formatter.formatIpAddress(dhcp.dns1);
                        String dns2 = Formatter.formatIpAddress(dhcp.dns2);
                        if (dns1 != null && !dns1.equals("0.0.0.0")) dnsList.add(dns1);
                        if (dns2 != null && !dns2.equals("0.0.0.0")) dnsList.add(dns2);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dnsList;
    }
}
