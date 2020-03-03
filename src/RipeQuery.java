import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RipeQuery {
    static ArrayList<String> IPToBeChecked = new ArrayList<>();
    static String RipeUrl = "https://stat.ripe.net/data/maxmind-geo-lite/data.json?resource=";
    String m_jsonData;
    Proxy proxy = Proxy.NO_PROXY;

    /**
     * Costruttore
     * Imposta il proxy di sistema
     */
    public RipeQuery() {
        getProxy();
    }

    /**
     * Interroga stats.ripe.net per IP allo scopo di ottenere la country relativa
     * TODO: Gestire meglio le eccezioni separandole per tipo (download json, interpretazione ecc.)
     * TODO: compattare i metodi in modo da avere solo quello country che fa tutto
     * @param IpValue Indirizzo IP di cui conoscere la country. Modifica lo stato interno della classe con il json ritornato.
     * @return Stato della query
     */
    public HttpStatusCodes queryIPAddressForCountry(String IpValue) {
        HttpStatusCodes respReturnCode=HttpStatusCodes.UNKNOWN_CODE;

        HttpsURLConnection urlConnection ;
        try {
            URL url = new URL(RipeUrl + IpValue);
            urlConnection = (HttpsURLConnection) url.openConnection(proxy);

            // Indispensabile altrimenti si ottiene un errore 403
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");

            int respCode = urlConnection.getResponseCode();
            respReturnCode = HttpStatusCodes.intToHttpStatusCode(respCode);
            if (respCode==200){
                m_jsonData = getData(urlConnection);
            } else {
                m_jsonData=null;
            }
//            switch (respCode) {
//                case 200:
//                    m_jsonData = getData(urlConnection);
//                    //retVal = true;
//                    break;
//                default:
//                    //retVal = false;
//                    System.out.println(RipeUrl + IpValue);
//                    System.out.println("Other than ok: " + respCode+" "+respReturnCode);
//            }
        } catch (IOException ex) {
            //ex.printStackTrace();
        }

        //return retVal;
        return respReturnCode;
    }

    /**
     * Ritorna la country nello stream interrogato.
     * @return Contry relativa all'IP interrogato, null se non è stato trovato oin caso di errore
     */
    public String country(){
        String retVal="Unknown";
        StringReader sr = new StringReader(m_jsonData);
        JsonReader jsonReader = Json.createReader(sr);
        JsonObject joMain = jsonReader.readObject();
        jsonReader.close();

        // Controllo tutti i rami per capire se esistono (ad es. 231.4.5.6 ha l'array dell locations vuoto)
        JsonObject data = joMain.getJsonObject("data");
        if (data!=null) {
            JsonArray locatedResources = data.getJsonArray("located_resources");
            if (locatedResources!=null && locatedResources.size()>0) {
                JsonObject firstLocatedResource = locatedResources.getJsonObject(0);
                if (firstLocatedResource!=null) {
                    JsonArray locations = firstLocatedResource.getJsonArray("locations");
                    if (locations != null && locations.size() > 0) {
                        JsonObject firstLocation = locations.getJsonObject(0);
                        if (firstLocation != null) {
                            retVal = firstLocation.getString("country");
                        }
                    }
                }
            }
        }

        return retVal;
    }

    ///////// PRIVATE METHODS /////////

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

    private void getProxy(){
        System.setProperty("java.net.useSystemProxies", "true");
        List l = null;
        try {
            l = ProxySelector.getDefault().select(new URI("https://stats.ripe.net"));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Prendo il primo proxy disponibile tra quelli definiti a sistema
        if (l!=null) {
            proxy = (Proxy) l.get(0);
        } else {
            proxy = Proxy.NO_PROXY;
        }
//        if (l != null) {
//            for (Iterator iter = l.iterator(); iter.hasNext();) {
//                java.net.Proxy proxy = (java.net.Proxy) iter.next();
//                System.out.println("proxy hostname : " + proxy.type());
//                InetSocketAddress addr = (InetSocketAddress) proxy.address();
//                if (addr == null) {
//                    System.out.println("No Proxy");
//                }
//                else {
//                    System.out.println("proxy hostname : " + addr.getHostName());
//                    System.out.println("proxy port : " + addr.getPort());
//                }
//            }
//        }
    }
}
