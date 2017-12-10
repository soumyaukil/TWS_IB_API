package quote;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Quote {
    @Builder.Default
    private String securityId = "";
    @Builder.Default
    private double bidPrice = 0;
    @Builder.Default
    private double askPrice = 0;
}
