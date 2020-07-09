package it.alessandromodica.product.persistence.test.po;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the plugin_gestione_utenti database table.
 * 
 */
@Entity
public class GestioneUtenti implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	private int idutente;

	@Column(length=256)
	private String email;

	@Column(nullable=false)
	private int idgruppo;

	private byte idtelegram;

	@Column(nullable=false)
	private Timestamp istante;

	@Column(nullable=false, length=256)
	private String nickname;

	@Lob
	private String privatekey;

	@Lob
	private String publickey;

	@Column(length=256)
	private String scarabocchio;

	@Column(length=256)
	private String usernametelegram;

	public GestioneUtenti() {
	}

	public int getIdutente() {
		return this.idutente;
	}

	public void setIdutente(int idutente) {
		this.idutente = idutente;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getIdgruppo() {
		return this.idgruppo;
	}

	public void setIdgruppo(int idgruppo) {
		this.idgruppo = idgruppo;
	}

	public byte getIdtelegram() {
		return this.idtelegram;
	}

	public void setIdtelegram(byte idtelegram) {
		this.idtelegram = idtelegram;
	}

	public Timestamp getIstante() {
		return this.istante;
	}

	public void setIstante(Timestamp istante) {
		this.istante = istante;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPrivatekey() {
		return this.privatekey;
	}

	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}

	public String getPublickey() {
		return this.publickey;
	}

	public void setPublickey(String publickey) {
		this.publickey = publickey;
	}

	public String getScarabocchio() {
		return this.scarabocchio;
	}

	public void setScarabocchio(String scarabocchio) {
		this.scarabocchio = scarabocchio;
	}

	public String getUsernametelegram() {
		return this.usernametelegram;
	}

	public void setUsernametelegram(String usernametelegram) {
		this.usernametelegram = usernametelegram;
	}

}