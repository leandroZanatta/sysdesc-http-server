package br.com.sysdesc.http.server.vo;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OKOptionsVO {

    private Date timestamp = new Date();
    private Integer status = 200;
    private String error = "Ok";
    private String message = "";
    private String path = "/";
}
