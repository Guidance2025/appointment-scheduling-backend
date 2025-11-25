package org.rocs.asa.domain.appointment.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BookedSlotsResponse {
    private LocalDateTime scheduledDate ;
    private LocalDateTime endDate;

    public BookedSlotsResponse(LocalDateTime scheduledDate, LocalDateTime endDate) {
        this.scheduledDate = scheduledDate;
        this.endDate = endDate;
    }
}
