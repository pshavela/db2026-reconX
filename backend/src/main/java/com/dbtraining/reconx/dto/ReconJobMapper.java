package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.ReconJob;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReconJobMapper {
    ReconJobResponse toResponse(ReconJob reconJob);
}
