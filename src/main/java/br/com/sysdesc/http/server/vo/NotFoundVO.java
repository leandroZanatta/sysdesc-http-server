package br.com.sysdesc.http.server.vo;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotFoundVO {

    private Date timestamp = new Date();
    private Integer status = 404;
    private String error = "Not Found";
    private String message = "No message available";
    private String path = "/";
}
