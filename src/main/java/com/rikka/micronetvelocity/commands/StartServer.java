package com.rikka.micronetvelocity.commands;

import com.rikka.micronetvelocity.Libraries.ShellLib;
import com.rikka.micronetvelocity.Libraries.VelocityCommandHandler.VelocityCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.io.File;

public class StartServer implements VelocityCommand {

    @Override
    public String name() {
        return "startserver";
    }

    @Override
    public void execute(ProxyServer server, CommandSource source, String[] args) {
        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /startserver <server-name>"));
            return;
        }

        String serverName = args[0];
        File serverDir = new File("Network/" + serverName);
        File jarFile = new File(serverDir, "server.jar");

        if (!serverDir.exists() || !jarFile.exists()) {
            source.sendMessage(Component.text("Server or server.jar not found: " + serverDir.getPath()));
            return;
        }

        startServer(serverDir.getAbsolutePath(), serverName, source);
    }

    public void startServer(String serverDir, String serverName, CommandSource source) {
        String command = "cd " + serverDir + " && java -jar server.jar -nogui";

        ShellLib.shell(command).thenAccept(output -> {
            if (output.isBlank()) {
                source.sendMessage(Component.text("Started server '" + serverName + "' (no output)"));
            } else {
                source.sendMessage(Component.text("Output from '" + serverName + "':\n" + output));
            }
        }).exceptionally(ex -> {
            source.sendMessage(Component.text("Failed to execute command: " + ex.getMessage()));
            return null;
        });
    }

}
