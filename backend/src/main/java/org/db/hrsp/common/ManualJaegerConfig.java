package org.db.hrsp.common;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

@Configuration
@ConditionalOnProperty(prefix = "telemetry.manual-jaeger", name = "enabled", havingValue = "true")
public class ManualJaegerConfig {

	@Bean
	public OpenTelemetry openTelemetry() {
		JaegerGrpcSpanExporter exporter = JaegerGrpcSpanExporter.builder().setEndpoint("http://localhost:4317") // Jaeger
																													// GRPC
																													// collector
				.build();

		Resource serviceResource = Resource
				.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "hr-staffing-process"));

		SdkTracerProvider provider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter))
				.setResource(Resource.getDefault().merge(serviceResource)).build();

		Runtime.getRuntime().addShutdownHook(new Thread(provider::close));

		return OpenTelemetrySdk.builder().setTracerProvider(provider).build();
	}

	@Bean
	public Tracer tracer(OpenTelemetry otel) {
		return otel.getTracer("manual");
	}
}