package com.bugboard.frontend.ui.view.issue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.bugboard.frontend.model.Comment;
import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.model.User;
import com.bugboard.frontend.services.ApiService;
import com.bugboard.frontend.utils.SessionManager;

public class IssueDetailDialog extends JDialog{
    private final Issue issue;
    private final ApiService apiService;

    private JComboBox<String> statusCombo;
    private JComboBox<String> assigneeCombo;
    private JPanel commentsHistoryPanel;
    private JTextArea newCommentArea;
    private JScrollPane commentsScroll;
    private boolean dataChanged = false;
    private List<User> listaUtentiBackend = new ArrayList<>();

    public IssueDetailDialog(Frame owner, Issue issue){
        super (owner, "Dettaglio segnalazione: "+issue.getTitle(),true);
        this.issue = issue;
        this.apiService = ApiService.getInstance();

        setSize(900,700);
        setMinimumSize(new Dimension(800,600));
        setResizable(true);
        setLocationRelativeTo(owner);

        initComponents();
        //scarico i commenti dal server e li mostro
        List<Comment> commentiDalServer = apiService.getCommentsByIssue(issue.getId());
        if (commentiDalServer != null) {
            issue.setComments(commentiDalServer); 
        }
        refreshComments();
    }

    //costruzione UI
    private void initComponents(){
        setLayout(new BorderLayout(10,10));

        //intestazione 
        JPanel headerPanel = new JPanel(new BorderLayout(5,5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        JLabel titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(new Font("arial",Font.BOLD,22));
        headerPanel.add(titleLabel,BorderLayout.NORTH);

        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,5));
        JLabel info = new JLabel(
            "ID: "+issue.getId() 
            + "| Tipo: "+ issue.getType()
            + "| Priorità: "+issue.getPriority());
        
        info.setForeground(Color.DARK_GRAY);
        metaPanel.add(info);
        metaPanel.add(Box.createHorizontalStrut(15));
        

        //tendina stato 
        metaPanel.add(new JLabel("Stato: "));
        String[] stati={"TODO", "IN_PROGRESS","DONE","CLOSED"};
        statusCombo = new JComboBox<>(stati);

        //valore corrente
        String currentStatus = issue.getStatus()!=null
        ? issue.getStatus().toUpperCase():"TODO";
        statusCombo.setSelectedItem(currentStatus);

        //solo l'assegnatario può modificare lo stato 
        boolean isAssignee = isCurrentUserAssignee();
        statusCombo.setEnabled(isAssignee);
        if(!isAssignee){
            statusCombo.setToolTipText("Solo l'assegnatario può modificare lo stato.");
        }

        metaPanel.add(statusCombo);
        metaPanel.add(Box.createHorizontalStrut(15));

        //tendina assegnatario 
         metaPanel.add(new JLabel("Assegnato a: "));
        assigneeCombo = new JComboBox<>();
        assigneeCombo.addItem("Non assegnato");

        // Carica gli utenti dal server per popolare la tendina assegnatario
        listaUtentiBackend = apiService.getAllUsers();
        for (User u : listaUtentiBackend) {
            assigneeCombo.addItem(u.getEmail());
        }

        if (issue.getAssigeeId() != null) {
            // Se la issue ha un ID assegnatario, cerca l'email corrispondente
            for (User u : listaUtentiBackend) {
                if (u.getId().equals(issue.getAssigeeId())) {
                    assigneeCombo.setSelectedItem(u.getEmail());
                    break;
                }
            }
        } else {
            assigneeCombo.setSelectedItem("Non assegnato");
        }

        //helper nel sessionManager controllo sull'admin
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        assigneeCombo.setEnabled(isAdmin);
        if (!isAdmin) {
            assigneeCombo.setToolTipText("Solo un Amministratore può assegnare o riassegnare i bug.");
        }
        metaPanel.add(assigneeCombo);

        headerPanel.add(metaPanel, BorderLayout.CENTER);
        add(headerPanel,BorderLayout.NORTH);

        //centro: descrizione e commenti 
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
        
        commentsHistoryPanel = new JPanel();
        commentsHistoryPanel.setLayout(new BoxLayout(commentsHistoryPanel, BoxLayout.Y_AXIS));
        commentsHistoryPanel.setBackground(Color.WHITE);
        
        commentsScroll = new JScrollPane(commentsHistoryPanel);
        commentsScroll.setBorder(BorderFactory.createTitledBorder("Discussione"));
        commentsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descScroll, commentsScroll);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
        

