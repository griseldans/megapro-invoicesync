package com.megapro.invoicesync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangePasswordRequestDTO {
    private String oldPassword;
    private String newPassword;
}
