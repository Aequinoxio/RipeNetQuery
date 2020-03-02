import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RipeQuery {
    static ArrayList<String> IPToBeChecked = new ArrayList<>();
    static String RipeUrl = "https://stat.ripe.net/data/maxmind-geo-lite/data.json?resource=";
    String m_jsonData;

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
        String retVal="Unknown";
        StringReader sr = new StringReader(m_jsonData);
        JsonReader jsonReader = Json.createReader(sr);
        JsonObject joMain = jsonReader.readObject();
        jsonReader.close();

        // TODO: implementare i controlli per capire se esiste il ramo. es 231.4.5.6
        JsonObject jo1 = joMain.getJsonObject("data");
        if (jo1!=null) {
            JsonArray jsonArray = jo1.getJsonArray("located_resources");
            if (jsonArray!=null && jsonArray.size()>0) {
                JsonObject jo2 = jsonArray.getJsonObject(0);
                if (jo2!=null) {
                    JsonArray jsonArray1 = jo2.getJsonArray("locations");
                    if (jsonArray1 != null && jsonArray1.size() > 0) {
                        JsonObject jo3 = jsonArray1.getJsonObject(0);
                        if (jo3 != null) {
                            retVal = jo3.getString("country");
                        }
                    }
                }
            }
        }
        //String countryValue = jo3.getString("country");

        return retVal;
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
