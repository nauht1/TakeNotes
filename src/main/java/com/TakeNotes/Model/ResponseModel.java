package com.TakeNotes.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseModel {
    private boolean status;
    private String message;
    private Object body;
}
