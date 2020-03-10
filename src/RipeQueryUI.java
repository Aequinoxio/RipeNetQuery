/* ******************************************************************************
 * Copyright (c) 2020. This code follow the GPL v3 license scheme.
 ******************************************************************************/

import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class RipeQueryUI {
    @NonNls
    private static final String VERSIONE = "Versione 1.3 (stabile)"+System.lineSeparator()+"By Gabriele Galluzzo";

    @NonNls
    private static final String MY_BUNDLE = "strings";
    private static final ResourceBundle StringBundle = ResourceBundle.getBundle(MY_BUNDLE, Locale.getDefault());

    private static final String INFORMAZIONE_TITLE_DIALOG =StringBundle.getString("informazione") ;
    private static final String DLG_DATICOPIATI =StringBundle.getString("dati.copiati") ;
    private static final String DLG_ERRORE = StringBundle.getString("errore");
    private static final String DLG_FILE_IP_DUPLICATES = StringBundle.getString("il.file.non.contiene.ip.validi.o.sono.gia.presenti.nella.lista");
    private static final String DLG_FILE_IP_ERROR = StringBundle.getString("il.file.non.contiene.ip.validi");
    private static final String STATUS_WORKING = StringBundle.getString("working");
    private static final String INCOLLA_IP_DA_CONTROLLARE = StringBundle.getString("incolla.ip.da.controllare");
    private static final String INCOLLA_IP = StringBundle.getString("incolla.ip");
    private static final String COPIA_LOG = StringBundle.getString("copia.log");
    private static final String COPIA_DATI = StringBundle.getString("copia.dati");
    private static final String CANCELLO_TUTTI_GLI_IP_DA_CONTROLLARE_E_CONTROLLATI = StringBundle.getString("cancello.tutti.gli.ip.da.controllare.e.controllati");
    private static final String CONFERMA = StringBundle.getString("conferma");
    private static final String ABOUT = StringBundle.getString("about");

    private static final String CLIPBOARD_ERROR_IP_DUPLICATES_OR_INVALID = StringBundle.getString("gli.appunti.non.contengono.ip.validi.o.sono.gia.presenti.nella.lista");
    private static final String LA_CLIPBOARD_NON_CONTIENE_ALCUN_IP_VALIDO = StringBundle.getString("la.clipboard.non.contiene.alcun.ip.valido");
    private static final String NUMERO_IP_VALIDI = StringBundle.getString("numero.ip.validi");
    private static final String IP_DUPLICATI = StringBundle.getString("ip.duplicati");
    private static final String CARICATI = StringBundle.getString("caricati");
    private static final String IP_DAL_FILE = StringBundle.getString("ip.dal.file");
    private static final String FILE_NON_TROVATO = StringBundle.getString("nfile.non.trovato");
    private static final String FILE_ESISTE_SOVRASCRIVO = StringBundle.getString("nfile.esiste.n.sovrascrivo");
    private static final String FILE = StringBundle.getString("file.n");
    private static final String SALVATO_CON_SUCCESSO = StringBundle.getString("n.salvato.con.successo");
    private static final String ERRORE_NEL_SALVARE_IL_FILE = StringBundle.getString("errore.nel.salvare.il.file.n");
    private static final String REGEXP_SPLIT_LINES = "\\r?\\n";
    private static final String COPIA_LE_RIGHE_SELEZIONATE = StringBundle.getString("copia.le.righe.selezionate");

    private JButton btnOpenFile;
    private JTextArea txtResults;
    private JPanel mainPanel;
    private JButton btnIniziaAnalisi;
    private JLabel lblStatus;
    private JTextArea txtIpList;
    private JButton btnCancellaTutto;
    private JTable tblResults;
    private JButton btnCopyToclipboard;
    private JButton incollaIpButton;
    private JButton btnSalvaRisultati;
    private JLabel lblQueryResultValue;
    private JLabel lblStatusBar;
    private JProgressBar pbWorking;
    private DefaultTableModel tblResultModel;
    private JLabel lblQueryResult;


    // Provo a riaprire il file chooser dall'ultima posizione salvata
    @NonNls
    private final static String LAST_USED_FOLDER = "RipeQueryUI.LAST_USED_FOLDER";
    private final Preferences prefs = Preferences.userRoot().node(getClass().getName());
    private final JFileChooser jFileChooser = new JFileChooser(
            prefs.get(LAST_USED_FOLDER,
                    new File(".").getAbsolutePath())
    );

    String IPFilename;
    File IPFile;
    private final ArrayList<String> IPToBeChecked = new ArrayList<>();

    @NonNls
    private final static String IPRegexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\/\\d+){0,1}$";

    private static final String NUM = StringBundle.getString("num");
    private static final String SEARCHED_IP = StringBundle.getString("searched.ip");
    private static final String SEARCH_TIME = StringBundle.getString("search.time");
    private static final String RESOURCE = StringBundle.getString("resource");
    private static final String COUNTRY = StringBundle.getString("country");
    private static final String CITY = StringBundle.getString("city");
    private static final String COVERED_PERCENTAGE = StringBundle.getString("covered.percentage");
    private static final String LONGITUDE = StringBundle.getString("longitude");
    private static final String LATITUDE = StringBundle.getString("latitude");
    private static final String QUERY_TIME = StringBundle.getString("query.time");
    private static final String LATEST_TIME = StringBundle.getString("latest.time");
    private static final String RESULT_TIME = StringBundle.getString("result.time");
    private static final String EARLIEST_TIME = StringBundle.getString("earliest.time");

    private final static Object[] ColumnsName = new Object[]{
            NUM, SEARCHED_IP, SEARCH_TIME, RESOURCE, COUNTRY, CITY,
            LATITUDE, LONGITUDE, COVERED_PERCENTAGE,
            QUERY_TIME, LATEST_TIME, RESULT_TIME, EARLIEST_TIME};


    public RipeQueryUI() {
        // Margine personalizzato nelle label
        Border margin = new EmptyBorder(4, 4, 4, 4);
        lblStatusBar.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                margin));

        btnOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFileChooser.setMultiSelectionEnabled(false);
                int retVal = jFileChooser.showOpenDialog(mainPanel);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    IPFile = jFileChooser.getSelectedFile();

                    // Salvo l'ultima posizione
                    prefs.put(LAST_USED_FOLDER, IPFile.getParent());

                    if (!IPFile.exists()) {
                        JOptionPane.showMessageDialog(mainPanel, IPFile.getAbsolutePath() + FILE_NON_TROVATO, DLG_ERRORE, JOptionPane.ERROR_MESSAGE);
                    } else {
                        ///// Non reinizializzo l'array, aggiungo quello che trovo
                        IPFilename = IPFile.getAbsolutePath();

                        int startingIPNumbers = IPToBeChecked.size();
                        int skippedIP = parseFile(); // Aggiorno l'array IPToBeChecked
                        lblStatus.setText(NUMERO_IP_VALIDI + IPToBeChecked.size() +
                                IP_DUPLICATI + skippedIP);

                        txtResults.append(CARICATI + (IPToBeChecked.size() - startingIPNumbers) + IP_DAL_FILE +IPFilename+" "+System.lineSeparator());

                        // Se ne trovo almeno uno attivo il bottone della ricerca
                        if (IPToBeChecked.size() > 0) {
                            btnIniziaAnalisi.setEnabled(true);
                            btnCancellaTutto.setEnabled(true);

                            // Se non ho aggiunto ip ne do info
                            if (IPToBeChecked.size() == startingIPNumbers) {
                                JOptionPane.showMessageDialog(mainPanel, DLG_FILE_IP_DUPLICATES, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
                            }

                        } else {
                            btnIniziaAnalisi.setEnabled(false);
                            btnCancellaTutto.setEnabled(false);
                            JOptionPane.showMessageDialog(mainPanel, DLG_FILE_IP_ERROR, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);

                        }

                        txtIpList.setText("");
                        for (String IP : IPToBeChecked) {
                            txtIpList.append(IP);
                            txtIpList.append(System.lineSeparator());
                        }
                    }
                }
            }
        });

        btnIniziaAnalisi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnIniziaAnalisi.setEnabled(false);
                pbWorking.setVisible(true);
                pbWorking.setMaximum(IPToBeChecked.size());
                lblStatusBar.setText(STATUS_WORKING);
                txtResults.setText("");
                tblResultModel.setRowCount(0);

                DownloadWorker downloadWorker = new DownloadWorker();
                downloadWorker.execute();
            }
        });

        // Creo il popup menu per l'incolla IP nella jtextarea specifica
        JPopupMenu IPListPopupMenu = new JPopupMenu(INCOLLA_IP_DA_CONTROLLARE);
        JMenuItem incollaIPMenuItem = new JMenuItem(INCOLLA_IP);
        incollaIPMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                incollaDaClipBoard();
            }
        });

        IPListPopupMenu.add(incollaIPMenuItem);

        // Aggiungo il mouse listener al componente giusto
        txtIpList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    IPListPopupMenu.show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });


        // Aggiungo un popup menu anche nella textarea del log
        // Creo il popup menu per l'incolla IP nella jtextarea specifica
        JPopupMenu logPopupMenu = new JPopupMenu(COPIA_LOG);
        JMenuItem logMenuItem = new JMenuItem(COPIA_LOG);
        logMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiaLogSuClipboard();
            }
        });

        logPopupMenu.add(logMenuItem);

        // Aggiungo il mouse listener al componente giusto
        txtResults.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    logPopupMenu.show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });

        ////////////
        // Aggiungo un popup menu anche nella tabella dei risultati
        // Creo il popup menu per copiare gli IP dalla tabella specifica
        JPopupMenu resultsTablePopupMenu = new JPopupMenu(COPIA_DATI);
        JMenuItem resultsTableMenuItem = new JMenuItem(COPIA_DATI);
        resultsTableMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiaTabellaSuClipboard(false);
            }
        });

        resultsTablePopupMenu.add(resultsTableMenuItem);

        ////////////
        // Aggiungo un popup menu anche nella tabella dei risultati
        // Creo il popup menu per la copia delle righe selezionate
        //JPopupMenu resultsTableSelectedRowsPopupMenu = new JPopupMenu(COPIA_DATI);
        JMenuItem resultsTableSelectedRowsPopupMenu = new JMenuItem(COPIA_LE_RIGHE_SELEZIONATE);
        resultsTableSelectedRowsPopupMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //copiaRigheSelezionateSuClipboard();
                copiaTabellaSuClipboard(true);
            }
        });

        resultsTablePopupMenu.add(resultsTableSelectedRowsPopupMenu);

        // Aggiungo il mouse listener al componente giusto
        tblResults.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    resultsTablePopupMenu.show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });

        btnCancellaTutto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(mainPanel,
                        CANCELLO_TUTTI_GLI_IP_DA_CONTROLLARE_E_CONTROLLATI,
                        CONFERMA,
                        JOptionPane.OK_CANCEL_OPTION);

                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }

                txtIpList.setText("");
                txtResults.setText("");
                IPToBeChecked.clear();
                lblStatus.setText(NUMERO_IP_VALIDI + IPToBeChecked.size());
                btnIniziaAnalisi.setEnabled(false);
                tblResultModel.setRowCount(0);
                btnCancellaTutto.setEnabled(false);
                lblQueryResultValue.setText("");
                lblStatusBar.setText(" ");
                lblQueryResultValue.setText("");
            }
        });

        btnCopyToclipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiaTabellaSuClipboard(false);
            }
        });

        incollaIpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                incollaDaClipBoard();
            }
        });

        btnSalvaRisultati.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFileChooser.setMultiSelectionEnabled(false);
                int retVal = jFileChooser.showSaveDialog(mainPanel);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File saveFileChoosen = jFileChooser.getSelectedFile();

                    // Salvo l'ultima posizione
                    prefs.put(LAST_USED_FOLDER, saveFileChoosen.getParent());

                    if (saveFileChoosen.exists()) {
                        int chosenOption = JOptionPane.showConfirmDialog(mainPanel, saveFileChoosen.getAbsolutePath() + FILE_ESISTE_SOVRASCRIVO, INFORMAZIONE_TITLE_DIALOG, JOptionPane.OK_CANCEL_OPTION);
                        if (chosenOption != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    if (saveFile(saveFileChoosen)) {
                        JOptionPane.showMessageDialog(mainPanel, FILE + saveFileChoosen.getAbsolutePath() + SALVATO_CON_SUCCESSO, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
                        lblStatusBar.setText(SALVATO_CON_SUCCESSO);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, ERRORE_NEL_SALVARE_IL_FILE + saveFileChoosen.getAbsolutePath(), INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
                        lblStatusBar.setText(ERRORE_NEL_SALVARE_IL_FILE);
                    }
                }
            }
        });
    }


    private void copiaTabellaSuClipboard(boolean copySelected) {
        StringSelection stringSelection = new StringSelection(tableToString(copySelected));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        //JOptionPane.showMessageDialog(mainPanel, DLG_DATICOPIATI, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
        lblStatusBar.setText(DLG_DATICOPIATI);
    }

