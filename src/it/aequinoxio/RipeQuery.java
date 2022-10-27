/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

package it.aequinoxio;

import org.jetbrains.annotations.NonNls;

import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RipeQuery {
    // Bundle
    @NonNls
    protected static final String MY_BUNDLE = "strings";
    private static final ResourceBundle StringBundle = ResourceBundle.getBundle(MY_BUNDLE, Locale.getDefault());

    // Message strings
    private static final String ERRORE_RECUPERANDO_LE_RISORSE_PER_L_IP = StringBundle.getString("errore.recuperando.le.risorse.specifiche.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_L_OGGETTO_LOCATION = StringBundle.getString("errore.nel.recuperare.l.oggetto.location");
    private static final String ERRORE_NEL_RECUPERARE_LE_LOCATIONS_PER_L_IP = StringBundle.getString("errore.nel.recuperare.le.locations.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_LA_PRIMA_LOCATED_RESOURCES_PER_L_IP = StringBundle.getString("errore.nel.recuperare.la.prima.located.resources.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_L_ARRAY_DELLE_LOCATED_RESOURCES_PER_L_IP = StringBundle.getString("errore.nel.recuperare.l.array.delle.located.resources.per.l.ip");
    private static final String ERRORE_NEL_RECUPERARE_I_DATI_PER_L_IP = StringBundle.getString("errore.nel.recuperare.i.dati.per.l.ip");
    private static final String CHECKING = StringBundle.getString("checking");
    private static final String MESSAGGIO_DAL_SERVER = StringBundle.getString("messaggio.dal.server");

    private static final String LOG_MESSAGE_SEPARATOR = "\n\t- ";

    // Proxy variables
    private static final String USER_AGENT = "User-Agent";
    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";
    private static final String HTTPS_STATS_RIPE_NET = "https://stats.ripe.net";
    Proxy proxy = Proxy.NO_PROXY;

    // Query variables
    private static final String RIPE_URL = "https://stat.ripe.net/data/maxmind-geo-lite/data.json?resource=";

    // Raw data fetched
    String m_jsonData;

    // Data parsed
    private LocationData locationData;
    private final ArrayList<LocationData> locationDataArrayList = new ArrayList<>();

    // Call back for update
    private final DownloadUpdateCallback downloadUpdateCallback;

    /**
     * Costruttore
     * Imposta il proxy di sistema e la callback
     */
    public RipeQuery(DownloadUpdateCallback downloadUpdateCallback) {
        // Imposto il proxy
        getProxy();
        this.downloadUpdateCallback = downloadUpdateCallback;
    }

    /**
     * Contatta stats.ripe.net ed analizza lo stream estraendo i dati della prima location
     * Se tutto va bene esegue il parsing dello stream ed imposta l'oggetto interno delal classe LocationData
     * Se ci sono stati problemi di connessione o di parsing lancia l'eccezione relativa
     *
     * @param IpValue Ip di cui scaricare i dati
     * @return Codice di stato della chiamata al sito
     */
    public HttpStatusCodes downloadAndParseLocationData(String IpValue) throws IOException, ClassCastException {
        //String retVal="Unknown";
        // boolean status=false;
        HttpStatusCodes respReturnCode = HttpStatusCodes.UNKNOWN_CODE;

        respReturnCode = queryIPAddressForCountry(IpValue);

// TODO: Da sistemare
        // Se non ho ottenuto nulla ritorno il codice di errore e basta, altrimenti faccio il parsing dello stream
        if (m_jsonData == null || m_jsonData.isEmpty()) {
            return respReturnCode;
        }

        parseStream(IpValue);

        return respReturnCode;
    }

    /**
     * Carica il json da file e ne esegue il parsing
     *
     * @param IpValueTemp Ip a cui il file si riferisce
     * @param tempFile    File da caricare
     * @return true se tutto è andato a buon fine, false in caso di errore nel caricamento del file o nel parsing
     */
    public boolean parseFromFile(String IpValueTemp, File tempFile) {

        StringBuilder sb = new StringBuilder();

        try (BufferedReader bfr = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8))
        ) {
            String linea;
            while ((linea = bfr.readLine()) != null) {
                sb.append(linea);
            }
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }

        m_jsonData = sb.toString();

        return parseStream(IpValueTemp);
    }

    /**
     * Parsing dello stream memorizzatoin m_jsonData
     *
     * @param IpValueTemp Ip a cui lo stream si riferisce
     * @return true se tutto è andato bene, false altrimenti
     */
    private boolean parseStream(String IpValueTemp) {
        boolean allOk = false;
        // Parsing dello stream
        StringReader sr = new StringReader(m_jsonData);
        JsonReader jsonReader = Json.createReader(sr);
        JsonObject joMain = null;
        try {
            joMain = jsonReader.readObject();
        } catch (Exception e) {
            if (downloadUpdateCallback != null) {
                downloadUpdateCallback.update("Errore nell'interpretazione dell'oggetto JSON"
                        + " (" + e.toString() + ")" +
                        LOG_MESSAGE_SEPARATOR);
            }
            //e.printStackTrace();
        }
        jsonReader.close();

        // Se non riesco ad interpretare il json esco
        if (joMain==null){
            return false;
        }

        // Controllo tutti i rami per capire se esistono (ad es. 231.4.5.6 ha l'array delle locations vuoto)
        JsonObject data = joMain.getJsonObject("data");

        //String IPResource;

        if (data != null) {
            String query_time;
            // Al 2 maggio 2020 sembra che questo parametro sia stato cancellato dal json. Se ho un errore salto tutto ed imposto un valore di default
            try {
                query_time = data.getJsonObject("parameters").getString("query_time");
            } catch (NullPointerException | ClassCastException e){
                query_time="-- NON DISPONIBILE --";
            }

            JsonArray locatedResources = data.getJsonArray("located_resources");

            if (locatedResources != null && locatedResources.size() > 0) {
                // Sembra che located resources nelle query per IP ritorni sempre un array con un solo elemento
                // In ogni caso faccio il parsiong come se fosse un array
                for (int k = 0; k < locatedResources.size(); k++) {
                    JsonObject locateResourceObject = locatedResources.getJsonObject(k);
                    if (locateResourceObject != null) {

                        JsonArray locations = locateResourceObject.getJsonArray("locations");

                        if (locations != null && locations.size() > 0) {
                            // Ciclo sull'array delle locations
                            for (int i = 0; i < locations.size(); i++) {
                                JsonObject locationObject = locations.getJsonObject(i);

                                if (locationObject != null) {

                                    try {
                                        // Estraggo tutte le eventuali resources  associate all'ip interrogato
                                        // e le aggiungo all'array che rappresenta la risposta: un oggetto per
                                        // ciascuna resource
                                        JsonArray resourcesArray = locationObject.getJsonArray("resources");

                                        // Devo fare un ciclo esplicito e non un for(JsonValue resourceValue : resourcesArray)
                                        // perché altrimenti il valore dell'array comprende le virgolettte
                                        for (int j = 0; j < resourcesArray.size(); j++) {
                                            //String resourceString = resourcesArray.getString(j);

                                            locationData = new LocationData();

                                            locationData.IPQueried = IpValueTemp;
                                            locationData.resource = resourcesArray.getString(j);

                                            locationData.country = locationObject.getString("country");
                                            locationData.city = locationObject.getString("city");
                                            locationData.latitude = locationObject.getJsonNumber("latitude").doubleValue();
                                            locationData.longitude = locationObject.getJsonNumber("longitude").doubleValue();
                                            locationData.covered_percentage = locationObject.getJsonNumber("covered_percentage").doubleValue();

                                            locationData.search_time = joMain.getString("time");

                                            locationData.query_time = query_time;

                                            locationData.latest_time = data.getString("latest_time");
                                            locationData.earliest_time = data.getString("earliest_time");
                                            locationData.result_time = data.getString("result_time");

                                            locationDataArrayList.add(locationData);

                                            // Aggiorno il caller
                                            if (downloadUpdateCallback != null) {
                                                downloadUpdateCallback.update(CHECKING + IpValueTemp + " - " + locationData.resource);
                                            }

                                            allOk = true;
                                        }
                                    } catch (NullPointerException | ClassCastException e) {
                                        if (downloadUpdateCallback != null) {
                                            downloadUpdateCallback.update(ERRORE_RECUPERANDO_LE_RISORSE_PER_L_IP + IpValueTemp + " (" + e.toString() + ")" +
                                                    LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
                                        }
                                        //e.printStackTrace();
                                    }

                                } else {
                                    if (downloadUpdateCallback != null) {
                                        downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_L_OGGETTO_LOCATION + i +
                                                LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
                                    }
                                }
                            }

                        } else {
                            if (downloadUpdateCallback != null) {
                                downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_LE_LOCATIONS_PER_L_IP + IpValueTemp +
                                        LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
                            }
                        }
                    } else {
                        if (downloadUpdateCallback != null) {
                            downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_LA_PRIMA_LOCATED_RESOURCES_PER_L_IP + IpValueTemp +
                                    LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
                        }
                    }
                }
            } else {
                if (downloadUpdateCallback != null) {
                    downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_L_ARRAY_DELLE_LOCATED_RESOURCES_PER_L_IP + IpValueTemp +
                            LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
                }
            }
        } else {
            if (downloadUpdateCallback != null) {
                downloadUpdateCallback.update(ERRORE_NEL_RECUPERARE_I_DATI_PER_L_IP + IpValueTemp +
                        LOG_MESSAGE_SEPARATOR + tryGetErrorMessages(joMain));
            }
        }

        return allOk;
    }

    /**
     * Prova a recuperare i messaggi (campo "messages" dello stream Json) in caso di errore
     *
     * @return Concantenazione dei messaggi recuperati o stringa vuota
     */
    private String tryGetErrorMessages(JsonObject joMain) {
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGGIO_DAL_SERVER);
        try {
            JsonArray messagesArray = joMain.getJsonArray("messages");
            if (messagesArray != null && messagesArray.size() > 0) {
                for (int i = 0; i < messagesArray.size(); i++) {
                    JsonArray messageObjects = messagesArray.getJsonArray(i);
                    if (messageObjects != null && messageObjects.size() > 0) {

                        sb.append(" - ");

                        for (int j = 0; j < messageObjects.size(); j++) {
                            sb.append(messageObjects.getString(j));
                            sb.append(" - ");
                        }
                    }

                    sb.append(System.lineSeparator());
                }
            }
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            // Salto semplicemente l'eccezione
        }
        return sb.toString();
    }

    public ArrayList<LocationData> getAllLocationsData() {
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

    public String getLatestResource() {
        return locationData.resource;
    }

    public String getLatestCountry() {
        return locationData.country;
    }

    public String getLatestCity() {
        return locationData.city;
    }

    public double getLatestLatitude() {
        return locationData.latitude;

    }

    public double getLatestLongitude() {
        return locationData.longitude;
    }

    public double getLatestCoveredPercentage() {
        return locationData.covered_percentage;
    }

    /**
     * Ritorna la stringa json scaricata dal sito ripe.net corrispondente all'ultimo downloadAndParse effettuato
     *
     * @return Stringa json
     */
    public String getRawResponse() {
        // Clono l'oggetto interno per evitare di esporlo
        StringBuilder sb = new StringBuilder();
        sb.append(m_jsonData);
        return sb.toString();
    }

    ///////// PRIVATE METHODS /////////

    /**
     * Interroga stats.ripe.net per IP allo scopo di ottenere la country relativa
     * Imposta la variabile di classe m_jsonData con il contenuto del json scaricato
     *
     * @param IpValue Indirizzo IP di cui conoscere la country. Modifica lo stato interno della classe con il json ritornato.
     * @return Stato della query
     */
    private HttpStatusCodes queryIPAddressForCountry(String IpValue) throws IOException {
        HttpStatusCodes respReturnCode = HttpStatusCodes.UNKNOWN_CODE;

        HttpsURLConnection urlConnection;
        URL url = new URL(RIPE_URL + IpValue);
        urlConnection = (HttpsURLConnection) url.openConnection(proxy);

        // Indispensabile altrimenti si ottiene un errore 403
        urlConnection.addRequestProperty(USER_AGENT, USER_AGENT_VALUE);

        int respCode = urlConnection.getResponseCode();
        respReturnCode = HttpStatusCodes.intToHttpStatusCode(respCode);
        if (respCode == 200) {
            m_jsonData = getData(urlConnection);
        } else {
            m_jsonData = null;
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

    private void getProxy() {
        System.setProperty("java.net.useSystemProxies", "true");
        List<Proxy> l = null;
        try {
            l = ProxySelector.getDefault().select(new URI(HTTPS_STATS_RIPE_NET));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Prendo il primo proxy disponibile tra quelli definiti a sistema
        if (l != null) {
            proxy = l.get(0);
        } else {
            proxy = Proxy.NO_PROXY;
        }
    }

    static class LocationData implements Cloneable {
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
