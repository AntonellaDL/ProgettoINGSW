package com.bugboard.frontend.ui.view.issue;

import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.services.ApiService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IssueDetailDialog extends JDialog {

    private Issue issue; // Il bug che stiamo visualizzando
    private ApiService apiService;
    
    // Componenti modificabili
    private JComboBox<String> statusCombo;
    private JComboBox<String> assigneeCombo;
    
    // Flag per dire alla dashboard se qualcosa è cambiato
    private boolean dataChanged = false;

    public IssueDetailDialog(Frame owner, Issue issue) {
        super(owner, "Dettaglio Segnalazione: " + issue.getTitle(), true);
        this.issue = issue;
        this.apiService = ApiService.getInstance();

        setSize(600, 500);
        setLocationRelativeTo(owner);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- PANNELLO NORD: Intestazione ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel metaLabel = new JLabel("ID: " + issue.getId() + " | Tipo: " + issue.getType() + " | Priorità: " + issue.getPriority());
        metaLabel.setForeground(Color.GRAY);
        
        headerPanel.add(titleLabel);
        headerPanel.add(metaLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- PANNELLO CENTRALE: Info e Modifiche ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // 1. Descrizione
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Descrizione:"), gbc);
        
        JTextArea descArea = new JTextArea(issue.getDescription());
        descArea.setEditable(false); // Sola lettura
        descArea.setBackground(new Color(250, 250, 250));
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(400, 80));
        
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(descScroll, gbc);

        // 2. Modifica Stato (Funzionalità 6)
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Stato Corrente:"), gbc);
        
        String[] stati = {"todo", "in_progress", "done"};
        statusCombo = new JComboBox<>(stati);
        statusCombo.setSelectedItem(issue.getStatus());
        
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(statusCombo, gbc);

        // 3. Assegna A (Funzionalità 4)
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("Assegnato a:"), gbc);
        
        // Simuliamo una lista utenti
        String[] utenti = {"Non assegnato", "Mario Rossi", "Luigi Verdi", "Anna Bianchi"};
        assigneeCombo = new JComboBox<>(utenti);
        assigneeCombo.setSelectedItem(issue.getAssignee());
        
        gbc.gridx = 1; gbc.gridy = 2;
        centerPanel.add(assigneeCombo, gbc);

        // 4. Visualizza Immagine (Se presente)
        if (issue.getImageData() != null) {
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            centerPanel.add(new JSeparator(), gbc);
            
            gbc.gridy = 4;
            JLabel imgTitle = new JLabel("Allegato: " + issue.getImageName());
            centerPanel.add(imgTitle, gbc);
            
            gbc.gridy = 5;
            // Creiamo l'icona dall'array di byte
            ImageIcon icon = new ImageIcon(issue.getImageData());
            // Scaliamo l'immagine se è troppo grande (max 200px)
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(img));
            centerPanel.add(imgLabel, gbc);
        }

        add(new JScrollPane(centerPanel), BorderLayout.CENTER);

        // --- PANNELLO SUD: Bottoni ---
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Salva Modifiche");
        JButton btnClose = new JButton("Chiudi");

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        btnPanel.add(btnSave);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void saveChanges() {
        // Recuperiamo i nuovi valori
        String newStatus = (String) statusCombo.getSelectedItem();
        String assigneeSelection = (String) assigneeCombo.getSelectedItem();
        
        String newAssignee = assigneeSelection.equals("Non assegnato") ? null : assigneeSelection;

        // Aggiorniamo l'oggetto locale
        issue.setStatus(newStatus);
        issue.setAssignee(newAssignee);
        
        JOptionPane.showMessageDialog(this, "Modifiche salvate!");
        dataChanged = true;
        dispose();
    }
    
    public boolean isDataChanged() {
        return dataChanged;
    }
}