
package com.dbtraining.reconx.dto;

import java.time.LocalDate;

public record VolumeResponse(LocalDate tradeDate, Long volume) {}