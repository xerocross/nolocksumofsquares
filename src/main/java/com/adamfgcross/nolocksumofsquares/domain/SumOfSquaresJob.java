package com.adamfgcross.nolocksumofsquares.domain;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;

public class SumOfSquaresJob {

	private Long taskId;
	
	private BigInteger rangeMin;
	private BigInteger rangeMax;
	private AtomicReference<BigInteger> sumOfSquares;
	private Boolean isComplete;
	
	public SumOfSquaresJob(Long taskId, SumOfSquaresRequest request) {
		this.taskId = taskId;
		this.rangeMin = new BigInteger(request.getRangeMin());
		this.rangeMax = new BigInteger(request.getRangeMax());
		sumOfSquares = new AtomicReference<>(BigInteger.valueOf(0L));
	}
	
	public Long getTaskId() {
		return taskId;
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
	public AtomicReference<BigInteger> getSumOfSquares() {
		return sumOfSquares;
	}
	public void setSumOfSquares(AtomicReference<BigInteger> sumOfSquares) {
		this.sumOfSquares = sumOfSquares;
	}
	public Boolean getIsComplete() {
		return isComplete;
	}
	public void setIsComplete(Boolean isComplete) {
		this.isComplete = isComplete;
	}
}
