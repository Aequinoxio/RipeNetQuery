import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    String IPFilename;
    File IPFile;
    ArrayList<String> IPToBeChecked;

    public RipeQueryUI() {
        btnScegliFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setMultiSelectionEnabled(false);
                int retVal = jFileChooser.showOpenDialog(mainPanel);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    IPFile = jFileChooser.getSelectedFile();

                    if (!IPFile.exists()) {
                        JOptionPane.showMessageDialog(mainPanel, IPFile.getAbsolutePath() + "\nFile non trovato", "Errore", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Reinizializzo l'array
                        IPToBeChecked = new ArrayList<>();

                        IPFilename = IPFile.getAbsolutePath();
                        lblFilename.setText(IPFilename);

                        parseFile();
                        lblStatus.setText("Numero IP validi: " + IPToBeChecked.size());

                        // Se ne trovo almeno uno attivo il bottone della ricerca
                        if (IPToBeChecked.size() > 0) {
                            btnIniziaAnalisi.setEnabled(true);
                        } else {
                            btnIniziaAnalisi.setEnabled(false);
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
//                        System.out.print(ip+" - Error: ");
//                        System.out.println(retval + " " + retval.getCode() + " " + retval.getCodeAsText() + " " + retval.getDesc());
                    }
                }

            }
        });
    }

    private void parseFile() {
        try (BufferedReader bfr = new BufferedReader(new FileReader(IPFile))) {
            String linea;
            while ((linea = bfr.readLine()) != null) {
                // Check se è un IP valido
                System.out.print(linea + " : ");
                if (linea.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
                    IPToBeChecked.add(linea);
                    System.out.println("ok");
                } else {
                    System.out.println("IP non valido");
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
