package com.brhenqu.paymentprocessor.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryStrategy {

    private final PaymentRepository pooledRepository;
    private final PaymentRepository syncRepository;
    private final boolean usePool;

    public PaymentRepositoryStrategy(
            @Qualifier("pooledPaymentRepository") PaymentRepository pooledRepository,
            @Qualifier("syncPaymentRepository") PaymentRepository syncRepository,
            @Value("${opensearch.usePool}") boolean usePool) {
        this.pooledRepository = pooledRepository;
        this.syncRepository = syncRepository;
        this.usePool = usePool;
    }

    public PaymentRepository getRepository() {
        return usePool ? pooledRepository : syncRepository;
    }
}
