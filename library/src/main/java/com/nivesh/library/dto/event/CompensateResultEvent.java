package com.nivesh.library.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompensateResultEvent {

    private String referenceNumber;
    private String failureReason;

    public CompensateResultEvent(String referenceNumber){
        this.referenceNumber = referenceNumber;
    }
}
