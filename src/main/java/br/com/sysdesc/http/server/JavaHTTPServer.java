package br.com.sysdesc.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaHTTPServer extends Thread {

	private ExecutorService executorService = Executors.newCachedThreadPool();

	private final Integer port;
	private final String[] apiPackages;

	public JavaHTTPServer(Integer port, String... apiPackages) {
		this.port = port;
		this.apiPackages = apiPackages;
	}

	@Override
	public void run() {

		ApisController.initialize(apiPackages);

		log.info("Inicializando o Servidor http na porta: " + this.port);

		try (ServerSocket serverConnect = new ServerSocket(port);) {

			while (true) {

				executorService.submit(new RestRequisition(serverConnect.accept()));
			}

		} catch (IOException e) {

			log.error("Não foi possivel inicializar o servidor", e);
		}
	}

}
