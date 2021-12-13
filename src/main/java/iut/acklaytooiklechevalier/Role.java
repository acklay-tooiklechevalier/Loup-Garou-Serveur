package iut.acklaytooiklechevalier;
public enum Role {
	//             id         tuer  sauver   pendant la nuit
	LOUP_GAROU("Loup-Garou",  true, false),
	VILLAGEOIS("Villageois", false, false),
	SORCIERE  ("Sorciere"  ,  true,  true);

	private final String role;
	private final boolean kill;
	private final boolean save;

    Role(String role, boolean kill, boolean save) {
		this.role=role;
		this.kill=kill;
		this.save=save;
	}

	public String getRole()  { return role; }
	public boolean getKill() { return kill; }
	public boolean getSave() { return save; }

	@Override
	public String toString() {
		return role;
	}
}
