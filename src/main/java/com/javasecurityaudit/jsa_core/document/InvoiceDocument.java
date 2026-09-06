package com.javasecurityaudit.jsa_core.document;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Document(indexName = "invoices", createIndex = true)
@Setting(settingPath = "elasticsearch/invoices-settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceDocument {

    @Id
    String id;

    @Field(type=FieldType.Keyword)
    String invoiceCode;

    @Field(type=FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    String customerName;

    @Field(type=FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    String customerEmail;

    @Field(type=FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    String customerPhone;

    @Field(type=FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    String description;

    @Field(type=FieldType.Keyword)
    BigDecimal totalAmount;

    @Field(type=FieldType.Text)
    @Builder.Default
    String status = "DRAFT";

   
    @Field(type=FieldType.Keyword)
    LocalDateTime invoiceDate;

    @Field(type=FieldType.Keyword)
    LocalDateTime dueDate;

    @Version
    @Field(type=FieldType.Long)
    @Builder.Default
    Long version = 0L;
}