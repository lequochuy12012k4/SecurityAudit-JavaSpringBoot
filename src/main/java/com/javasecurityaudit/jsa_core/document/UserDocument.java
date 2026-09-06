package com.javasecurityaudit.jsa_core.document;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.HashSet;
import java.util.Set;

@Document(indexName = "users", createIndex = true)
@Setting(settingPath = "elasticsearch/users-settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDocument {

    @Id
    String id;

    @Field(type = FieldType.Keyword) // Keyword: dùng để filter/match chính xác
    String username;

    @Field(type = FieldType.Keyword)
    String email;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    String fullName;

    @Field(type = FieldType.Boolean)
    Boolean enabled;

    @Field(type = FieldType.Boolean)
    Boolean accountNonLocked;

    @Field(type = FieldType.Keyword) // Lưu danh sách tên role ("ROLE_USER", "ROLE_ADMIN")
    @Builder.Default
    Set<String> roles = new HashSet<>();
}