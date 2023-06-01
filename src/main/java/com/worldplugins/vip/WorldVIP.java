package com.worldplugins.vip;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static me.post.lib.util.Colors.color;

public class WorldVIP extends JavaPlugin {
    private @Nullable Runnable onDisable;

    @Override
    public void onEnable() {
        String ip;
        try {
            final URL url = new URL("http://checkip.amazonaws.com");
            final BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ip = br.readLine();
            br.close();
        } catch (Exception ex){
            ex.printStackTrace();
            ip = null;
        }

        final String plugin = "WorldVIP";
        final Runnable disable = () -> {
            Bukkit.getConsoleSender().sendMessage(color("&b[" + plugin + "] &fSeu ip nao consta na data-base, veja no discord!"));
            Bukkit.getConsoleSender().sendMessage(color("&b[" + plugin + "] &fO plugin foi desativado!"));
            this.setEnabled(false);
        };

        try {
            if (ip == null) {
                throw new Exception();
            }

            Bukkit.getConsoleSender().sendMessage(color("&b[" + plugin + "] &fVerificando acesso ao ip &b" + ip + " &fAguarde!!!"));
            boolean success;

            try {
                final String pluginSlug = "world-vip";
                final URL url = new URL("https://apiwp.worldplugins.com/api/status?ip=" + ip + "&plugin=" + pluginSlug);
                final HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("ipaddr", ip);

                final BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));

                final StringBuilder content = new StringBuilder();
                String line;

                while ((line = isr.readLine()) != null) {
                    content.append(line);
                    content.append('\n');
                }

                final JsonObject json = new JsonParser().parse(content.toString()).getAsJsonObject();
                success = json.get("ok").getAsBoolean();
            } catch (Exception ex) {
                ex.printStackTrace();
                success = false;
            }

            if (!success) {
                disable.run();
                return;
            }

            Bukkit.getConsoleSender().sendMessage(color("&b[WorldPlugins] &fSeu ip foi verificado!, o &bplugin &finiciou!"));
            Bukkit.getConsoleSender().sendMessage(color("&b[WorldPlugins] &fO Plugin &b" + plugin + " &ffoi inicializado com sucesso, agradecemos a preferencia!"));
        } catch (Exception ex) {
            this.setEnabled(false);
            return;
        }

        onDisable = new PluginExecutor(this).execute();
    }

    @Override
    public void onDisable() {
        if (onDisable != null) {
            onDisable.run();
        }
    }
}
