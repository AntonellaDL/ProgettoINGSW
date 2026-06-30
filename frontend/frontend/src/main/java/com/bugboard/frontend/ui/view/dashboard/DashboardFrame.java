package com.bugboard.frontend.ui.view.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.model.User;
import com.bugboard.frontend.services.ApiService;
import com.bugboard.frontend.ui.view.auth.CreateUserDialog;
import com.bugboard.frontend.ui.view.issue.CreateIssueDialog;
import com.bugboard.frontend.ui.view.issue.IssueDetailDialog;
import com.bugboard.frontend.utils.SessionManager;

public class DashboardFrame extends JFrame {

    private JTable issueTable;
    private DefaultTableModel tableModel;
    private ApiService apiService;
    
    // Variabili per il filtraggio e ordinamento
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> priorityFilterCombo;
    private JTextField searchFilterField;

    public DashboardFrame() {
        this.apiService = ApiService.getInstance();

        setTitle("BugBoard26 - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600)); 

        initComponents();
        loadData(); 

    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // contenitore principale in alto
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        // Bottoni-
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10)); 
        
        JButton btnNewIssue = new JButton("+ Nuova Issue");
        btnNewIssue.setBackground(new Color(60, 120, 180));
        btnNewIssue.setForeground(Color.WHITE);
        btnNewIssue.setFocusPainted(false);
        
        JButton btnRefresh = new JButton("Aggiorna Lista");

        JButton btnCreateUser = new JButton("+ Crea Utente");
        btnCreateUser.setBackground(new Color(46, 204, 113)); 
        btnCreateUser.setForeground(Color.WHITE);
        btnCreateUser.setFocusPainted(false);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60)); 
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);

        topPanel.add(btnNewIssue);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(btnRefresh);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(btnLogout);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            topPanel.add(Box.createHorizontalStrut(10));
            topPanel.add(btnCreateUser);
        }

        // Filtri
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        filterPanel.add(new JLabel("Filtra per:"));

        //  Tendina Tipologia
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(new JLabel("Tipo ="));
        typeFilterCombo = new JComboBox<>(new String[]{"Tutte", "BUG", "FEATURE", "DOCUMENTATION", "QUESTION"});
        filterPanel.add(typeFilterCombo);

        // Tendina Stato
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Stato ="));
        statusFilterCombo = new JComboBox<>(new String[]{"Tutti", "todo", "in_progress", "done"});
        filterPanel.add(statusFilterCombo);

        // Tendina Priorità
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Priorità ="));
        priorityFilterCombo = new JComboBox<>(new String[]{"Tutte", "ALTA", "MEDIA", "BASSA"});
        filterPanel.add(priorityFilterCombo);

        // Ricerca Testuale nel Titolo
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Cerca nel Titolo:"));
        searchFilterField = new JTextField(12);
        filterPanel.add(searchFilterField);

        headerPanel.add(topPanel);
        headerPanel.add(filterPanel);
        add(headerPanel, BorderLayout.NORTH);

        // TABELLA
       String[] columnNames = {"ID", "Titolo", "Tipologia", "Assegnatario", "Stato", "Priorità"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        issueTable = new JTable(tableModel);
        issueTable.setRowHeight(30); 
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //  configurazione Sorter per ordinamento automatico
        sorter = new TableRowSorter<>(tableModel);
        issueTable.setRowSorter(sorter);
        
        add(new JScrollPane(issueTable), BorderLayout.CENTER);

        // ---  EVENTI ---

        btnNewIssue.addActionListener(e -> openCreateDialog());

        btnRefresh.addActionListener(e -> {
            loadData();
            JOptionPane.showMessageDialog(DashboardFrame.this, "Lista aggiornata!");
        });

        issueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openDetailDialog();
                }
            }
        });

        btnCreateUser.addActionListener(e -> openCreateUserDialog());
        
        btnLogout.addActionListener(e -> {
            SessionManager.getInstance().logout();
            dispose(); 
            SwingUtilities.invokeLater(() -> {
                new com.bugboard.frontend.ui.view.auth.LoginFrame().setVisible(true);
            });
        });

        // Eventi dei filtri
        typeFilterCombo.addActionListener(e -> applyFilters());
        statusFilterCombo.addActionListener(e -> applyFilters());
        priorityFilterCombo.addActionListener(e -> applyFilters());
        
        searchFilterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
    }

    private void applyFilters() {
        String type = (String) typeFilterCombo.getSelectedItem();
        String status = (String) statusFilterCombo.getSelectedItem();
        String priority = (String) priorityFilterCombo.getSelectedItem();
        String searchText = searchFilterField.getText().trim();

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        //  Filtro Titolo (Colonna indice 1)
        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1)); 
        }

        //  Filtro Tipologia (Colonna indice 2)
        if (!"Tutte".equals(type)) {
            filters.add(RowFilter.regexFilter("(?i)^" + type + "$", 2));
        }
        //  Filtro Stato (Colonna indice 4)
        if (!"Tutti".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 4));
        }

        //  Filtro Priorità (Colonna indice 5)
        if (!"Tutte".equals(priority)) {
            filters.add(RowFilter.regexFilter("(?i)^" + priority + "$", 5));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void loadData() {
        tableModel.setRowCount(0); 
        List<Issue> issues = apiService.getIssues();
        
        for (Issue issue : issues) {
            Object[] row = {
                issue.getId(),
                issue.getTitle(),
                issue.getType(),        
                issue.getAssignee() != null ? issue.getAssignee() : "Non assegnato",
                issue.getStatus(),      
                issue.getPriority()     
            };
            tableModel.addRow(row);
        }
    }

    private void openCreateDialog() {
        CreateIssueDialog dialog = new CreateIssueDialog(this);
        dialog.setVisible(true); 
        if (dialog.isSaved()) {
            loadData(); 
        }
    }

    private void openDetailDialog() {
        int row = issueTable.getSelectedRow();
        if (row != -1) {
            String id = (String) issueTable.getValueAt(row, 0);
            
            for (Issue i : apiService.getIssues()) {
                if (i.getId().equals(id)) {
                    IssueDetailDialog detail = new IssueDetailDialog(this, i);
                    detail.setVisible(true);
                    
                    if (detail.isDateChanged()) {
                        loadData(); 
                    }
                    return;
                }
            }
        }
    }

    private void openCreateUserDialog() {
        CreateUserDialog dialog = new CreateUserDialog(this);
        dialog.setVisible(true); 
        if (dialog.isSaved()) {
            System.out.println("[Dashboard] Un nuovo utente è stato registrato dall'amministratore.");
        }
    }
}