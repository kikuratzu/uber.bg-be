package com.uber.bg.uber.bg.DTOs;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class StreamLocationForPassengerDTO{
    private String username;
    private String profilePhoto;
    private CarDTO carDTO;
}
