# QPati asennusohje / Käyttöönotto

# Esivaatimukset

Tietoallas infra ja peruspaketti pitää olla asennettu.

# Asennuspaketin rakennus (kehitysympäristö)

Asennuspaketin rakentamiseen liittyvät yleiset tietoaltaan rakentamisvaatimukset [TODO: linkki]

```
cd <tuote>/integrations/QPati [dev|test|prod]
../package_component.sh -c [dev|test|prod] -w [tuote] -i QPati

```

Asennuspaketti on tähän hakemistoon syntynyt QPati.tgz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf QPati.tgz

# Asennus (manager)

	cd /opt/DataLake/QPati
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan ja käytettävissä. Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

# Tietokantojen ja taulujen luonti (headnode)

## Raakadata-altaan (staging) tietokannan ja taulujen luonti (headnode)

*HUOM! Nämä vaiheet suoritetaan headnodella!*

```
cd /opt/DataLake/QPati/staging/
./initial_setup.sh -v
```
"-v"-optiolla näet lokia mitä ajon aikana tapahtuu. .log-tiedostosta näkee paremmin hive-tapahtumat.

Tämän jälkeen tietokanta staging_qpati ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.

## Varastoaltaan (storage) tietokantojen ja taulujen luonti

	source /opt/DataLake/config/config.sh
	source /opt/DataLake/QPati/config/config.sh
	/opt/DataLake/lib/storage/initial_setup.sh QPati $COMP_STORAGE_URL $COMP_TABLE_NAMES_FILE

Tarkista lokitiedosto:

	more $(ls -d /var/log/DataLake/QPati/initial_setup_Storage* | tail -1)

Tämän jälkeen tietokanta qpati ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.

# Komponentin metadatan asennus (manager)

Metadataa tarvitaan datan prosessoinnissa varastoon (mm. pseudonymisoinnissa). Lisäksi metadata tarjoaa loppukäyttäjille tietoa siirrettävän datan sisällöstä. 

OPTIONAL: Uudelleen asennuksessa (jos metadataan tullut muutoksia), vanha metadata pitää ensin poistaa. Se tapahtuu seuraavasti:

	/opt/DataLake/lib/metadata/remove_metadata.sh QPati -v

Metadatan luonti (insert) tietoaltaan tietokantoihin tapahtuu seuraavasti:

	python ~/git/integrations/Metadata/tools/writeCsv2Db.py data_column column_metadata.csv INSERT
	python ~/git/integrations/Metadata/tools/writeCsv2Db.py data_table table_metadata.csv INSERT
	python ~/git/integrations/Metadata/tools/writeCsv2Db.py integration_status integration_status_metadata.csv INSERT

Jos data on jo luotu meatadatatauluihin, päivitys (update) voidaan tehdä seuraavasti:
	/opt/DataLake/lib/metadata/upload_metadata.sh QPati -v

Lokia voi tarkastella ajosta (viimeisin upload_metadata*-tiedosto):

	more $(ls -d /var/log/DataLake/QPati/upload_metadata* | tail -1)

Lopuksi ladataan mahdolliset korjaukset metadataan:

	/opt/DataLake/lib/metadata/upload_statusdata_from_sql.sh QPati /opt/DataLake/QPati/metadata/initialize_status_db.sql

# Alkulataus

Suuren datamassan (esim. vuosien) lataus suoritetaan erikseen käsin tiedon siirron varmisamiseksi, mutta periaateessa suoritus on samanlainen kuin normaalin inkrementaalilatauksen suhteen, eli lähderjärjestelmästä siirretään alkalatauksen tiedostosetti sftp-palvelimelle QPatin data-kansioon. 

# Inkrementaalilataukset

Lähdejärjestelmä lähettää tiedostot sftp-palvelimelle, josta ne siirretään hive-tietokantaan. Inkrementaalilataukset hoituvat komponentin QPati_inkr-toimesta - sen käyttöönotolle löytyy oma dokumenttinsa.

## Lopputila
* Lataus- ja varastotason ohjelmistot on asennettu ja valmiina ajettaviksi.
* Lataus- ja varastotason tietokannat ja taulut on luotu
* Metadata on asennettu
* Automaattista latausta ei ole tässä vaiheessa käynnistetty
