/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

package it.aequinoxio;import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RipeQueryCmdLine {
    static final ArrayList<String> IPToBeChecked = new ArrayList<>();

    public static void main(@NotNull String[] args) {
        RipeQuery ripeQueryCmdLine = new RipeQuery(null);
        if (args.length > 0) {
            String ipFile = args[0];
            //String ipFile = "C:\\Users\\utente\\Downloads\\temp\\ip.txt";

//            try (BufferedReader bfr = new BufferedReader(new FileReader(ipFile))) {
            try (BufferedReader bfr = new BufferedReader(
                    new InputStreamReader(new FileInputStream(ipFile), StandardCharsets.UTF_8))
            ) {
                String linea;
                while ((linea = bfr.readLine()) != null) {
                    System.out.print(linea + " : ");
                    // Check se è un IP valido
                    if (linea.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
                        IPToBeChecked.add(linea);
                        System.out.println("ok");
                    } else {
                        System.out.println("IP non valido");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Uso: " + RipeQueryCmdLine.class.getName() + " ip_file");
            System.exit(1);
        }
        System.out.println("Checking...");
        for (String ip : IPToBeChecked) {
            HttpStatusCodes retval ;
            try {
                //retval = ripeQueryCmdLine.queryIPAddressForCountry(ip);
                retval = ripeQueryCmdLine.downloadAndParseLocationData(ip);
                if (retval == HttpStatusCodes.OK) {
                    //System.out.println(ripeQueryCmdLine.m_jsonData);
                    System.out.println(ip + " - " + ripeQueryCmdLine.getLatestCountry());
                } else {
                    System.out.print(ip + " - Error: ");
                    System.out.println(retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
    }
}
