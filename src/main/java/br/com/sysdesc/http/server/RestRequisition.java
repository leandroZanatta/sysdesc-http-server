package br.com.sysdesc.http.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import br.com.sysdesc.http.server.enumeradores.HttpMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestRequisition extends Thread {

	private Socket socket;

	public RestRequisition(Socket socket) {

		this.socket = socket;
	}

	@Override
	public void run() {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream())) {

			String input = in.readLine();

			StringTokenizer parse = new StringTokenizer(input);

			HttpMethod method = HttpMethod.valueOf(parse.nextToken().toUpperCase());

			String urlParams[] = parse.nextToken().toLowerCase().split("\\?");

			String fileRequested = urlParams[0];

			String headerLine = null;

			Map<String, String> headers = new HashMap<>();

			Map<String, String> queryParams = new HashMap<>();

			if (urlParams.length > 1) {

				String[] arrayParam = urlParams[1].split("&");

				for (String itemArray : arrayParam) {

					String[] query = itemArray.split("=");

					if (query.length == 2) {
						queryParams.put(query[0], query[1]);
					}
				}

			}

			while ((headerLine = in.readLine()).length() != 0) {

				String[] params = headerLine.split(":");

				if (params.length == 2) {
					headers.put(params[0], params[1]);
				}
			}

			StringBuilder payload = new StringBuilder();

			while (in.ready()) {
				payload.append((char) in.read());
			}

			log.info(String.format("Requisição recebida: Método: %s - Path: %s ", method, fileRequested));

			Request request = new Request(method, fileRequested, headers, queryParams, payload.toString(), printWriter,
					bufferedOutputStream);

			request.process();

		} catch (IOException ioe) {

			log.error("Server error ", ioe);
		}

	}

}
