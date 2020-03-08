/* ******************************************************************************
 * Copyright (c) 2020. This code follow the GPL v3 license scheme.
 ******************************************************************************/

import org.jetbrains.annotations.NonNls;

import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class RipeQuery {
    @NonNls
    private static final String MY_BUNDLE = "strings";
    private static final ResourceBundle StringBundle = ResourceBundle.getBundle(MY_BUNDLE, Locale.getDefault());

    private static final String ERRORE_RECUPERANDO_LE_RISORSE_PER_L_IP = StringBundle.getString("errore.recuperando.le.risorse.specifiche.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_L_OGGETTO_LOCATION = StringBundle.getString("errore.nel.recuperare.l.oggetto.location");
    private static final String ERRORE_NEL_RECUPERARE_LE_LOCATIONS_PER_L_IP = StringBundle.getString("errore.nel.recuperare.le.locations.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_LA_PRIMA_LOCATED_RESOURCES_PER_L_IP = StringBundle.getString("errore.nel.recuperare.la.prima.located.resources.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_L_ARRAY_DELLE_LOCATED_RESOURCES_PER_L_IP = StringBundle.getString("errore.nel.recuperare.l.array.delle.located.resources.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_I_DATI_PER_L_IP = StringBundle.getString("errore.nel.recuperare.i.dati.per.l.ip");
    //private ArrayList<String> IPToBeChecked = new ArrayList<>();
    private final String RipeUrl = "https://stat.ripe.net/data/maxmind-geo-lite/data.json?resource=";
    String m_jsonData;
    Proxy proxy = Proxy.NO_PROXY;

    private LocationData locationData; // = new LocationData();
    private final ArrayList<LocationData> locationDataArrayList = new ArrayList<>();

    private final DownloadUpdateCallback downloadUpdateCallback;
    /**
     * Costruttore
     * Imposta il proxy di sistema
     */
    public RipeQuery(DownloadUpdateCallback downloadUpdateCallback) {
        // Imposto il proxy
        getProxy();
        this.downloadUpdateCallback=downloadUpdateCallback;
    }


    /**
     * Contatta stats.ripe.net ed analizza lo stream estraendo i dati della prima location
     * Se tutto va bene esegue il parsing dello stream ed imposta l'oggetto interno delal classe LocationData
     * Se ci sono stati problemi di connessione o di parsing lancia l'eccezione relativa
     * @return Codice di stato della chiamata al sito
     */
    public HttpStatusCodes downloadAndParseLocationData(String IpValue) throws IOException, ClassCastException {
        //String retVal="Unknown";
       // boolean status=false;
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
        //String IPResource;

        if (data!=null) {
            JsonArray locatedResources = data.getJsonArray("located_resources");

            if (locatedResources!=null && locatedResources.size()>0) {
                // Sembra che located resources nelle query per IP ritorni sempre un array con un solo elemento
                // N.B. nelle query per AS ritorna un array con più elementi - Tenerne conto in caso di estenda la classe per altri tipi di query
                JsonObject firstLocatedResource = locatedResources.getJsonObject(0);
                if (firstLocatedResource!=null) {
                    //IPResource = firstLocatedResource.getString("resource");
                    //locationData.IP= IPResource;
                    JsonArray locations = firstLocatedResource.getJsonArray("locations");

                    if (locations != null && locations.size() > 0) {
                        // Ciclo sull'array delle locations
                        for (int i=0;i< locations.size();i++) {
                            JsonObject locationObject = locations.getJsonObject(i);

                            if (locationObject != null) {

                                try {
                                    // Estraggo tutte le eventuali resources  associate all'ip interrogato
                                    // e le aggiungo all'array che rappresenta la risposta: un oggetto per
                                    // ciascuna resource
                                    JsonArray resourcesArray = locationObject.getJsonArray("resources");

                                    // Devo fare un ciclo esplicito e non un for(JsonValue resourceValue : resourcesArray)
                                    // perché altrimenti il valore dell'array comprende le virgolettte
                                    for (int j = 0 ;j< resourcesArray.size();j++){
                                        //String resourceString = resourcesArray.getString(j);

                                        locationData = new LocationData();

                                        locationData.IPQueried = IpValue;
                                        locationData.resource= resourcesArray.getString(j);
                                        locationData.country = locationObject.getString("country");
                                        locationData.city = locationObject.getString("city");
                                        locationData.latitude = locationObject.getJsonNumber("latitude").doubleValue();
                                        locationData.longitude = locationObject.getJsonNumber("longitude").doubleValue();
                                        locationData.covered_percentage = locationObject.getJsonNumber("covered_percentage").doubleValue();

                                        locationData.search_time=joMain.getString("time");

                                        locationData.query_time=data.getJsonObject("parameters").getString("query_time");
                                        locationData.latest_time=data.getString("latest_time");
                                        locationData.earliest_time=data.getString("earliest_time");
                                        locationData.result_time=data.getString("result_time");

                                        locationDataArrayList.add(locationData);

                                        if (downloadUpdateCallback!=null){
                                            downloadUpdateCallback.update(IpValue+ " - "+ locationData.resource);
                                        }
                                    }
                                } catch (NullPointerException | ClassCastException e){
                                    downloadUpdateCallback.update(ERRORE_RECUPERANDO_LE_RISORSE_PER_L_IP +IpValue+ " ("+e.toString()+ ")");
                                    //e.printStackTrace();
                                }

                            } else {
                                downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_L_OGGETTO_LOCATION +i);
                            }
                        }

                    } else {
                        downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_LE_LOCATIONS_PER_L_IP +IpValue);
                    }
                } else {
                    downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_LA_PRIMA_LOCATED_RESOURCES_PER_L_IP +IpValue);
                }
            } else {
                downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_L_ARRAY_DELLE_LOCATED_RESOURCES_PER_L_IP +IpValue);
            }
        } else {
            downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_I_DATI_PER_L_IP +IpValue);
        }

        return respReturnCode;
    }

    public ArrayList<LocationData> getAllLocationsData(){
        // Deep copy
        ArrayList<LocationData> temp = new ArrayList<>();
        try {
            for (LocationData loc : locationDataArrayList) {
                temp.add((LocationData) loc.clone());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return temp;
    }

    public String getLatestResource(){
        return locationData.resource;
    }
    public String getLatestCountry(){
        return locationData.country;
    }

    public String getLatestCity(){
        return locationData.city;
    }

    public double getLatestLatitude(){
        return locationData.latitude;

    }
    public double getLatestLongitude(){
        return locationData.longitude;
    }

    public double getLatestCoveredPercentage(){
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

        // Per sicurezza
        reader.close();
        data.close();
        return sb.toString();
    }

    private void getProxy(){
        System.setProperty("java.net.useSystemProxies", "true");
        List<Proxy> l = null;
        try {
            l = ProxySelector.getDefault().select(new URI("https://stats.ripe.net"));
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Prendo il primo proxy disponibile tra quelli definiti a sistema
        if (l!=null) {
            proxy = l.get(0);
        } else {
            proxy = Proxy.NO_PROXY;
        }
    }

    static class LocationData implements Cloneable{
        String IPQueried;
        String search_time;
        String resource;
        String country;
        String city;
        double longitude;
        double latitude;
        double covered_percentage;

        String query_time;
        String latest_time;
        String result_time;
        String earliest_time;

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
