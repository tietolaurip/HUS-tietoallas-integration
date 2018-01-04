# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka CA integraation peruskomponentti otetaan käyttöön. Otsikon perässä suluissa kerrotaan millä palvelimella kohdan suoritus tulee tehdä.

# Esiehdot

  * CA-integraatioiden eri lähdejärjestemien asentamista tulee asentaa CAn peruskomponentti, joka "CA" ilman loppupäätettä.
  * Vanha metadata pitää ensin poistaa. Se tapahtuu seuraavasti:

	/opt/DataLake/lib/metadata/remove_metadata.sh CA -v

  * Komponentin tietokantoja ei saa olla Hivessä. Poista kannat seuraavasti:

        husdev@hn0-mainda:~$ beeline -u 'jdbc:hive2://localhost:10001/;transportMode=http'
        0: jdbc:hive2://localhost:10001/> drop database staging_ca cascade;
        0: jdbc:hive2://localhost:10001/> drop database storage_ca cascade;

  * Seuraavat komponentit tulee myös olla asennettuna:
    * base
    * LoadTool
	* Metadata
	* StatusDb

# Asennuspaketin luonti (kehitysympäristö)

Asennuspaketti rakennetaan kehitysympäristössä seuraavasti:

	cd <path_to_git>/integrations/CA
	../pack.sh hus  [dev|test|prod]

"dev/test/prod" valitaan sen mukaan mihin ympäristöön asennuspakettia luodaan.
Asennuspaketti on tähän hakemistoon syntynyt CA-hus-[dev|test|prod].tar.gz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf CA-hus-[dev|test|prod].tar.gz

# Komponentin asennus (manager)

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	cd /opt/DataLake/CA
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan. Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

# Komponentin alustus (headnode)

Kirjaudu headnodelle. CA-komponentti ei itse sisällä taulumäärityksiä, vaan ne ladataan tässä vaiheessa lähdetietokannasta. Seuraavat komennot luovat staging- ja storage-tietokannat tauluineen lähdetietokannan vastaavien perusteella:

	nohup /opt/DataLake/lib/import/initial_setup.sh CA -v >/dev/null 2>&1 &

Voit tarkastella ajon etenemistä seuraavalla komennolla:

	tail -f $(ls -d /var/log/DataLake/CA/initial_setup* | tail -1)

Tämän jälkeen tietokanta staging_CA ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.
Lisäksi komponentin metadata on luotu status-kantaan.

# Komponentin metadatan päivitys (manager)

Metadataa tarvitaan datan prosessoinnissa varastoon (mm. pseudonymisoinnissa). Lisäksi metadata tarjoaa loppukäyttäjille tietoa siirrettävän datan sisällöstä. Alustuksen yhteydessä luodussa metadatassa ei ole taulujen ja kenttien kuvauksia, eikä pseudonymisointi-tietoja. Nämä tiedon on kerätty ja talletettu erikseen komponentin metadata-kansioon

Metadatan asennus tietoaltaan tietokantoihin tapahtuu seuraavasti:

	nohup /opt/DataLake/lib/metadata/upload_metadata.sh CA  -v >/dev/null 2>&1 &

Lokia voi tarkastella ajosta (viimeisin upload_metadata*-tiedosto):

	tail -f $(ls -d /var/log/DataLake/CA/upload_metadata* | tail -1)

Lopuksi päivitetään vielä statusDb:n sisältöä:

	/opt/DataLake/lib/metadata/upload_statusdata_from_sql.sh CA /opt/DataLake/CA/metadata/update_status_db.sql -v

# Alkulataus (headnode)

Jos käytetty lähde-kanta on tarpeeksi pieni tai muuten on syytä uskoa kokonaisten taulujen latauksen toimivan, niin koko lähdekanta voidaan ladata kokonaisuudessaan. Jos dataa on paljon tai latausaika on rajattu, niin lataus kannattaa tehdä pienemmissä palasissa. Lataus koko lähdetietokannan suoritetaan 3 osassa seuraavasti 1) ladataan data staging-kantaan 2) alustetaan integraatioiden statusDb ja 3) ladataan data staging-kannasta storage-kantaan:

	/opt/DataLake/lib/import/initial_import_to_staging.sh CA -v >/dev/null 2>&1 &

Seuraavaksi prosessoidaan data storageen:

	/opt/DataLake/lib/import/batch_sta_to_sto.sh CA -v >/dev/null 2>&1 &

Latauksen jälkeen (tarkista lokista: /var/log/DataLake/CA/initial_import_to_staging_<date>.log), voidaan suorittaa myös (onnistuu samanaikaisesti varastoonprosessoinnin kanssa) statusDB:n alustus:

	/opt/DataLake/lib/import/initialize_statusdb_after_import.sh CA -v >/dev/null 2>&1 &

StatusDB:n alustus (headnodella) tapahtuu ao. komennolla. Alustus pitää sisällään 2 asiaa: 1) Hive-tyyppien tarkistus ja mahdollinen korjaus ja 2) Alkulatauksen tietojen päivitys statusDB:hen hive-kannasta (koska viimeksi ladattu ja mikä on viimeisin id/aikaleima, mihin asti lataus on suoritettu). 

# Lopputilanne

Kun alustus ja alkualtaus on tehty, niin voidaan jatakaa inkrementaali-komponentin (CA_inkr) asennuksella ja käyttöönotolla.
