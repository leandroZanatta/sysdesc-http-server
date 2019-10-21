package br.com.sysdesc.http.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.sysdesc.http.server.anotation.RequestBody;
import br.com.sysdesc.http.server.anotation.RequestParam;
import br.com.sysdesc.http.server.enumeradores.HttpMethod;
import br.com.sysdesc.http.server.vo.ApiMethodVO;
import br.com.sysdesc.http.server.vo.InternalErrorVO;
import br.com.sysdesc.http.server.vo.NotFoundVO;
import br.com.sysdesc.http.server.vo.OKOptionsVO;
import br.com.sysdesc.util.classes.ListUtil;
import br.com.sysdesc.util.classes.StringUtil;

public class Request {

	private HttpMethod method;
	private String fileRequested;
	private Map<String, String> headers;
	private Map<String, String> queryParams;
	private String payload;
	private PrintWriter printWriter;
	private BufferedOutputStream bufferedOutputStream;

	public Request(HttpMethod method, String fileRequested, Map<String, String> headers,
			Map<String, String> queryParams, String payload, PrintWriter printWriter,
			BufferedOutputStream bufferedOutputStream) {
		this.method = method;
		this.fileRequested = fileRequested;
		this.headers = headers;
		this.queryParams = queryParams;
		this.payload = payload;
		this.printWriter = printWriter;
		this.bufferedOutputStream = bufferedOutputStream;
	}

	public void process() throws IOException {

		if (this.method.equals(HttpMethod.OPTIONS)) {

			OKOptionsVO okOptionsVO = new OKOptionsVO();
			okOptionsVO.setPath(this.fileRequested);

			retornarOk(okOptionsVO);

			return;
		}

		Optional<ApiMethodVO> metodo = this.findMethod();

		if (metodo.isPresent()) {

			try {

				ApiMethodVO apiMethodVO = metodo.get();

				Parameter[] parametros = apiMethodVO.getMethod().getParameters();

				List<Object> valores = new ArrayList<>();

				for (Parameter parametro : parametros) {

					if (parametro.isAnnotationPresent(RequestBody.class)) {

						valores.add(getParameter(parametro));
					} else if (parametro.isAnnotationPresent(RequestParam.class)) {

						valores.add(getQueryParameter(parametro));
					}
				}

				retornarOk(apiMethodVO.getMethod().invoke(apiMethodVO.getInstance(),
						ListUtil.toArray(valores, Object.class)));

			} catch (Exception e) {

				retornarInternalError(e);
			}

			return;
		}

		retornarNotFound();
	}

	private Object getQueryParameter(Parameter parametro) {

		RequestParam param = parametro.getAnnotation(RequestParam.class);

		String queryParam = this.queryParams.get(param.value().toLowerCase());

		if (!StringUtil.isNullOrEmpty(queryParam)) {
			return parametro.getType().cast(queryParam);
		}

		return null;
	}

	private Object getParameter(Parameter parametro) {

		if (parametro.getType().equals(List.class)) {
			Class<?> type = (Class<?>) ((ParameterizedType) parametro.getAnnotatedType().getType())
					.getActualTypeArguments()[0];

			return new Gson().fromJson(this.payload, TypeToken.getParameterized(List.class, type).getType());
		}

		return new Gson().fromJson(this.payload, parametro.getType());
	}

	private void retornarInternalError(Exception e) throws IOException {

		InternalErrorVO objError = new InternalErrorVO();
		objError.setPath(this.fileRequested);
		objError.setMessage(e.getMessage());

		retornoResponse(500, objError, "Internal Server Error");
	}

	private void retornarOk(Object object) throws IOException {

		retornoResponse(200, object, "Ok");
	}

	private void retornarNotFound() throws IOException {

		NotFoundVO notFoundVO = new NotFoundVO();
		notFoundVO.setPath(this.fileRequested);

		retornoResponse(404, notFoundVO, "Not Found");
	}

	private void retornoResponse(Integer code, Object response, String serverStatus) throws IOException {

		String objJson = new Gson().toJson(response);

		printWriter.println(String.format("HTTP/1.1 %d %s", code, serverStatus));
		printWriter.println("Server: Java HTTP Server");
		printWriter.println("Date: " + new Date());
		printWriter.println("Content-type: " + "application/json");
		printWriter.println("Content-length: " + objJson.length());
		printWriter.println("Access-Control-Allow-Origin: *");
		printWriter.println("Access-Control-Allow-Headers: *");
		printWriter.println();

		bufferedOutputStream.write(objJson.getBytes());

		printWriter.flush();
		bufferedOutputStream.flush();

	}

	private Optional<ApiMethodVO> findMethod() {

		if (ApisController.getInstance().getApis().containsKey(this.method)) {
			return Optional.ofNullable(ApisController.getInstance().getApis().get(this.method).get(this.fileRequested));
		}

		return Optional.empty();
	}

}
