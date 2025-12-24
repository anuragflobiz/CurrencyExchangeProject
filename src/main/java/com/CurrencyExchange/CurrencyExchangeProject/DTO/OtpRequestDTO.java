package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import com.coinShiftProject.coinShiftProject.enums.OtpPurpose;
import lombok.Data;

@Data
public class OtpRequestDTO {
    private String email;
    private OtpPurpose purpose;
}
