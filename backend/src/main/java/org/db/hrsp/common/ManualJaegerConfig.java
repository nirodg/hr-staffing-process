package org.db.hrsp.common;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "telemetry.manual.jaeger", name = "enabled", havingValue = "true")
public class ManualJaegerConfig {

	public static final String MANUAL_LOGGER = "manual-logger";

	@Value("${spring.application.name}")
	private String APP_NAME;

	@Value("${telemetry.manual.jaeger.url}")
	private String JAEGER_URL;

	@Bean
	OpenTelemetry otel() {
	  JaegerThriftSpanExporter exporter = JaegerThriftSpanExporter.builder()
	      .setEndpoint(JAEGER_URL)
	      .build();

	  Resource service = Resource.create(
	      Attributes.of(ResourceAttributes.SERVICE_NAME, APP_NAME));

	  SdkTracerProvider provider = SdkTracerProvider.builder()
	      .addSpanProcessor(SimpleSpanProcessor.create(exporter))
	      .setResource(Resource.getDefault().merge(service))
	      .build();

	  Runtime.getRuntime().addShutdownHook(new Thread(provider::close));
	  return OpenTelemetrySdk.builder().setTracerProvider(provider)
			  .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
	}

	@Bean
	Tracer tracer(OpenTelemetry otel) {
	  return otel.getTracer(MANUAL_LOGGER);
	}
}