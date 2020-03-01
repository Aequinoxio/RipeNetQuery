import org.jetbrains.annotations.NotNull;
import sun.security.x509.IPAddressName;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.json.*;

public class RipeQueryCmdLine {
    static ArrayList<String> IPToBeChecked = new ArrayList<>();
    static String RipeUrl = "https://stat.ripe.net/data/maxmind-geo-lite/data.json?resource=";
    String m_jsonData;

    public static void main(@NotNull String[] args) {
        RipeQueryCmdLine ripeQueryCmdLine = new RipeQueryCmdLine();
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


    /**
     * Interroga stats.ripe.net per IP allo scopo di ottenere la country relativa
     * TODO: Portare all'esterno tutti i controlli sull'esito del download
     * @param IpValue Indirizzo IP di cui conoscere la country. Modifica lo stato interno della classe con il json ritornato.
     * @return Stato della query
     */
    public HttpStatusCodes queryIPAddressForCountry(String IpValue) {
        //boolean retVal = false;
        HttpStatusCodes respReturnCode=HttpStatusCodes.UNKNOWN_CODE;

        HttpsURLConnection urlConnection ;
        try {
            URL url = new URL(RipeUrl + IpValue);
            urlConnection = (HttpsURLConnection) url.openConnection();

            // Indispensabile altrimenti si ottiene un errore 403
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            int respCode = urlConnection.getResponseCode();
            respReturnCode = HttpStatusCodes.intToHttpStatusCode(respCode);
            switch (respCode) {
                case 200:
                    m_jsonData = getData(urlConnection);
                    //retVal = true;
                    break;
                default:
                    //retVal = false;
                    System.out.println(RipeUrl + IpValue);
                    System.out.println("Other than ok: " + respCode+" "+respReturnCode);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //return retVal;
        return respReturnCode;
    }

    /**
     * Ritorna la countru nello stream interrogato.
     * @return Contry relativa all'IP interrogato, null se non è stato trovato oin caso di errore
     */
    public String country(){
        StringReader sr = new StringReader(m_jsonData);
        JsonReader jsonReader = Json.createReader(sr);
        JsonObject joMain = jsonReader.readObject();
        jsonReader.close();

        JsonObject jo1 = joMain.getJsonObject("data");
        JsonObject jo2 = jo1.getJsonArray("located_resources").getJsonObject(0);
        JsonObject jo3 = jo2.getJsonArray("locations").getJsonObject(0);
        //String countryValue = jo3.getString("country");

        return jo3.getString("country");
    }

    ///////// PRIVATE /////////

    /**
     * Ritorna la pagina letta da un HttpURLConnection aperta
     *
     * @param urlConnection Url da aprire
     * @return Pagina remota
     * @throws IOException Eccezione lanciata in caso di errore nel recuperare la pagina
     */
    private String getData(HttpURLConnection urlConnection) throws IOException {
        InputStream data;
        BufferedReader reader;
        data = urlConnection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
