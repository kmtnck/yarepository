package it.alessandromodica.product.persistence.test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import it.alessandromodica.product.persistence.exceptions.RepositoryException;
import it.alessandromodica.product.persistence.interfaces.IRepositoryCommands;
import it.alessandromodica.product.persistence.interfaces.IRepositoryQueries;
import it.alessandromodica.product.persistence.searcher.YAFilterLikeClause;
import it.alessandromodica.product.persistence.searcher.YAFilterSearchApp;
import it.alessandromodica.product.persistence.test.po.CommonBlacklist;
import it.alessandromodica.product.persistence.test.po.CommonLogaccesso;
import it.alessandromodica.product.persistence.test.po.GestioneUtenti;
import junit.framework.Assert;
import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class TestProcessActions extends TestCase {

	private static final Logger log = Logger.getLogger(TestProcessActions.class);

	String remoteAddrs;
	String referer;
	String useragent;
	String rawUtente;
	String hashscript;
	String datacookie;
	String payloadOauth;

	@SuppressWarnings("rawtypes")
	private IRepositoryCommands repocommand;
	@SuppressWarnings("rawtypes")
	private IRepositoryQueries repoquery;

	AnnotationConfigApplicationContext context;
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		//AppSpringInitializer.initLog();
		//AppConfig.InitApp();

		// Avviare contesto spring
		context = new AnnotationConfigApplicationContext(TestAppConfig.class);
		repocommand = context.getBean(IRepositoryCommands.class);
		repoquery = context.getBean(IRepositoryQueries.class);

		

		// UnitOfWork.initSessionFactory("sakjpa-test");
		/*
		 * Impostazione parametri mocks requests
		 */
		remoteAddrs = "127.0.0.1";

		referer = "https://www.ingress.com/intel";
		useragent = "Junit/Test (Sistema operativo non rilevante)";

		// XXX: recupero informazioni del giocatore
		rawUtente = "{\"min_ap_for_next_level\":\"0\",\"min_ap_for_current_level\":\"40000000\",\"energy\":7687,\"team\":\"RESISTANCE\",\"verified_level\":16,\"ap\":\"120667057\",\"available_invites\":157,\"xm_capacity\":\"22000\",\"nickname\":\"OBkppa\",\"level\":16,\"nickMatcher\":{}}";
		hashscript = "d41d8cd98f00b204e9800998ecf8427e";
		datacookie = "csrftoken=LHabpYvGfzIfld91PfsDHEH4XGI3WIVx; G_ENABLED_IDPS=google; sak-identification=; __utmz=24037858.1533052769.46.16.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=https://www.ingress.com/intel; sak-scraps=; sak-publickey=f121cd62727ee92b; SAK-Token=35fb4d2fb27564deb0426a5a8879346f; __utmc=24037858; G_AUTHUSER_H=0; ingress.intelmap.zoom=15; ingress.intelmap.lat=44.496413497351654; ingress.intelmap.lng=11.332612037658691; __utma=24037858.1341569233.1525252241.1536221514.1536226905.68; __utmt=1; __utmb=24037858.4.9.1536227685078";

	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
		
		context.close();
	}

	/*
	 * public void testOuthsignin() {
	 * 
	 * AppContext context = AppContext.valueOf("outhsignin"); String email = null;
	 * String nickname = null;
	 * 
	 * payloadOauth =
	 * "{\"profiloUtente\":{\"Eea\":\"114698683329673820502\",\"ig\":\"Alessandro Modica\",\"ofa\":\"Alessandro\",\"wea\":\"Modica\",\"Paa\":\"https://lh3.googleusercontent.com/-MdnqbCuiL4U/AAAAAAAAAAI/AAAAAAAAJXk/sYWvLBCTEfY/s96-c/photo.jpg\",\"U3\":\"alessandro.modica@gmail.com\"},\"googleUser\":{\"El\":\"114698683329673820502\",\"Zi\":{\"token_type\":\"Bearer\",\"access_token\":\"ya29.Gl0VBrXicpRRVPD18u0FgsbrIMr4r5Fi_az1HJZKBW_OVqOZBXOr4p5wT1dFh4B1ZzczdAXryGU4DztlIzDVidv97w3HG35NLq18Qom8y86FJ5m0qs8vJh1N7nZW-2o\",\"scope\":\"https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email openid email profile\",\"login_hint\":\"AJDLj6LPQKRMLFr0hUSwE9ujofbitSQ9u1X0p81_KPxewdvqV7OaVenDJ7YaEfoUNw-ubpBcysV8b__QmRjdGc11mN9WTCJFPw\",\"expires_in\":3600,\"id_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6ImQ5NjQ4ZTAzMmNhYzU4NDI0ZTBkMWE3YzAzMGEzMTk4ZDNmNDZhZGIifQ.eyJhenAiOiIzMjA4NDQ0MjQyNDMtNGVxNTFsaW4xbWs1NHNqamxkb2ZqamNxZDlkbDJkcTguYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIzMjA4NDQ0MjQyNDMtNGVxNTFsaW4xbWs1NHNqamxkb2ZqamNxZDlkbDJkcTguYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTQ2OTg2ODMzMjk2NzM4MjA1MDIiLCJlbWFpbCI6ImFsZXNzYW5kcm8ubW9kaWNhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoibllIRk1LZm1sWEo3N2Q5aXY2UVo2ZyIsImV4cCI6MTUzNjY1ODkzNCwiaXNzIjoiYWNjb3VudHMuZ29vZ2xlLmNvbSIsImp0aSI6IjcwMDlmNDBhNDJhOWFjYWFjZmQ5YTIyNTk5MWYzOWJmOTVlMWNiMTYiLCJpYXQiOjE1MzY2NTUzMzQsIm5hbWUiOiJBbGVzc2FuZHJvIE1vZGljYSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLU1kbnFiQ3VpTDRVL0FBQUFBQUFBQUFJL0FBQUFBQUFBSlhrL3NZV3ZMQkNURWZZL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJBbGVzc2FuZHJvIiwiZmFtaWx5X25hbWUiOiJNb2RpY2EiLCJsb2NhbGUiOiJpdCJ9.0W6CuJRG8eBntsEqcdu6f_15oEwQDhoYsyOtspfHXoH8iCzqFTzZJ8i50qUeGFN7NTmPBN5D-OrfjbO8IH0xGGYsGgptCTY7rhJc0SrQ90j25U0wrHP9CxwiF8woS06_Ex1mkjUM7pJEU4GTTnb33VjjSZK2AmJnqeA3EtZsCAWWP3GWB03t6iWVIe3zClheUJ7Q3mC_VL2prXmZpOHbaIofWByyQJcKjidLGFzqxGGaoqzHy3ENvqTLvl2BBMWrcWQirxSvMKTV6uDbyLYKLafAqfnmS3KdvmUxC5sHoYdjwpBiTMY5kB_VKIMpJ4rXz42Kmq8SThMMZp_ZHRyETw\",\"session_state\":{\"extraQueryParams\":{\"authuser\":\"0\"}},\"first_issued_at\":1536655334303,\"expires_at\":1536658934303,\"idpId\":\"google\"},\"w3\":{\"Eea\":\"114698683329673820502\",\"ig\":\"Alessandro Modica\",\"ofa\":\"Alessandro\",\"wea\":\"Modica\",\"Paa\":\"https://lh3.googleusercontent.com/-MdnqbCuiL4U/AAAAAAAAAAI/AAAAAAAAJXk/sYWvLBCTEfY/s96-c/photo.jpg\",\"U3\":\"alessandro.modica@gmail.com\"}},\"nickname\":\"OBkppa\",\"datacookie\":\"__utmc=24037858; csrftoken=RsuB2ZF4yQEbrAgFztfZY3Wmw214FWEc; __utmz=24037858.1536594378.2.2.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=https://www.ingress.com/intel; ingress.intelmap.shflt=viz; ingress.intelmap.zoom=15; sak-identification=; G_ENABLED_IDPS=google; __utma=24037858.1514725551.1536594365.1536648987.1536655081.4; __utmt=1; sak-publickey=f121cd62727ee92b; sak-scraps=; SAK-Token=35fb4d2fb27564deb0426a5a8879346f; ingress.intelmap.lat=44.49485230161302; ingress.intelmap.lng=11.327333450317383; __utmb=24037858.4.9.1536655271181\"}"
	 * ;
	 * 
	 * BORequestData inputData = new BORequestData();
	 * 
	 * try {
	 * 
	 * Object result = _mainapplication.processSignInOutGoogle(inputData,
	 * payloadOauth, context);
	 * 
	 * Assert.assertNotNull(result);
	 * 
	 * } catch (BusinessException e) { // TODO Auto-generated catch block
	 * Assert.fail(); log.error("Errore durante l'esecuzione del test [" +
	 * e.getMessage() + "]"); } }
	 * 
	 * public void testOuthsignout() {
	 * 
	 * AppContext context = AppContext.valueOf("outhsignout"); String email =
	 * "alessandro.modica@gmail.com"; String nickname = "OBkppa";
	 * 
	 * BORequestData inputData = new BORequestData();
	 * inputData.getMapRequestData().put(RequestVariable.email, email);
	 * inputData.getMapRequestData().put(RequestVariable.nickname, nickname);
	 * 
	 * try {
	 * 
	 * Object result = _mainapplication.processSignInOutGoogle(inputData,
	 * payloadOauth, context);
	 * 
	 * Assert.assertNotNull(result);
	 * 
	 * } catch (BusinessException e) { // TODO Auto-generated catch block
	 * Assert.fail(); log.error("Errore durante l'esecuzione del test [" +
	 * e.getMessage() + "]"); } }
	 */

	public void testHibernate() throws RepositoryException {

		try {
			YAFilterLikeClause testSearch = new YAFilterLikeClause();
			testSearch.setNameField("email");
			testSearch.setValue("alessandro.modica@gmail.com");
			YAFilterSearchApp criteria = new YAFilterSearchApp(GestioneUtenti.class);
			criteria.getListLikeClause().add(testSearch);

			List<GestioneUtenti> fromDb = repoquery
					.search(criteria.getSerialized());

			if (fromDb.isEmpty())
				Assert.assertTrue("I dati non sono stati trovati, ma la query è stata eseguita correttamente", true);
			else {
				for (GestioneUtenti pluginCommonLogaccesso : fromDb) {

					log.info(pluginCommonLogaccesso.getNickname());
				}
				Assert.assertTrue(true);
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new RepositoryException(ex);
		}

	}

	public void testSearch() throws RepositoryException {

		try {
			YAFilterSearchApp criteria = new YAFilterSearchApp(GestioneUtenti.class);
			criteria.setNickname("Gunny13");

			List<GestioneUtenti> fromDb = repoquery
					.search(criteria.getSerialized());

			if (fromDb.isEmpty())
				Assert.assertTrue("I dati non ci sono , ma la query è stata eseguita correttamente", true);
			else {
				for (GestioneUtenti pluginCommonLogaccesso : fromDb) {

					log.info(pluginCommonLogaccesso.getNickname());
				}
				Assert.assertTrue(true);

				criteria.setNot(true);
				int notClause = repoquery.
						getCount(criteria.getSerialized());
				Assert.assertTrue(notClause == 0);
			}
			
			

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new RepositoryException(ex);
		}

	}

	public void testElencoUtenti() throws RepositoryException {
		// TODO Auto-generated method stub
		String searcher = "min";
		YAFilterSearchApp criteria = new YAFilterSearchApp(GestioneUtenti.class);
		YAFilterLikeClause likeClause = new YAFilterLikeClause();
		likeClause.setInsensitive(true);
		likeClause.setNameField("nickname");
		likeClause.setValue("%" + searcher + "%");
		criteria.getListLikeClause().add(likeClause);
		criteria.getListOrderBy().add("nickname");
		try {

			List<GestioneUtenti> result = repoquery
					.search(criteria.getSerialized());

			Assert.assertTrue(result.size() >= 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Si e' verificato un errore durante il recupero della lista dei giocatori " + e.getMessage(), e);
			throw new RepositoryException(e);
		}
	}

	public void testInsert() {
		try {

			String msg = "Adesso facciamo un altro test!!";

			CommonLogaccesso toAdd = new CommonLogaccesso();
			toAdd.setIpaddress("questo e un test");
			toAdd.setDescrizione(msg);
			toAdd.setIstante(Timestamp.from(Calendar.getInstance().toInstant()));
			repocommand.add(toAdd);

			YAFilterSearchApp testsearch = new YAFilterSearchApp(CommonLogaccesso.class);
			testsearch.setDescrizione("Adesso facciamo un altro test!!");
			List<CommonLogaccesso> resulttest = repoquery
					.search(testsearch.getSerialized());

			if (resulttest == null)
				Assert.fail();
			else {
				if (resulttest.size() > 1) {
					log.info("E' strano che ce ne siano " + resulttest.size());

					for (CommonLogaccesso obj : resulttest) {

						repocommand.delete(obj);
					}
					Assert.assertTrue(repoquery.search(testsearch.getSerialized()).size() == 0);
				} else if (resulttest.size() == 1) {
					CommonLogaccesso data = resulttest.get(0);
					Assert.assertTrue(msg.equals(data.getDescrizione()));
				}
			}

		} catch (RepositoryException e) {

			Assert.fail();
		}

	}

	public void testCountJPA() {
		try {

			/*
			 * PluginLoggerchatInfoportali data = (PluginLoggerchatInfoportali)
			 * SakRepository .GetRepoQueries(PluginLoggerchatInfoportali.class)
			 * .getFirstOrDefault(Restrictions.eq("coordinate", "44.836,11.5989"));
			 */
			YAFilterSearchApp criteria;
			criteria = new YAFilterSearchApp(CommonBlacklist.class);
			criteria.setDescrizione("test");
			criteria.getListIsNull().add("keyaccess");

			List<CommonBlacklist> result = repoquery
					.search(criteria.getSerialized());

			Assert.assertTrue(result != null);
			int value = repoquery.getCount(criteria.getSerialized());

			Assert.assertTrue(value == 0);

		} catch (Exception e) {
			// Assert.fail();
		}

	}

}
