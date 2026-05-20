package com.bugboard.frontend.ui.view.auth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.bugboard.frontend.services.AuthService;
import com.bugboard.frontend.ui.view.dashboard.DashboardFrame;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private AuthService authService;

    public LoginFrame() {

        this.authService = new AuthService();

        setTitle("BugBoard26 -Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();

    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        //Label Titolo
        JLabel titleLabel = new JLabel("Login to BugBoard26");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        //reset gridwidth
        gbc.gridwidth = 1;

        //Email Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);

        emailField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(emailField, gbc);

        //Password Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        //Login Button
        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        //azione bottone login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        panel.add(loginButton, gbc);
        add(panel);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // controllo mail
        if (!com.bugboard.frontend.utils.EmailValidator.isValid(email)) {
            JOptionPane.showMessageDialog(this, "Inserisci un indirizzo email valido (es. nome@dominio.com).", "Formato non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // tutti i controlli passati, prova a fare il login
        boolean success = authService.login(email, password);
        
        if (success) {
            this.dispose();

            SwingUtilities.invokeLater(() -> {
                new DashboardFrame().setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
