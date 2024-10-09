package com.adamfgcross.nolocksumofsquares.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Task {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String rangeMin;
	private String rangeMax;
	private TaskStatus status;
	private String sumOfSquares;
	
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
	public Long getId() {
		return id;
	}
}
