/* ******************************************************************************
 * Copyright (c) 2020. This code follow the GPL v3 license scheme.
 ******************************************************************************/

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

public class RipeQueryUI {
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
    private final static String LAST_USED_FOLDER = "RipeQueryUI.LAST_USED_FOLDER";
    private final Preferences prefs = Preferences.userRoot().node(getClass().getName());
    private final JFileChooser jFileChooser = new JFileChooser(
            prefs.get(LAST_USED_FOLDER,
                    new File(".").getAbsolutePath())
    );

    String IPFilename;
    File IPFile;
    private final ArrayList<String> IPToBeChecked = new ArrayList<>();

    private final static String IPRegexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\/\\d+){0,1}$";

    private final static Object[] ColumnsName = new Object[]{
            "Num", "Searched IP", "Search time", "Resource", "Country", "City",
            "Latitude", "Longitude", "Covered percentage",
            "Query time", "Latest time", "Result time", "Earliest time"};

    public RipeQueryUI() {
        // Margine personalizzato nelle label
        Border margin = new EmptyBorder(4, 4, 4, 4);
        lblStatusBar.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                margin));
//        lblFilename.setBorder(new CompoundBorder(
//                new LineBorder(Color.BLACK, 1, true),
//                margin));

        //lblStatus.setBorder(margin);

        btnOpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setMultiSelectionEnabled(false);
                int retVal = jFileChooser.showOpenDialog(mainPanel);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    IPFile = jFileChooser.getSelectedFile();

                    // Salvo l'ultima posizione
                    prefs.put(LAST_USED_FOLDER, IPFile.getParent());

                    if (!IPFile.exists()) {
                        JOptionPane.showMessageDialog(mainPanel, IPFile.getAbsolutePath() + "\nFile non trovato", "Errore", JOptionPane.ERROR_MESSAGE);
                    } else {
                        ///// Non reinizializzo l'array, aggiungo quello che trovo

                        IPFilename = IPFile.getAbsolutePath();

                       // lblFilename.setText(IPFilename);

                        int startingIPNumbers = IPToBeChecked.size();
                        int skippedIP = parseFile(); // Aggiorno l'array IPToBeChecked
                        lblStatus.setText("Numero IP validi: " + IPToBeChecked.size() +
                                " - IP duplicati: " + skippedIP);

                        txtResults.append("Caricati "+ (IPToBeChecked.size() - startingIPNumbers) +" IP dal file "+IPFilename+" "+"\n");

                        // Se ne trovo almeno uno attivo il bottone della ricerca
                        if (IPToBeChecked.size() > 0) {
                            btnIniziaAnalisi.setEnabled(true);
                            btnCancellaTutto.setEnabled(true);

                            // Se non ho aggiunto ip ne do info
                            if (IPToBeChecked.size() == startingIPNumbers) {
                                JOptionPane.showMessageDialog(mainPanel, "Il file non contiene IP validi o sono già presenti nella lista", "Informazione", JOptionPane.INFORMATION_MESSAGE);
                            }

                        } else {
                            btnIniziaAnalisi.setEnabled(false);
                            btnCancellaTutto.setEnabled(false);
                            JOptionPane.showMessageDialog(mainPanel, "Il file non contiene IP validi", "Informazione", JOptionPane.INFORMATION_MESSAGE);

                        }

                        //txtResults.setText("");
                        txtIpList.setText("");
                        for (String IP : IPToBeChecked) {
                            txtIpList.append(IP);
                            txtIpList.append("\n");
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
                lblStatusBar.setText("Working...");
                txtResults.setText("");
                tblResultModel.setRowCount(0);

                DownloadWorker downloadWorker = new DownloadWorker();
                downloadWorker.execute();
                //JOptionPane.showMessageDialog(mainPanel,"Working","OK",JOptionPane.OK_CANCEL_OPTION);

//                RipeQuery ripeQuery = new RipeQuery();
//                txtResults.setText("");
//                tblResultModel.setRowCount(0);
//                int masterCounter = 1;
//
//                // Recupero tutti i dati di interesse per ogni ip della lista
//                for (String ip : IPToBeChecked) {
//                    HttpStatusCodes retval = null;
//                    try {
//                        //retval = ripeQuery.queryIPAddressForCountry(ip);
//                        retval = ripeQuery.downloadAndParseLocationData(ip);
//
//                        // loggo gli errori e proseguo con il successivo IP
//                        if (retval != HttpStatusCodes.OK) {
//                            txtResults.append(
//                                    ip + " - Error: \t" +
//                                            retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc()
//                            );
//
//                            txtResults.append("\n");
//
//                            //lblQueryResultValue.setText("");
//
//                            // In caso di errore metto nei log un messaggio di errore e proseguo con gli altri IP
//                            continue;
//                        }
//                    } catch (IOException ex) {
//                        JOptionPane.showMessageDialog(mainPanel, ex.toString(), "Errore", JOptionPane.ERROR_MESSAGE);
//                        break;
//                    }
//                }
//
//                // Al temine del ciclo di recupero dati, mostro quelli che ho recuperato
//                ArrayList<RipeQuery.LocationData> locationDataArrayList = ripeQuery.getAllLocationsData();
//                for (RipeQuery.LocationData locationData : locationDataArrayList) {
//                    txtResults.append(
//                            masterCounter +
//                                    " - " + locationData.IPQueried +
//                                    " - " + locationData.search_time +
//                                    " - " + locationData.resource +
//                                    " - " + locationData.country +
//                                    " - " + locationData.city +
//                                    " - " + locationData.latitude +
//                                    " - " + locationData.longitude +
//                                    " - " + locationData.covered_percentage +
//                                    " - " + locationData.query_time +
//                                    " - " + locationData.latest_time +
//                                    " - " + locationData.result_time +
//                                    " - " + locationData.earliest_time +
//                                    "\n");
//
//                    tblResultModel.addRow(new Object[]{
//                            masterCounter,
//                            locationData.IPQueried,
//                            locationData.search_time,
//                            locationData.resource,
//                            locationData.country,
//                            locationData.city,
//                            locationData.latitude,
//                            locationData.longitude,
//                            locationData.covered_percentage,
//                            locationData.query_time,
//                            locationData.latest_time,
//                            locationData.result_time,
//                            locationData.earliest_time
//                    });
//
//                    masterCounter++;
//
//                    lblQueryResultValue.setText("IP: " + (masterCounter - 1));
//                }
            }
        });

        // Creo il popup menu per l'incolla IP nella jtextarea specifica
        JPopupMenu IPListPopupMenu = new JPopupMenu("Incolla IP da controllare");
        JMenuItem incollaIPMenuItem = new JMenuItem("Incolla IP");
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
        JPopupMenu logPopupMenu = new JPopupMenu("Copia log");
        JMenuItem logMenuItem = new JMenuItem("Copia log");
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
        // Creo il popup menu per l'incolla IP nella jtextarea specifica
        JPopupMenu resultsTablePopupMenu = new JPopupMenu("Copia dati");
        JMenuItem resultsTableMenuItem = new JMenuItem("Copia dati");
        resultsTableMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiaTabellaSuClipboard();
            }
        });

        resultsTablePopupMenu.add(resultsTableMenuItem);

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
                        "Cancello tutti gli IP da controllare e controllati?",
                        "Conferma",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }

                txtIpList.setText("");
                txtResults.setText("");
                IPToBeChecked.clear();
                lblStatus.setText("Numero IP validi: " + IPToBeChecked.size());
                btnIniziaAnalisi.setEnabled(false);
                tblResultModel.setRowCount(0);
                btnCancellaTutto.setEnabled(false);
                lblQueryResultValue.setText("");
                lblStatusBar.setText(" ");
            }
        });

        btnCopyToclipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copiaTabellaSuClipboard();
