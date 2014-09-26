import java.net.*;
import java.io.*;

public class SS extends ServerFunctions {
	static final String invocationerror = "Este programa deve ser invocado do seguinte modo: java ss [-p SSport]";

	static ServerSocket SSSocket;

	public static void main(String[] args) {
		/*
		 * Por defeito é o porto onde deveremos abrir a porta que fica à escuta
		 */
		int ssport = 59000;
		boolean listening = true;
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
					ssport = Integer.parseInt(args[1]);
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
			SSSocket = new ServerSocket(ssport);
		} catch (IOException e) {
			System.err.println(unavailablePort);
		}
		while (listening) {
			Socket socket;
			try {
				socket = SSSocket.accept();
				if (socket.isConnected()) {
					SSThread thread = new SSThread(socket);
					thread.start();
				}
			} catch (IOException e) {
				System.err.println(ioerror);
			}
		}

	}

	public static byte[] readFile(String filename) throws IOException {
		try {
			File file = new File(System.getProperty("user.dir")
					+ "/storagefiles/" + filename);
			byte[] fileData = new byte[(int) file.length()];
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();

			System.out.println("Loaded the file.");
			return fileData;

		} catch (FileNotFoundException e) {
			System.err
					.println("O ficheiro correspondente ao tópico pedido não se encontra neste servidor de conteúdos.");
			return "nok".getBytes();
		}

	}

	public static class SSThread extends Thread {
		Socket socket;

		public SSThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {

				DataOutputStream out = new DataOutputStream(socket
						.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));

				String inputLine;
				String status = "";
				String requestedSubject = "";

				if ((inputLine = in.readLine()) != null) {
					String[] aux = inputLine.split(" ");
					if (aux[0].equals("REQ")) {
						requestedSubject = aux[1];
						String toEcho = requestedSubject + " "
								+ socket.getInetAddress().getHostAddress()
								+ socket.getPort();
						System.out.println(toEcho);
						String toSend = "REP ";
						byte[] tofile = readFile(requestedSubject);
						if (!(new String(tofile).equals("nok"))) {
							out.write((toSend + "ok " + tofile.length + " ")
									.getBytes());
							out.write(tofile);
							out.write("\n".getBytes());
							System.out.println("Ficheiro enviado.");
						} else {
							out.write((toSend + "nok\n").getBytes());
						}

						out.flush();
					}else {
						out.write(("REP nok\n").getBytes());
					}
				}

				socket.close();
				out.close();
				in.close();
			} catch (IOException e) {
				System.err.println("Erro de I/O.");
			}
			this.stop();
		}
	}

}
