package com.javasecurityaudit.jsa_core.excutor;

import com.javasecurityaudit.jsa_core.entity.UserActivityLog;
import com.javasecurityaudit.jsa_core.enums.AuditAction;
import com.javasecurityaudit.jsa_core.repository.UserActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class AsyncAuditConsumer {

    private final StringRedisTemplate redisTemplate;
    private final UserActivityLogRepository activityLogRepository;

    @Value("${audit.redis-stream-key:audit:activity:stream}")
    private String streamKey;

    @Scheduled(fixedDelay = 2000)
    public void consumeAuditLogs() {
        try {
            // Đọc tất cả các bản ghi từ Redis Stream
            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                    .read(StreamOffset.fromStart(streamKey));

            if (records == null || records.isEmpty()) {
                return;
            }

            List<UserActivityLog> logEntities = new ArrayList<>();
            List<String> recordIdsToDelete = new ArrayList<>();

            for (MapRecord<String, Object, Object> record : records) {
                Map<Object, Object> valueMap = record.getValue();

                UserActivityLog activityLog = UserActivityLog.builder()
                        .username((String) valueMap.get("username"))
                        .action(AuditAction.valueOf((String) valueMap.get("action")))
                        .description((String) valueMap.get("description"))
                        .status(Integer.parseInt((String) valueMap.get("status")))
                        .ipAddress((String) valueMap.get("ipAddress"))
                        .timestamp(LocalDateTime.parse((String) valueMap.get("timestamp")))
                        .build();

                logEntities.add(activityLog);
                recordIdsToDelete.add(record.getId().getValue());
            }

            // Batch insert toàn bộ xuống MySQL
            activityLogRepository.saveAll(logEntities);

            // Xóa các record đã lưu khỏi Redis Stream để dọn dẹp bộ nhớ
            redisTemplate.opsForStream().delete(streamKey, recordIdsToDelete.toArray(new String[0]));
            log.info("Đã lưu async thành công {} bản ghi Activity Log từ Redis Stream xuống Database.", logEntities.size());

        } catch (Exception e) {
            log.error("Lỗi khi đọc Activity Log từ Redis Stream: {}", e.getMessage());
        }
    }
}