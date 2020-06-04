package chat;

import static dados.CriptoAES.decrypt;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import dados.Transferencia;

@SuppressWarnings("serial")
public class Cliente extends JFrame {

	JTextField textoParaEnviar;
	Socket socket;
	PrintWriter escritor;
	String nome;
	JTextArea textoRecebido;
	Scanner leitor;
	FileWriter fw;
	byte[] textoencriptado;
	static String chaveencriptacao = "0123456789abcdef";
	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	Date date = new Date();
	String nomeArquivo;
	String nomeMensagem;

	static Logger logger = Logger.getLogger(Cliente.class.getName());

	private class EscutaServidor implements Runnable {

		@Override
		public void run() {
			try {
				String texto;
				while ((texto = leitor.nextLine()) != null) {

					String mensagem = null, temp = null;

					if (Transferencia.checkAQV(texto)) {
						mensagem = new String(texto.replace(":{", "").getBytes(StandardCharsets.UTF_8));

						temp = new String(texto.getBytes(StandardCharsets.UTF_8));
						mensagem = mensagem + temp;
						mensagem = mensagem.replace("}:", "");

						if (!nome.equalsIgnoreCase(nomeMensagem)) {
							Transferencia.receberAQV(mensagem, nomeArquivo.replaceAll(" ", ""));

							int env = 1;
							env = JOptionPane.showConfirmDialog(null, "Deseja abrir o arquivo?");
							if (0 == env) {
								URI link = new URI(Paths.get("").toAbsolutePath().toString().replace("\\", "//")
										+ "//Downloads//" + nomeArquivo.replaceAll(" ", ""));

								Desktop.getDesktop().browse(link);
								nomeArquivo = "";
							}
						}

					} else {
						mensagem = decrypt(texto, chaveencriptacao);
						textoRecebido.append(mensagem + "\n");
						File file = new File(mensagem);
						nomeArquivo = Transferencia.getNameFile(file, mensagem);
						nomeMensagem = getNomeMensagem(mensagem);

					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Erro ao ler texto", e);
			}
		}

	}

	public Cliente(String nome) throws IOException {
		super("ChatSC - Usuário: " + nome);

		this.nome = nome;

		Font fonte = new Font("Serif", Font.PLAIN, 20);

		textoParaEnviar = new JTextField();
		textoParaEnviar.setFont(fonte);
		textoParaEnviar.addKeyListener(new EnviarKeyListener());

		JButton botao = new JButton("Enviar");
		botao.setFont(fonte);
		botao.addActionListener(new EnviarListener());

		JButton botaoArquivo = new JButton("Arquivo");
		botaoArquivo.setFont(fonte);
		botaoArquivo.addActionListener(new EnviarArquivo());

		Container envio = new JPanel();
		envio.setLayout(new BorderLayout());
		envio.add(BorderLayout.CENTER, textoParaEnviar);
		envio.add(BorderLayout.EAST, botao);
		envio.add(BorderLayout.WEST, botaoArquivo);

		textoRecebido = new JTextArea();
		textoRecebido.setFont(fonte);
		textoRecebido.setEditable(false);

		JScrollPane scroll = new JScrollPane(textoRecebido);

		String path = "src\\chat.txt";
		// String path = "src\\historico.txt";
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String linha = "", mensagem = "";
			while ((linha = br.readLine()) != null) {
				mensagem = decrypt(linha, chaveencriptacao);
				textoRecebido.append(mensagem + "\n");
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Erro ao recuperar conversa", e);
		}

		getContentPane().add(BorderLayout.CENTER, scroll);
		getContentPane().add(BorderLayout.SOUTH, envio);
		configurarRede();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 500);
		setVisible(true);

	}

	private class EnviarListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			escritor.println(nome + " [" + formatter.format(date) + "]: " + textoParaEnviar.getText());
			escritor.flush();
			textoParaEnviar.setText(" ");
			textoParaEnviar.requestFocus();
		}

	}

	private class EnviarArquivo implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser arquivo = new JFileChooser();
			arquivo.setDialogTitle("Procurar Arquivo");
			arquivo.setFileSelectionMode(JFileChooser.FILES_ONLY);

			FileNameExtensionFilter filter = new FileNameExtensionFilter("Imagem", "jpg", "png", "Arquivo", "txt",
					"pdf", "csv", "mp3", "mp4", "rar", "zip");

			arquivo.setFileFilter(filter);
			int retorno = arquivo.showOpenDialog(null);

			if (retorno == JFileChooser.APPROVE_OPTION) {
				File file = arquivo.getSelectedFile();
				try {
					String cripto = new String(Transferencia.enviarAQV(file));
					JOptionPane.showMessageDialog(rootPane, "Arquivo enviado com sucesso");
					escritor.println(nome + " [" + formatter.format(date) + "]: " + file.getName());
					escritor.println(":{" + cripto + "}:");
					escritor.flush();
					textoParaEnviar.setText(" ");
					textoParaEnviar.requestFocus();

				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Erro ao enviar arquivo.", ex);
				}

			}
		}
	}

	private class EnviarKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				escritor.println(nome + " [" + formatter.format(date) + "]: " + textoParaEnviar.getText());
				escritor.flush();
				textoParaEnviar.setText(" ");
				textoParaEnviar.requestFocus();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

	}

	private void configurarRede() {
		try {
			String gateway = "";
			Servidor.GetGateway(gateway);
			socket = new Socket(gateway, 5000);
			escritor = new PrintWriter(socket.getOutputStream());
			leitor = new Scanner(socket.getInputStream());
			new Thread(new EscutaServidor()).start();

		} catch (IOException e) {
			logger.log(Level.SEVERE, null, e);
		}
	}

	public static String getNomeMensagem(String nome) {
		nome = nome.substring(0, nome.indexOf(']') + 1);
		return nome;
	}

	public static String getNome(String nome) {
		int temp = 1;
		try {
			nome = JOptionPane.showInputDialog(null, "Digite seu nome:");

			if (nome == null || nome.trim().equalsIgnoreCase("") || nome.isEmpty()) {
				temp = JOptionPane.showConfirmDialog(null, "Ops! Nenhum nome foi digitado. Deseja sair?");

				if (0 < temp) {
					getNome(nome);
				} else {
					System.exit(0);
				}
			}
		} catch (NullPointerException e) {
			logger.log(Level.INFO, "Erro em getNome()" + temp, e);
		}
		return nome;
	}

	public static void main(String[] args) throws IOException {
		String nome = "";
		try {
			nome = getNome(nome);
			if (!nome.isEmpty()) {
				new Cliente(nome.trim());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, nome, e);
		}

	}

}
