package com.adamfgcross.nolocksumofsquares.service;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;
import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresResponse;
import com.adamfgcross.nolocksumofsquares.helper.SumOfSquaresComputationHelper;

@Service
public class SumOfSquaresService {

	private TaskService taskService;
	private final int THREAD_POOL_SIZE = 3;
	private ExecutorService domainComputationsExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	private ExecutorService ioExecutorService = Executors.newVirtualThreadPerTaskExecutor();
	
	@Value("${spring.concurrency.batch-size}")
	private Integer BATCH_SIZE;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public SumOfSquaresService(TaskService taskService) {
		this.taskService = taskService;
	}
			
	public SumOfSquaresResponse computeSumOfSquares(SumOfSquaresRequest request) {
		var task = taskService.createNewTaskFromRequest(request);
		final var taskId = task.getId();
		request.setTaskId(task.getId());
		
		var sumOfSquaresComputationHelper = new SumOfSquaresComputationHelper(domainComputationsExecutorService, request);
		sumOfSquaresComputationHelper.setBatchSize(BATCH_SIZE);
		CompletableFuture<BigInteger> futureResult = sumOfSquaresComputationHelper.computeSumOfSquares();
		
		futureResult.thenAcceptAsync((sum) -> {
			taskService.completeTask(taskId, sum.toString());
		}, ioExecutorService);
		var response = new SumOfSquaresResponse(task);
		return response;
	}
	
	public SumOfSquaresResponse getSumOfSquaresResult(Long taskId) {
		var task = taskService.getTaskById(taskId).orElseThrow(() -> new ResourceNotFoundException("task with id " + taskId + " not found"));
		var response = new SumOfSquaresResponse(task);
		return response;
	}
	
}
