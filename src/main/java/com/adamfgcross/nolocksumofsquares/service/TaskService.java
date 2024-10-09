package com.adamfgcross.nolocksumofsquares.service;

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
		taskRepository.save(task);
		return task;
	}
	
	@Transactional
	public void setTaskComplete(Long id) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("task with given id not found"));
		task.setStatus(TaskStatus.COMPLETE);
	}
}
