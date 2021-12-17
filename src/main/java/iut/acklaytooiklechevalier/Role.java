package iut.acklaytooiklechevalier;
public enum Role {
	//             id
	LOUP_GAROU("Loup-Garou"),
	VILLAGEOIS("Villageois"),
	SORCIERE  ("Sorciere");

	private final String role;

    Role(String role) {
		this.role=role;
	}

	@Override
	public String toString() {
		return role;
	}
}
