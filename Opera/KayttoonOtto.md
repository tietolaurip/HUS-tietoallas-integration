# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka Opera integraatiokomponentti otetaan käyttöön.

# Esiehdot

Seuraavat komponentit tulee olla asennettuna ensin:
- base
- StatusDb
- Metadata
- LoadTool

# Asennuspaketin Rakennus (kehitysympäristö)

Asennuspaketti rakennetaan kehitysympäristössä seuraavasti:

	cd <path_to_git>/integrations/Opera
	../package_component.sh -c [dev|test|prod] -w hus -i Opera

"dev/test/prod" valitaan sen mukaan mihin ympäristöön asennuspakettia luodaan.
Asennuspaketti on tähän hakemistoon syntynyt Opera.tgz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf Opera.tgz

# Asennus

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	cd /opt/DataLake/Opera
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan ja joissakin tapauksissa myös käynnistetty (stunnel). Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

Paketti ei kuitenkaan sisällä jdbc-ajureita, jotka pitää asentaa erikseen käsin.

# Komponentin metadatan asennus (manager)

Metadataa tarvitaan datan prosessoinnissa varastoon (mm. pseudonymisoinnissa). Lisäksi metadata tarjoaa loppukäyttäjille tietoa siirrettävän datan sisällöstä. 

OPTIONAL: Uudelleen asennuksessa (jos metadataan tullut muutoksia), vanha metadata pitää ensin poistaa. Se tapahtuu seuraavasti:

	/opt/DataLake/lib/metadata/remove_metadata.sh Opera

Metadatan asennus tietoaltaan tietokantoihin tapahtuu seuraavasti:

	/opt/DataLake/lib/metadata/upload_metadata.sh Opera

Lokia voi tarkastella ajosta (viimeisin upload_metadata*-tiedosto):

	cd /var/log/DataLake/Opera
	less upload_meatadata.sh.<latest-date>.log


# Tietokantojen luonti (headnode)

Selvyyden vuoksi on yleensä parasta aloittaa varmistamalla tietokantayhteyden toimivuus, koska myöhempien vaiheiden kompleksisuuden takia niistä voi olla hankala hahmottaa itse ongelmaa. Tämä vaihe ei kuitenkaan ole pakollinen.

Kirjaudu ssh:lla tietoaltaan pääkäyttäjänä tietoaltaan HD Insight headnodelle. Osoitteen saa esim. Azure portaalista katsomalla Public IP-olion jonka nimi alkaa publicIpheadnode. Riippuen palomuuriasetuksista tämä ei ehkä onnistu suoraan vaan pelkästään manager-palvelimen kautta.

	cd /opt/DataLake/Opera/import
	source ../config/config.sh
	./test_db_connection.sh

Yllä olvea skripti tulostaa onnistuessaan rivimäärän yhdestä Opera-taulusta.

# Tietokantojen luonti (headnode)

Aloita kirjautumalla headnodelle kuten luvussa "Tarkistetaan tietokantayhteys"

Opera-komponentti ei itse sisällä taulumäärityksiä, vaan ne ladataan tässä vaiheessa lähde-tietokannasta. Seuraava  komento luo staging- ja storage-tietokannat tauluineen lähderjärjestelmän kantojen perusteella:

	/opt/DataLake/lib/import/initial_setup.sh Opera

Tämän jälkeen tietokannat staging_opera ja opera, sekä niiden kaikki taulut ovat Hivellä nähtävissä.

# Alkulataus (headnode)

Alkulatauksessa ladataan lähdejärjestelmästä kaikki data nyky hetkeen asti. Alkulataus käynnistyy seuraavasti

	cd /opt/DataLake/lib/import/initial_import.sh Opera

Latauksen jälkeen tietoaltaassa on lähdejärjestelmästä integraation skooppiin kuuluva kaikki data lataushetkeen asti. 

### Inkrementaalisten latausten käynnistys (manager)

Kun alkulataus on tehty, voidaan käynnistää inkrementaaliset säännölliset lataukset. Ensin luodaan orkestrointiin ajettavat tehtävät, jotka lataavat muutokset viimeisestä aiemmin varmasti mukana olevasta aikaleimasta alkaen. Ajastettu inkrementtilatausten ajo käyttnistetään seuraavasti:

	cd /opt/DataLake/lib/orchestrate/create_incremental_jobs.sh Opera 0 0
	
Yllä olevalla komennolla lataukset suoritetaan kerran vuorokaudessa klo 00:00. 
