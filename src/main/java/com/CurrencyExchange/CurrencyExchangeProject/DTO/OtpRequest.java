package com.CurrencyExchange.CurrencyExchangeProject.DTO;

import com.coinShiftProject.coinShiftProject.enums.OtpPurpose;
import lombok.Data;

@Data
public class OtpRequest {
    private String email;
    private OtpPurpose purpose;
}
