package com.future.tcfm.model.ReqResModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    private String email;
    private String file;
    private Integer period;
    private Boolean multipart;
}
