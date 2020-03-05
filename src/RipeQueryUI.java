import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class RipeQueryUI {
    private JButton btnScegliFile;
    private JTextArea txtResults;
    private JLabel lblFilename;
    private JPanel mainPanel;
    private JButton btnIniziaAnalisi;
    private JLabel lblStatus;
    private JTextArea txtIpList;
    private JButton btnCancellaTutto;
    private JTable tblResults;
    private JButton btnCopyToclipboard;
    private JButton incollaIpButton;
    private JButton btnSalvaRisultati;
    private DefaultTableModel tblResultModel;

    // Provo a riaprire il file chooser dall'ultima posizione salvata
    static private String LAST_USED_FOLDER = "RipeQueryUI.LAST_USED_FOLDER";
    Preferences prefs = Preferences.userRoot().node(getClass().getName());
    private JFileChooser jFileChooser = new JFileChooser(
            prefs.get(LAST_USED_FOLDER,
            new File(".").getAbsolutePath())
    );

    String IPFilename;
    File IPFile;
    ArrayList<String> IPToBeChecked = new ArrayList<>();

    private static String IPRegexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public RipeQueryUI() {
        // Margine personalizzato nelle label
        Border margin = new EmptyBorder(4, 4, 4, 4);
        lblFilename.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                margin));

        //lblStatus.setBorder(margin);

        btnScegliFile.addActionListener(new ActionListener() {
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
                        // Reinizializzo l'array
                        //IPToBeChecked = new ArrayList<>();
                        //IPToBeChecked.clear();

                        ///// *** NB ***
                        ///// Non reinizializzo l'array, aggiungo quello che trovo


                        IPFilename = IPFile.getAbsolutePath();
                        lblFilename.setText(IPFilename);

                        int startingIPNumbers= IPToBeChecked.size();
                        int skippedIP=parseFile(); // Aggiorno l'array IPToBeChecked
                        lblStatus.setText("Numero IP validi: " + IPToBeChecked.size()+
                                " - IP duplicati: "+ skippedIP);

//                        +
//                                        " - IP nuovi: " + (IPToBeChecked.size()-startingIPNumbers)
//                                );

                        // Se ne trovo almeno uno attivo il bottone della ricerca
                        if (IPToBeChecked.size() > 0) {
                            btnIniziaAnalisi.setEnabled(true);
                            btnCancellaTutto.setEnabled(true);

                            // Se non ho aggiunto ip ne do info
                            if (IPToBeChecked.size()==startingIPNumbers){
                                JOptionPane.showMessageDialog(mainPanel,"Il file non contiene IP validi o sono già presenti nella lista","Informazione",JOptionPane.INFORMATION_MESSAGE);
                            }

                        } else {
                            btnIniziaAnalisi.setEnabled(false);
                            btnCancellaTutto.setEnabled(false);
                            JOptionPane.showMessageDialog(mainPanel,"Il file non contiene IP validi","Informazione",JOptionPane.INFORMATION_MESSAGE);

                        }

                        txtResults.setText("");
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
                RipeQuery ripeQuery = new RipeQuery();
                txtResults.setText("");
                tblResultModel.setRowCount(0);
                for (String ip : IPToBeChecked) {
                    HttpStatusCodes retval = null;
                    try {
                        //retval = ripeQuery.queryIPAddressForCountry(ip);
                        retval = ripeQuery.downloadAndParseLocationData(ip);
                        if (retval == HttpStatusCodes.OK) {
                            txtResults.append(ip +
                                    " - " + ripeQuery.getCountry() +
                                    " - " + ripeQuery.getCity() +
                                    " - " + ripeQuery.getLatitude() +
                                    " - " + ripeQuery.getLongitude() +
                                    " - " + ripeQuery.getCoveredPercentage() +
                                    "\n");
                            tblResultModel.addRow(new Object[]{
                                    ip,
                                    ripeQuery.getCountry(),
                                    ripeQuery.getCity(),
                                    ripeQuery.getLatitude(),
                                    ripeQuery.getLongitude(),
                                    ripeQuery.getCoveredPercentage()
                            });
                        } else {
                            txtResults.append(
                                    ip + " - Error: \t" +
                                            retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc()
                            );
                            txtResults.append("\n");
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(mainPanel, ex.toString(), "Errore", JOptionPane.ERROR_MESSAGE);
                        break;
                    }

                }
            }
        });

        // Creo il popup menu per l'incolla IP
        JPopupMenu popupMenu = new JPopupMenu("Incolla IP da controllare");
        JMenuItem menuItem = new JMenuItem("Incolla IP");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                incollaDaClipBoard();
//                Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
//                Transferable clipTf = sysClip.getContents(null);
//                if (clipTf != null) {
//                    if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//                        try {
//                            String values = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
//                            // Parsing del contenuto per verificare se ci sono IP
//                            // Dividol'analisi per righe
//                            String[] lines = values.split("\\r?\\n");
//                            for (String linea : lines) {
//                                if (linea.matches(IPRegexp)) {
//                                    IPToBeChecked.add(linea);
//                                    txtIpList.append(linea);
//                                    txtIpList.append("\n");
//                                    //  System.out.println("ok");
//                                } else {
//                                    //  System.out.println("IP non valido");
//                                }
//                            }
//                            lblStatus.setText("Numero IP validi: " + IPToBeChecked.size());
//                            if (IPToBeChecked.size() > 0) {
//                                btnIniziaAnalisi.setEnabled(true);
//                            } else {
//                                JOptionPane.showMessageDialog(mainPanel, "La Clipboard non contiene alcuin ip valido", "Errore", JOptionPane.ERROR_MESSAGE);
//                                btnIniziaAnalisi.setEnabled(false);
//                            }
//
//                        } catch (UnsupportedFlavorException | IOException ex) {
//                            JOptionPane.showMessageDialog(mainPanel, "La Clipboard non contiene alcuin ip valido", "Errore", JOptionPane.ERROR_MESSAGE);
//                            ex.printStackTrace();
//                        }
//
//                    }
//                }
            }
        });

        popupMenu.add(menuItem);

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
                    popupMenu.show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });

        // TODO: txtResults.addMouseListener(txtIpList.getMouseListeners()[0]);

