package ch07.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Income {
    private long gross;
    private long net;
}
