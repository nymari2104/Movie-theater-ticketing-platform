package com.ticket.event_service.service.impl;

import com.ticket.event_service.dto.SeatLockRequest;
import com.ticket.event_service.dto.SeatLockResponse;
import com.ticket.event_service.model.Seat;
import com.ticket.event_service.model.SeatStatus;
import com.ticket.event_service.repository.SeatRepository;
import com.ticket.event_service.service.SeatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    public SeatServiceImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            return new SeatLockResponse(false, "One or more seats not found.");
        }

        // Kiểm tra xem tất cả các ghế có còn trống không
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                return new SeatLockResponse(false, "Seat " + seat.getSeatNumber() + " is already reserved or booked.");
            }
        }

        // Khóa các ghế (chuyển sang RESERVED)
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.RESERVED);
        }
        seatRepository.saveAll(seats);

        return new SeatLockResponse(true, "Seats locked successfully.");
    }

    @Override
    @Transactional
    public SeatLockResponse unlockSeats(SeatLockRequest request) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.RESERVED) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
        }
        seatRepository.saveAll(seats);

        return new SeatLockResponse(true, "Seats unlocked successfully.");
    }

    @Override
    @Transactional
    public SeatLockResponse confirmSeats(SeatLockRequest request) {
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.RESERVED) {
                seat.setStatus(SeatStatus.BOOKED);
            }
        }
        seatRepository.saveAll(seats);

        return new SeatLockResponse(true, "Seats confirmed successfully.");
    }
}
