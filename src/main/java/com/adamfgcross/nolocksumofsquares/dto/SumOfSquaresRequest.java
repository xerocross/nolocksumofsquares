package com.adamfgcross.nolocksumofsquares.dto;

import java.math.BigInteger;

public class SumOfSquaresRequest {

	private BigInteger rangeMin;
	private BigInteger rangeMax;
	private Long taskId;
	
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	public BigInteger getRangeMin() {
		return rangeMin;
	}
	public void setRangeMin(BigInteger rangeMin) {
		this.rangeMin = rangeMin;
	}
	public BigInteger getRangeMax() {
		return rangeMax;
	}
	public void setRangeMax(BigInteger rangeMax) {
		this.rangeMax = rangeMax;
	}
}
