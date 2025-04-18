package com.rikka.micronetvelocity.Libraries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class ShellLib {
    public static CompletableFuture<String> shell(String command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Process process = new ProcessBuilder("bash", "-c", command).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                process.waitFor();
                return "Output:\n" + output;
            } catch (Exception e) {
                e.printStackTrace();
                return "Error running shell command: " + e.getMessage();
            }
        });
    }
}