//        // Intercetto il control+v
//        txtIpList.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
//                    System.out.println("woot!");
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//
//            }
//        });

        btnCancellaTutto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(mainPanel, "Cancello tutti gli IP da controllare e controllati?", "Conferma", JOptionPane.OK_CANCEL_OPTION);
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
            }
        });

        btnCopyToclipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

//                StringBuilder stringBuilder = new StringBuilder();
//                String head = "IP\tCountry\tCity\tLatitude\tLongitude\tCovered percentage";
//                stringBuilder.append(head).append(System.lineSeparator());
//                for (int i = 0; i < tblResultModel.getRowCount(); i++) {
//                    stringBuilder.append(tblResultModel.getValueAt(i, 0)).append("\t");
//                    stringBuilder.append(tblResultModel.getValueAt(i, 1)).append("\t");
//                    stringBuilder.append(tblResultModel.getValueAt(i, 2)).append("\t");
//                    stringBuilder.append(tblResultModel.getValueAt(i, 3)).append("\t");
//                    stringBuilder.append(tblResultModel.getValueAt(i, 4)).append("\t");
//                    stringBuilder.append(tblResultModel.getValueAt(i, 5)).append(System.lineSeparator());
//                }
//                StringSelection stringSelection = new StringSelection(stringBuilder.toString());
                StringSelection stringSelection = new StringSelection(tableToString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(mainPanel, "Dati copiati", "Informazione", JOptionPane.INFORMATION_MESSAGE);
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
                    if (saveFile(saveFileChoosen)){
                        JOptionPane.showMessageDialog(mainPanel,"File:\n"+saveFileChoosen.getAbsolutePath()+"\n salvato con successo ", "Informazione",JOptionPane.INFORMATION_MESSAGE);
                    } else{
                        JOptionPane.showMessageDialog(mainPanel,"Errore nel salvare il file:\n"+saveFileChoosen.getAbsolutePath(), "Informazione",JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }


    private String tableToString(){
        StringBuilder stringBuilder = new StringBuilder();
        String head = "IP\tCountry\tCity\tLatitude\tLongitude\tCovered percentage";
        stringBuilder.append(head).append(System.lineSeparator());
        for (int i = 0; i < tblResultModel.getRowCount(); i++) {
            stringBuilder.append(tblResultModel.getValueAt(i, 0)).append("\t");
            stringBuilder.append(tblResultModel.getValueAt(i, 1)).append("\t");
            stringBuilder.append(tblResultModel.getValueAt(i, 2)).append("\t");
            stringBuilder.append(tblResultModel.getValueAt(i, 3)).append("\t");
            stringBuilder.append(tblResultModel.getValueAt(i, 4)).append("\t");
            stringBuilder.append(tblResultModel.getValueAt(i, 5)).append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }


    private boolean saveFile(File fileToBeSaved){
        boolean result = false;
        try(BufferedWriter bfw = new BufferedWriter(new FileWriter(fileToBeSaved))){
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
     * @return il numero di IP validi saltati in quanto già presenti nella lista
     */
    private int parseFile() {
        int skippedValidIP=0;
        try (BufferedReader bfr = new BufferedReader(new FileReader(IPFile))) {
            String linea;
            while ((linea = bfr.readLine()) != null) {
                // Check se è un IP valido
                if (linea.matches(IPRegexp)) {
                    if (!IPToBeChecked.contains(linea)) {
                        IPToBeChecked.add(linea);
                    } else{
                        skippedValidIP++;
                    }
                } else {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skippedValidIP;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("RipeQueryUI");
        frame.setContentPane(new RipeQueryUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        tblResultModel = new DefaultTableModel(new Object[]{"IP", "Country", "City", "Latitude", "Longitude", "Covered percentage"}, 0);
        tblResults = new JTable(tblResultModel);
        tblResults.setDefaultEditor(Object.class,null);
        //tblResults.setEnabled(false);

        //tblResults.doLayout();
    }

    private void incollaDaClipBoard() {
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipTf = sysClip.getContents(null);
        int startingIPNumbers=IPToBeChecked.size();

        if (clipTf != null) {
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String values = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
                    // Parsing del contenuto per verificare se ci sono IP
                    // Dividol'analisi per righe
                    String[] lines = values.split("\\r?\\n");
                    int skippedIP=0;
                    for (String linea : lines) {
                        if (linea.matches(IPRegexp)) {
                            if (!IPToBeChecked.contains(linea)) {
                                IPToBeChecked.add(linea);
                                txtIpList.append(linea);
                                txtIpList.append("\n");
                            } else {
                                skippedIP++;
                            }
                            //  System.out.println("ok");
                        } else {
                            //  System.out.println("IP non valido");
                        }
                    }

                    lblStatus.setText("Numero IP validi: " + IPToBeChecked.size() +
                            " - IP duplicati: "+ skippedIP
                    );

//                    +
//                            " - IP nuovi: " + (IPToBeChecked.size()-startingIPNumbers)
//                    );

                    if (IPToBeChecked.size() > 0) {
                        if (IPToBeChecked.size()==startingIPNumbers){
                            JOptionPane.showMessageDialog(mainPanel,"Gli appunti non contengono IP validi o sono già presenti nella lista","Informazione",JOptionPane.INFORMATION_MESSAGE);
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
}
