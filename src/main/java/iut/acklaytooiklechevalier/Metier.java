package iut.acklaytooiklechevalier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Metier {
	/**
	 * state<br>
	 * value : 0 = partie non start<br>
	 * value : 1 = day<br>
	 * value : 2 = Phase Loup<br>
	 * value : 3 = phase sorcière<br>
	 */
	private int state = 0;

    private final ArrayList<Client> vivant ;

    //nuitLoup
    private int nbLoupMax = 0;
    private final int nbVoteLoup = 0 ;
    private final ArrayList<String> voteLoup ;

    private final int nbVoteVillage = 0 ;
    private final ArrayList<String> voteVillage ;

    private Client leMortDesLoup ;
	private Client leMortDeLaSorciere;

	public Metier(){
        voteLoup = new ArrayList<>();
        voteVillage = new ArrayList<>();
		vivant = new ArrayList<>();
        vivant.addAll(Serveur.getInstance().getClient());
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
								Serveur.getInstance().envoi("Vous avez voté pour dévorer " + client.getVotePerso().getNum() + " " + client.getVotePerso().getPseudo() + " !", client);
								finVoteLoup();
								return true;
							}
						}

						if (client.getRole() == Role.SORCIERE && !client.getAJouer() && state == 3) {
							if(leMeurtreDeLaSorciere(commande[1])) {
								if (state == 3)
									client.setaJouer(true);
								Serveur.getInstance().envoi("Vous avez choisi d'assasiner " + leMortDeLaSorciere.getNum() + " " + leMortDeLaSorciere.getPseudo() + " !", client);
								return true;
							}
						}
						return false;
					case 'v': //vote
						if (state == 1) {
							if(!client.getAJouer() && vote(commande[1], client)){
								client.setaJouer(true);
								Serveur.getInstance().envoi("Vous avez voté pour " + client.getVotePerso().getNum() + " " + client.getVotePerso().getPseudo() + " !", client);
								finVoteVillage();
								return true;
							}
							else{
								Serveur.getInstance().envoi("Impossible de changer de voté, votre vote est pour " + client.getVotePerso().getNum() + " " + client.getVotePerso().getPseudo() + " !", client);
								return true;
							}
						}
					case 'c': //chat

						break;
					case 'a': //alive
						if (client.getRole() == Role.SORCIERE && !client.getAJouer() && state == 3){
							leSauvetageDeLaSorciere();
							if (state == 3)
								client.setaJouer(true);
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

        String leMort;

        if (voteVillage.size() == vivant.size()) {
            /**
             * Hashmap
             *
             * @String = Nom victime (Player)
             * @Integer = Nombre total Vote pour la victime
             */
			leMort = temp(voteVillage);

			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
            //leMort
			for (Client client : vivant) {
                if (Objects.equals(leMort, client.getPseudo()) || Objects.equals(leMort, "" + client.getNum())) {
                    estMort(client, null);
					return;
                }
            }
        }
    }

	private String temp(ArrayList<String> voteVillage) {
		String leMort;
		HashMap<String, Integer> nbVote = new HashMap<>();

		for (String s : voteVillage) {
			if (!nbVote.containsKey(s)) {
				nbVote.put(s, 1);
			} else {
				nbVote.put(s, nbVote.get(s) + 1);
			}
		}

		leMort = getLeMort(nbVote, voteVillage);
		return leMort;
	}

	private String getLeMort(HashMap<String, Integer> nbVote, ArrayList<String> vote) {
		String leMort = "";
		for(int i = 0 ; i < vote.size(); i++){
			for (String s : vote) {
				if (nbVote.get(vote.get(i)) > nbVote.get(s)) {
					nbVote.remove(s);
					leMort = vote.get(i);
				} else if (Objects.equals(nbVote.get(vote.get(i)), nbVote.get(s))) {
					leMort = vote.get(i);
				}
			}
		}
		return leMort;
	}

	private void estMort(Client cli, Client cli2) {
        //dire a tout le monde son role et son nom
		for (Client c : Serveur.getInstance().getClient()) {
			c.setVotePerso(null);
			c.setaJouer(false);
			if (c!=cli)
				if (cli == null && state > 1)
					Serveur.getInstance().envoi("Aucun mort est à déplorer cette nuit !", c);
				else if (cli == null)
					Serveur.getInstance().envoi("Le village n'a pas su choisir qui exécuter en place public !", c);
				else if (cli2 == null)
					Serveur.getInstance().envoi("le joueur "+cli.getPseudo()+" est mort , c'etait un(e) "+cli.getRole(), c);
				else
					Serveur.getInstance().envoi("le joueur "+cli.getPseudo()+" est mort , c'etait un(e) "+cli.getRole()+" , et le joueur "+cli2.getPseudo()+" est mort , c'etait un(e) "+cli2.getRole()+ ", le jour se leve sur le village", c);
			else
				Serveur.getInstance().envoi("Vous êtes mort ! Bienvenue dans le monde des fantôme !", c);
			if (state == 1) {
				Serveur.getInstance().envoi("La nuit tombe, tous le monde par dormir !", c);
			} else {
				Serveur.getInstance().envoi("Une nouvelle journée commence au Village", c);
			}
		}
		if (cli != null) {
			cli.setVivant(false);
			vivant.remove(cli);
		} if (cli2 != null) {
			cli2.setVivant(false);
			vivant.remove(cli2);
		}
		// permutation jour nuit
		state = (state == 1) ? 2 : 1;
	}

	public void finVoteLoup() {
		if (voteLoup.size() == nbLoupMax) {
            /**
             * Hashmap
             *
             * @String = Nom victime (Player)
             * @Integer = Nombre total Vote pour la victime
             */
			String leMort = temp(voteLoup);

			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
            //leMort

            for (Client client : vivant) {
				if (Objects.equals(leMort, client.getPseudo()) || Objects.equals(leMort, "" + client.getNum())) {
					this.leMortDesLoup = client;
				}
            }
			for (Client client: vivant) {
                if (Role.SORCIERE == client.getRole()) {
					//envoie a la sorcier avec nom du mort
					Serveur.getInstance().envoi("Le villageois " + this.leMortDesLoup.getNum() + " " + this.leMortDesLoup.getPseudo() + " a été attaqué par les Loup-Garou !", client);
					state = 3;
				}
			}
			// La sorciere est morte
            if (state != 3) {
                for (Client client : vivant) {
					if (Objects.equals(leMort, client.getPseudo()) || Objects.equals(leMort, "" + client.getNum())) {
						estMort(client, null);
					}
            	}
			}
            
        }
    }

	public boolean vote(String pseudo, Client voteur) {
		for(Client client : vivant){
			if (Objects.equals(pseudo, client.getPseudo()) || Objects.equals(pseudo, "" + client.getNum())) {
				this.voteVillage.add(client.getPseudo());
				voteur.setVotePerso(client);
				return true;
			}
		}
		return false ;
	}

    public boolean laNuitDesPutainDeLoupGarou(String nomVictime, Client voteur) {
        for (Client client : vivant) {
            if (Objects.equals(nomVictime, client.getPseudo()) || Objects.equals(nomVictime, "" + client.getNum())) {
                this.voteLoup.add(client.getNum() + "");
				voteur.setVotePerso(client);
                return true;
            }
        }
        return false;
    }

    public boolean leMeurtreDeLaSorciere(String nomVictime) {
        for (Client client : vivant) {
            if (Objects.equals(nomVictime, client.getPseudo()) || Objects.equals(nomVictime, "" + client.getNum())) {
                // on la tue
				leMortDeLaSorciere = client;
                //on fini la nuit
                estMort(leMortDeLaSorciere, leMortDesLoup);

                return true;
            }
        }
        return false;
    }

    public void leSauvetageDeLaSorciere() {
		estMort(null, null);
    }

    

	public Boolean getDay() {
		return state == 1;
	}

	public int getState() {
		return state;
	}
}
