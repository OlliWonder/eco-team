package com.sber.java13.ecoteam.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PointDTO extends GenericDTO {
    private String city;
    private String address;
    private String workingTime;
    private Set<Long> wastesIds;
}