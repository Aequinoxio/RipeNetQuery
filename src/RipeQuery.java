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

    private LocationData locationData = new LocationData();

    /**
     * Costruttore
     * Imposta il proxy di sistema
     */
    public RipeQuery() {
        // Imposto il proxy
        getProxy();
    }


    /**
     * Contatta stats.ripe.net ed analizza lo stream estraendo i dati della prima location
     * Se tutto va bene esegue il parsing dello stream ed imposta l'oggetto interno delal classe LocationData
     * Se ci sono stati problemi di connessione o di parsing lancia l'eccezione relativa
     * @return Codice di stato della chiamata al sito
     */
    public HttpStatusCodes downloadAndParseLocationData(String IpValue) throws IOException, ClassCastException {
        //String retVal="Unknown";
        boolean status=false;
        HttpStatusCodes respReturnCode=HttpStatusCodes.UNKNOWN_CODE;

        respReturnCode = queryIPAddressForCountry(IpValue);

        // Se non ho ottenuto nulla ritorno il codice di errore e basta, altrimenti faccio il parsing dello stream
        if (m_jsonData==null || m_jsonData.isEmpty()){
            return respReturnCode;
        }

        // Parsing dello stream
        StringReader sr = new StringReader(m_jsonData);
        JsonReader jsonReader = Json.createReader(sr);
        JsonObject joMain = jsonReader.readObject();
        jsonReader.close();

        // Controllo tutti i rami per capire se esistono (ad es. 231.4.5.6 ha l'array delle locations vuoto)
        // TODO: per robustezza verificare come passare il fatto che ci può essere un errore nel parsing del json (eccezione?)
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
                            //retVal = firstLocation.getString("country");
                            locationData.country=firstLocation.getString("country");
                            locationData.city=firstLocation.getString("city");
                            locationData.latitude=firstLocation.getJsonNumber("latitude").doubleValue();
                            locationData.longitude=firstLocation.getJsonNumber("longitude").doubleValue();
                            locationData.covered_percentage=firstLocation.getJsonNumber("covered_percentage").doubleValue();
                            //status=true;
                        }
                    }
                }
            }
        }

        return respReturnCode;
    }

    public String getCountry(){
        return locationData.country;
    }

    public String getCity(){
        return locationData.city;
    }

    public double getLatitude(){
        return locationData.latitude;

    }
    public double getLongitude(){
        return locationData.longitude;
    }

    public double getCoveredPercentage(){
        return locationData.covered_percentage;
    }

    ///////// PRIVATE METHODS /////////

    /**
     * Interroga stats.ripe.net per IP allo scopo di ottenere la country relativa
     * @param IpValue Indirizzo IP di cui conoscere la country. Modifica lo stato interno della classe con il json ritornato.
     * @return Stato della query
     */
    private HttpStatusCodes queryIPAddressForCountry(String IpValue) throws IOException {
        HttpStatusCodes respReturnCode=HttpStatusCodes.UNKNOWN_CODE;

        HttpsURLConnection urlConnection ;
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
        return respReturnCode;
    }


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
    }

    class LocationData{
        String country;
        String city;
        double longitude;
        double latitude;
        double covered_percentage;
    }
}
