package com.bugboard.frontend;

import com.bugboard.frontend.ui.view.auth.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Il punto di ingresso ufficiale dell'applicazione adesso è la schermata di login
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}