//    private void copiaRigheSelezionateSuClipboardq(){
//        int[] righeSelezionate = tblResults.getSelectedRows();
//
//        int rowIndex=0;
//        StringBuilder sb = new StringBuilder();
//        for (int i=0;i<righeSelezionate.length;i++){
//            rowIndex=righeSelezionate[i];
//            for (int j=0;j<tblResults.getColumnCount();j++) {
//                sb.append(tblResultModel.getValueAt(rowIndex, j)).append("\t");
//            }
//        }
//        StringSelection stringSelection = new StringSelection(tableToString());
//        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//        clipboard.setContents(stringSelection, null);
//        JOptionPane.showMessageDialog(mainPanel, DLG_DATICOPIATI, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
//
//    }

    private void copiaLogSuClipboard() {
        if (txtResults.getText().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, StringBundle.getString("nessun.dato.da.copiare.e.presente.nella.log.area"), INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(txtResults.getText());
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(mainPanel, DLG_DATICOPIATI, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
    }


    private String tableToString(boolean copySelected) {
        StringBuilder stringBuilder = new StringBuilder();
        int maxColumns = tblResults.getColumnCount();
        int maxRows;
        int[] rowsToBeCopied;
        if (copySelected){
            rowsToBeCopied = tblResults.getSelectedRows();
            maxRows = rowsToBeCopied.length;
        } else {
            rowsToBeCopied = new int [tblResults.getRowCount()];
            maxRows = tblResults.getRowCount();
        }
        // Costruisco la riga con le intestazioni
        for (int i = 0; i < maxColumns - 1; i++) {
            stringBuilder.append(tblResults.getColumnName(i)).append("\t");
        }

        stringBuilder.append(tblResults.getColumnName(maxColumns - 1)).append(System.lineSeparator());

        int rowCounter=0;
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxColumns - 1; j++) {
                if (copySelected){
                    rowCounter=rowsToBeCopied[i];
                } else {
                    rowCounter=i;
                }
                stringBuilder.append(tblResultModel.getValueAt(rowCounter, j)).append("\t");
            }
            stringBuilder.append(tblResultModel.getValueAt(i, maxColumns - 1)).append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }


    private boolean saveFile(File fileToBeSaved) {
        boolean result = false;
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(fileToBeSaved))) {
            String tableData = tableToString(false);
            bfw.write(tableData);
            result = true;
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return result;
    }

    /**
     * Parsing del file contenente la lista degl IP da controllare
     *
     * @return il numero di IP validi saltati in quanto già presenti nella lista
     */
    private int parseFile() {
        int skippedValidIP = 0;
        try (BufferedReader bfr = new BufferedReader(new FileReader(IPFile))) {
            String linea;
            while ((linea = bfr.readLine()) != null) {
                // Check se è un IP valido
                if (linea.matches(IPRegexp)) {
                    if (!IPToBeChecked.contains(linea)) {
                        IPToBeChecked.add(linea);
                    } else {
                        skippedValidIP++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skippedValidIP;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("RipeQueryUI");

        JMenuBar menuBar = new JMenuBar();
        JMenu jMenuQuestion = new JMenu("?");
        JMenuItem jMenuItemAbout = new JMenuItem(ABOUT);
        jMenuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, VERSIONE,INFORMAZIONE_TITLE_DIALOG,JOptionPane.INFORMATION_MESSAGE);
            }
        });
        jMenuQuestion.add(jMenuItemAbout);
        menuBar.add(jMenuQuestion);
        frame.setJMenuBar(menuBar);

        frame.setContentPane(new RipeQueryUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     *
     */
    private void createUIComponents() {
        tblResultModel = new DefaultTableModel(ColumnsName, 0);
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblResults = new JTable(tblResultModel);
        tblResults.getTableHeader().setReorderingAllowed(false);

        tblResults.setDefaultEditor(Object.class, null);
        tblResults.setDefaultRenderer(Object.class, defaultTableCellRenderer);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tblResults.getModel());

        // Imposto l'ordinamento della prima colonna ordina come numero.
        // Il sorter di default ordina come stringa
        sorter.setComparator(0, (Comparator<Integer>) Integer::compareTo);

        tblResults.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));

        sorter.setSortKeys(sortKeys);

    }

    private void incollaDaClipBoard() {
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipTf = sysClip.getContents(null);
        int startingIPNumbers = IPToBeChecked.size();

        if (clipTf != null) {
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String values = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
                    // Parsing del contenuto per verificare se ci sono IP
                    // Divido i dati per righe
                    String[] lines = values.split(REGEXP_SPLIT_LINES);
                    int skippedIP = 0;
                    for (String linea : lines) {
                        if (linea.matches(IPRegexp)) {
                            if (!IPToBeChecked.contains(linea)) {
                                IPToBeChecked.add(linea);
                                txtIpList.append(linea);
                                txtIpList.append(System.lineSeparator());
                            } else {
                                skippedIP++;
                            }

                        }
                    }

                    lblStatus.setText(NUMERO_IP_VALIDI + IPToBeChecked.size() +
                            IP_DUPLICATI + skippedIP
                    );

                    if (IPToBeChecked.size() > 0) {
                        if (IPToBeChecked.size() == startingIPNumbers) {
                            JOptionPane.showMessageDialog(mainPanel, CLIPBOARD_ERROR_IP_DUPLICATES_OR_INVALID, INFORMAZIONE_TITLE_DIALOG, JOptionPane.INFORMATION_MESSAGE);
                        }

                        btnIniziaAnalisi.setEnabled(true);
                        btnCancellaTutto.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, LA_CLIPBOARD_NON_CONTIENE_ALCUN_IP_VALIDO, DLG_ERRORE, JOptionPane.ERROR_MESSAGE);
                        btnIniziaAnalisi.setEnabled(false);
                        btnCancellaTutto.setEnabled(false);
                    }

                } catch (UnsupportedFlavorException | IOException ex) {
                    JOptionPane.showMessageDialog(mainPanel, LA_CLIPBOARD_NON_CONTIENE_ALCUN_IP_VALIDO, DLG_ERRORE, JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

            }
        }

    }

    class DownloadWorker extends SwingWorker<ArrayList<RipeQuery.LocationData>, String> implements DownloadUpdateCallback {
        private final String ERROR = StringBundle.getString("error.t");
        private final String IP = StringBundle.getString("ip");
        private final String FINISHED_FETCHED = StringBundle.getString("finished.fetched");
        private final String IP1 = StringBundle.getString("ip1");

        int masterCounter = 1; // Contatore per gli IP in tabella
        int masterPublishCounter = 0;   // Contatore per gli IP trattati e pubblicati. Potrebbero essere lo stesso valore ma il primo è
                                        // aggiornato in fase di update della tabella, l'altro per ogni IP recuperato dalla classe RipeQuery

        final RipeQuery ripeQuery = new RipeQuery(this);

        @Override
        protected ArrayList<RipeQuery.LocationData> doInBackground() {

            masterCounter = 1;

            // Recupero tutti i dati di interesse per ogni ip della lista
            for (String ip : IPToBeChecked) {
                HttpStatusCodes retval ;
                try {
                    retval = ripeQuery.downloadAndParseLocationData(ip);

                    // loggo gli errori e proseguo con il successivo IP
                    if (retval != HttpStatusCodes.OK) {

                        publish(ip + ERROR +
                                retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());

                        // In caso di errore metto nei log un messaggio di errore e proseguo con gli altri IP
                        continue;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainPanel, ex.toString(), DLG_ERRORE, JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }

            return null;
        }


        @Override
        protected void process(List<String> chunks) {
            super.process(chunks);
            for (String i : chunks) {
                txtResults.append(i + System.lineSeparator());
            }
        }

        @Override
        protected void done() {
            super.done();

            masterCounter=1; // Resetto il contatore per mostrare i numeri di riga nella tabella

            // Al temine del ciclo di recupero dati, mostro quelli che ho recuperato
            ArrayList<RipeQuery.LocationData> locationDataArrayList = ripeQuery.getAllLocationsData();
            for (RipeQuery.LocationData locationData : locationDataArrayList) {
                txtResults.append(
                        masterCounter +
                                " - " + locationData.IPQueried +
                                " - " + locationData.search_time +
                                " - " + locationData.resource +
                                " - " + locationData.country +
                                " - " + locationData.city +
                                " - " + locationData.latitude +
                                " - " + locationData.longitude +
                                " - " + locationData.covered_percentage +
                                " - " + locationData.query_time +
                                " - " + locationData.latest_time +
                                " - " + locationData.result_time +
                                " - " + locationData.earliest_time +
                                System.lineSeparator());

                tblResultModel.addRow(new Object[]{
                        masterCounter,
                        locationData.IPQueried,
                        locationData.search_time,
                        locationData.resource,
                        locationData.country,
                        locationData.city,
                        locationData.latitude,
                        locationData.longitude,
                        locationData.covered_percentage,
                        locationData.query_time,
                        locationData.latest_time,
                        locationData.result_time,
                        locationData.earliest_time
                });

                masterCounter++;

                lblQueryResultValue.setText(IP + (masterCounter - 1));
            }

            btnIniziaAnalisi.setEnabled(true);
            pbWorking.setVisible(false);
            lblStatusBar.setText(FINISHED_FETCHED + masterPublishCounter + IP1);

        }

        @Override
        public void update(String message) {
            publish(message);
            lblQueryResultValue.setText(IP + (masterPublishCounter - 1)+ " di "+IPToBeChecked.size());
            pbWorking.setValue(masterPublishCounter);
            masterPublishCounter++;
        }
    }

}
