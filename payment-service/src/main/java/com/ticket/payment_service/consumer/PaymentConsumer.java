package com.ticket.payment_service.consumer;

import com.ticket.common.event.BookingCreatedEvent;
import com.ticket.common.event.PaymentResultEvent;
import com.ticket.payment_service.model.Payment;
import com.ticket.payment_service.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    public PaymentConsumer(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "booking-created-events", groupId = "payment-group")
    public void consumeBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent: Booking ID = {}, User ID = {}, Amount = {}", 
                event.getBookingId(), event.getUserId(), event.getTotalPrice());

        // Giả lập xử lý thanh toán bất đồng bộ qua Stripe/Paypal (chờ 2 giây)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Tỷ lệ thành công 80%, thất bại 20%
        boolean paymentSuccess = random.nextInt(100) < 80;
        String status = paymentSuccess ? "SUCCESS" : "FAILED";
        String message = paymentSuccess ? "Payment completed successfully." : "Insufficient balance or payment timeout.";

        // Lưu thông tin thanh toán vào DB
        Payment payment = new Payment(event.getBookingId(), event.getTotalPrice(), status, java.time.LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Saved transaction: Payment ID = {}, Status = {}", savedPayment.getId(), status);

        // Gửi kết quả sang Kafka topic: payment-result-events
        PaymentResultEvent resultEvent = new PaymentResultEvent(
                event.getBookingId(),
                savedPayment.getId(),
                status,
                message
        );

        kafkaTemplate.send("payment-result-events", resultEvent.getBookingId().toString(), resultEvent);
        log.info("Published PaymentResultEvent to Kafka for Booking ID = {}", resultEvent.getBookingId());
    }
}
