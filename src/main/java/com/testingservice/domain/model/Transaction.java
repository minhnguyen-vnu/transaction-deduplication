package com.testingservice.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @JsonProperty("transactionID") private int transactionID;
    @JsonProperty("amount")        private double amount;
    @JsonProperty("userID")        private int userID;
    @JsonProperty("phone")         private String phone;
    @JsonProperty("IP")            private String IP;
    @JsonProperty("timestamp")     private Instant timestamp;
}