        // in basso nuovo commento e bottoni 

        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        newCommentArea = new JTextArea(3, 40);
        newCommentArea.setLineWrap(true);
        newCommentArea.setWrapStyleWord(true);
        JScrollPane newCommentScroll = new JScrollPane(newCommentArea);
        newCommentScroll.setBorder(BorderFactory.createTitledBorder("Scrivi un commento..."));
        footerPanel.add(newCommentScroll, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAddComment = new JButton("Invia Commento");
        btnAddComment.setBackground(new Color(60, 120, 180));
        btnAddComment.setForeground(Color.WHITE);
        btnAddComment.setFocusPainted(false);
        btnAddComment.addActionListener(e -> addComment());
        
        JButton btnSave  = new JButton("Salva Modifiche");
        JButton btnClose = new JButton("Chiudi");
        

        // mostra salva modifiche solo all'assegnatario o all'admin: 
        btnSave.setVisible(isAssignee || isAdmin);

       bloccaUISeStatoDone(btnSave);
        
        btnSave.addActionListener(e -> saveStatusChange());
        btnClose.addActionListener(e -> dispose());
         
                btnPanel.add(btnAddComment);
                btnPanel.add(btnSave);
                btnPanel.add(btnClose);
                footerPanel.add(btnPanel, BorderLayout.SOUTH);
                add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Congela i componenti dell'interfaccia se la segnalazione è già stata risolta
     */
    private void bloccaUISeStatoDone(JButton btnSave) {
        String currentStatus = issue.getStatus() != null ? issue.getStatus().toUpperCase() : "TODO";
        
        if ("DONE".equals(currentStatus) || "CLOSED".equals(currentStatus)) {
            statusCombo.setEnabled(false);
            assigneeCombo.setEnabled(false);
            // Forza la scomparsa del bottone salva
            if (btnSave != null) {
                btnSave.setVisible(false);
            }
        }
    }

    private boolean isCurrentUserAssignee(){
        User currentUser= SessionManager.getInstance().getCurrentUser();
        if(currentUser == null || issue.getAssigeeId()==null){
            return false;
        }
        else{
            return issue.getAssigeeId().equals(currentUser.getId());
        }
    }
    
   private void saveStatusChange(){
        User currentUser = SessionManager.getInstance().getCurrentUser();
        //controllo sessione 
        if(currentUser==null){
            JOptionPane.showMessageDialog(this,"Sessione scaduta. Effettua nuovamente il login. ","Sessione non valida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //controllo autorizzazione 
        if (!isCurrentUserAssignee() && !SessionManager.getInstance().isAdmin()){
            JOptionPane.showMessageDialog(this,"Non hai i permessi per modificare questa segnalazione.", "Operazione non consentita", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus =(String) statusCombo.getSelectedItem();
        String currentAssigneeName = (String) assigneeCombo.getSelectedItem();

        // Non si può risolvere un bug se non c'è un assegnatario
        if ("DONE".equals(newStatus) && "Non assegnato".equals(currentAssigneeName)) {
            JOptionPane.showMessageDialog(this, 
                "Impossibile risolvere un bug senza un assegnatario.\nAssegna prima la segnalazione a qualcuno.", 
                "Azione non valida", JOptionPane.WARNING_MESSAGE);
            return; 
        }

        // conferma esplicita se si imposta Done 
        if("DONE".equals(newStatus)){
            int confirm = JOptionPane.showConfirmDialog(this,"Stai impostando la segnalazione come RISOLTA.\n"+"Il creatore riceverà una notifica automatica. Continuare? ",
                "Conferma risoluzione", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(confirm != JOptionPane.YES_OPTION){
                return;
            }
        }

        // logica di salvataggio
        
        boolean successStatus = true;
        boolean successAssign = true;
        boolean haEffettuatoModifiche = false;

        // gestione assegnazione issue
        if (SessionManager.getInstance().isAdmin()) {
            Long assigneeId = null;
            
            if ("Non assegnato".equals(currentAssigneeName)) {
                // Se l'admin prova a rimuovere un assegnatario esistente
                if (issue.getAssigeeId() != null) {
                    JOptionPane.showMessageDialog(this, 
                        "Questo sistema non supporta la rimozione dell'assegnatario.\nSeleziona un altro utente per riassegnare la segnalazione.", 
                        "Operazione non supportata", JOptionPane.WARNING_MESSAGE);
                    return; // Interrompe il salvataggio
                }
            } else {
                // Ricerca normale dell'ID
                for (User u : listaUtentiBackend) {
                    if (u.getEmail().equals(currentAssigneeName)) {
                        assigneeId = u.getId();
                        break;
                    }
                }
            }
            
            // Se l'ID è stato trovato ed è diverso da quello attuale, invia al server
            if (assigneeId != null && !assigneeId.equals(issue.getAssigeeId())) {
                successAssign = apiService.assignIssue(issue.getId(), assigneeId, currentUser.getId());
                if (successAssign) {
                    issue.setAssignee(currentAssigneeName);
                    issue.setAssigneeId(assigneeId); // aggiornamento id locale
                    haEffettuatoModifiche = true;
                }
            }
        }

        // gestione dello stato, la fa solo l'assegnatario
        // Se lo stato è cambiato e l'utente corrente è l'effettivo assegnatario, aggiorna lo stato
        String oldStatus = issue.getStatus() != null ? issue.getStatus().toUpperCase() : "TODO";
        if (!oldStatus.equals(newStatus) && isCurrentUserAssignee()) {
            successStatus = apiService.updateIssueStatus(issue.getId(), newStatus, currentUser.getId());
            if (successStatus) {
                issue.setStatus(newStatus);
                haEffettuatoModifiche = true;
            }
        }

        // messaggio di conferma o errore
        if (successStatus && successAssign) {
            if (haEffettuatoModifiche) {
                dataChanged = true;

                if (getOwner() instanceof com.bugboard.frontend.ui.view.dashboard.DashboardFrame) {
                    ((com.bugboard.frontend.ui.view.dashboard.DashboardFrame) getOwner()).aggiornaBadgeNotifiche();
                }
                String messaggio = "Modifiche salvate con successo!";
                if ("DONE".equals(newStatus) && isCurrentUserAssignee() && !oldStatus.equals(newStatus)) {
                    messaggio += "\nIl creatore della segnalazione è stato notificato.";
                }
                JOptionPane.showMessageDialog(this, messaggio, "Aggiornamento completato", JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Impossibile salvare i dati sul server.\n"
                + "Verifica i tuoi permessi o che il server sia raggiungibile.",
                "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addComment(){
        String testo = newCommentArea.getText().trim();
        if(testo.isEmpty()){
            return;
        }
        User u = SessionManager.getInstance().getCurrentUser();
        if(u == null){
            JOptionPane.showMessageDialog(this,"Sessione scaduta. Effettua nuovamente il login.","Sessione non valida", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String autore = u.getEmail();

        boolean success = apiService.postComment(Long.valueOf(issue.getId()), u.getId(), testo);
        if(!success){
            JOptionPane.showMessageDialog(this,"Impossibile salvare il commento sul server.","Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Comment nuovoCommento = new Comment();
        nuovoCommento.setAuthor(autore);
        nuovoCommento.setText(testo);
        nuovoCommento.setCreationDate(java.time.LocalDateTime.now());

        issue.addComment(nuovoCommento);
        newCommentArea.setText("");
        refreshComments();
        dataChanged = true;
    }

    private void refreshComments(){
        commentsHistoryPanel.removeAll();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for(Comment c : issue.getComments()){
            JPanel box = new JPanel(new BorderLayout());
            box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,10,5), BorderFactory.createLineBorder(new Color (220,220,220))));
            box.setBackground(new Color (245,245,245));

            String data = c.getCreationDate()!= null ? c.getCreationDate().format(fmt): "";
            JLabel header = new JLabel("<html><body style='padding:3px;'><b>"+c.getAuthor()+"</b> <i style='color:gray; font-size:9px;'>("+data+")</i>:</body></html>");

            JTextArea body = new JTextArea(c.getText());
            body.setEditable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setBackground(new Color (245,245,245));
            body.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

            box.add(header, BorderLayout.NORTH);
            box.add(body,BorderLayout.CENTER);
            commentsHistoryPanel.add(box);
        }

        commentsHistoryPanel.add(Box.createVerticalGlue());
        commentsHistoryPanel.revalidate();
        commentsHistoryPanel.repaint();

        SwingUtilities.invokeLater(()->{
            JScrollBar bar = commentsScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public boolean isDateChanged(){
        return dataChanged;
    }
        
    
}
