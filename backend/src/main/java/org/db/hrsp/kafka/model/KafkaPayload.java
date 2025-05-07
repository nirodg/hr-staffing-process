package org.db.hrsp.kafka.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaPayload {

    private String userId;
    private Topic topic;
    private Action action;
    private Long entityId; // e.g., comment ID, staffing ID, client ID
    private Long parentId; // optional: for replies, or nesting

    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        ;

        public static Action fromString(String action) {
            return Action.valueOf(action.toUpperCase());
        }
    }

    @Getter
    public enum Topic {
        STAFFING_PROCESS("STAFFING_PROCESS"),
        CLIENTS("CLIENTS"),
        EMPLOYEES("EMPLOYEES"),
        COMMENTS("COMMENTS"),
        ;

        private final String topicName;

        Topic(String topicName) {
            this.topicName = topicName;
        }

    }
}
