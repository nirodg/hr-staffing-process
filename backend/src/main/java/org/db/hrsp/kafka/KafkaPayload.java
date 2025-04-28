package org.db.hrsp.kafka;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaPayload {


    private String userId;
    private Topic topic;
    private Action action;

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
