package com.dbtraining.reconx.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.dbtraining.reconx.repository.entity.ReconJob;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReconJobMapper {
    ReconJobResponse toResponse(ReconJob reconJob);

    @Mapping(target = "startedAt", source = "startAt")
    ReconJobSummary toSummary(ReconJob reconJob);
}
