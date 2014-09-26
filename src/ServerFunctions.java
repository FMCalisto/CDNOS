import java.io.*;
import java.net.*;

public class ServerFunctions {
	/* Mensagens de erro do cliente e dos servidores */
	protected static final String numberFormat = "O numero de porto deve conter só digitos";
	protected static final String unkownHost = "Não é possivel ligar ao \"host\" pedido.";
	protected static final String unavailablePort = "Não é possivel abrir o porto especificado.";
	protected static final String timeOut = "O tempo de conecção máximo expirou";
	protected static final String ioerror = "Erro de I/O.";
	protected static final String nomsg = "A mensagem está vazia";
	protected static final String fileerr = "O ficheiro especificado não existe";
	protected static final String invalidmsg = "A mensagem enviada não está nos conformes do protocolo especificado.";
	protected static final String invalidFileName = "Nome de ficheiro inválido.";
	protected static final String badmsg = "A mensagem não é válida.";

	public ServerFunctions(){}
	
	public static void sendTCP(Socket socket, String msg) {

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);

		} catch (IOException e) {
			System.err.println(ioerror);
		}

	}

}
