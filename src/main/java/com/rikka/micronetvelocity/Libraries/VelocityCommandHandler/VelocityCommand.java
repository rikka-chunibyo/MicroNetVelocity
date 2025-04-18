package com.rikka.micronetvelocity.Libraries.VelocityCommandHandler;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public interface VelocityCommand {
    String name();
    default void setPlugin(Object plugin) {}
    void execute(ProxyServer server, CommandSource source, String[] args);
}
