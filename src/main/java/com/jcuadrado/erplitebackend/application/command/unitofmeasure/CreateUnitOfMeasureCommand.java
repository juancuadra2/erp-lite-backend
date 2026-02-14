package com.jcuadrado.erplitebackend.application.command.unitofmeasure;

public record CreateUnitOfMeasureCommand(
        String name,
        String abbreviation,
        Long userId
) {
}