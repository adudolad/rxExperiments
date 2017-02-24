package ch07.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Health {
    private long score;
}
