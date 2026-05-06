package com.bugboard.backend.service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    //cartella di destinazione per i file caricati
    private final Path uploadPath = Paths.get("uploads");

    public FileStorageService(){
        try{
            //crea la cartella se non esiste
            Files.createDirectories(uploadPath);
        } catch (IOException e){
            throw new RuntimeException("Impossibile creare la cartella di upload", e);
        }
    }

    public String saveFile(MultipartFile file){
        try{
            //genera un nome unico per evitare conflitti
            String fileName= UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            //risolve il percorso completo del file
            Path filePath = uploadPath.resolve(fileName);

            //copaia il file nella cartella di destinazione
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e){
            throw new RuntimeException("Impossibile salvare il file", e);
        }
        
    }
}