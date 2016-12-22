package ch05.single;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("city")
    private String city;
    @JsonProperty("zip")
    private String zip;
    @JsonProperty("street")
    private String street;
}
