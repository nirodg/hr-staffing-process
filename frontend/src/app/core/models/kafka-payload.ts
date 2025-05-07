import { KafkaAction } from "../constants/kafka-actions";
import { KafkaTopic } from "../constants/kafka-topic";

export interface KafkaPayload {
  action?: KafkaAction;
  topic?: KafkaTopic;
  userId?: string;
  entityId?: number;
  parentId?: number;
}
