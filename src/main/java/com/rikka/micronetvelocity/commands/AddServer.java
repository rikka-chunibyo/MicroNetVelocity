package com.rikka.micronetvelocity.commands;

import com.rikka.micronetvelocity.Libraries.VelocityCommandHandler.VelocityCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.rikka.micronetvelocity.MicroNetVelocity.dataDirectory;
import static com.rikka.micronetvelocity.MicroNetVelocity.plugin;

public class AddServer implements VelocityCommand {
    @Override
    public String name() {
        return "addserver";
    }

    @Override
    public void execute(ProxyServer server, CommandSource source, String[] args) {
        if (args.length != 4) {
            source.sendMessage(Component.text("Usage: /addserver <paper|bukkit> <version> <server-name> <port>"));
            return;
        }

        String type = args[0].toLowerCase();
        String version = args[1];
        String folderName = args[2];
        String port = args[3];

        File serverDir = new File("Network", folderName);
        serverDir.mkdirs();
        File outputJar = new File(serverDir, "server.jar");

        server.getScheduler().buildTask(plugin, () -> {
            try {
                if (type.equals("paper")) {
                    downloadPaper(version, outputJar);
                } else if (type.equals("bukkit")) {
                    downloadBukkit(version, outputJar);
                } else {
                    source.sendMessage(Component.text("❌ Invalid type. Use 'paper' or 'bukkit'"));
                    return;
                }
                source.sendMessage(Component.text("✅ Downloaded " + type + " " + version + " to " + outputJar.getPath()));
                acceptEula(serverDir);
                source.sendMessage(Component.text("✅ Accepted Eula"));
                copyAndPatchServerProperties(serverDir, Integer.parseInt(port));
                source.sendMessage(Component.text("✅ Created Server Properties File"));
                source.sendMessage(Component.text("✅ Ready to Start Server! Run 'startserver " + folderName + "'"));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("❌ Failed: " + e.getMessage()));
            }
        }).schedule();
    }

    private void downloadPaper(String version, File output) throws Exception {
        String apiBase = "https://api.papermc.io/v2/projects/paper";
        URL versionUrl = new URL(apiBase + "/versions/" + version);
        JSONObject versionJson = getJson(versionUrl);
        JSONArray builds = versionJson.getJSONArray("builds");
        int latestBuild = builds.getInt(builds.length() - 1);

        String jarName = "paper-" + version + "-" + latestBuild + ".jar";
        URL jarUrl = new URL(apiBase + "/versions/" + version + "/builds/" + latestBuild + "/downloads/" + jarName);
        downloadFile(jarUrl, output);
    }

    private void downloadBukkit(String version, File output) throws Exception {
        String downloadUrl = "https://download.getbukkit.org/craftbukkit/craftbukkit-" + version + ".jar";
        downloadFile(new URL(downloadUrl), output);
    }

    private JSONObject getJson(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "VelocityPlugin");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return new JSONObject(response.toString());
    }

    private void downloadFile(URL url, File output) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "VelocityPlugin");
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(output)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
        }
    }

    private void acceptEula(File serverDir) {
        File eulaFile = new File(serverDir, "eula.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(eulaFile))) {
            writer.println("eula=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyAndPatchServerProperties(File serverDir, int port) throws IOException {
        File targetFile = new File(serverDir, "server.properties");
        File sourceFile = dataDirectory.resolve("server.properties").toFile();

        if (!targetFile.exists()) {
            if (!sourceFile.exists()) {
                throw new FileNotFoundException("Missing server.properties in plugin data folder: " + sourceFile.getAbsolutePath());
            }
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        }

        List<String> lines = Files.readAllLines(targetFile.toPath());
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("server-port=")) {
                lines.set(i, "server-port=" + port);
                found = true;
                break;
            }
        }

        if (!found) {
            lines.add("server-port=" + port);
        }

        Files.write(targetFile.toPath(), lines);
    }
}
