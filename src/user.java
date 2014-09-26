import java.net.*;
import java.io.*;

import java.util.Scanner;

public class user extends ServerFunctions {

	/* Informação da localização do servidor LS */
	static final String invocationerror = "Este programa deve ser invocado do seguinte modo: java user [-n LSname] [-p LSport]";
	static String lsname = "localhost";
	static int lsport = 58025;
	static Scanner scan = new Scanner(System.in);

	/* Objectos de conecção para comunicação UDP */
	static InetAddress address;
	static DatagramSocket LSSocket = null; // udp connections socket
	static DatagramPacket packet = null;

	/* Objectos de conecção para comunicação em TCP */
	static int ssport = -1;
	static String ssIP = "";
	static Socket sssocket = null;

	static int chances = 0;

	public static void main(String[] args) {

		/*
		 * Tratamento dos argumentos opcionais, nestes especifica-se o nome da
		 * máquina a que nos queremos ligar, ou o porto, ou ambos. Caso o número
		 * de argumentos seja impar ou superior a 4 o programa sai com uma
		 * mensagem de erro.
		 */

		if (args.length > 4 || (args.length % 2) == 1) {
			System.err.println(invocationerror);
			System.exit(1);
		}

		if (args.length == 2) {
			if (args[0].equals("-n")) { // Se a localização é especificada
				lsname = args[1];
			} else if (args[0].equals("-p")) { // Se o porto é especificado
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

		if (args.length == 4) {
			if (args[0].equals("-n") && args[2].equals("-p")) {
				lsname = args[1];
				try {
					lsport = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					System.err.println(numberFormat);
					System.exit(1);
				}
			} else {
				System.err.println(invocationerror);
				System.exit(1);
			}
		}

		/*
		 * Preparando a ligação ao servidor LS para o pedido de informação,
		 * dando mensagens de erro limpas caso dê para o torto, armando também o
		 * timer para dar timeout caso não receba a resposta a uma mensagem udp
		 * enviada
		 */
		try {
			address = InetAddress.getByName(lsname);
			LSSocket = new DatagramSocket();
			LSSocket.setSoTimeout(4000);

		} catch (UnknownHostException e) {
			System.err.println(unkownHost);
			System.exit(1);
		} catch (SocketException e) {
			System.err.println(unavailablePort);
			System.exit(1);
		}
		/*
		 * Envio da mensagem de RQT ao servidor LS e tratamento da recepção da
		 * resposta
		 */

		String[] splitResult = sendUDP(("RQT\n").getBytes(), address, lsport,
				LSSocket);
		chances = 0;
		String toEcho = new String();
		if (splitResult == null) {
			System.err.println("Não existem dados recebidos.");
			System.exit(1);
		} else if (splitResult[0].equals("AWT")) {
			toEcho = splitResult[1] + "\n";

			for (int i = 3; i < splitResult.length - 1; i++)
				toEcho += (i - 2) + ". " + splitResult[i] + "\n";
		} else {
			System.err.println(invalidmsg);
			System.exit(1);
		}
		System.out.println(toEcho);

		System.out.println("Escolha um tópico: ");
		int ntopic = scan.nextInt();
		while(ntopic<0){
			ntopic = scan.nextInt();
		}
		if(ntopic==0)
			return;
			

		/* Envio do pedido da localização do tópico desejado */
		String toSend = ("RQC " + ntopic + "\n");

		/* Tratamento da resposta ao pedido anterior */
		splitResult = sendUDP(toSend.getBytes(), address, lsport, LSSocket);
		chances = 0;

		if (splitResult[0].equals("AWC")) {
			ssport = Integer.parseInt(splitResult[3]);
			ssIP = splitResult[2];

		} else
			System.err.println(invalidmsg);
		if (ssport == -1 || ssIP.equals(""))
			System.err.println("Endereço do porto deve ser positivo.");
		/*
		 * Após tratar a mensagem AWC, ligar por TCP ao servidor SS para que se
		 * possa obter o ficheiro desejado
		 */
		try {
			sssocket = new Socket(ssIP, ssport);
			String topicName = splitResult[1];
			sendTCP(sssocket, "REQ " + topicName);

			/*
			 * Após enviar o pedido do tópico ao servidor de SS é necessário
			 * tratar a mensagem e guardar o ficheiro que foi enviado
			 */
			byte[] resultBuff = new byte[0];
			byte[] buff = new byte[133169152];
			int k = -1;
			/*
			 * Recebe da stream a mensagem toda em bytes
			 */
			BufferedInputStream in = new BufferedInputStream(sssocket
					.getInputStream());

			while ((k = in.read(buff, 0, buff.length)) > -1) {
				byte[] tbuff = new byte[resultBuff.length + k];
				System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length);
				System.arraycopy(buff, 0, tbuff, resultBuff.length, k);
				resultBuff = tbuff;
			}
			in.close();

			byte[] data = resultBuff;

			/*
			 * Vai colocar os bytes recebidos num objecto que permita fazer um
			 * pequenbo parse às primeiras partes da mensagem de modo a saber se
			 * está nos conformes
			 */
			Scanner scan = new Scanner(new ByteArrayInputStream(data));

			boolean validmsg = scan.next().equals("REP");
			boolean statusOK = scan.next().equals("ok");

			if (validmsg && statusOK) {
				int size = scan.nextInt();
				int offSet = ("REP ok " + size + " ").getBytes().length;

				byte[] toFile = new byte[size];
				int i;
				for (i = 0; i < toFile.length; i++)
					toFile[i] = resultBuff[i + offSet];

				DataOutputStream out = new DataOutputStream(
						new FileOutputStream("userFiles/" + topicName));

				out.write(toFile);
				out.close();

				System.out.println("Tamanho que o ficheiro deverá ter: " + size
						+ " bytes.");
				System.out.println("Tamanho que o ficheiro tem: " + i
						+ " bytes.");
				System.out.println("Ficheiro bem recebido? " + (size == i));

			} else
				System.out
						.println("Não foi possivel receber o ficheiro, por inexistência no servidor de conteúdos .");

			sssocket.close();

		} catch (UnknownHostException e) {
			System.err.println(unkownHost);
		} catch (IOException e) {
			System.err.println(ioerror);
		}
	}

	public static String[] sendUDP(byte[] buf, InetAddress address, int port,
			DatagramSocket Socket) {
		try {
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					address, port);

			Socket.send(packet);

			byte[] buffer = new byte[1024];
			packet = new DatagramPacket(buffer, buffer.length);

			LSSocket.receive(packet);

			String response = new String(packet.getData());

			return response.split(" |\n");

		} catch (SocketTimeoutException e) {
			if (chances < 5) {
				chances++;
				System.out.println("TimedOut trying again");
				sendUDP(buf, address, port, Socket);
			} else {
				System.err.println(timeOut);
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println(ioerror);
			System.exit(-1);
		}
		return null;
	}
}
