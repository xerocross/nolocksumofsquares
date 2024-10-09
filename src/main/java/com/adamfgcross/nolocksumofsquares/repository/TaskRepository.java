package com.adamfgcross.nolocksumofsquares.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adamfgcross.nolocksumofsquares.domain.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

}
