package com.ticket.notification_service.consumer;

import com.ticket.common.event.BookingCreatedEvent;
import com.ticket.common.event.PaymentResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "booking-created-events", groupId = "notification-group")
    public void consumeBookingCreated(BookingCreatedEvent event) {
        log.info("\n" +
                "========================================================================\n" +
                "[SMS/Email Notification - SENDING]\n" +
                "To: User {}\n" +
                "Message: Yêu cầu đặt vé cho đơn hàng {} đang được xử lý.\n" +
                "Số tiền cần thanh toán: {} VND.\n" +
                "========================================================================",
                event.getUserId(), event.getBookingId(), event.getTotalPrice());
    }

    @KafkaListener(topics = "payment-result-events", groupId = "notification-group")
    public void consumePaymentResult(PaymentResultEvent event) {
        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            log.info("\n" +
                    "========================================================================\n" +
                    "[SMS/Email Notification - SUCCESS]\n" +
                    "Message: Thanh toán THÀNH CÔNG cho đơn hàng {}.\n" +
                    "Mã giao dịch thanh toán: {}.\n" +
                    "Trạng thái: Vé xem phim của bạn đã được XÁC NHẬN chính thức!\n" +
                    "========================================================================",
                    event.getBookingId(), event.getPaymentId());
        } else {
            log.info("\n" +
                    "========================================================================\n" +
                    "[SMS/Email Notification - FAILED]\n" +
                    "Message: Thanh toán THẤT BẠI cho đơn hàng {}.\n" +
                    "Lý do: {}\n" +
                    "Trạng thái: Đơn đặt vé của bạn đã bị HỦY TỰ ĐỘNG.\n" +
                    "========================================================================",
                    event.getBookingId(), event.getMessage());
        }
    }
}
