package com.brhenqu.paymentprocessor.repository.config;

import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class OpenSearchClientConfig {

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    @Value("${opensearch.scheme}")
    private String scheme;

    @Value("${opensearch.username}")
    private String username;

    @Value("${opensearch.password}")
    private String password;

    @Value("${opensearch.max.connections.total:50}") // Define um limite total de conexões (padrão 50)
    private int maxConnectionsTotal;

    @Value("${opensearch.max.connections.per.route:10}") // Define um limite de conexões por host (padrão 10)
    private int maxConnectionsPerRoute;

    @Value("${opensearch.connection.timeout:5000}") // Timeout de conexão em ms (padrão 5000)
    private int connectionTimeout;

    @Value("${opensearch.socket.timeout:60000}") // Timeout de leitura de socket em ms (padrão 60000)
    private int socketTimeout;

    @Value("${opensearch.request.timeout:10000}") // Timeout para requisições (padrão 10000)
    private int requestTimeout;

    private RestHighLevelClient client;

    @Bean(name = "pooledClient")
    public RestHighLevelClient restHighLevelClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpAsyncClientBuilder ->
                        configureHttpClient(httpAsyncClientBuilder, credentialsProvider))
                            .setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                .setConnectionRequestTimeout(requestTimeout)
                                .setConnectTimeout(connectionTimeout)
                                .setSocketTimeout(socketTimeout)
                );

        client = new RestHighLevelClient(builder);

        return client;
    }

    private HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder httpClientBuilder, CredentialsProvider credentialsProvider) {
        try {
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                    .setIoThreadCount(Runtime.getRuntime().availableProcessors()) // Ajusta os threads conforme a CPU
                    .build();

            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
            PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);

            // Configurando limites de conexões
            connectionManager.setMaxTotal(maxConnectionsTotal);
            connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

            return httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager);
        } catch (IOReactorException e) {
            throw new RuntimeException("Erro ao configurar o pool de conexões do OpenSearch", e);
        }
    }

    @PreDestroy
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
