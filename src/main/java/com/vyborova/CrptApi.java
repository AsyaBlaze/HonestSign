package com.vyborova;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final int requestsLimit;
    private final long expirationTime;
    private AtomicInteger requestsLeft;
    private Date endTime;

    public CrptApi(TimeUnit timeUnit, int requestsLimit) {
        this.requestsLeft = new AtomicInteger(requestsLimit);
        this.requestsLimit = requestsLimit;
        this.endTime = new Date();
        expirationTime = timeUnit.toMillis(1);
        endTime = new Date(endTime.getTime() + expirationTime);
    }

    public Document createTestDocument() {
        return new DocumentBuilder()
                .setDescription(new Description("string"))
                .setDocId("string")
                .setDocStatus("string")
                .setDocType("LP_INTRODUCE_GOODS")
                .setImportRequest(true)
                .setOwnerInn("string")
                .setParticipantInn("string")
                .setProducerInn("string")
                .setProductionDate(new Date())
                .setProductionType("string")
                .setProducts(new Product[] {new ProductBuilder()
                        .setCertificateDocument("string")
                        .setCertificateDocumentDate(new Date())
                        .setCertificateDocumentNumber("string")
                        .setOwnerInn("string")
                        .setProducerInn("string")
                        .setProductionDate(new Date())
                        .setTnvedCode("string")
                        .setUituCode("string")
                        .setUnitCode("string")
                        .build()})
                .setRegDate(new Date())
                .setRegNumber("string")
                .build();
    }

    public void createDocument(Document document, String signature) {
        if (!checkLimit()) return;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(document);
            StringEntity params = new StringEntity(json, ContentType.APPLICATION_JSON);
            HttpPost request = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
            request.addHeader("Content-type", "application/json");
            request.addHeader("sign", signature); // в ТЗ не было написано как использовать подпись, поэтому я её добавила в header
            request.setEntity(params);
            CloseableHttpResponse response = client.execute(request);
            response.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized boolean checkLimit() {
        if (new Date().after(endTime)) {
            requestsLeft = new AtomicInteger(requestsLimit);
            endTime = new Date();
            endTime.setTime(endTime.getTime() + expirationTime);
        }
        return requestsLeft.decrementAndGet() >= 0;
    }

    @Getter
    @Setter
    class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        // "doc_type": "LP_INTRODUCE_GOODS" не совсем поняла - отличается ли чем-то данная переменна от остальных переменных строкового типа данных
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date;
        private String production_type;
        private Product[] products;
        private Date reg_date;
        private String reg_number;

        public Document(DocumentBuilder builder) {
            this.description = builder.description;
            this.doc_id = builder.doc_id;
            this.doc_status = builder.doc_status;
            this.doc_type = builder.doc_type;
            this.importRequest = builder.importRequest;
            this.owner_inn = builder.owner_inn;
            this.participant_inn = builder.participant_inn;
            this.producer_inn = builder.producer_inn;
            this.production_date = builder.production_date;
            this.production_type = builder.production_type;
            this.products = builder.products;
            this.reg_date = builder.reg_date;
            this.reg_number = builder.reg_number;
        }
    }

    class DocumentBuilder{
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date;
        private String production_type;
        private Product[] products;
        private Date reg_date;
        private String reg_number;

        public DocumentBuilder setDescription(Description description) {
            this.description = description;
            return this;
        }

        public DocumentBuilder setDocId(String doc_id) {
            this.doc_id = doc_id;
            return this;
        }

        public DocumentBuilder setDocStatus(String doc_status) {
            this.doc_status = doc_status;
            return this;
        }

        public DocumentBuilder setDocType(String doc_type) {
            this.doc_type = doc_type;
            return this;
        }

        public DocumentBuilder setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
            return this;
        }

        public DocumentBuilder setOwnerInn(String owner_inn) {
            this.owner_inn = owner_inn;
            return this;
        }

        public DocumentBuilder setParticipantInn(String participant_inn) {
            this.participant_inn = participant_inn;
            return this;
        }

        public DocumentBuilder setProducerInn(String producer_inn) {
            this.producer_inn = producer_inn;
            return this;
        }

        public DocumentBuilder setProductionDate(Date production_date) {
            this.production_date = production_date;
            return this;
        }

        public DocumentBuilder setProductionType(String production_type) {
            this.production_type = production_type;
            return this;
        }

        public DocumentBuilder setProducts(Product[] products) {
            this.products = products;
            return this;
        }

        public DocumentBuilder setRegDate(Date reg_date) {
            this.reg_date = reg_date;
            return this;
        }

        public DocumentBuilder setRegNumber(String reg_number) {
            this.reg_number = reg_number;
            return this;
        }

        public Document build() {
            return new Document(this);
        }
    }


    @Getter
    @Setter
    class Product {
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String unit_code;
        private String uitu_code;

        public Product(ProductBuilder builder) {
            this.certificate_document = builder.certificate_document;
            this.certificate_document_date = builder.certificate_document_date;
            this.certificate_document_number = builder.certificate_document_number;
            this.owner_inn = builder.owner_inn;
            this.producer_inn = builder.producer_inn;
            this.production_date = builder.production_date;
            this.tnved_code = builder.tnved_code;
            this.unit_code = builder.unit_code;
            this.uitu_code = builder.uitu_code;
        }
    }

    class ProductBuilder {
        private String certificate_document;
        private Date certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_code;
        private String unit_code;
        private String uitu_code;


        public ProductBuilder setCertificateDocument(String certificateDocument) {
            this.certificate_document = certificateDocument;
            return this;
        }

        public ProductBuilder setCertificateDocumentDate(Date certificateDocumentDate) {
            this.certificate_document_date = certificateDocumentDate;
            return this;
        }

        public ProductBuilder setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificate_document_number = certificateDocumentNumber;
            return this;
        }

        public ProductBuilder setOwnerInn(String ownerInn) {
            this.owner_inn = ownerInn;
            return this;
        }

        public ProductBuilder setProducerInn(String producerInn) {
            this.producer_inn = producerInn;
            return this;
        }

        public ProductBuilder setProductionDate(Date productionDate) {
            this.production_date = productionDate;
            return this;
        }

        public ProductBuilder setTnvedCode(String tnvedCode) {
            this.tnved_code = tnvedCode;
            return this;
        }

        public ProductBuilder setUnitCode(String unitCode) {
            this.unit_code = unitCode;
            return this;
        }

        public ProductBuilder setUituCode(String uituCode) {
            this.uitu_code = uituCode;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }

    @Getter
    @Setter
    class Description {
        private String participantInn;

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }
    }
}



