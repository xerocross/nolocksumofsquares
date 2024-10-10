package com.adamfgcross.nolocksumofsquares.dto;

public class SumOfSquaresRequest {

	private String rangeMin;
	private String rangeMax;
	private Long taskId;
	
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
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
}
