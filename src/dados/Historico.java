/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dados;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dani
 */
public class Historico {

	static String chaveencriptacao = "0123456789abcdef";
	static Logger logger = Logger.getLogger(Historico.class.getName());

	public static void gravarChat(String texto) throws IOException {

		FileWriter fw = new FileWriter("src\\chat.txt", true);
		// FileWriter fw = new FileWriter("src\\historico.txt", true);

		try (BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(texto);
			bw.flush();
		} catch (Exception e) {
			logger.log(Level.ALL, "Erro ao gravar txt. ", e);
		}
	}

	public static void gravarAQV(String texto) throws IOException {

		FileWriter fw = new FileWriter("src\\bytes.txt");

		try (BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(texto);
			bw.flush();
		} catch (Exception e) {
			logger.log(Level.ALL, "Erro ao gravar arquivo. ", e);
		}
	}

}
