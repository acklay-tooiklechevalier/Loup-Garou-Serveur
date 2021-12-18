package iut.acklaytooiklechevalier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Metier {
	/**
	 * state<br>
	 * value : 0 = partie non start<br>
	 * value : 1 = day<br>
	 * value : 2 = Phase Loup<br>
	 * value : 3 = phase sorcière<br>
	 */
	private int state = 0;

	private final ArrayList<Client> clientEnJeu;
	private final ArrayList<Client> vivant ;

	//nuitLoup
	private int nbLoupMax = 0;
	private final int nbVoteLoup = 0 ;
	private final ArrayList<Client> voteLoup ;

	private final int nbVoteVillage = 0 ;
	private final ArrayList<Client> voteVillage ;

	private Client leMortDesLoup ;
	private boolean isPotionKillAvailable;
	private boolean isPotionAliveAvailable;

	public Metier(){
		voteLoup = new ArrayList<>();
		voteVillage = new ArrayList<>();
		vivant = new ArrayList<>();
		clientEnJeu = new ArrayList<>();
		vivant.addAll(Serveur.getInstance().getClient());
		clientEnJeu.addAll(Serveur.getInstance().getClient());
		initialisation();
	}

	public void initialisation() {
		int nbSorcier = 1;
		int nbLoup = nbLoupMax = (int) ((Serveur.getInstance().getClient().size() - nbSorcier) * 0.30);
		int nbVillageois = Serveur.getInstance().getClient().size() - nbLoup - nbSorcier;

		int alea;
		boolean attributionReussi;

		for (Client client : Serveur.getInstance().getClient()) {
			do {
				alea = (int) (Math.random() * 3) +1;
				attributionReussi = false;
				switch (alea) {
					case 1:
					if (nbSorcier > 0) {
						client.setRole(Role.SORCIERE);
						Serveur.getInstance().envoi("Votre rôle est : " + client.getRole(), client);
						isPotionKillAvailable = true;
						isPotionAliveAvailable = true;
						nbSorcier--;
						attributionReussi = true;
					}
					break;
					case 2:
					if (nbLoup > 0) {
						client.setRole(Role.LOUP_GAROU);
						Serveur.getInstance().envoi("Votre rôle est : " + client.getRole(), client);
						nbLoup--;
						attributionReussi = true;
					}
					break;
					case 3:
					if (nbVillageois > 0) {
						client.setRole(Role.VILLAGEOIS);
						Serveur.getInstance().envoi("Votre rôle est : " + client.getRole(), client);
						nbVillageois--;
						attributionReussi = true;
					}
					break;
				}
			} while (!attributionReussi);
		}
		for (Client c : Serveur.getInstance().getClient()) {
			Serveur.getInstance().envoi("La nuit tombe, tous le monde par dormir !", c);
		}
		//day = false;
		state = 2;
	}

	public boolean receptionInformation(String info, Client client) {
		// commande possible :kill,vote,chat,alive,rien
		// format attendu >[Action] [Num Joueur]
		if (vivant.contains(client)) {
			String[] commande = info.split(" ");
			if (commande[0].charAt(0) == '/'){
				switch (commande[0].toLowerCase().charAt(1)) {
					case 'k': // kill
						if (client.getRole() == Role.LOUP_GAROU && !client.getAJouer() && state == 2) {
							if (laNuitDesPutainDeLoupGarou(commande[1], client)) {
								if (state == 2)
									client.setaJouer(true);
								Serveur.getInstance().envoi("Vous avez voté pour dévorer " + client.getVotePerso() + " !", client);
								finVoteLoup();
								return true;
							}
						}

						if (client.getRole() == Role.SORCIERE && !client.getAJouer() && state == 3) {
							if(isPotionKillAvailable && leMeurtreDeLaSorciere(commande[1], client)) {
								if (state == 3)
									client.setaJouer(true);
								return true;
							} else if (!isPotionKillAvailable){
								Serveur.getInstance().envoi("Vous avez déjà utilisé votre potion mortel !", client);
								return true;
							}
						}
						return false;
					case 'v': //vote
						if (state == 1) {
							if(!client.getAJouer() && vote(commande[1], client)){
								client.setaJouer(true);
								Serveur.getInstance().envoi("Vous avez voté pour " + client.getVotePerso() + " !", client);
								finVoteVillage();
								return true;
							}
							else{
								Serveur.getInstance().envoi("Impossible de changer de voté, votre vote est pour " + client.getVotePerso() + " !", client);
								return true;
							}
						}
					case 'c': //chat

						break;
					case 'a': //alive
						if (client.getRole() == Role.SORCIERE && !client.getAJouer() && state == 3){
							if (isPotionAliveAvailable) {
								leSauvetageDeLaSorciere();
								if (state == 3)
									client.setaJouer(true);
							} else {
								Serveur.getInstance().envoi("Vous avez déjà utilisé votre potion de soin !", client);
							}
							return true;
						}
					case 'r': //alive
						if (client.getRole() == Role.SORCIERE && !client.getAJouer() && state == 3){
							estMort(leMortDesLoup, null);
							if (state == 3)
								client.setaJouer(true);
							return true;
						}
				}
			}
		}
		return false;
	}

	public void finVoteVillage() {
		if (voteVillage.size() == vivant.size()) {
			Client leMort = setLeMort(voteVillage);

			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
			//leMort
			estMort(leMort, null);
			/*
			for (Client client : vivant) {
				if (Objects.equals(leMort, client)) {
					estMort(client, null);
					return;
				}
			}
			*/
		}
	}

	private Client setLeMort(ArrayList<Client> vote) {
		/*
		 * nbVote<br><br>
		 *
		 * @String = Nom victime (Player)
		 * @Integer = Nombre total Vote pour la victime
		 */
		HashMap<Client, Integer> nbVote = new HashMap<>();

		for (Client c : vote) {
			if (!nbVote.containsKey(c)) {
				nbVote.put(c, 1);
			} else {
				nbVote.put(c, nbVote.get(c) + 1);
			}
		}

		return getLeMort(nbVote, vote);
	}

	private Client getLeMort(HashMap<Client, Integer> nbVote, ArrayList<Client> vote) {
		final Client[] leMort = {null};

		final Client[] cLast = {null};
		final Integer[] iLast = {null};
		final int[] voteSize = {nbVote.size()};
		nbVote.forEach((client, integer) -> {
			if (voteSize[0] == 1) {
				leMort[0] = client;
				return;
			}
			if (cLast[0] == null) {
				cLast[0] = client;
				iLast[0] = integer;
			} else if (iLast[0] > integer) {
				leMort[0] = cLast[0];
			} else if (iLast[0].equals(integer)) {
				if (cLast[0] == client) {
					leMort[0] = client;
				} else {
					voteSize[0]--;
					leMort[0] = null;
				}
			} else {
				leMort[0] = client;
			}
		});
		return leMort[0];
	}

	private void estMort(Client cli, Client cli2) {
		//dire a tout le monde son role et son nom
		for (Client c : Serveur.getInstance().getClient()) {
			if (c != cli && c != cli2)
				if (cli == null && state > 1)
					Serveur.getInstance().envoi("Aucun mort est à déplorer cette nuit !", c);
				else if (cli == null)
					Serveur.getInstance().envoi("Le village n'a pas su choisir qui exécuter en place public !", c);
				else if (cli2 == null)
					Serveur.getInstance().envoi("le joueur " + cli + " est mort , c'etait un(e) " + cli.getRole(), c);
				else
					Serveur.getInstance().envoi("le joueur " + cli + " est mort , c'etait un(e) " + cli.getRole() + " , et le joueur " + cli2 + " est mort , c'etait un(e) " + cli2.getRole(), c);
			else
				Serveur.getInstance().envoi("Vous êtes mort ! Bienvenue dans le monde des fantôme !", c);
			if (state == 1) {
				Serveur.getInstance().envoi("La nuit tombe, tous le monde par dormir !", c);
			} else {
				Serveur.getInstance().envoi("Une nouvelle journée commence au Village", c);
			}
			c.setVotePerso(null);
			c.setaJouer(false);
		}
		if (cli != null) {
			cli.setVivant(false);
			vivant.remove(cli);
		} if (cli2 != null) {
			cli2.setVivant(false);
			vivant.remove(cli2);
		}
		voteVillage.clear();
		voteLoup.clear();
		checkVictoire();
		// permutation jour nuit
		state = (state == 1) ? 2 : 1;
	}

	/**
	 * checkVictoire
	 * victoire des loup-garou lorsqu'ils sont plus nombre que le village (villageois + sorciere)
	 * ou égale si la sorciere n'a plus de potion
	 * victoire du village lorsque plus aucun loup
	 */
	private void checkVictoire() {
		int nbLoup = 0;
		int nbVillageois = 0;
		Client sorciere = null;
		for (Client c : vivant) {
			if (c.getRole() == Role.LOUP_GAROU) nbLoup++;
			else if (c.getRole() == Role.SORCIERE) {sorciere = c; nbVillageois++;}
			else nbVillageois++;
		}
		if (sorciere != null && !isPotionKillAvailable && !isPotionAliveAvailable) {
			if (nbLoup >= nbVillageois)
				clientEnJeu.forEach(c -> Serveur.getInstance().envoi("Victoire des Loup-Garou", c));
		} else if (nbLoup > nbVillageois)
			clientEnJeu.forEach(c -> Serveur.getInstance().envoi("Victoire des Loup-Garou", c));
		else if (nbLoup == 0)
			clientEnJeu.forEach(c -> Serveur.getInstance().envoi("Victoire du Village", c));
	}

	public void finVoteLoup() {
		if (voteLoup.size() == nbLoupMax) {
			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
			//leMort
			leMortDesLoup = setLeMort(voteLoup);
			for (Client client: vivant) {
				if (Role.SORCIERE == client.getRole()) {
					if (leMortDesLoup != null)
						//envoie a la sorcier avec nom du mort
						Serveur.getInstance().envoi("Le villageois " + leMortDesLoup + " a été attaqué par les Loup-Garou !", client);
					else Serveur.getInstance().envoi("Les loup-garou on pas réussi à choisir une cible !",client);
					state = 3;
				} else if (Role.LOUP_GAROU == client.getRole()) {
					if (leMortDesLoup != null)
						Serveur.getInstance().envoi("Vous avez attaqué " + leMortDesLoup + ", vous allez vous reposer !", client);
				}
			}
			// La sorciere est morte
			if (state != 3) {
				estMort(leMortDesLoup, null);
			}
		}
	}

	public boolean vote(String pseudo, Client voteur) {
		for(Client client : vivant){
			if (Objects.equals(pseudo, client.getPseudo()) || Objects.equals(pseudo, "" + client.getNum())) {
				this.voteVillage.add(client);
				voteur.setVotePerso(client);
				return true;
			}
		}
		return false ;
	}

	public boolean laNuitDesPutainDeLoupGarou(String nomVictime, Client voteur) {
		for (Client client : vivant) {
			if (Objects.equals(nomVictime, client.getPseudo()) || Objects.equals(nomVictime, "" + client.getNum())) {
				this.voteLoup.add(client);
				voteur.setVotePerso(client);
				return true;
			}
		}
		return false;
	}

	public boolean leMeurtreDeLaSorciere(String nomVictime, Client sorciere) {
		for (Client client : vivant) {
			if (Objects.equals(nomVictime, client.getPseudo()) || Objects.equals(nomVictime, "" + client.getNum())) {
				// on la tue
				isPotionKillAvailable = false;
				Serveur.getInstance().envoi("Vous avez choisi d'assasiner " + client + " !", sorciere);
				//on fini la nuit
				estMort(client, leMortDesLoup);

				return true;
			}
		}
		return false;
	}

	public void leSauvetageDeLaSorciere() {
		isPotionAliveAvailable = false;
		estMort(null, null);
	}

	public void removeClient(Client c) {
		clientEnJeu.remove(c);
		vivant.remove(c);
		voteLoup.remove(c);
		voteVillage.remove(c);
	}

	public ArrayList<Client> getClientEnJeu() {
		return clientEnJeu;
	}

	public Boolean getDay() {
		return state == 1;
	}

	public int getState() {
		return state;
	}
}
