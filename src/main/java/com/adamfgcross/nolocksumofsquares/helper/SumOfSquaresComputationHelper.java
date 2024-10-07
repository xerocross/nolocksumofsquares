package com.adamfgcross.nolocksumofsquares.helper;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;

public class SumOfSquaresComputationHelper {

	private AtomicReference<BigInteger> totalSum = new AtomicReference<>(BigInteger.valueOf(0L));
	
	private ExecutorService executorService;
	
	private AtomicReference<BigInteger> remainingComputations;
	
	
	public SumOfSquaresComputationHelper(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	public void computeSumOfSquares(SumOfSquaresRequest request) {
		BigInteger rangeMin = request.getRangeMin();
		BigInteger rangeMax = request.getRangeMax();

		remainingComputations = new AtomicReference<>(rangeMax.subtract(rangeMin));
		
		
		for (BigInteger i = rangeMin; i.compareTo(rangeMax) < 0; i.add(BigInteger.valueOf(1))) {
			executorService.submit(() -> {
				BigInteger square = i.multiply(i);
				BigInteger currentSum;
				do {
					currentSum = totalSum.get();
				} while (!totalSum.compareAndSet(currentSum, currentSum.add(square)));
				
				BigInteger currentRemainingComputations;
				do {
					currentRemainingComputations = remainingComputations.get();
				} while (!remainingComputations.compareAndSet(currentRemainingComputations, 
						currentRemainingComputations.subtract(BigInteger.valueOf(1))));
				
			});
		}
		
		
	}
}
