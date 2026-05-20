package com.bugboard.frontend.ui.view.dashboard;
import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.services.ApiService;
import com.bugboard.frontend.ui.view.auth.CreateUserDialog;
import com.bugboard.frontend.ui.view.issue.CreateIssueDialog;
import com.bugboard.frontend.ui.view.issue.IssueDetailDialog;
import com.bugboard.frontend.model.User;
import com.bugboard.frontend.utils.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DashboardFrame extends JFrame {

    private JTable issueTable;
    private DefaultTableModel tableModel;
    private ApiService apiService;

    public DashboardFrame() {
        this.apiService = ApiService.getInstance();

        setTitle("BugBoard26 - Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra

        initComponents();
        loadData(); // Carica i dati all'avvio
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- 1. TOOLBAR (Bottoni in alto) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Un po' di margine
        
        JButton btnNewIssue = new JButton("+ Nuova Issue");
        btnNewIssue.setBackground(new Color(60, 120, 180));
        btnNewIssue.setForeground(Color.WHITE);
        btnNewIssue.setFocusPainted(false);
        
        JButton btnRefresh = new JButton("Aggiorna Lista");

        // Bottone Crea Utente visibile solo agli Admin
        JButton btnCreateUser = new JButton("+ Crea Utente");
        btnCreateUser.setBackground(new Color(46, 204, 113)); 
        btnCreateUser.setForeground(Color.WHITE);
        btnCreateUser.setFocusPainted(false);

        //bottone per il logout
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60)); 
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);

        topPanel.add(btnNewIssue);
        topPanel.add(Box.createHorizontalStrut(10)); // Spazio tra i bottoni
        topPanel.add(btnRefresh);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(btnLogout);

        //controllo di sicurezza, per mostrare il bottone se l'utente è admin
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            topPanel.add(Box.createHorizontalStrut(10)); // Spazio
            topPanel.add(btnCreateUser);
        }
        
        add(topPanel, BorderLayout.NORTH);

        // --- 2. TABELLA (Centro) ---
        // Colonne: ID, Titolo, Descrizione, Stato, Priorità
        String[] columnNames = {"ID", "Titolo", "Descrizione", "Stato", "Priorità"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rende la tabella non modificabile direttamente
            }
        };

        issueTable = new JTable(tableModel);
        issueTable.setRowHeight(30); // Righe più alte per leggibilità
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // ScrollPane per lo scorrimento
        add(new JScrollPane(issueTable), BorderLayout.CENTER);

        // --- 3. EVENTI (Azioni) ---

        // Bottone Nuova Issue
        btnNewIssue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateDialog();
            }
        });

        // Bottone Aggiorna
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
                JOptionPane.showMessageDialog(DashboardFrame.this, "Lista aggiornata!");
            }
        });

        // Doppio Click sulla tabella per aprire i dettagli
        issueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openDetailDialog();
                }
            }
        });

        // Azione Bottone Crea Utente
        btnCreateUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateUserDialog();
            }
        });

        
        //Azione per il bottone di logout
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Svuota la sessione finta
                SessionManager.getInstance().logout();
                
                // Chiude la Dashboard
                dispose(); 
                
                // Riapre la schermata di Login
                SwingUtilities.invokeLater(() -> {
                    new com.bugboard.frontend.ui.view.auth.LoginFrame().setVisible(true);
                });
            }
        });
    }

    // Metodo per caricare i dati dal Service
    private void loadData() {
        tableModel.setRowCount(0); // Pulisce la tabella
        List<Issue> issues = apiService.getIssues();
        
        for (Issue issue : issues) {
            Object[] row = {
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(), // Aggiunta descrizione
                issue.getStatus(),
                issue.getPriority()
            };
            tableModel.addRow(row);
        }
    }

    // Apre la finestra di creazione
    private void openCreateDialog() {
        CreateIssueDialog dialog = new CreateIssueDialog(this);
        dialog.setVisible(true); // Blocca qui finché non chiudi
        
        if (dialog.isSaved()) {
            loadData(); // Ricarica se è stato salvato qualcosa
        }
    }

    // Apre la finestra di dettaglio
    private void openDetailDialog() {
        int row = issueTable.getSelectedRow();
        if (row != -1) {
            String id = (String) tableModel.getValueAt(row, 0);
            
            // Cerchiamo l'oggetto Issue completo nella lista del service
            for (Issue i : apiService.getIssues()) {
                if (i.getId().equals(id)) {
                    IssueDetailDialog detail = new IssueDetailDialog(this, i);
                    detail.setVisible(true);
                    
                    if (detail.isDataChanged()) {
                        loadData(); // Ricarica se hai modificato stato/assegnatario
                    }
                    return;
                }
            }
        }
    }

    // Apre la finestra per creare un nuovo utente visibile solo all'admin
    private void openCreateUserDialog() {
        CreateUserDialog dialog = new CreateUserDialog(this);
        dialog.setVisible(true); // Rimane bloccato qui finché il dialog non viene chiuso
        
        if (dialog.isSaved()) {
            System.out.println("[Dashboard] Un nuovo utente è stato registrato dall'amministratore.");
            // Qui in futuro potremo ricaricare una tabella utenti se necessario
        }
    }
}