package com.rikka.micronetvelocity.Libraries.VelocityCommandHandler;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import org.reflections.Reflections;

import java.util.Set;

public class VelocityCommandRegistrar {

    public static void registerAll(ProxyServer server, CommandManager commandManager, String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<? extends VelocityCommand>> commandClasses = reflections.getSubTypesOf(VelocityCommand.class);

        for (Class<? extends VelocityCommand> clazz : commandClasses) {
            try {
                VelocityCommand command = clazz.getDeclaredConstructor().newInstance();

                commandManager.register(commandManager.metaBuilder(command.name()).build(), new SimpleCommand() {
                    @Override
                    public void execute(Invocation invocation) {
                        String[] args = invocation.arguments();
                        CommandSource source = invocation.source();
                        command.execute(server, source, args);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
