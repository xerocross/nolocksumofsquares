package com.adamfgcross.nolocksumofsquares.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adamfgcross.nolocksumofsquares.domain.SumOfSquaresJob;
import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;
import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class SumOfSquaresService {

	private TaskService taskService;
	private final int THREAD_POOL_SIZE = 3;
	private ExecutorService domainComputationsExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	private ExecutorService ioExecutorService = Executors.newVirtualThreadPerTaskExecutor();
	
	
	@Value("${spring.concurrency.batch-size}")
	private Integer BATCH_SIZE;
	
	private Integer WORK_QUEUE_SIZE = 100_000;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private WorkScheduler workScheduler;
	private WorkLeaseGeneratorThread workLeaseGeneratorThread;
	
	public SumOfSquaresService(TaskService taskService) {
		this.taskService = taskService;
	}
			
	public SumOfSquaresResponse computeSumOfSquares(SumOfSquaresRequest request) {
		var task = taskService.createNewTaskFromRequest(request);
		final var taskId = task.getId();
		request.setTaskId(task.getId());
		var job = new SumOfSquaresJob(taskId, request);
		workLeaseGeneratorThread.addJob(job);
		var response = new SumOfSquaresResponse(task);
		return response;
	}
	
	@PostConstruct
	public void init() {
		var queue = new ArrayBlockingQueue<WorkNode>(WORK_QUEUE_SIZE);
		workScheduler = new WorkScheduler(queue);
		workLeaseGeneratorThread = new WorkLeaseGeneratorThread(queue);
		workScheduler.start();
		workLeaseGeneratorThread.start();
	}
	
	@PreDestroy
	public void tearDown() {
		workLeaseGeneratorThread.terminate();
		workScheduler.terminate();
	}
	
	public SumOfSquaresResponse getSumOfSquaresResult(Long taskId) {
		var task = taskService.getTaskById(taskId).orElseThrow(() -> new ResourceNotFoundException("task with id " + taskId + " not found"));
		var response = new SumOfSquaresResponse(task);
		return response;
	}
	
	
	private static class JobPlaceholder {
		private boolean isTerminated;
		private SumOfSquaresJob job;
		private BigInteger index;
		
		public JobPlaceholder(SumOfSquaresJob job, BigInteger index, boolean isTerminated) {
			this.job = job;
			this.isTerminated = isTerminated;
			this.index = index;
		}
		
		public boolean isTerminated() {
			return isTerminated;
		}
		public SumOfSquaresJob getJob() {
			return job;
		}
		public BigInteger getIndex() {
			return index;
		}
	}
	
	
	private class WorkLeaseGeneratorThread extends Thread {
		
		private BlockingQueue<WorkNode> workQueue;
		private BlockingQueue<SumOfSquaresJob> jobQueue = new LinkedBlockingQueue<>();
		private boolean isStarted = false;

		private boolean terminateThread = false;
		private JobPlaceholder jobPlaceholder;
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		public WorkLeaseGeneratorThread(BlockingQueue<WorkNode> workQueue) {
			this.workQueue = workQueue;
		}
		
		public void terminate() {
			this.terminateThread = true;
		}
		
		
		public void addJob(SumOfSquaresJob job) {
			jobQueue.add(job);
		}
		
		public void run() {
			while (!terminateThread) {
				
				if (!isStarted) {
					try {
						logger.info("attempting to pull first job...");
						pullNextJob();
						isStarted = true;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				}
				
				if (jobPlaceholder.isTerminated()) {
					logger.info("found that job is terminated: adding poison pill");
					workQueue.add(WorkNode.getTerminal(jobPlaceholder.getJob()));
					try {
						pullNextJob();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
				} else {
					var batchMin = jobPlaceholder.getIndex();
					var upperBound = batchMin.add(BigInteger.valueOf(BATCH_SIZE));
					var rangeMax = jobPlaceholder.getJob().getRangeMax();
					var batchMax = upperBound.min(jobPlaceholder.getJob().getRangeMax());
					
					try {
						logger.info("attempting to queue work...");
						workQueue.put(new WorkNode(jobPlaceholder.getJob(), batchMin, batchMax));
						logger.info("queued work for range {} - {}...", batchMin, batchMax);
					} catch (InterruptedException e) {
						continue;
					}
					logger.info("batch max was {} and range max {}", batchMax, rangeMax);
					boolean isTerminated = batchMax.compareTo(rangeMax) >= 0;
					logger.info("found that is terminated: {}", isTerminated);
					jobPlaceholder = new JobPlaceholder(jobPlaceholder.getJob(), batchMax, isTerminated);
				}
			}
			logger.info("work lease thread terminating");
		}
		
		private void pullNextJob() throws InterruptedException {
			var job = jobQueue.take();
			jobPlaceholder = new JobPlaceholder(job, job.getRangeMin(), false);
		}
	}
	
	private class WorkScheduler extends Thread {
	
		private BlockingQueue<WorkNode> workQueue;
		private boolean terminated = false;
		private Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private ConcurrentHashMap<Long, Set<CompletableFuture<Void>>> futures = new ConcurrentHashMap<>();
		
		public WorkScheduler(BlockingQueue<WorkNode> workQueue) {
			this.workQueue = workQueue;
		}
		
		public void terminate() {
			this.terminated = true;
		}
		
		private void onAllJobTasksScheduled(SumOfSquaresJob sumOfSquaresJob) {
			logger.info("all tasks for job {} have been scheduled", sumOfSquaresJob.getTaskId());
			var jobFutures = futures.get(sumOfSquaresJob.getTaskId());
			var allCompleteFuture = CompletableFuture.allOf(jobFutures.toArray(new CompletableFuture[0]));
			
			allCompleteFuture.thenRunAsync(() -> {
				try {
					sumOfSquaresJob.setIsComplete(true);
					var sum = sumOfSquaresJob.getSumOfSquares().get();
					
					taskService.completeTask(sumOfSquaresJob.getTaskId(), sum.toString());
					removeJobFutures(sumOfSquaresJob);
					var runTime = sumOfSquaresJob.getRuntime();
					logger.info("job {}; sum {}; runtime {}", sumOfSquaresJob.getTaskId(), sum, runTime);
				} catch (Exception e) {
					logger.error("error", e);
				}
			}, ioExecutorService);
		}
		
		private void removeJobFutures(SumOfSquaresJob job) {
			var taskId = job.getTaskId();
			futures.remove(taskId);
		}
		
		public void run() {
			while (!terminated) {
				try {
					//logger.info("getting work...");
					var workNode = workQueue.take();
					var job = workNode.getSumOfSquaresJob();
					var taskId = job.getTaskId();
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						// compute work from workNode
						try {
							job.startClock();
						} catch (Exception e) {
							logger.error("error", e);
						}
						var batchMin = workNode.getBatchMin();
						var batchMax = workNode.getBatchMax();
						
						// handle work complete for job
						if (workNode.getIsTerminal()) {
							logger.info("got work terminal signal");
							onAllJobTasksScheduled(job);
							return;
						}
//						logger.info("computing squares for range {} - {}", batchMin.toString(), batchMax.toString());
						BigInteger batchSumOfSquares = BigInteger.valueOf(0L);
						for (BigInteger i = batchMin; i.compareTo(batchMax) < 0; i = i.add(BigInteger.valueOf(1L))) {
							batchSumOfSquares = batchSumOfSquares.add(i.multiply(i));
						}
						
						// perform update
						BigInteger currentSum;
						var runningSumOfSquares = job.getSumOfSquares();
//						logger.info("batch sum of squares is {}", batchSumOfSquares);
						try {
							do {
								currentSum = runningSumOfSquares.get();
//								logger.info("-- currentSum is " + currentSum.toString());
							} while (!runningSumOfSquares.compareAndSet(currentSum, currentSum.add(batchSumOfSquares)));
//							logger.info("computation of batch update successful");
						} catch (Exception e) {
							logger.error("exception", e);
						}
						
					}, domainComputationsExecutorService);
					
					var set = futures.computeIfAbsent(taskId, (id) -> {
						Set<CompletableFuture<Void>> newSet = ConcurrentHashMap.newKeySet();
						return newSet;
					});
					set.add(future);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			logger.info("terminating scheduler");
		}
		
	}
	
}
