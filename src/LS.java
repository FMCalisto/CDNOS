import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class LS extends ServerFunctions {

	static final String invocationerror = "Este programa deve ser invocado do seguinte modo: java LS [-p LSport]";

	/* Objectos para comunicação UDP */
	static DatagramSocket LSsocket = null;
	static String subject;
	static ArrayList<String> topics = new ArrayList<String>();
	static ArrayList<String> topicLoc = new ArrayList<String>();

	public static void main(String[] args) {
		/*
		 * Por defeito este é o numero do porto que fica aberto para escuta
		 */
		int lsport = 58025;

		/*
		 * Tratamento do argumento opcional, este especifica o numero do porto
		 * que desejamos abrir. Caso o programa seja invocado de modo inválido
		 * este sai com uma mensagem de erro.
		 */

		if (args.length > 2 || (args.length % 2) == 1) {
			System.err.println(invocationerror);
			System.exit(1);
		}

		if (args.length == 2) {
			if (args[0].equals("-p")) { // Se o porto é especificado
				try {
					lsport = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.err.println(numberFormat);
					System.exit(1);
				}
			} else {
				System.err.println(invocationerror);
				System.exit(1);
			}
		}

		try {
			LSsocket = new DatagramSocket(lsport);
		} catch (SocketException e) {
			System.err.println(unavailablePort);
			System.exit(1);
		}

		boolean listening = true;
		try {
			readFile();
		} catch (IOException e) {
			System.err.println(ioerror);
		}
		while (listening) {
			thread();
		}
		LSsocket.close();
	}

	public static void thread() {

		try {
			byte[] buf = new byte[256];

			// recebe pedidos
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			LSsocket.receive(packet);

			// trata da resposta
			String response = new String(packet.getData());

			System.out.println("Received request: " + response + "From "
					+ packet.getAddress().getHostAddress() + ":"
					+ packet.getPort());

			/*
			 * Fazer um split e decidir o que fazer consoante a primeira parte
			 * da resposta
			 */

			String[] aux = response.split(" |\n");

			/* Identificação do pedido e tratamento deste. */
			if (aux.length == 0) {
				System.err.println(nomsg);
			} else if (aux[0].equals("RQT")) {
				response = "AWT" + " " + subject + " " + topics.size();
				for (String s : topics)
					response += " " + s;
			}

			else if (aux[0].equals("RQC")) {
				int i = Integer.parseInt(aux[1]);
				response = "AWC " + topics.get(i - 1) + " "
						+ topicLoc.get(i - 1);
			}

			else {
				System.out.println(invalidmsg);
				return;
			}

			response += "\n";
			buf = response.getBytes();

			// send the response to the client at "address" and "port"
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			packet = new DatagramPacket(buf, buf.length, address, port);
			LSsocket.send(packet);

		} catch (IOException e) {
			System.err.println(ioerror);
		}
	}

	public static void readFile() throws IOException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					"listingfiles/LSInfo"));
			String aux;

			subject = in.readLine();

			for (aux = in.readLine(); aux != null; aux = in.readLine()) {
				String[] aux2 = aux.split(" ");
				String topicaux = "";
				for (int i = 0; i < aux2.length - 2; i++) {
					topicaux += aux2[i];
				}

				topics.add(topicaux);

				topicLoc.add(aux2[aux2.length - 2] + " "
						+ aux2[aux2.length - 1]);

			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(fileerr);
			System.exit(1);
		}

	}
}
