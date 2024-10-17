package com.adamfgcross.nolocksumofsquares.service;

import java.math.BigInteger;

import com.adamfgcross.nolocksumofsquares.domain.SumOfSquaresJob;

public class WorkNode {
	public static WorkNode getTerminal(SumOfSquaresJob sumOfSquaresJob) {
		var node = new WorkNode(sumOfSquaresJob, null, null);
		node.isTerminal = true;
		return node;
	}
	
	public WorkNode(SumOfSquaresJob sumOfSquaresJob, BigInteger batchMin, BigInteger batchMax) {
		this.sumOfSquaresJob = sumOfSquaresJob;
		this.batchMin = batchMin;
		this.batchMax = batchMax;
		this.isTerminal = false;
	}

	private SumOfSquaresJob sumOfSquaresJob;
	
	public SumOfSquaresJob getSumOfSquaresJob() {
		return sumOfSquaresJob;
	}

	private BigInteger batchMin;
	
	public BigInteger getBatchMin() {
		return batchMin;
	}

	public BigInteger getBatchMax() {
		return batchMax;
	}

	private BigInteger batchMax;
	private Boolean isTerminal;
	
	public Boolean getIsTerminal() {
		return isTerminal;
	}
}
