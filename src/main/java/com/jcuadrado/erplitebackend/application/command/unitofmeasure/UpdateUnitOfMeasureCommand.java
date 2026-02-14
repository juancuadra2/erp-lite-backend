package com.jcuadrado.erplitebackend.application.command.unitofmeasure;

public record UpdateUnitOfMeasureCommand(
        String name,
        String abbreviation,
        Long userId
) {
}