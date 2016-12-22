package ch04.booking;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Ticket {
    private String code;
}
