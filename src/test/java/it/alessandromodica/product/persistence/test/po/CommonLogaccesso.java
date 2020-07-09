package it.alessandromodica.product.persistence.test.po;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the plugin_common_logaccesso database table.
 * 
 */
@Entity
public class CommonLogaccesso implements Serializable {
	private static final long serialVersionUID = 1L;
	private int idaccesso;
	private String descrizione;
	private String ipaddress;
	private Timestamp istante;

	public CommonLogaccesso() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getIdaccesso() {
		return this.idaccesso;
	}

	public void setIdaccesso(int idaccesso) {
		this.idaccesso = idaccesso;
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
}