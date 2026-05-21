package com.bugboard.frontend.ui.view.issue;

import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.services.IssueService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CreateIssueDialog extends JDialog{

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> typeCombo;
    private JComboBox<String> priorityCombo;
    private JLabel image;
    

    private IssueService issueService;
    private boolean isSaved = false;

    private byte selectImageBytes[] = null;
    private String selectedImageName = null;

    public CreateIssueDialog(Frame owner) {
        super(owner, "Crea Nuova Segnalazione", true);
        this.issueService = new IssueService();

        setSize(500, 600);
        setLocationRelativeTo(owner);
        setResizable(true);

        initComponents();


    }

    private void initComponents(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;

        //titolo
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField = new JTextField();
        panel.add(titleField, gbc);

        //tipo
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Type:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] types = {"BUG","FEATURE","DOCUMENTATION","QUESTION"};
        typeCombo = new JComboBox<>(types);
        panel.add(typeCombo, gbc);

        //priority
        gbc.gridx=0; gbc.gridy=2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Priority:"), gbc);

        gbc.gridx=1; gbc.gridy=2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] priorities = {"ALTA", "MEDIA", "BASSA"};
        priorityCombo = new JComboBox<>(priorities);
        panel.add(priorityCombo, gbc);

        //description
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5,20);
        descriptionArea.setLineWrap(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        //file
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadButton = new JButton("Upload Image");
        image = new JLabel("Nessun file selezionato");
        image.setForeground(Color.GRAY);

        uploadButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                chooseImage();
            }
        });
        filePanel.add(uploadButton);
        filePanel.add(image);
        panel.add(filePanel, gbc);

        //buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(200,40));
        saveButton.setBackground(new Color(60,120,180));
        saveButton.setForeground(Color.WHITE);

        saveButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                saveIssue();
            }
        });

        panel.add(saveButton, gbc);
        add(panel);
    }

    private void chooseImage(){
        JFileChooser fileChooser = new JFileChooser();

        //accetta solo immagini 
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(this);
        if(returnValue == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            try{
                selectedImageName = selectedFile.getName();
                selectImageBytes = Files.readAllBytes(selectedFile.toPath());

                image.setText(selectedImageName);
                image.setForeground(new Color(0,100,0));
            } catch (IOException ex){
                JOptionPane.showMessageDialog(this, "Errore nel caricamento dell'immagine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    private void saveIssue(){
        if( titleField.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this, "Il campo titolo è obbligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            CreateIssueRequest request = new CreateIssueRequest(
                titleField.getText(),
                descriptionArea.getText(),
                ((String) typeCombo.getSelectedItem()).trim(),
                ((String) priorityCombo.getSelectedItem()).trim(),
                selectImageBytes,
                selectedImageName
            );

            boolean success = issueService.createIssue(request);

            if(success){
            isSaved = true;
            JOptionPane.showMessageDialog(this, "Segnalazione creata correttamente");
            dispose();
            }else {
              JOptionPane.showMessageDialog(this, "Errore: utente non autorizzato o sessione scaduta.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore duramente il salvataggio");
        }
    }

    public boolean isSaved(){
        return isSaved;
    }
}