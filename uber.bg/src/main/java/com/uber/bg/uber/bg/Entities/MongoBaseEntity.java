package com.uber.bg.uber.bg.Entities;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;


import java.util.UUID;

@Getter
@Setter
public abstract class MongoBaseEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @CreatedDate
    private Long createdDate;

    @LastModifiedDate
    private Long modifiedDate;
}