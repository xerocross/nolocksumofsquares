package com.adamfgcross.nolocksumofsquares.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.adamfgcross.nolocksumofsquares.repository.TaskRepository;
import com.adamfgcross.nolocksumofsquares.domain.Task;
import com.adamfgcross.nolocksumofsquares.domain.TaskStatus;
import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;

import jakarta.transaction.Transactional;

@Service
public class TaskService {

	private TaskRepository taskRepository;
	
	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	@Transactional
	public Task createNewTaskFromRequest(SumOfSquaresRequest request) {
		var task = new Task();
		task.setRangeMin(request.getRangeMin().toString());
		task.setRangeMax(request.getRangeMax().toString());
		task.setStatus(TaskStatus.SCHEDULED);
		taskRepository.save(task);
		return task;
	}
	
	@Transactional
	public Optional<Task> getTaskById(Long id) {
		return taskRepository.findById(id);
	}
	
	@Transactional
	public void completeTask(Long id, String sumOfSquares) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task with given id not found"));
		task.setStatus(TaskStatus.COMPLETE);
		task.setSumOfSquares(sumOfSquares);
	}
	
	@Transactional
	public void setSumOfSquares(Long id, String sumOfSquares) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task with given id not found"));
		task.setSumOfSquares(sumOfSquares);
	}
}
