package it.alessandromodica.product.persistence.searcher;

import java.util.Date;

/**
 * Classe specifica per l'applicazione che si vuole sviluppare. Ogni field qui
 * definito e' automaticamente riconosciuto dal serializzatore del BOSearcher
 * come clausola di uguaglianza. E' sufficiente aggiungere un nuovo field per
 * fornire all'applicazione la gestione di un nuovo campo.
 * 
 * @author Alessandro
 *
 */
public class YAFilterSearchApp extends YAFilterSearch {

	@Deprecated
	public YAFilterSearchApp() {
		super();
	}

	public YAFilterSearchApp(Class<?> classEntity) {
		super(classEntity);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3465637985118214119L;

	private String nickname;
	private Date data;
	private Date ora;
	private String email;
	private String coordinate;
	private String idtoken;
	private String guid;
	private String descrizione;

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(String coordinate) {
		this.coordinate = coordinate;
	}

	private String ipaddress;
	private String tokenapp;
	private int idutente;
	private String nomeparametro;

	public String getNomeparametro() {
		return nomeparametro;
	}

	public void setNomeparametro(String nomeparametro) {
		this.nomeparametro = nomeparametro;
	}

	public int getIdutente() {
		return idutente;
	}

	public void setIdutente(int idutente) {
		this.idutente = idutente;
	}

	@Override
	public boolean checkCompositeId(String nameProperty) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getTokenapp() {
		return tokenapp;
	}

	public void setTokenapp(String tokenapp) {
		this.tokenapp = tokenapp;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Date getOra() {
		return ora;
	}

	public void setOra(Date ora) {
		this.ora = ora;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIdtoken() {
		return idtoken;
	}

	public void setIdtoken(String idtoken) {
		this.idtoken = idtoken;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

}