//                StringSelection stringSelection = new StringSelection(tableToString());
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                clipboard.setContents(stringSelection, null);
//                JOptionPane.showMessageDialog(mainPanel, "Dati copiati", "Informazione", JOptionPane.INFORMATION_MESSAGE);
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
                        int chosenOption = JOptionPane.showConfirmDialog(mainPanel, saveFileChoosen.getAbsolutePath() + "\nFile esiste\n Sovrascrivo?", "Informazione", JOptionPane.OK_CANCEL_OPTION);
                        if (chosenOption != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    if (saveFile(saveFileChoosen)) {
                        JOptionPane.showMessageDialog(mainPanel, "File:\n" + saveFileChoosen.getAbsolutePath() + "\n salvato con successo ", "Informazione", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "Errore nel salvare il file:\n" + saveFileChoosen.getAbsolutePath(), "Informazione", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private void copiaTabellaSuClipboard() {
        StringSelection stringSelection = new StringSelection(tableToString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(mainPanel, "Dati copiati", "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }

    private void copiaLogSuClipboard() {
        if (txtResults.getText().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Nessun dato da copiare è presente nella log area", "Informazione", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(txtResults.getText());
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(mainPanel, "Dati copiati", "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }


    private String tableToString() {
        StringBuilder stringBuilder = new StringBuilder();
        int maxColumns = tblResults.getColumnCount();
        // Costruisco la riga con le intestazioni
        for (int i = 0; i < maxColumns - 1; i++) {
            stringBuilder.append(tblResults.getColumnName(i)).append("\t");
        }

        stringBuilder.append(tblResults.getColumnName(maxColumns - 1)).append(System.lineSeparator());

        for (int i = 0; i < tblResultModel.getRowCount(); i++) {
            for (int j = 0; j < maxColumns - 1; j++) {
                stringBuilder.append(tblResultModel.getValueAt(i, j)).append("\t");
            }
            stringBuilder.append(tblResultModel.getValueAt(i, maxColumns - 1)).append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }


    private boolean saveFile(File fileToBeSaved) {
        boolean result = false;
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(fileToBeSaved))) {
            String tableData = tableToString();
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
        JMenuItem jMenuItemAbout = new JMenuItem("About");
        jMenuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame,"Versione 6","Informazione",JOptionPane.INFORMATION_MESSAGE);
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
//        {
//            @Override
//            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//                Component component = super.prepareRenderer(renderer, row, column);
//                int rendererWidth = component.getPreferredSize().width;
//                TableColumn tableColumn = getColumnModel().getColumn(column);
//                tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
//                return component;
//                //return super.prepareRenderer(renderer, row, column);
//            }
//        };

        //tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tblResults.setDefaultEditor(Object.class, null);
        tblResults.setDefaultRenderer(Object.class, defaultTableCellRenderer);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tblResults.getModel());

        // La prima colonna ordina come numero. Il sorter di default ordina come stringa
        sorter.setComparator(0, (Comparator<Integer>) Integer::compareTo);

        tblResults.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        //sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
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
                    // Dividol'analisi per righe
                    String[] lines = values.split("\\r?\\n");
                    int skippedIP = 0;
                    for (String linea : lines) {
                        if (linea.matches(IPRegexp)) {
                            if (!IPToBeChecked.contains(linea)) {
                                IPToBeChecked.add(linea);
                                txtIpList.append(linea);
                                txtIpList.append("\n");
                            } else {
                                skippedIP++;
                            }

                        }
                    }

                    lblStatus.setText("Numero IP validi: " + IPToBeChecked.size() +
                            " - IP duplicati: " + skippedIP
                    );

//                    +
//                            " - IP nuovi: " + (IPToBeChecked.size()-startingIPNumbers)
//                    );

                    if (IPToBeChecked.size() > 0) {
                        if (IPToBeChecked.size() == startingIPNumbers) {
                            JOptionPane.showMessageDialog(mainPanel, "Gli appunti non contengono IP validi o sono già presenti nella lista", "Informazione", JOptionPane.INFORMATION_MESSAGE);
                        }

                        btnIniziaAnalisi.setEnabled(true);
                        btnCancellaTutto.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "La Clipboard non contiene alcun ip valido", "Errore", JOptionPane.ERROR_MESSAGE);
                        btnIniziaAnalisi.setEnabled(false);
                        btnCancellaTutto.setEnabled(false);
                    }

                } catch (UnsupportedFlavorException | IOException ex) {
                    JOptionPane.showMessageDialog(mainPanel, "La Clipboard non contiene alcun ip valido", "Errore", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

            }
        }

    }

    class DownloadWorker extends SwingWorker<ArrayList<RipeQuery.LocationData>, String> implements DownloadUpdateCallback {
        int masterCounter = 1; // Contatore per gli IP in tabella
        int masterPublishCounter = 0; // Contatore per gli IP trattati e pubblicati. Potrebbero essere lo stesso valore ma il primo è
                                        // aggiornato in fase di update della tabella, l'altro per ogni IP recuperato dalla classe RipeQuery

        final RipeQuery ripeQuery = new RipeQuery(this);

        @Override
        protected ArrayList<RipeQuery.LocationData> doInBackground() {

//            txtResults.setText("");
//            tblResultModel.setRowCount(0);
            masterCounter = 1;

            // Recupero tutti i dati di interesse per ogni ip della lista
            for (String ip : IPToBeChecked) {
                HttpStatusCodes retval ;
                try {
                    //retval = ripeQuery.queryIPAddressForCountry(ip);
                    retval = ripeQuery.downloadAndParseLocationData(ip);

                    // loggo gli errori e proseguo con il successivo IP
                    if (retval != HttpStatusCodes.OK) {

                        publish(ip + " - Error: \t" +
                                retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());

                        // In caso di errore metto nei log un messaggio di errore e proseguo con gli altri IP
                        continue;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainPanel, ex.toString(), "Errore", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }

            return null;
        }


        @Override
        protected void process(List<String> chunks) {
            super.process(chunks);
            for (String i : chunks) {
                //lblQueryResultValue.setText("IP: " + i);
                txtResults.append(i + "\n");
            }
        }

        @Override
        protected void done() {
            super.done();
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
                                "\n");

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

                lblQueryResultValue.setText("IP: " + (masterCounter - 1));
            }

            btnIniziaAnalisi.setEnabled(true);
            pbWorking.setVisible(false);
            lblStatusBar.setText("Finished. Fetched " + masterPublishCounter + " IP");

        }

        @Override
        public void update(String message) {
            publish(message);
            masterPublishCounter++;
        }
    }

}
