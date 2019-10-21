package br.com.sysdesc.http.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import br.com.sysdesc.http.server.anotation.RequestMethod;
import br.com.sysdesc.http.server.anotation.RestController;
import br.com.sysdesc.http.server.enumeradores.HttpMethod;
import br.com.sysdesc.http.server.exception.ApiInvocationException;
import br.com.sysdesc.http.server.vo.ApiMethodVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApisController {

	private static ApisController instance;

	private final String[] apiPackages;

	private Map<HttpMethod, Map<String, ApiMethodVO>> apis = new HashMap<>();

	private ApisController(String... apiPackages) {
		this.apiPackages = apiPackages;

		scanAllPackages();
	}

	private void scanAllPackages() {

		for (String pacote : apiPackages) {
			scanPackage(pacote);
		}
	}

	private void scanPackage(String pacote) {

		Reflections reflections = new Reflections(pacote);

		Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);

		for (Class<?> controller : controllers) {

			try {

				RestController restController = controller.getAnnotation(RestController.class);

				Object objeto = controller.newInstance();

				Method[] methods = controller.getDeclaredMethods();

				for (Method method : methods) {

					getMethod(method, objeto, restController);

				}

			} catch (InstantiationException | IllegalAccessException e) {
				log.error("erro ao instanciar classe Controller", e);
			}
		}
	}

	private void getMethod(Method method, Object objeto, RestController restController) {

		if (method.isAnnotationPresent(RequestMethod.class)) {

			RequestMethod requestMethod = method.getAnnotation(RequestMethod.class);

			putMethod(new ApiMethodVO(method, objeto), requestMethod, restController);
		}

	}

	private void putMethod(ApiMethodVO apiMethodVO, RequestMethod requestMethod, RestController restController) {

		if (!apis.containsKey(requestMethod.method())) {
			apis.put(requestMethod.method(), new HashMap<>());
		}

		Map<String, ApiMethodVO> endpoint = apis.get(requestMethod.method());

		String url = restController.path() + requestMethod.path();

		log.info("encontrada url: " + url);

		endpoint.put(url.toLowerCase(), apiMethodVO);
	}

	public Map<HttpMethod, Map<String, ApiMethodVO>> getApis() {
		return apis;
	}

	public void setApis(Map<HttpMethod, Map<String, ApiMethodVO>> apis) {
		this.apis = apis;
	}

	public static ApisController getInstance() {

		if (instance == null) {

			throw new ApiInvocationException("Apis controller n�o foi inicializado");
		}

		return instance;
	}

	public static void initialize(String... apiPackages) {

		if (instance != null) {

			throw new ApiInvocationException("Apis controller j� foi inicializado");
		}

		instance = new ApisController(apiPackages);
	}

}
