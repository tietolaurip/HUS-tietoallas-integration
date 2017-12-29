# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka Healthweb integraation peruskomponentti otetaan käyttöön. Otsikon perässä suluissa kerrotaan millä palvelimella kohdan suoritus tulee tehdä.

# Esiehdot

  * Seuraavat komponentit tulee myös olla asennettuna (voidaan myös asentaa tämän integraatiokomponentin jälkeen):
    * stream-pseudonymizer
	* stream-to-hdfs
	* StatusDb

# Asennuspaketin luonti (kehitysympäristö)

Asennuspaketti rakennetaan kehitysympäristössä seuraavasti:

	cd <path_to_git>/integrations/Healthweb
	../pack.sh hus  [dev|test|prod]

"dev/test/prod" valitaan sen mukaan mihin ympäristöön asennuspakettia luodaan.
Asennuspaketti on tähän hakemistoon syntynyt Healthweb-hus-[dev|test|prod].tar.gz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf Healthweb-hus-[dev|test|prod].tar.gz

# Komponentin asennus (manager)

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	cd /opt/DataLake/Healthweb
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan. Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

# Komponentin alustus (headnode)

KKirjaudu headnodelle. Healthweb-komponentti ei itse sisällä taulumäärityksiä, vaan ne ladataan tässä vaiheessa lähdetietokannasta. Seuraavat komennot luovat staging- ja storage-tietokannat tauluineen lähdetietokannan vastaavien perusteella:

* StatusDb: Kafka-offset alustus: Aja skripti

	initialize_kafka_offsets.sql

* Hive alustus: aja skripti

	create_schema.hql

Tämän jälkeen tietokanta staging_Healthweb ja sen kaikki taulut (tyhjinä) ovat Hivellä nähtävissä.

# Komponentin metadatan päivitys (manager)

Metadataa tarvitaan datan prosessoinnissa varastoon (mm. pseudonymisoinnissa). Lisäksi metadata tarjoaa loppukäyttäjille tietoa siirrettävän datan sisällöstä. Alustuksen yhteydessä luodussa metadatassa ei ole taulujen ja kenttien kuvauksia, eikä pseudonymisointi-tietoja. Nämä tiedon on kerätty ja talletettu erikseen komponentin metadata-kansioon

# Alkulataus (headnode)

Ei ole validi Healhweb:lle.


# Lopputilanne

Kun alustus ja alkualtaus on tehty, niin integraatio vastaanottaa sanomia Healthweb-järjestelmästä ja kirjoittaa dataa kafka-jonoon. Jotta data saadaan vielä pseudonymisoituna hive-kantaan, tulee vielä vermistaa, että komponentit stream-pseudonymizer ja stream-to-hdfs on asennettu. Jos stream-pseudonymizer on jo asennettu jonkin muun integraation toimesta, kyseisestä komponentista tulee kuitenkin tarvittaessa asentaa Healthweb:iä tukeva komponentti.
