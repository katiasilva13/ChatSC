package chat;

import static dados.CriptoAES.encrypt;
import static dados.Historico.gravarAQV;
import static dados.Historico.gravarChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {

	static String chaveencriptacao = "0123456789abcdef";

	List<PrintWriter> escritores = new ArrayList<>();

	static Logger logger = Logger.getLogger(Servidor.class.getName());

	public Servidor() {

		ServerSocket server;
		try {
			server = new ServerSocket(5000);

			while (true) {

				Socket socket = server.accept();
				new Thread(new EscutaCliente(socket)).start();
				PrintWriter p = new PrintWriter(socket.getOutputStream());
				escritores.add(p);

			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Erro de socket", e);
		}

	}

	private void encaminhaParaTodos(String msg) {
		for (PrintWriter w : escritores) {
			try {
				w.println(msg);
				w.flush();

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Erro ao encaminhar mensagem", e);
			}

		}
	}

	private class EscutaCliente implements Runnable {
		Scanner leitor;

		public EscutaCliente(Socket socket) {
			try {
				leitor = new Scanner(socket.getInputStream());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Erro ao receber mensagem", e);
			}
		}

		@Override
		public void run() throws NoSuchElementException, NullPointerException {
			String texto;
			try {
				while ((texto = leitor.nextLine()) != null) {

					if (texto.contains("]: ")) {
						// encaminhaParaTodos(texto);
						String mensagem = encrypt(texto, chaveencriptacao);
						gravarChat(mensagem + "\n");
						encaminhaParaTodos(mensagem);
					} else {
						encaminhaParaTodos(texto);
						gravarAQV(texto);
					}

				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Erro ao ler/salvar mensagem. ", e);
			}
		}

	}

	public static String GetGateway(String gateway) throws IOException {

		Process result = Runtime.getRuntime().exec("netstat -rn");

		BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));

		String line = br.readLine();

		while (line != null) {

			if (line.trim().startsWith("default") == true || line.trim().startsWith("0.0.0.0") == true)
				break;
			line = br.readLine();
		}
		if (line == null) {

		}
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken();
		st.nextToken();
		gateway = st.nextToken();

		return gateway;
	}

	public static void main(String[] args) {
		new Servidor();
	}
}
