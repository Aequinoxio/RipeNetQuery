import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RipeQueryUI {
    private JButton btnScegliFile;
    private JTextArea txtResults;
    private JLabel lblFilename;
    private JPanel mainPanel;
    private JButton btnIniziaAnalisi;
    private JLabel lblStatus;
    private JTextArea txtIpList;
    private JButton cancellaTuttoButton;

    private JFileChooser jFileChooser = new JFileChooser();

    String IPFilename;
    File IPFile;
    ArrayList<String> IPToBeChecked = new ArrayList<>();

    private static String IPRegexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public RipeQueryUI() {
        // Margine personalizzato nelle label
        Border margin = new EmptyBorder(4, 4, 4, 4);
        lblFilename.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                margin
        ));

        //lblStatus.setBorder(margin);

        btnScegliFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setMultiSelectionEnabled(false);
                int retVal = jFileChooser.showOpenDialog(mainPanel);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    IPFile = jFileChooser.getSelectedFile();

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

                        parseFile(); // Aggiorno l'array IPToBeChecked
                        lblStatus.setText("Numero IP validi: " + IPToBeChecked.size());

                        // Se ne trovo almeno uno attivo il bottone della ricerca
                        if (IPToBeChecked.size() > 0) {
                            btnIniziaAnalisi.setEnabled(true);
                        } else {
                            btnIniziaAnalisi.setEnabled(false);
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
                for (String ip : IPToBeChecked) {
                    HttpStatusCodes retval = ripeQuery.queryIPAddressForCountry(ip);
                    if (retval == HttpStatusCodes.OK) {
                        //System.out.println(ripeQueryCmdLine.m_jsonData);
                        txtResults.append(ip + " - " + ripeQuery.country() + "\n");
                        //System.out.println(ip+" - "+ripeQuery.country());
                    } else {
                        txtResults.append(
                                ip + " - Error: \t" +
                                        retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc()
                        );
                        txtResults.append("\n");
//                        System.out.print(ip+" - Error: ");
//                        System.out.println(retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());
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
                //JOptionPane.showMessageDialog(mainPanel,"TODO", "Info", JOptionPane.INFORMATION_MESSAGE);
                Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable clipTf = sysClip.getContents(null);
                if (clipTf != null) {
                    if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            String values = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
                            // Parsing del conteno per verificare se ci sono IP
                            String[] lines = values.split("\\r?\\n");
                            for (String linea:lines){
                                if (linea.matches(IPRegexp)) {
                                    IPToBeChecked.add(linea);
                                    txtIpList.append(linea);
                                    txtIpList.append("\n");
                                    //  System.out.println("ok");
                                } else {
                                    //  System.out.println("IP non valido");
                                }
                            }
                            lblStatus.setText("Numero IP validi: " + IPToBeChecked.size());
                            if (IPToBeChecked.size()>0){
                                btnIniziaAnalisi.setEnabled(true);
                            } else {
                                btnIniziaAnalisi.setEnabled(false);
                            }
                            //txtIpList.append(values);
                        } catch (UnsupportedFlavorException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                }
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

        cancellaTuttoButton.addActionListener(new ActionListener() {
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
            }
        });
    }

    private void parseFile() {
        try (BufferedReader bfr = new BufferedReader(new FileReader(IPFile))) {
            String linea;
            while ((linea = bfr.readLine()) != null) {
                // Check se è un IP valido
                //System.out.print(linea + " : ");
                if (linea.matches(IPRegexp)) {
                    IPToBeChecked.add(linea);
                    //  System.out.println("ok");
                } else {
                    //  System.out.println("IP non valido");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("RipeQueryUI");
        frame.setContentPane(new RipeQueryUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
