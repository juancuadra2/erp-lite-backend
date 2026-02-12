package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalityResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateMunicipalityRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {DepartmentDtoMapper.class})
public interface MunicipalityDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "department", source = "departmentId", qualifiedByName = "departmentFromId")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Municipality toDomain(CreateMunicipalityRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Municipality toDomain(UpdateMunicipalityRequestDto dto);

    MunicipalityResponseDto toResponseDto(Municipality municipality);

    @Named("departmentFromId")
    default Department departmentFromId(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return Department.builder().id(departmentId).build();
    }
}
