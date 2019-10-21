package br.com.sysdesc.http.server.vo;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InternalErrorVO {

    private Date timestamp = new Date();
    private Integer status = 500;
    private String error = "Internal Error";
    private String message = "";
    private String path = "/";
}
