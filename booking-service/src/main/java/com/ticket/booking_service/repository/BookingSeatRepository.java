package com.ticket.booking_service.repository;

import com.ticket.booking_service.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {
    List<BookingSeat> findByBookingId(UUID bookingId);
}
