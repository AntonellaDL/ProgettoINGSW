package com.bugboard.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bugboard.backend.model.Entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

  // Metodo per trovare tutta la cronologia di una specifica Issue
  // Ordiniamo per data decrescente (dal più recente al più vecchio)
  List<History> findByIssueIdOrderByDateDesc(Long issueId);

}
