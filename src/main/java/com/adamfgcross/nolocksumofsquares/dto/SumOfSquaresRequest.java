package com.adamfgcross.nolocksumofsquares.dto;

import java.math.BigInteger;

public class SumOfSquaresRequest {

	private BigInteger rangeMin;
	private BigInteger rangeMax;
	
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
