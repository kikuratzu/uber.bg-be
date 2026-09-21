package com.uber.bg.uber.bg.DTOs;

import com.uber.bg.uber.bg.Entities.Car;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class StreamLocationForPassengerDTO{
    private String username;
    private byte[] profilePhoto;
    private CarDTO carDTO;
}
