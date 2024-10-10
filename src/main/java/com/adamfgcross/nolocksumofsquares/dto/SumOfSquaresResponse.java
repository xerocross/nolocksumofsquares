package com.adamfgcross.nolocksumofsquares.dto;

import com.adamfgcross.nolocksumofsquares.domain.Task;
import com.adamfgcross.nolocksumofsquares.domain.TaskStatus;

public class SumOfSquaresResponse {
	private String rangeMin;
	private String rangeMax;
	private Long taskId;
	private TaskStatus status;
	private String sumOfSquares;
	
	public SumOfSquaresResponse(SumOfSquaresRequest request) {
		this.rangeMin = request.getRangeMin();
		this.rangeMax = request.getRangeMax();
	}
	
	public SumOfSquaresResponse(Task task) {
		this.rangeMin = task.getRangeMin();
		this.rangeMax = task.getRangeMax();
		this.status = task.getStatus();
		this.sumOfSquares = task.getSumOfSquares();
		this.taskId = task.getId();
	}
	
	public String getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}

	public String getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getSumOfSquares() {
		return sumOfSquares;
	}

	public void setSumOfSquares(String sumOfSquares) {
		this.sumOfSquares = sumOfSquares;
	}

	
	
	
}
