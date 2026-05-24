package com.bugboard.frontend.ui.view.issue;

import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.services.ApiService;
import com.bugboard.frontend.utils.SessionManager;
import com.bugboard.frontend.model.Comment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IssueDetailDialog extends JDialog {

    private Issue issue;
    private ApiService apiService;
    
    // Componenti modificabili
    private JComboBox<String> statusCombo;
    private JComboBox<String> assigneeCombo;

    private JPanel commentsHistoryPanel;
    private JTextArea newCommentArea;
    private JScrollPane commentsScroll;
    
    // Flag per dire alla dashboard se qualcosa è cambiato
    private boolean dataChanged = false;

    public IssueDetailDialog(Frame owner, Issue issue) {
        super(owner, "Dettaglio Segnalazione: " + issue.getTitle(), true);
        this.issue = issue;
        this.apiService = ApiService.getInstance();

        setSize(900, 700);

        setMinimumSize(new Dimension(800, 600));
        setResizable(true);

        setLocationRelativeTo(owner);
        initComponents();

        //caricamento dei commenti
        refreshComments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Margine globale
        
  
        // ZONA NORD: Intestazione e Metadati
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // Pannello orizzontale per Info, Stato e Assegnatario
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        
        JLabel lblInfo = new JLabel("ID: " + issue.getId() + " | Tipo: " + issue.getType() + " | Priorità: " + issue.getPriority());
        lblInfo.setForeground(Color.DARK_GRAY);
        metaPanel.add(lblInfo);
        
        metaPanel.add(Box.createHorizontalStrut(30)); // Spaziatore
        
        // Tendina Stato
        metaPanel.add(new JLabel("Stato: "));
        String[] stati = {"todo", "in_progress", "done"};
        statusCombo = new JComboBox<>(stati);
        statusCombo.setSelectedItem(issue.getStatus());
        metaPanel.add(statusCombo);
        
        metaPanel.add(Box.createHorizontalStrut(15));
        
        // Tendina Assegnatario
        metaPanel.add(new JLabel("Assegnato a: "));
        String[] utenti = {"Non assegnato", "Mario Rossi", "Luigi Verdi", "Anna Bianchi"};
        assigneeCombo = new JComboBox<>(utenti);
        if (issue.getAssignee() != null) {
            assigneeCombo.setSelectedItem(issue.getAssignee());
        } else {
            assigneeCombo.setSelectedItem("Non assegnato");
        }
        metaPanel.add(assigneeCombo);

        headerPanel.add(metaPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        //  Zona centrale : SplitPane (Descrizione / Commenti)
        
        // Area superiore: Descrizione (ed eventuale immagine)
        JPanel descContainer = new JPanel();
        descContainer.setLayout(new BoxLayout(descContainer, BoxLayout.Y_AXIS));
        descContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        descContainer.setBackground(new Color(253, 253, 253));

        JTextArea descArea = new JTextArea(issue.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(253, 253, 253));
        descContainer.add(descArea);

        // Gestione Immagine preesistente
        if (issue.getImageData() != null) {
            descContainer.add(Box.createVerticalStrut(15));
            descContainer.add(new JSeparator(SwingConstants.HORIZONTAL));
            descContainer.add(Box.createVerticalStrut(5));
            descContainer.add(new JLabel("Allegato: " + issue.getImageName()));
            
            ImageIcon icon = new ImageIcon(issue.getImageData());
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            descContainer.add(new JLabel(new ImageIcon(img)));
        }

        JScrollPane descScroll = new JScrollPane(descContainer);
        descScroll.setBorder(BorderFactory.createTitledBorder("Descrizione"));

        //  Area inferiore: Storico Commenti
        commentsHistoryPanel = new JPanel();
        commentsHistoryPanel.setLayout(new BoxLayout(commentsHistoryPanel, BoxLayout.Y_AXIS));
        commentsHistoryPanel.setBackground(Color.WHITE);
        
        commentsScroll = new JScrollPane(commentsHistoryPanel); 
        commentsScroll.setBorder(BorderFactory.createTitledBorder("Discussione"));
        commentsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //  Creazione del divisore trascinabile tra descrizione e commenti
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descScroll, commentsScroll);
        splitPane.setResizeWeight(0.4); // Il 40% dello spazio iniziale va alla descrizione
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);


        //  Zona sud: Nuovo Commento e Bottoni
        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Area di testo per scrivere un nuovo commento
        newCommentArea = new JTextArea(3, 40);
        newCommentArea.setLineWrap(true);
        newCommentArea.setWrapStyleWord(true);
        JScrollPane newCommentScroll = new JScrollPane(newCommentArea);
        newCommentScroll.setBorder(BorderFactory.createTitledBorder("Scrivi un commento..."));
        footerPanel.add(newCommentScroll, BorderLayout.CENTER);

        // Pannello Bottoni
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAddComment = new JButton("Invia Commento");
        btnAddComment.setBackground(new Color(60, 120, 180));
        btnAddComment.setForeground(Color.WHITE);
        btnAddComment.setFocusPainted(false);
        
        JButton btnSave = new JButton("Salva Modifiche");
        JButton btnClose = new JButton("Chiudi");

        // Evento Invia Commento
        btnAddComment.addActionListener(e -> {
            String testo = newCommentArea.getText().trim();
            if (!testo.isEmpty()) {
                //  chi scrive il commento, con l'email di chi è loggato
                String autore = "Sconosciuto";
                if (SessionManager.getInstance().getCurrentUser() != null) {
                    autore = SessionManager.getInstance().getCurrentUser().getEmail(); 
                }

                // creazione del nuovo commento
                Comment nuovoCommento = new Comment();
                nuovoCommento.setAuthor(autore);
                nuovoCommento.setText(testo);
                nuovoCommento.setCreationDate(java.time.LocalDateTime.now());
                // id e issueID saranno gestiti dal backend

                // aggiunta del commento alla issue
                issue.addComment(nuovoCommento);

                //pulizia
                newCommentArea.setText("");
                refreshComments();
                dataChanged = true; 
            }
        });

        // Evento Salva Modifiche
        btnSave.addActionListener(e -> saveChanges());

        // Evento Chiudi
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnAddComment);
        btnPanel.add(btnSave);
        btnPanel.add(btnClose);
        
        footerPanel.add(btnPanel, BorderLayout.SOUTH);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void saveChanges() {
        String newStatus = (String) statusCombo.getSelectedItem();
        String assigneeSelection = (String) assigneeCombo.getSelectedItem();
        String newAssignee = assigneeSelection.equals("Non assegnato") ? null : assigneeSelection;

        issue.setStatus(newStatus);
        issue.setAssignee(newAssignee);
        
        JOptionPane.showMessageDialog(this, "Modifiche salvate con successo!");
        dataChanged = true;
        dispose();
    }
    
    public boolean isDataChanged() {
        return dataChanged;
    }

    private void refreshComments() {
        commentsHistoryPanel.removeAll(); 

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Comment c : issue.getComments()) {
            JPanel commentBox = new JPanel(new BorderLayout());
            commentBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 10, 5), 
                BorderFactory.createLineBorder(new Color(220, 220, 220)) 
            ));
            commentBox.setBackground(new Color(245, 245, 245));

            String dataFormattata = "";
            if (c.getCreationDate() != null) {
                dataFormattata = c.getCreationDate().format(formatter);
            }

            JLabel header = new JLabel("<html><body style='padding:3px;'><b>" + c.getAuthor() + "</b> <i style='color:gray; font-size:9px;'>(" + dataFormattata + ")</i>:</body></html>");
            
            JTextArea body = new JTextArea(c.getText());
            body.setEditable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setBackground(new Color(245, 245, 245));
            body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            commentBox.add(header, BorderLayout.NORTH);
            commentBox.add(body, BorderLayout.CENTER);
            
            commentsHistoryPanel.add(commentBox);
        }

        commentsHistoryPanel.add(Box.createVerticalGlue());

        commentsHistoryPanel.revalidate();
        commentsHistoryPanel.repaint();

        // Sposta la barra di scorrimento verso il basso in modo asincrono dopo un commento aggiunto
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = commentsScroll.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
}