package br.com.sysdesc.http.server.vo;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiMethodVO {

    private Method method;

    private Object instance;
}
