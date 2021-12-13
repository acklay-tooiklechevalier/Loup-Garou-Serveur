package iut.acklaytooiklechevalier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Metier {
    private Boolean day = null;

    private final ArrayList<Client> vivant ;

    //nuitLoup
    private int nbLoup = 2;
    private final int nbVoteLoup = 0 ;
    private final ArrayList<String> voteLoup ;

    private final int nbVoteVillage = 0 ;
    private final ArrayList<String> voteVillage ;

	private boolean isSorciereVote;

    private Client leMort ;

    public Metier(){
        voteLoup = new ArrayList<>();
        voteVillage = new ArrayList<>();
        vivant = Serveur.getInstance().getClient(); 
        initialisation();
    }

    public void initialisation() {
        int nbSorcier = 1;
    	nbLoup = (int) ((Serveur.getInstance().getClient().size() - nbSorcier) * 0.30);
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
		day = false;
    }

    public boolean receptionInformation(String info, Client client) {
        // commande possible :kill,vote,chat,alive,rien
        // format attendu >[Action] [Num Joueur]
		System.out.println("reception : " + info);
		if (vivant.contains(client)) {
			String[] commande = info.split(" ");
			// System.out.println(commande[0] + " : " + commande[1] + " = " + info);
			switch (commande[0].toLowerCase().charAt(0)) {
				case 'k': // kill
					if (client.getRole() == Role.LOUP_GAROU && !client.getAJouer() && !day){
						if(vote(commande[1])){
							client.setaJouer(true);
						}

                        finVoteLoup();
					}
					else{
						finVoteLoup();
					}
                    
                    if (client.getRole() == Role.SORCIERE && !client.getAJouer() && !day){
                        if(leMeurtreDeLaSorciere(commande[1])){
							client.setaJouer(true);
						}
                    }
					return true;
				case 'v': //vote
					if (client.getVivant() && day){
						if(laNuitDesPutainDeLoupGarou(commande[1])){
							client.setaJouer(true);
						}
					}
                    else{
						//dire impossible de jouer
					}
					finVoteVillage();
					return true;
				case 'c': //chat

					break;
				case 'a': //alive
                    if (client.getRole() == Role.SORCIERE && !client.getAJouer() && !day){
                        if(leSauvetageDeLaSorciere(commande[1])){
							client.setaJouer(true);
						}
                    }
					return true;
                case 'r': //alive
                    if (client.getRole() == Role.SORCIERE && !client.getAJouer() && !day){
                        client.setaJouer(true);
                    }
					return true;
			}
        }
		return false;
    }

    public boolean vote(String pseudo) {
        for(Client client : vivant){
            if (Objects.equals(pseudo, client.getPseudo()) || Objects.equals(pseudo, "" + client.getNum())) {
                this.voteVillage.add(client.getPseudo());
                return true;
            }
        }
        return false ; 
    }

    public void finVoteVillage() {

        String leMort = "";

        if (voteVillage.size() == vivant.size())
		{
            /**
             * Hashmap
             *
             * @String = Nom victime (Player)
             * @Integer = Nombre total Vote pour la victime
             */
            HashMap<String, Integer> nbVote = new HashMap<>();

			for (String s : voteVillage) {
				if (nbVote.containsKey(s)) {
					nbVote.put(s, 0);
				} else {
					nbVote.put(s, nbVote.get(s) + 1);
				}
			}

			leMort = getLeMort(leMort, nbVote, voteVillage);

			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
            //leMort
            for (Client client : vivant) {
                if (Objects.equals(leMort, client.getPseudo())) {
                    vivant.remove(client);
                    client.setVivant(false);
                    estMort(client);
                }
            }
        }
    }

	private String getLeMort(String leMort, HashMap<String, Integer> nbVote, ArrayList<String> voteVillage) {
		for(int i = 0 ; i < nbVote.size(); i++){
			for(int j = 0; i < nbVote.size(); j++) {
				if (nbVote.get(voteVillage.get(i)) > nbVote.get(voteVillage.get(j))) {
					nbVote.remove(voteVillage.get(j));
				}
			}
			leMort = voteVillage.get(i);
		}
		return leMort;
	}

	private void estMort(Client cli) {
        //dire a tout le monde son role et son nom
		for (Client c : Serveur.getInstance().getClient()) {
			if (c!=cli)
				Serveur.getInstance().envoi("le joueur "+cli.getPseudo()+" est mort , c'etait un(e) "+cli.getRole(), c);
			else 
				Serveur.getInstance().envoi("Vous êtes mort ! Bienvenue dans le monde des fantôme", c);
		}
	}

    private void finNuitMort(Client cli, Client cli2) {
        //dire a tout le monde son role et son nom
		for (Client c : Serveur.getInstance().getClient()) {
			if (c!=cli)
				Serveur.getInstance().envoi("le joueur "+cli.getPseudo()+" est mort , c'etait un(e) "+cli.getRole()+" , et le joueur "+cli2.getPseudo()+" est mort , c'etait un(e) "+cli2.getRole()+ ", le jour se leve sur le village", c);
			else 
				Serveur.getInstance().envoi("Vous êtes mort ! Bienvenue dans le monde des fantôme", c);
		}
	}

    private void finNuitPasMort(Client cli) {
        //dire a tout le monde son role et son nom
		for (Client c : Serveur.getInstance().getClient()) {
			if (c!=cli)
				Serveur.getInstance().envoi("pas de mort cette nuit le jour se leve sur le village", c);
			else 
				Serveur.getInstance().envoi("Vous êtes mort ! Bienvenue dans le monde des fantôme", c);
		}
	}

	public void finVoteLoup() {

        String leMort = "";

        if (voteLoup.size() == nbLoup) {
            /**
             * Hashmap
             *
             * @String = Nom victime (Player)
             * @Integer = Nombre total Vote pour la victime
             */
            HashMap<String, Integer> nbVote = new HashMap<>();

			for (String s : voteLoup) {
				if (nbVote.containsKey(s)) {
					nbVote.put(s, 0);
				} else {
					nbVote.put(s, nbVote.get(s) + 1);
				}

			}

            //on lance le tour de la sorciere et on lui dit que le mort est :  
            //leMort

			leMort = getLeMort(leMort, nbVote, voteLoup);

			//on tue le jouer annonce sa mort et son role puis on passe a la nuit
            //leMort

            for (Client client : vivant) {
				if (Objects.equals(leMort, client.getPseudo())) {
					this.leMort = client;
				}
                if (Role.SORCIERE == client.getRole()) {
                    //envoie a la sorcier avec nom du mort
					Serveur.getInstance().envoi("Le villageois " + this.leMort.getNum() + " " + this.leMort.getPseudo() + " a été attaqué par les Loup-Garou !", client);
					isSorciereVote = true;
                    client.setaJouer(false);//la elle peux jouer 

				}
            }
            if (!isSorciereVote) {
				day = true;
                for (Client client : vivant) {
					if (Objects.equals(leMort, client.getPseudo())) {
						vivant.remove(client);
						client.setVivant(false);
						estMort(client);
					}
            	}
			}
            
        }
    }

    public boolean laNuitDesPutainDeLoupGarou(String nomVictime) {
        for (Client client : vivant) {
            if (Objects.equals(nomVictime, client.getPseudo())) {
                this.voteLoup.add(nomVictime);
                return true;
            }
        }
        return false;
    }

    public boolean leMeurtreDeLaSorciere(String nomVictime) {
        for (Client client : vivant) {
            if (Objects.equals(nomVictime, client.getPseudo())) {
                // on la tue
                vivant.remove(client);
                client.setVivant(false);

                vivant.remove(leMort);
                leMort.setVivant(false);

                //on fini la nuit
                finNuitMort(client,leMort);

                return true;
            }
        }
        return false;
    }

    public boolean leSauvetageDeLaSorciere(String nomVictime) {
        for (Client client : vivant) {
            if (Objects.equals(nomVictime, client.getPseudo())) {
                // on la sauve
                //on fini la nuit
                finNuitPasMort(client);
                return true;
            }
        }
        return false;
    }

    

	public Boolean getDay() {
		return day;
	}

	public boolean getIsSorciereVote() {
		return isSorciereVote;
	}

    public Role role(int nbplayer){
        //return role[loup,loup,loup,villageos];
		return null;
    }
}
