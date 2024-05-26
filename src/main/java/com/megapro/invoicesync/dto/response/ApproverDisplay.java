package com.megapro.invoicesync.dto.response;

import java.util.List;

import com.megapro.invoicesync.model.UserApp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApproverDisplay {
    private String role;
    private List<UserApp> approvers;
}
