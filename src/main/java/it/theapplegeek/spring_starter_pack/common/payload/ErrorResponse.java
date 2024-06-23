package it.theapplegeek.spring_starter_pack.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private String path;
    private String error;
    private String message;
    private int status;
}
