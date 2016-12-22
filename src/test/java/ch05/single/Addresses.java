package ch05.single;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Addresses {
    @JsonProperty("addresses")
    private List<Address> addresses;
}
