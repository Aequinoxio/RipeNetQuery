/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class CoordinatesToMap extends JFrame {
    private static final String HTTPS_WWW_OPENSTREETMAP_ORG_MAP = "https://www.openstreetmap.org/#map=17/";
    private JPanel pnlMainPanel;
    private JButton btnChiudi;
    private JPanel pnlMapPanel;
    private JProgressBar pbLoadingStatus;
    private JLabel txtLatitude;
    private JLabel txtLongitude;
    private JLabel txtStatus;
    private WebEngine webEngine;
    private final JFXPanel jfxPanel = new JFXPanel();
    final double latitude;
    final double longitude;

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

    // TODO: Da sistemare il fatto che al secondo avvio del frame non viene visualizzato nulla
    // Sembra un comportamento legato al fatto che c'è già un runnable in esecuzione e non ne viene
    // lanciato un altro
    // Sembra che lasciando aperta una finestra il problema non si presenti, chiudendole tutte
    // quando si riapre il frame no nviene visualizzata la webview
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
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void onOK() {
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
                    //txtStatus.setText(tmp + "***");
                } catch (MalformedURLException e) {
                    return;
                }

                webEngine.load(tmp);
            }
        });
    }


    private void loadAndDisplayMap() {
        String mapUrl = HTTPS_WWW_OPENSTREETMAP_ORG_MAP + String.valueOf(latitude) + "/" + String.valueOf(longitude);
        txtStatus.setText(mapUrl);
        loadURL(mapUrl);
        //loadURL("https://www.google.com/maps/@41.9100711,12.3959222,11z");
    }


//    public static void main(String[] args) {
//        CoordinatesToMap dialog = new CoordinatesToMap(0, 0);
//        // dialog.pack();
//        //dialog.temp(dialog);
//        dialog.setVisible(true);
//        //dialog.loadURL("https://www.google.com/maps/@41.9100711,12.3959222,11z");
//        //dialog.loadURL("https://www.openstreetmap.org/#map=17/41.89574/12.49953");
//        dialog.loadAndDisplayMap();
//        //System.exit(0);
//    }

    private void createUIComponents() {
        pnlMapPanel = new JPanel();
        //pnlMapPanel.setPreferredSize(PREFERRED_SIZE);
    }
}
