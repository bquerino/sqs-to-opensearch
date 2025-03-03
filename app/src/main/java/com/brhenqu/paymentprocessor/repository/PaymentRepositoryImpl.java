package com.brhenqu.paymentprocessor.repository;

import com.brhenqu.paymentprocessor.domain.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;




import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Repository("pooledPaymentRepository")
public class PaymentRepositoryImpl implements PaymentRepository {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    @Value("${opensearch.index.payments:payments}")
    private String indexName;

    public PaymentRepositoryImpl(@Qualifier("pooledClient") RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public String createPayment(Payment payment) {
        try {
            payment.setId(UUID.randomUUID().toString());

            String jsonPayment = objectMapper.writeValueAsString(payment);

            IndexRequest request = new IndexRequest("payments")
                    .id(payment.getId())
                    .source(jsonPayment, XContentType.JSON);  // ✅ Garante que seja JSON

            IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            //log.info("Pagamento criado com ID: {}", response.getId());
            return response.getId();
        } catch (IOException e) {
            //log.error("Erro ao criar pagamento no OpenSearch", e);
            throw new RuntimeException("Falha ao salvar pagamento", e);
        }
    }



    public Map<String, Object> getPaymentById(String paymentId) {
        try {
            GetRequest request = new GetRequest("payments", paymentId);
            GetResponse response = client.get(request, RequestOptions.DEFAULT);

            if (!response.isExists()) {
                //log.warn("Pagamento não encontrado para ID: {}", paymentId);
                return null;
            }

            return response.getSourceAsMap();
        } catch (IOException e) {
            //log.error("Erro ao buscar pagamento no OpenSearch", e);
            throw new RuntimeException("Falha ao buscar pagamento", e);
        }
    }


}
