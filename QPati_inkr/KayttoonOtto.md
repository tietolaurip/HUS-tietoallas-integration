# Komponentin asennusohje / Käyttöönotto

# Esivaatimukset

* Tietoallas infra ja peruspaketti (base) pitää olla asennettu. 
* Komponentin "emokomponentti" (komponentin nimi ilman "_inkr"-päätettä) tulee olla sennettu
* Kafkan tulee olla asennettu

# Asennuspaketin rakennus (kehitysympäristö)

## Luo paketti seuraavasti

	cd <git-root>/integrations/QPati_inkr
	../pack.sh hus prod

Asennuspaketti on tähän hakemistoon syntynyt QPati_inkr.tgz

## Lataa jar-tiedosto artifactoryyn 

	mvn deploy

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf QPati_inkr.tgz

# Asennus (manager) ja inkrementtilatausten käynnistäminen

	cd /opt/DataLake/QPati_inkr
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan, Kafkaan on luotu komponentin topic ja inkrementaalilataus on käynnistynyt. Käytännössä data kulkee nyt sftp-palvelimen csv-tiedostoista Kafkaan.

# Konfiguroi ja uudelleenkäynnistä Kafka Consumer

Jotta data tallentuisi Kafkasta myös Azure Datalake Storeen ja Hiveen, tulee Kafka Consumer (kConsumer) vielä konfiguroida komponentin suhteen ja käynnistää se uudelleen.

## Lisää komponentin Kafka-offset status-kantaan

Status-kannassa on taulu, kafka_offset_info, johon tulee lisätä komponentin tiedot. kConsumer-prosesseja voi olla useita (skaalautuva), joten kuorman tasaamiseksi tulee valita sopiva Kafka group_id (alla esimerkkinä "integration-consumer-group").

	insert into kafka_offset_info values ('qpati', 0, 0, 'integration-consumer-group');

## Pysäytä Kafka Consumer

Pysäytä kConsumer seuraavasti:

  1. Kirjaudu Ambariin selaimella: https//<nic-gateway-1 IP>/#/main/services/YARN/summary
  2. Klikkaa vasemman laidan listalta YARN-linkkiä
  3. Valitse ylhäällä keskellä valintalistalta "Quick Links"-linkki "<*.cloudapp.net (Active)"->"ResourceManager UI"
  

## Käynnistä Kafka Consumer uudelleen


# Inkrementaalilataukset

Inkrementaaliltataukset jdbc-pohjaisessa järjestelmässä perustuvat tietoaltaan kontrolloimista latauksista lähdejärjestelmästä ja sftp-pohjaisessa siirrossa lähdejärjestelmän lähettämien inkrementaalisten tiedostojen prosessointiin. Inkrementaalilatauksen prosessi käynnistyy käsin seuraavasti

	/opt/DataLake/lib/incremental_import.sh QPati

## Lopputila
* Lataus- ja varastotason ohjelmistot on asennettu ja valmiina ajettaviksi.
* Lataus- ja varastotason tietokannat ja taulut on luotu
* Inkrementaalilataukset ovat käynnissä
