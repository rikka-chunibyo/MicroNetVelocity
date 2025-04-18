package com.rikka.micronetvelocity;

import com.rikka.micronetvelocity.Libraries.VelocityCommandHandler.VelocityCommandRegistrar;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Plugin(id = "micronetvelocity", name = "MicroNetVelocity", version = "1.0", authors = {"Rikka"})
public class MicroNetVelocity {
    public static Object plugin;
    public static Path dataDirectory;

    private final ProxyServer server;

    @Inject
    public MicroNetVelocity(ProxyServer server, CommandManager commandManager, @DataDirectory Path dataDirectory) {
        plugin = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
        VelocityCommandRegistrar.registerAll(server, commandManager, "com.rikka.micronetvelocity.commands");

        copyResourcesToFileSystem(dataDirectory);
    }

    private void copyResourcesToFileSystem(Path dataDirectory) {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        String jarPath = MicroNetVelocity.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith("resources/") && !entry.isDirectory()) {
                    Path targetPath = dataDirectory.resolve(name.substring("resources/".length()));

                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath.getParent());

                        try (InputStream inputStream = jarFile.getInputStream(entry);
                             FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
