import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RipeQueryCmdLine {
    static ArrayList<String> IPToBeChecked = new ArrayList<>();

    public static void main(@NotNull String[] args) {
        RipeQuery ripeQueryCmdLine = new RipeQuery();
        if (args.length>0){
            //String ipFile = args[1];
            String ipFile = "C:\\Users\\utente\\Downloads\\temp\\ip.txt";

            try (BufferedReader bfr = new BufferedReader(new FileReader(ipFile))) {
                String linea ;
                while((linea=bfr.readLine())!=null){
                    // Check se è un IP valido
                    System.out.print(linea+" : ");
                    if (linea.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
                        IPToBeChecked.add(linea);
                        System.out.println("ok");
                    } else{
                        System.out.println("IP non valido");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Uso: "+RipeQueryCmdLine.class.getName()+" ip_file");
            System.exit(1);
        }
        System.out.println("Checking...");
        for (String ip:IPToBeChecked) {
            HttpStatusCodes retval = ripeQueryCmdLine.queryIPAddressForCountry(ip);
            if (retval == HttpStatusCodes.OK) {
                //System.out.println(ripeQueryCmdLine.m_jsonData);
                System.out.println(ip+" - "+ripeQueryCmdLine.country());
            } else {
                System.out.print(ip+" - Error: ");
                System.out.println(retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());
            }
        }
    }
}
