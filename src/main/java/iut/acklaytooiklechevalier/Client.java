package iut.acklaytooiklechevalier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class Client {

	private static int nbClient = 0;
	private final int num;
	private final String id; // en vue de creation d'une BDD pour les stats
	private String pseudo;
	private Role role;
	private final BufferedReader in ;
	private final PrintWriter out;

	private boolean vivant ;
	private boolean aJouer ;

	public Client(BufferedReader in, PrintWriter out) {
		this.in = in ;
		this.out = out;
		id = UUID.randomUUID().toString();
		num = ++nbClient;
		pseudo = "";
		try {
			if (in != null)
				pseudo = in.readLine();
		} catch (IOException e) { 
			e.printStackTrace();
		}
		role = null;
		//vivant = true;
	}

	public String getPseudo() {
		return pseudo;
	}

	public String getId() {
		return id;
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

	public void setaJouer(boolean aJouer) {
		this.aJouer = aJouer;
	}
}
