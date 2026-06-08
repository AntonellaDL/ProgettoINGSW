package com.bugboard.frontend;

import com.bugboard.frontend.ui.view.auth.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
       //apre il login
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}