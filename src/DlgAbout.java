/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class DlgAbout extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane txtFormattedAbout;
    private JTextPane txtFormattedAboutText;

    private static final String TITLE = "About";
    private static final String ABOUT_TEXT_FILE = "About.html";
    private static final String ABOUT_TEXT_HEAD_FILE = "about_Title_Version.html";

    public DlgAbout(Frame owner, boolean modal) {
        super(owner, TITLE, modal);
        DlgAboutUIBuild();

        setLocationRelativeTo(owner);

    }

    private void DlgAboutUIBuild() {

        setContentPane(contentPane);

        try {
            //txtFormattedAbout.setEditorKit(new HTMLEditorKit());
            txtFormattedAboutText.setPage(this.getClass().getResource(ABOUT_TEXT_FILE));
            txtFormattedAbout.setPage(this.getClass().getResource(ABOUT_TEXT_HEAD_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // Indispensabile per creare la corretta visualizzazione
        // Se non c'è viene viualizzata solo la barra del titolo ed i pulsanti di chiusura
        pack();
    }

    private void onOK() {
        dispose();
    }

}
