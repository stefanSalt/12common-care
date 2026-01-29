package com.example.admin.dto.stats;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class IncomeTrendDto {
    /** yyyy-MM-dd */
    private String date;

    private BigDecimal amount;
}

