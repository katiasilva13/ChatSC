package dados;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dani
 * @author eriks
 * @author ktia-
 */
public class Transferencia {
	static String chaveencriptacao = "0123456789abcdef";
	static Logger logger = Logger.getLogger(Transferencia.class.getName());

	public static String enviarAQV(File arquivo) throws Exception {

		byte[] bytes = Files.readAllBytes(arquivo.toPath());
		String conversao = new String(Base64.getEncoder().encodeToString(bytes));

		return conversao;
	}

	public static byte[] receberAQV(String criptografado, String nomeArquivo) throws Exception {

		byte[] descrip = Base64.getMimeDecoder()
				.decode(criptografado.trim().replace("=", "").getBytes(StandardCharsets.UTF_8));

		try (FileOutputStream fos = new FileOutputStream(
				Paths.get("").toAbsolutePath().toString() + "\\Downloads\\" + nomeArquivo)) {

			fos.write(descrip);
			fos.flush();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Erro ao receber arquivo. ", e);
		}
		return descrip;

	}

	public static boolean checkAQV(String texto) {
		if (texto.startsWith(":{"))
			return true;

		return false;
	}

	private static String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return null;
	}

	public static String getNameFile(File file, String texto) {
		String nomeArquivo;
		if (texto.contains("]: ")) {
			if (null != Transferencia.getFileExtension(file) && texto.contains("]: ")) {
				nomeArquivo = file.getName();
				nomeArquivo = texto.substring(texto.indexOf(']') + 1);
				nomeArquivo = nomeArquivo.replace(": ", "");
				return nomeArquivo;
			}
		}
		return null;
	}

}
