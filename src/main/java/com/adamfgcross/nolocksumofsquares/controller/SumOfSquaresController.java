package com.adamfgcross.nolocksumofsquares.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresRequest;
import com.adamfgcross.nolocksumofsquares.dto.SumOfSquaresResponse;
import com.adamfgcross.nolocksumofsquares.service.SumOfSquaresService;

@RestController
@RequestMapping("/api/squares")
public class SumOfSquaresController {
	
	private SumOfSquaresService sumOfSquaresService;
	
	public SumOfSquaresController(SumOfSquaresService sumOfSquaresService) {
		this.sumOfSquaresService = sumOfSquaresService;
	}

	@PostMapping
	public ResponseEntity<?> computeSumOfSquares(@RequestBody SumOfSquaresRequest request) {
		var response = sumOfSquaresService.computeSumOfSquares(request);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getTaskResult(@PathVariable("id") Long id) {
		var taskResponse = sumOfSquaresService.getSumOfSquaresResult(id);
		return ResponseEntity.ok(taskResponse);
	}
}
