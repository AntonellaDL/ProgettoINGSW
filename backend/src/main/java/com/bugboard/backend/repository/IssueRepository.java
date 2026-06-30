package com.bugboard.backend.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bugboard.backend.model.Entity.Issue;
import com.bugboard.backend.model.Enum.IssueStatus;
import com.bugboard.backend.model.Enum.IssueType;
import com.bugboard.backend.model.Enum.Priority;

//la repository serve per fare le query al database

// non inserisco @Repository in quanto quando si utilizza Spring Data JPA 
// qualsiasi interfaccia che estende JpaRepository viene riconosciuta automaticamente
//quando scriviamo extends JpaRepository<Issue, Long> stiamo dicendo che questa repository
//  gestisce l'entità Issue e che la chiave primaria di Issue è di tipo Long

public interface IssueRepository extends JpaRepository<Issue, Long> {

    //ttrova issue per id
    Issue findById(long id);
@Override
    //trova tutte le issue
    List<Issue> findAll();

    //trova issue in base al tipo
    List<Issue> findByType(IssueType type);

    //trova issue in base alla priorità
    List<Issue> findByPriority(Priority priority);

    //trova issue in base allo stato
    List<Issue> findByStatus(IssueStatus status);

    //trova issue in base al titolo in ordine alfabetico
    List<Issue> findAllByOrderByTitleAsc();

    //trovare issue per tipo e priorità
    List<Issue> findByTypeAndPriority(IssueType type, Priority priority);

    //trovare issue per stato e priorità
    List<Issue> findByStatusAndPriority(IssueStatus status, Priority priority);

    //trovare issue per tipo e stato
    List<Issue> findByTypeAndStatus(IssueType type, IssueStatus status);

}
