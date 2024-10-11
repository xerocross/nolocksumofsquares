package com.adamfgcross.nolocksumofsquares.helper;

import java.math.BigInteger;
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
	
	
	public SumOfSquaresComputationHelper(ExecutorService executorService, 
			SumOfSquaresRequest request) {
		this.executorService = executorService;
		this.request = request;
	}
	
	public CompletableFuture<BigInteger> computeSumOfSquares() {
		this.result = new CompletableFuture<BigInteger>();
		
		BigInteger rangeMin = new BigInteger(request.getRangeMin());
		BigInteger rangeMax = new BigInteger(request.getRangeMax());
		var workQueue = new ArrayBlockingQueue<WorkNode>(100_000);
		var taskProducerThread = new TaskProducerThread(workQueue, rangeMin, rangeMax);
		taskProducerThread.start();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		
		for (int i = 0; i < NUM_CONSUMERS; i++) {
			var squareComputer = new SquareComputer((long) i, workQueue, totalSum);
			futures.add(squareComputer.getCompletionFuture());
			executorService.submit(squareComputer);
		}
		CompletableFuture<Void> allDone = CompletableFuture.allOf((CompletableFuture<?>[]) futures.toArray());
		
		allDone.thenRunAsync(() -> {
			var sum = totalSum.get();
			logger.info("completed sum: " + sum);
			result.complete(sum);
		}, executorService);
		
		return result;
	}

	
	private static class WorkNode {
		
		private BigInteger num;
		private Boolean isTerminal;
		
		public BigInteger getNum() {
			return num;
		}

		public static WorkNode getTerminal() {
			var node = new WorkNode(null);
			node.isTerminal = true;
			return node;
		}
		
		public Boolean getIsTerminal() {
			return isTerminal;
		}

		public WorkNode(BigInteger num) {
			this.num = num;
		}
	}
	
	private static class SquareComputer implements Runnable {
		
		private BlockingQueue<WorkNode> workQueue;
		private AtomicReference<BigInteger> totalSum;
		private Long id;
		private CompletableFuture<Void> completionFuture = new CompletableFuture<>();

		public CompletableFuture<Void> getCompletionFuture() {
			return completionFuture;
		}

		public SquareComputer(Long id,
				BlockingQueue<WorkNode> workQueue,
				AtomicReference<BigInteger> totalSum) {
			this.id = id;
			this.workQueue = workQueue;
			this.totalSum = totalSum;
		}
		
		public void run() {
			while (true) {
				try {
					var workNode = workQueue.take();
					if (workNode.getIsTerminal()) {
						logger.info("terminating SquareComputer consumer: " + id);
						completionFuture.complete(null);
						break;
					}
					var num = workNode.getNum();
					BigInteger square = num.multiply(num);
					BigInteger currentSum;
					do {
						currentSum = totalSum.get();
					} while (!totalSum.compareAndSet(currentSum, currentSum.add(square)));
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
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
							queue.put(WorkNode.getTerminal());
							numTerminalWorkNodesAdded++;
						}
						logger.info("ending task producer thread");
						break;
					} else {
						queue.put(new WorkNode(index));
						index = index.add(BigInteger.valueOf(1));
						if (index.compareTo(rangeMax) >= 0) {
							isTerminated = true;
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				
			}
		}
	}
	
}







