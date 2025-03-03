package com.brhenqu.paymentprocessor;

import com.brhenqu.paymentprocessor.domain.model.Payment;
import com.brhenqu.paymentprocessor.repository.PaymentRepository;
import com.brhenqu.paymentprocessor.repository.PaymentRepositoryStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class PaymentProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentProcessorApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(PaymentRepositoryStrategy repositoryStrategy) {
		return args -> {
			PaymentRepository paymentRepository = repositoryStrategy.getRepository();

			ExecutorService executor = Executors.newFixedThreadPool(20); // Usa 10 threads simultâneas
			List<Future<String>> futures = new ArrayList<>();

			long startTime = System.nanoTime();
			for (int i = 0; i < 100000; i++) {
				final int id = i;
				futures.add(executor.submit(() -> {
					Payment payment = new Payment("1" + id, "2", 123.1, "R$", "Pending");
					paymentRepository.createPayment(payment);
					return paymentRepository.getPaymentById(payment.getId()).toString();
				}));
			}

			for (Future<String> future : futures) {
				future.get(); // Aguarda todas as threads terminarem
			}

			executor.shutdown();
			long duration = (System.nanoTime() - startTime) / 1_000_000;
			System.out.println("⏱ Tempo total (com pool e multithreading): " + duration + " ms");

		};
	}


}
