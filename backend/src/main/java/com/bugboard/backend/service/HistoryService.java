package com.bugboard.backend.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bugboard.backend.model.Entity.History;
import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Entity.User;
import com.bugboard.backend.repository.HistoryRepository;

@Service
public class HistoryService {
    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    //lettura
    public List<History> getHistoryByIssueId(Long issueId) {
        return historyRepository.findByIssueIdOrderByDateDesc(issueId);
    }

    //Metodo per salvare una nuovo evento
    public void longEvent(Issue issue, User user, String action) {
        History history = new History();
        history.setIssue(issue);
        history.setUser(user);
        history.setAction(action);
        history.setDate(LocalDateTime.now());
        historyRepository.save(history);
    }

    //specifica per la creazione di una segnalazione di bug
    public void logCreationEvent(Issue issue, User user) {
        longEvent(issue, user, "Ha creato la segnalazione");
    }

    //specifica per la modifica di una segnalazione di bug
    public void logUpdateEvent(Issue issue, User user, String fieldName, String oldValue, String newValue) {
        String action = String.format("Ha modificato %s da '%s' a '%s'", fieldName, oldValue, newValue);
        longEvent(issue, user, action);
    }

    
}
