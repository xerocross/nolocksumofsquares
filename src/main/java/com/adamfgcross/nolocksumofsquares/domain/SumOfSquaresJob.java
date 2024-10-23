package com.adamfgcross.nolocksumofsquares.domain;

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;

public class SumOfSquaresJob {

	private Long taskId;
	private BigInteger rangeMin;
	private BigInteger rangeMax;
	private AtomicReference<BigInteger> sumOfSquares;
	private JobStatus status;
	private Set<CompletableFuture<Void>> futures = ConcurrentHashMap.newKeySet();
	
	private AtomicInteger workTasksRemaining = new AtomicInteger(0);
	
	public int incrementWorkTask() {
		return workTasksRemaining.incrementAndGet();
	}
	
	public int decrementWorkTask() {
		return workTasksRemaining.decrementAndGet();
	}
	
	public int getWorkTasksRemaining() {
		return workTasksRemaining.get();
	}
	public Set<CompletableFuture<Void>> getFutures() {
		return futures;
	}
	
	public void addFuture(CompletableFuture<Void> future) {
		futures.add(future);
	}

	public void removeFuture(CompletableFuture<Void> future) {
		futures.remove(future);
	}
	
	public void clearFutures() {
		this.futures.clear();
	}
	
	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}
	
	private AtomicReference<Clock> timeClock = new AtomicReference<>(new Clock(null, null, false));
	
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

	public void setIsComplete(Boolean isComplete) {
		stopClock();
		this.status = JobStatus.COMPLETE;
	}
	
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

}
