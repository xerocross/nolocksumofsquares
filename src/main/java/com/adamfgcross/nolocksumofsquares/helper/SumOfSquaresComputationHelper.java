package com.adamfgcross.nolocksumofsquares.helper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;

public class SumOfSquaresComputationHelper {

	private AtomicReference<BigInteger> totalSum = new AtomicReference<>(BigInteger.valueOf(0L));
	private static Logger logger = LoggerFactory.getLogger(SumOfSquaresComputationHelper.class);
	private ExecutorService executorService;
	private SumOfSquaresRequest request;
	private static final int NUM_CONSUMERS = 4;
	private CompletableFuture<BigInteger> result;
	private static final Integer BATCH_SIZE = 1000;
	private BigInteger numBatches;
	private AtomicReference<BigInteger> batchesComplete = new AtomicReference<>(BigInteger.valueOf(0L));
	
	
	public SumOfSquaresComputationHelper(ExecutorService executorService, 
			SumOfSquaresRequest request) {
		this.executorService = executorService;
		this.request = request;
	}
	
	public CompletableFuture<BigInteger> computeSumOfSquares() {
		this.result = new CompletableFuture<BigInteger>();
		
		BigInteger rangeMin = new BigInteger(request.getRangeMin());
		BigInteger rangeMax = new BigInteger(request.getRangeMax());
		setNumBatches(rangeMin, rangeMax);
		
		var workQueue = new ArrayBlockingQueue<WorkNode>(100_000);
		var taskProducerThread = new TaskProducerThread(workQueue, rangeMin, rangeMax);
		taskProducerThread.start();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		
		for (int i = 0; i < NUM_CONSUMERS; i++) {
			var squareComputer = new SquareComputer((long) i, workQueue, totalSum, batchesComplete);
			squareComputer.setNumBatches(numBatches);
			squareComputer.setTaskId(request.getTaskId());
			futures.add(squareComputer.getCompletionFuture());
			executorService.submit(squareComputer);
		}
		CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
		
		allDone.thenRunAsync(() -> {
			var sum = totalSum.get();
			logger.info("completed sum: " + sum);
			result.complete(sum);
		}, executorService);
		
		return result;
	}

	
	private void setNumBatches(BigInteger rangeMin, BigInteger rangeMax) {
		BigInteger size = rangeMax.subtract(rangeMin);
		numBatches = size.divide(BigInteger.valueOf(BATCH_SIZE));		
	}
	
	private static class WorkNode {
		
		public static WorkNode getTerminal() {
			var node = new WorkNode(null, null);
			node.isTerminal = true;
			return node;
		}
		
		public WorkNode(BigInteger batchMin, BigInteger batchMax) {
			this.batchMin = batchMin;
			this.batchMax = batchMax;
			this.isTerminal = false;
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
	
	private static class SquareComputer implements Runnable {
		
		private BlockingQueue<WorkNode> workQueue;
		private AtomicReference<BigInteger> totalSum;
		private AtomicReference<BigInteger> batchesComplete;
		private Long id;
		private CompletableFuture<Void> completionFuture = new CompletableFuture<>();
		private BigInteger numBatches;
		private Long taskId;

		public Long getTaskId() {
			return taskId;
		}

		public void setTaskId(Long taskId) {
			this.taskId = taskId;
		}

		public BigInteger getNumBatches() {
			return numBatches;
		}

		public void setNumBatches(BigInteger numBatches) {
			this.numBatches = numBatches;
		}

		public CompletableFuture<Void> getCompletionFuture() {
			return completionFuture;
		}

		public SquareComputer(Long id,
				BlockingQueue<WorkNode> workQueue,
				AtomicReference<BigInteger> totalSum,
				AtomicReference<BigInteger> batchesComplete
				) {
			this.id = id;
			this.workQueue = workQueue;
			this.totalSum = totalSum;
			this.batchesComplete = batchesComplete;
		}
		
		public void run() {
			logger.info("started computation in SquareComputer " + id);
			while (true) {
				try {
					//logger.info("getting work...");
					var workNode = workQueue.take();
					//logger.info("got work");
					if (workNode.getIsTerminal()) {
						logger.info("terminating SquareComputer consumer: " + id);
						completionFuture.complete(null);
						break;
					}
					
					var batchMin = workNode.getBatchMin();
					var batchMax = workNode.getBatchMax();
					BigInteger sumOfSquares = BigInteger.valueOf(0L);
					for (BigInteger i = batchMin; i.compareTo(batchMax) < 0; i = i.add(BigInteger.valueOf(1L))) {
						sumOfSquares = sumOfSquares.add(i.multiply(i));
					}
					
					
					BigInteger currentSum;
					do {
						currentSum = totalSum.get();
						//logger.info("-- currentSum is " + currentSum.toString());
					} while (!totalSum.compareAndSet(currentSum, currentSum.add(sumOfSquares)));
					
					
					BigInteger currentBatchesComplete;
					Boolean didUpdate;
					do {
						currentBatchesComplete = batchesComplete.get();
						didUpdate = batchesComplete.compareAndSet(currentBatchesComplete, currentBatchesComplete.add(BigInteger.valueOf(1L)));
						if (didUpdate) {
							BigDecimal progress = new BigDecimal(currentBatchesComplete).divide(new BigDecimal(numBatches), 2, RoundingMode.HALF_UP);
							logger.info("task " + taskId + " progress: " + progress.toString());
						}
					} while (!didUpdate);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logger.info("interrupt exception on SquareComputer " + id);
					continue;
				} catch (Exception e) {
					logger.info("some other exception on SquareComputer " + id);
					e.printStackTrace();
					break;
				}
			}
		}
	}
	
	private static class TaskProducerThread extends Thread {
		
		public TaskProducerThread(BlockingQueue<WorkNode> queue, BigInteger rangeMin, BigInteger rangeMax) {
			this.queue = queue;
			this.rangeMax = rangeMax;
			this.index = rangeMin;
		}
		private BlockingQueue<WorkNode> queue;
		private BigInteger rangeMax;
		private BigInteger index;
		private Boolean isTerminated = false;
		private int numTerminalWorkNodesAdded = 0;
		
		public void run() {
			while (true) {
				try {
					if (isTerminated) {
						if (numTerminalWorkNodesAdded < NUM_CONSUMERS) {
							logger.info("adding terminal to queue.");
							queue.put(WorkNode.getTerminal());
							numTerminalWorkNodesAdded++;
						} else {
							logger.info("ending task producer thread");
							break;
						}
					} else {
						BigInteger batchMin = index;
						BigInteger upperBound = index.add(BigInteger.valueOf(BATCH_SIZE));
						
						BigInteger batchMax = upperBound.min(rangeMax);
						queue.put(new WorkNode(batchMin, batchMax));
						index = batchMax;
						if (index.compareTo(rangeMax) >= 0) {
							isTerminated = true;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
			}
		}
	}
	
}







