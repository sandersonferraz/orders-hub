package com.ordershub.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.notification.domain.Notification;
import com.ordershub.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    NotificationRepository notifications;

    NotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new NotificationConsumer(new ObjectMapper(), notifications);
    }

    @Test
    void shouldSaveOrderCreatedNotification() {
        consumer.onOrder("{\"orderId\":9001,\"productId\":1}");

        Notification saved = capture();
        assertThat(saved.getType()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getOrderId()).isEqualTo(9001L);
    }

    @Test
    void shouldSavePaymentEventNotification() {
        consumer.onPayment("{\"orderId\":9002,\"paymentId\":\"p-1\"}");

        Notification saved = capture();
        assertThat(saved.getType()).isEqualTo("PAYMENT_EVENT");
        assertThat(saved.getOrderId()).isEqualTo(9002L);
    }

    @Test
    void shouldSaveInventoryEventNotification() {
        consumer.onInventory("{\"orderId\":9003,\"productId\":1}");

        Notification saved = capture();
        assertThat(saved.getType()).isEqualTo("INVENTORY_EVENT");
        assertThat(saved.getOrderId()).isEqualTo(9003L);
    }

    @Test
    void shouldSaveWithNullOrderIdWhenAbsent() {
        consumer.onOrder("{}");

        Notification saved = capture();
        assertThat(saved.getOrderId()).isNull();
    }

    private Notification capture() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(captor.capture());
        return captor.getValue();
    }
}
