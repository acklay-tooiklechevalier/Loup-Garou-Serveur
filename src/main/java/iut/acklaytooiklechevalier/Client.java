package iut.acklaytooiklechevalier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Client {

	private static int nbClient = 0;
	private int num;
	//private final String id; // en vue de creation d'une BDD pour les stats
	private String pseudo;
	private Role role;
	private final BufferedReader in ;
	private final PrintWriter out;

	private Client votePerso;
	private boolean vivant ;
	private boolean aJouer ;

	public Client(BufferedReader in, PrintWriter out) {
		this.in = in ;
		this.out = out;
		//id = UUID.randomUUID().toString();
		num = ++nbClient;
		pseudo = "";
		try {
			if (in != null)
				pseudo = in.readLine();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		role = null;
		votePerso = null;
		vivant = true;
	}

	public String getPseudo() {
		return pseudo;
	}

	/*
	public String getId() {
		return id;
	}
	*/

	public void setNum(int num) {
		this.num = num;
	}

	public static void setNbClient(int nbClient) {
		Client.nbClient = nbClient;
	}

	public int getNum() {
		return num;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public BufferedReader getIn() {
		return in;
	}

	public PrintWriter getOut() {
		return out;
	}

	public boolean getVivant() {
		return vivant;
	}
	public void setVivant (boolean vivant){
		this.vivant = vivant ;
	}

	public boolean getAJouer() {
		return aJouer;
	}

	public void setVotePerso(Client votePerso) {
		this.votePerso = votePerso;
	}

	public Client getVotePerso() {
		return votePerso;
	}

	public void setaJouer(boolean aJouer) {
		this.aJouer = aJouer;
	}

	@Override
	public String toString() {
		return num + " " + pseudo;
	}
}
