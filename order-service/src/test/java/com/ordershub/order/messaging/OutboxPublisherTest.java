package com.ordershub.order.messaging;

import com.ordershub.order.domain.OutboxEvent;
import com.ordershub.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    OutboxEventRepository outbox;

    @Mock
    KafkaTemplate<String, String> kafka;

    OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxPublisher(outbox, kafka);
    }

    @Test
    void shouldSendEventAndMarkAsPublished() {
        OutboxEvent event = new OutboxEvent("Order", 1L, "OrderCreated", "{\"orderId\":1}");
        when(outbox.findByPublishedFalse()).thenReturn(List.of(event));
        CompletableFuture<SendResult<String, String>> sent = CompletableFuture.completedFuture(null);
        when(kafka.send(eq("orders.events"), any(), any())).thenReturn(sent);

        publisher.publishPending();

        verify(kafka).send("orders.events", "1", "{\"orderId\":1}");
        assertThat(event.isPublished()).isTrue();
    }

    @Test
    void shouldKeepEventUnpublishedWhenSendFails() {
        OutboxEvent event = new OutboxEvent("Order", 1L, "OrderCreated", "{\"orderId\":1}");
        when(outbox.findByPublishedFalse()).thenReturn(List.of(event));
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("kafka down"));
        when(kafka.send(eq("orders.events"), any(), any())).thenReturn(failed);

        publisher.publishPending();

        assertThat(event.isPublished()).isFalse();
    }

    @Test
    void shouldDoNothingWhenNoPendingEvents() {
        when(outbox.findByPublishedFalse()).thenReturn(List.of());

        publisher.publishPending();

        verifyNoInteractions(kafka);
    }
}
