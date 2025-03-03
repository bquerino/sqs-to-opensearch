package com.brhenqu.paymentprocessor.repository;

import com.brhenqu.paymentprocessor.domain.model.Payment;

import java.util.Map;

public interface PaymentRepository {
    String createPayment(Payment payment);
    Map<String, Object> getPaymentById(String paymentId);
}
