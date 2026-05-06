package com.bugboard.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bugboard.backend.model.Entity.History;
import com.bugboard.backend.service.HistoryService;

//Questo controller gestisce la lettura della history.
//serve solo per leggere i dati, la scrittura avviene in automatico nel service delle Issue.
@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {
  
  //private final HistoryRepository historyRepository;
  //cambiato con HistoryService
  private final HistoryService historyService;
  public HistoryController(HistoryService historyService) {
    this.historyService = historyService;
  }

  // Il frontend drovrà chiamare una GET http://localhost:8080/api/history/issue/5
  @GetMapping("/issue/{issueId}")
  public ResponseEntity<List<History>> getHistoryByIssue(@PathVariable Long issueId) {

    // metodo personalizzato della repository per averli già ordinati
    // dal più recente al più vecchio (così la timeline è corretta).
    List<History> historyLog = historyService.getHistoryByIssueId(issueId);
    return ResponseEntity.ok(historyLog);
  }
}
