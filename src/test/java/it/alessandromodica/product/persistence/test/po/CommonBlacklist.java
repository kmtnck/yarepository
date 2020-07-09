package it.alessandromodica.product.persistence.test.po;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the plugin_common_blacklist database table.
 * 
 */
@Entity
public class CommonBlacklist implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int idblacklist;

	private String cookie;

	private String descrizione;

	private String ipaddress;

	private Timestamp istante;

	private String keyaccess;

	private String scarabocchio;

	private String utente;

	public String getUtente() {
		return utente;
	}

	public void setUtente(String utente) {
		this.utente = utente;
	}

	public CommonBlacklist() {
	}

	public int getIdblacklist() {
		return this.idblacklist;
	}

	public void setIdblacklist(int idblacklist) {
		this.idblacklist = idblacklist;
	}

	public String getCookie() {
		return this.cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getDescrizione() {
		return this.descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getIpaddress() {
		return this.ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public Timestamp getIstante() {
		return this.istante;
	}

	public void setIstante(Timestamp istante) {
		this.istante = istante;
	}

	public String getKeyaccess() {
		return this.keyaccess;
	}

	public void setKeyaccess(String keyaccess) {
		this.keyaccess = keyaccess;
	}

	public String getScarabocchio() {
		return this.scarabocchio;
	}

	public void setScarabocchio(String scarabocchio) {
		this.scarabocchio = scarabocchio;
	}

}