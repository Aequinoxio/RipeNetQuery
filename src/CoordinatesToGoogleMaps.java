/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class CoordinatesToGoogleMaps extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;


    public CoordinatesToGoogleMaps() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    public static void main(String[] args) {
        CoordinatesToGoogleMaps dialog = new CoordinatesToGoogleMaps();
        dialog.pack();
        dialog.temp(dialog);
        dialog.setVisible(true);
        System.exit(0);
    }

    public void temp(CoordinatesToGoogleMaps test) {

       // JFrame test = new JFrame("Google Maps");

        try {
            //String imageUrl = "http://maps.google.com/staticmap?center=40,26&zoom=1&size=150x112&maptype=satellite&key=ABQIAAAAgb5KEVTm54vkPcAkU9xOvBR30EG5jFWfUzfYJTWEkWk2p04CHxTGDNV791-cU95kOnweeZ0SsURYSA&format=jpg";
            String imageUrl = "https://www.google.com/maps/@41.442726,7.9101561,6z";
            String destinationFile = "C:\\Users\\utente\\Downloads\\temp\\image.jpg";
            String str = destinationFile;
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(destinationFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        test.add(new JLabel(new ImageIcon((new ImageIcon("image.jpg")).getImage().getScaledInstance(630, 600,
                java.awt.Image.SCALE_SMOOTH))));

        test.setVisible(true);
        test.pack();

    }

}
