package com.bugboard.frontend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.bugboard.frontend.model.Comment;
import com.bugboard.frontend.model.HistoryLog;
import com.bugboard.frontend.model.Issue;
import com.bugboard.frontend.model.dto.CreateIssueRequest;
import com.bugboard.frontend.utils.SessionManager;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IssueService {
    private final ApiService apiService;
    private final ObjectMapper mapper;

    public IssueService(){
        this.apiService = ApiService.getInstance();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public boolean createIssue(CreateIssueRequest request) {
        // Per ora reindirizzo la chiamata all'ApiService
        return apiService.createIssue(request);
    }

}