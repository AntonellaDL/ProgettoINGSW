package com.bugboard.frontend.ui.view.auth;

import com.bugboard.frontend.services.AuthService;
import javax.swing.*;
import java.awt.*;

public class CreateUserDialog extends JDialog {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton btnSave;
    private JButton btnCancel;
    private AuthService authService;
    private boolean saved = false;

    public CreateUserDialog(Frame parent) {
        super(parent, "Registrazione Nuovo Utente", true); 
        this.authService = new AuthService();


        initComponents();
        pack();
        setLocationRelativeTo(parent); // Centra il dialog rispetto alla Dashboard
        setResizable(false);
    }

   private void initComponents() {

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(7, 7, 7, 7);

    // Titolo
    JLabel titleLabel = new JLabel("Crea Nuova Utenza");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;

    formPanel.add(titleLabel, gbc);

    // Reset gridwidth
    gbc.gridwidth = 1;

    
    // Campo Email
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.0;

    formPanel.add(new JLabel("Email:"), gbc);

    emailField = new JTextField(25);
    emailField.setHorizontalAlignment(JTextField.LEFT);

    // Tooltip dinamico per il campo email
    emailField.getDocument().addDocumentListener(
        new javax.swing.event.DocumentListener() {

            private void updateTooltip() {
                emailField.setToolTipText(emailField.getText());
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTooltip();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTooltip();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTooltip();
            }
        }
    );

    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.weightx = 1.0;

    formPanel.add(emailField, gbc);

    // Campo Password
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.0;

    formPanel.add(new JLabel("Password:"), gbc);

    passwordField = new JPasswordField(25);

    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.weightx = 1.0;

    formPanel.add(passwordField, gbc);

   
    // Campo Ruolo
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 0.0;

    formPanel.add(new JLabel("Ruolo:"), gbc);

    String[] roles = {"NORMALE", "ADMIN"};
    roleComboBox = new JComboBox<>(roles);

    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;

    formPanel.add(roleComboBox, gbc);

    
    // Bottoni
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    btnSave = new JButton("Salva");
    btnSave.setBackground(new Color(46, 204, 113));
    btnSave.setForeground(Color.WHITE);
    btnSave.setFocusPainted(false);

    btnCancel = new JButton("Annulla");

    buttonPanel.add(btnSave);
    buttonPanel.add(btnCancel);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    formPanel.add(buttonPanel, gbc);

    // Azioni bottoni
    btnSave.addActionListener(e -> performRegistration());

    btnCancel.addActionListener(e -> dispose());


    // Wrapper panel per centrare il form
    JPanel wrapperPanel = new JPanel(new GridBagLayout());

    wrapperPanel.add(formPanel);

    add(wrapperPanel);
}

    private void performRegistration() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        // Validazione base degli input 
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tutti i campi sono obbligatori.", "Errore di Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //controllo mail tramite EmailValidator
         if (!com.bugboard.frontend.utils.EmailValidator.isValid(email)) {
            JOptionPane.showMessageDialog(this, "Inserisci un indirizzo email valido (es. nome@dominio.com).", "Formato non valido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        //  Controllo Password  tramite PasswordValidator
        if (!com.bugboard.frontend.utils.PasswordValidator.isValid(password)) {
            JOptionPane.showMessageDialog(this, "La password deve contenere almeno 6 caratteri.", "Password troppo debole", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Chiamata al servizio per registrare l'utente nel sistema
        boolean success = authService.register(email, password, role);

        if (success) {
            JOptionPane.showMessageDialog(this, "Utente creato con successo!", "Operazione Completata", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose(); // Chiude il dialog
        } else {
            JOptionPane.showMessageDialog(this, "Impossibile creare l'utente. L'email potrebbe essere già registrata o non hai i permessi.", "Errore di Registrazione", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}