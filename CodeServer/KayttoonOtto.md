# Komponentin asennusohje / Käyttöönotto

# Esivaatimukset

* Tietoallas infra ja peruspaketti (base) pitää olla asennettu. 


# Asennuspaketin rakennus (kehitysympäristö)

## Luo paketti seuraavasti

	cd <git-root>/integrations/CodeServer
	../pack.sh hus prod

Asennuspaketti on tähän hakemistoon syntynyt CodeServer-hus-prod.tar.gz

# Asennuspaketin vienti tietoaltaaseen

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf CodeServer-hus-prod.tar.gz

# Konfigurointi

Oikea konfiguraatio tiettyyn altaaseen on paketoitu asennuspakettiin. Erityissyistä, vaikkapa erikoiskokeiluja varten, tätä voi tässä vaiheessa muuttaa hakemistossa `/opt/DataLake/CodeServer/config`.

CodeServer integraatio ei poikkeuksellisesti sisällä salaisuuksia, joten niitä ei myös tarvitse konfiguroida.

# Asennus (manager) ja inkrementtilatausten käynnistäminen

	cd /opt/DataLake/CodeServer
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan ja cron-job on asetettu lataamaan koodistot kerran vuorokaudessa koodistopalvelimelta tietoaltaaseen.

Tämä voi kestää melko pitkään riippuen aikaisemmista asennuksista tähän altaaseen. Anna sille 15 minuuttia päästä loppuun. Keskeyttäminen saattaa jättää jotkin tietoaltaan VM:t tilaan, josta on hankala palata (rpmdb transaktio jää kesken). Virheen tapahtuessa näytölle tulle punaisella virhedetaljit.

# Tarkistetaan kommunikaatioyhteys CodeServeriin

Kirjaudu ssh:lla tietoaltaan pääkäyttäjänä tietoaltaan HD Insight headnodelle. Osoitteen saa esim. Azure portaalista katsomalla Public IP-olion jonka nimi alkaa publicIpheadnode. Riippuen palomuuriasetuksista tämä ei ehkä onnistu suoraan vaan pelkästään manager-palvelimen kautta.

```
cd /opt/DataLake/CodeServer/import
source ../config/config.sh
./test_connection.sh
```
, joka onnistuessaan tulostaa WSDL-kuvauksen CodeServeristä.

# Tietokantojen luonti

Aloita kirjautumalla headnodelle kuten luvussa "Tarkistetaan kommunikaatioyhteys CodeServeriin"

Seuraavat komennot luovat HDFS-hakemistorakenteen ja staging-tietokannan tauluineen konfiguroitujen koodistonimien perusteella
```
cd /opt/DataLake/CodeServer/import
./initial_setup.sh
```
Tämän jälkeen tietokanta staging_codeserver ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.

Seuraavaksi luodaan vastaava varastotason tietokanta staging-tietokannan perusteella
```
cd /opt/DataLake/CodeServer/storage
./initial_setup.sh
```
Tämän jälkeen tietokanta varasto_codeserver_historia_log ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.

# Alkulataus

CodeServer ei sisällä valtavasti dataa ja tällä hetkellä käytössä oleva koodistojoukko on vain pieni osa saatavilla olevista koodistoista. Alkulataus ei itse asiassa tekniseltä sisällöltään eroa inkrementaalisesta laatauksesta, mutta se on validointimielesssä hyvä tehdä ensin kerran käsin, jotta voidaan varmistua konfiguraation toimivuudesta uudessa ympäristössä.

```
cd /opt/DataLake/CodeServer/import
./incremental_import.sh $(date +%Y-%m-%d)
```

# Metadatan päivitys

Metadata päivitetään status-tietokantaan tietoaltaan manager-noodilla. Metatiedot luodaan skriptillä haluttujen koodistojen mukaisesti. Sama skripti vie metatiedot status-tietokantaan. Aja skripti seuraavasti: 
```
source /opt/DataLake/StatusDb/config/config.sh 
cd /opt/DataLake/CodeServer/metadata
./create_metadata_for_codesets.sh all_codesets
```


