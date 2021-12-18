package iut.acklaytooiklechevalier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Serveur {
	public static ArrayList<Client> client = new ArrayList<>();
	private final Scanner sc = new Scanner(System.in);

	private static final int nbPlayerRequired = 8;
	/*
	 TODO : si le temps le permet
	 private static final int minPlayer = 6;
	 private static final int maxPlayer = 15;
	*/
	private final ServerSocket serveurSocket;

	private Metier metier;
	private static Serveur instance;

	public Serveur() throws IOException {
		instance = this;
		Socket clientSocket;

		BufferedReader in;
		PrintWriter out;

		serveurSocket = new ServerSocket(6000);
		envoi();

		while (!serveurSocket.isClosed()) {
			clientSocket = serveurSocket.accept();
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());

			Client cli = new Client(in, out);
			
			System.out.println("[+] " + cli);
			for (Client c : client) {
				PrintWriter o = c.getOut();
				o.println("[+] " + cli);
				o.flush();
			}
			client.add(cli);
			recevoir(cli);
			if ( client.size() == nbPlayerRequired ){
				Timer temp = new Timer();
				temp.schedule(new TimerTask() {
					@Override
					public void run() {
						metier = new Metier();
					}
				}, 1000L);
			}
		}
		serveurSocket.close();
	} 

	public void renvoi(String msg, Client outClient) {
		PrintWriter out;

		if (metier == null) {
			for (Client client : client) {
				out = client.getOut();
				out.println(outClient + " : " + msg);
				out.flush();
			}
		} else {
			if (metier.getState() == 1 && outClient.getVivant()) {
				for (Client client : client) {
					out = client.getOut();
					out.println(outClient + " : " + msg);
					out.flush();
				}
			} else if (!outClient.getVivant()) {
				for (Client client : client) {
					if (!client.getVivant()) {
						out = client.getOut();
						out.println(outClient + " : " + msg);
						out.flush();
					}
				}
			} else {
				if (metier.getState() == 2) { // tour des loup
					for (Client client : client) {
						if (outClient.getRole() == Role.LOUP_GAROU && client.getRole() == Role.LOUP_GAROU) {
							out = client.getOut();
							out.println(outClient + " : " + msg);
							out.flush();
						} else if (client == outClient){
							out = client.getOut();
							out.println("Vous ne pouvez pas parler en " + (client.getRole()==Role.VILLAGEOIS?"dormant !" : "lisant vos grimoire"));
							out.flush();
						}
					}
				} else {
					for (Client client : client) {
						if (outClient.getRole() == Role.SORCIERE && client.getRole() == Role.SORCIERE) {
							out = client.getOut();
							out.println(outClient + " : " + msg);
							out.flush();
						} else if (client == outClient) {
							out = client.getOut();
							out.println("Vous ne pouvez pas parler en " + (client.getRole()==Role.VILLAGEOIS?"dormant !" : "après avoir bien mangé"));
							out.flush();
						}
					}
				}
			}
		}
	}

	public void envoi(String msg, Client client) {
		PrintWriter out;

		out = client.getOut();
		out.println(msg);
		out.flush();
	}

	private void envoi() {
		Thread envoi = new Thread(new Runnable() {
			String msg;
			PrintWriter out;

			@Override
			public void run() {
				while (!serveurSocket.isClosed()) {
					msg = sc.nextLine();
					if (!msg.equals(""))
						for (Client client : client) {
							out = client.getOut();
							out.println("Serveur : " + msg);
							out.flush();
						}
				}
			}
		});
		envoi.start();
	}

	private void recevoir(Client cli) {
		Thread recevoir = new Thread(new Runnable() {
			String msg;

			@Override
			public void run() {
				try {
					msg = cli.getIn().readLine();
					// tant que le client est connecté
					while (msg != null) {
						System.out.println(cli + " : " + msg);
						if (metier != null) {
							if(!metier.receptionInformation(msg, cli))
								renvoi(msg, cli);
						} else {
							renvoi(msg, cli);
						}
						msg = cli.getIn().readLine();
					}
					// sortir de la boucle si le client a déconecté
					if (metier != null) {
						for (Client c : metier.getClientEnJeu()) {
							PrintWriter o = c.getOut();
							o.println("[-] " + cli);
							o.flush();
						}
						metier.removeClient(cli);
					}
					else {
						for (Client c : client) {
							PrintWriter o = c.getOut();
							o.println("[-] " + cli);
							o.flush();
						}
						client.remove(cli);
					}
					System.out.println("[-] " + cli);
					// fermer le flux et la session socket
					cli.getOut().close();
					cli.getIn().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		recevoir.start();
	}

	public ArrayList<Client> getClient() {
		return client;
	}

	public static Serveur getInstance() {
		return instance;
	}

	public static void main(String[] test) throws IOException {
		new Serveur();
	}
}