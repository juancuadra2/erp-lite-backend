package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalitySimplifiedDto {

    private UUID uuid;
    private String code;
    private String name;
}
