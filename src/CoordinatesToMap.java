/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class CoordinatesToMap extends JFrame {
    private JPanel pnlMainPanel;
    private JButton btnChiudi;
    private JPanel pnlMapPanel;
    private JProgressBar pbLoadingStatus;
    private JLabel txtLatitude;
    private JLabel txtLongitude;
    private JLabel txtStatus;
    private WebEngine webEngine;
    private JFXPanel jfxPanel = new JFXPanel();
    double latitude;
    double longitude;

    private static final Dimension PREFERRED_SIZE = new Dimension(1024, 768);

    public CoordinatesToMap(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        txtLatitude.setText(String.valueOf(latitude));
        txtLongitude.setText(String.valueOf(longitude));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setupUI();
            }
        });
    }

    // TODO: Da sistemare il fatto che al secondo avvio del frame no nviene visualizzato nulla
    // Sembra legato al fatto che c'è già un runnable in esecuzione
    private void setupUI() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView webView = new WebView();
                webEngine = webView.getEngine();
                jfxPanel.setScene(new Scene(webView));
                jfxPanel.setPreferredSize(PREFERRED_SIZE);
                pbLoadingStatus.setStringPainted(true);

                webEngine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                pbLoadingStatus.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                loadAndDisplayMap();
            }
        });

        setContentPane(pnlMainPanel);
        pnlMapPanel.add(jfxPanel, BorderLayout.CENTER);
        pnlMapPanel.setPreferredSize(PREFERRED_SIZE);

        getRootPane().setDefaultButton(btnChiudi);

        btnChiudi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
    }

    private void onOK() {
        // add your code here
        //Platform.exit();
        dispose();
    }


    private void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp;
                try {
                    tmp = new URL(url).toExternalForm();
                    txtStatus.setText(tmp + "***");
                } catch (MalformedURLException e) {
                    return;
                }

                webEngine.load(tmp);
            }
        });
    }


    private void loadAndDisplayMap() {
        String mapUrl = "https://www.openstreetmap.org/#map=17/" + String.valueOf(latitude) + "/" + String.valueOf(longitude);
        txtStatus.setText(mapUrl);
        loadURL(mapUrl);
        //loadURL("https://www.google.com/maps/@41.9100711,12.3959222,11z");
    }


    public static void main(String[] args) {
        CoordinatesToMap dialog = new CoordinatesToMap(0, 0);
        // dialog.pack();
        //dialog.temp(dialog);
        dialog.setVisible(true);
        //dialog.loadURL("https://www.google.com/maps/@41.9100711,12.3959222,11z");
        //dialog.loadURL("https://www.openstreetmap.org/#map=17/41.89574/12.49953");
        dialog.loadAndDisplayMap();
        //System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        pnlMapPanel = new JPanel();
        //pnlMapPanel.setPreferredSize(PREFERRED_SIZE);
    }


//    public void temp(CoordinatesToGoogleMaps test) {
//
//        // JFrame test = new JFrame("Google Maps");
//
//        try {
//            //String imageUrl = "http://maps.google.com/staticmap?center=40,26&zoom=1&size=150x112&maptype=satellite&key=ABQIAAAAgb5KEVTm54vkPcAkU9xOvBR30EG5jFWfUzfYJTWEkWk2p04CHxTGDNV791-cU95kOnweeZ0SsURYSA&format=jpg";
//            String imageUrl = "https://www.google.com/maps/@41.442726,7.9101561,6z";
//            String destinationFile = "C:\\Users\\utente\\Downloads\\temp\\image.jpg";
//            String str = destinationFile;
//            URL url = new URL(imageUrl);
//            InputStream is = url.openStream();
//            OutputStream os = new FileOutputStream(destinationFile);
//
//            byte[] b = new byte[2048];
//            int length;
//
//            while ((length = is.read(b)) != -1) {
//                os.write(b, 0, length);
//            }
//
//            is.close();
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        test.add(new JLabel(new ImageIcon((new ImageIcon("image.jpg")).getImage().getScaledInstance(630, 600,
//                java.awt.Image.SCALE_SMOOTH))));
//
//        test.setVisible(true);
//        test.pack();
//
//    }

}
