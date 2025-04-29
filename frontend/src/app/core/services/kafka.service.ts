import { Injectable } from "@angular/core";

import { Kafka}  from "kafkajs";

@Injectable({ providedIn: "root" })
export class KafkaService {
  kafka = new Kafka({
    clientId: "my-app",
    brokers: ["localhost:9092"],
  });
}
