package org.rocs.asa.service.appointment.buffer;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Component
public class AppointmentBuffer {

    private final int BUFFER_MINUTES = 10;

    public LocalDateTime applyBufferBefore(LocalDateTime time) {
        return time.minusMinutes(BUFFER_MINUTES);
    }

    public LocalDateTime applyBufferAfter(LocalDateTime time) {
        return time.plusMinutes(BUFFER_MINUTES);
    }

}
