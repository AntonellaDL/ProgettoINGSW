package com.bugboard.frontend.ui.view.issue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.services.IssueService;

public class CreateIssueDialog extends JDialog{
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> typeCombo;
    private JComboBox<String> priorityCombo;
    private JLabel imageLabel;

    private final IssueService issueService;
    private boolean isSaved = false;

    private byte[] selectedImageBytes = null;
    private String selectedImageName = null;

    public CreateIssueDialog(Frame owner){
        super (owner, "Crea Nuova segnalazione", true);
        this.issueService = new IssueService();

        setSize(500,620);
        setLocationRelativeTo(owner);
        setResizable(true);

        initComponents();
    }

    private void initComponents(){
        JPanel panel = new JPanel (new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets (8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;

        //titolo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill= GridBagConstraints.NONE;
        panel.add(new JLabel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField = new JTextField();
        panel.add(titleField, gbc);

        //tipo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tipo:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] types = {"BUG", "FEATURE", "DOCUMENTATION","QUESTION"};
        typeCombo = new JComboBox<>(types);
        panel.add(typeCombo, gbc);

        //Priorità opzionale
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Priorità:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //il primo elemento vuoto consente di non specificare la priorità
        String [] priorities = {"", "BASSA", "MEDIA", "ALTA"};
        priorityCombo = new JComboBox<>(priorities);
        panel.add(priorityCombo, gbc);

        //descrizione obbligatoria
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Descrizione * :"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        // aggiungi immagine opzionale 
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadButton = new JButton("Allega immagine");
        imageLabel = new JLabel ("Nessun file selezionato");
        imageLabel.setForeground(Color.GRAY);
        uploadButton.addActionListener(e-> chooseImage());
        filePanel.add(uploadButton);
        filePanel.add(imageLabel);
        panel.add(filePanel, gbc);

        //nota campo obbligatorio
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel noteLabel = new JLabel("* Campo obbligatorio");
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC,11f));
        panel.add(noteLabel, gbc);

        // bottone salva 
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton =new JButton("Salva segnalazione");
        saveButton.setPreferredSize( new Dimension(200, 40));
        saveButton.setBackground(new Color(60,120,180));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveIssue());
        panel.add(saveButton, gbc);

        add(panel);
    }

    //seleziona un'immagine 
    private void chooseImage(){
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Immagini (JPG, JPEG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        if( fileChooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();

            try{
                selectedImageName = selectedFile.getName();
                selectedImageBytes = Files.readAllBytes(selectedFile.toPath());
                imageLabel.setText(selectedImageName);
                imageLabel.setForeground(new Color(0,100,0));
            }catch(IOException ex){
                JOptionPane.showMessageDialog(this, "Errore nel caricamento dell'immagine: " , "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //salvataggio 
    private void saveIssue(){
        //validazione titolo obbligatorio
        if(titleField.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this,"Il campo Titolo è obbligatorio", "Errore di validazione", JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return;
        }

        //validazione descrizione obbligatoria
        if(descriptionArea.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this,"Il campo Descrizione è obbligatorio", "Errore di validazione", JOptionPane.ERROR_MESSAGE);
            descriptionArea.requestFocus();
            return;
        }

        try{
            /*la priorità è l'elemento selezionato, se è la stringa vuota
                viene passata come null e il backend la gestisce come opzionale 
            */

            String priority = ((String) priorityCombo.getSelectedItem()).trim();

            CreateIssueRequest request = new CreateIssueRequest(
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                ((String) typeCombo.getSelectedItem()).trim(),
                priority,
                selectedImageBytes,
                selectedImageName
            );

            boolean succes = issueService.createIssue(request);
            if(succes){
                isSaved = true;
                JOptionPane.showMessageDialog(this, "Segnalazione creata correttamente");
                dispose();
            }else{
                JOptionPane.showMessageDialog(this,"Errore: utente non autorizzato o sessione scaduta", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore durante il salvataggio:"+e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved(){
        return isSaved;
    }

}