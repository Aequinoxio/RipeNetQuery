/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

package it.aequinoxio;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;

public class DlgAbout extends JDialog {
    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=UTF-8";
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane txtFormattedAbout;
    private JTextPane txtFormattedAboutText;

    private static final String TITLE = "About";
    private static final String ABOUT_TEXT_FILE = "/about.html";
    private static final String ABOUT_TEXT_HEAD_FILE = "/about_Title_Version.html";

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public DlgAbout(Frame owner, boolean modal) {
        super(owner, TITLE, modal);
        DlgAboutUIBuild();

        setLocationRelativeTo(owner);

    }

    private void DlgAboutUIBuild() {

        setContentPane(contentPane);

//        LOGGER.log(Level.INFO, getClass().getResource(ABOUT_TEXT_HEAD_FILE).toString());
//        LOGGER.log(Level.INFO, getClass().getResource(ABOUT_TEXT_FILE).toString());

        // Imposto il corretto charset
        txtFormattedAboutText.setContentType(TEXT_HTML_CHARSET_UTF_8);
        txtFormattedAbout.setContentType(TEXT_HTML_CHARSET_UTF_8);

        try {
            txtFormattedAboutText.setPage(this.getClass().getResource(ABOUT_TEXT_FILE));
            txtFormattedAbout.setPage(this.getClass().getResource(ABOUT_TEXT_HEAD_FILE));

        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonOK.addActionListener(e -> onOK());

        // Indispensabile per creare la corretta visualizzazione
        // Se non c'è viene viualizzata solo la barra del titolo ed i pulsanti di chiusura
        pack();
    }

    private void onOK() {
        dispose();
    }

}
