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
	private AtomicReference<Clock> timeClock = new AtomicReference<>(new Clock(null, null, false));
	
	private class Clock {
		private Long startTime;
		private Long endTime;
		private Boolean isStarted;
		
		public Long getStartTime() {
			return startTime;
		}

		public Long getEndTime() {
			return endTime;
		}

		public Boolean getIsStarted() {
			return isStarted;
		}

		public Clock (Long startTime, Long endTime, Boolean isStarted) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.isStarted = isStarted;
		}
	}
	
	public Clock getClock() {
		return timeClock.get();
	}
	
	public Long getRuntime() {
		var clock = timeClock.get();
		return clock.getEndTime() - clock.getStartTime();
	}
	
	public void startClock() {
		Clock clock;
		Long startTime;
		do {
			clock = timeClock.get();
			if (clock.isStarted) {
				return;
			}
			startTime = System.currentTimeMillis();
		} while (!timeClock.compareAndSet(clock, new Clock(startTime, null, true)));
	}

	public void stopClock() {
		Clock clock;
		Long endTime;
		do {
			clock = timeClock.get();
			if (clock.endTime != null) {
				return;
			}
			endTime = System.currentTimeMillis();
		} while (!timeClock.compareAndSet(clock, new Clock(clock.startTime, endTime, true)));
	}
	
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
		stopClock();
		this.isComplete = isComplete;
	}
}
