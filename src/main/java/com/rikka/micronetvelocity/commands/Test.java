package com.rikka.micronetvelocity.commands;

import com.rikka.micronetvelocity.Libraries.ShellLib;
import com.rikka.micronetvelocity.Libraries.VelocityCommandHandler.VelocityCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

public class Test implements VelocityCommand {
    @Override
    public String name() {
        return "test";
    }

    @Override
    public void execute(ProxyServer server, CommandSource source, String[] args) {
        if (args.length == 0) {
            source.sendMessage(Component.text("Usage: /test <args>"));
            return;
        }

        String command = String.join(" ", args);
        ShellLib.shell(command).thenAccept(output -> {
            source.sendMessage(Component.text(output));
        });
    }
}
