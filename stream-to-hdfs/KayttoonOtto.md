# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka stream-to-hdfs komponentti otetaan käyttöön. Otsikon perässä suluissa kerrotaan millä palvelimella kohdan suoritus tulee tehdä.

# Esiehdot

  * Seuraavat komponentit tulee myös olla asennettuna (voidaan myös asentaa tämän integraatiokomponentin jälkeen):
    * <jokin integraatio, jonka sanomaajonon dataa pseudonymisoidaan>

# Asennuspaketin luonti (kehitysympäristö)

Asennuspaketti rakennetaan kehitysympäristössä seuraavasti:

	cd <path_to_git>/integrations/
	../pack.sh hus  [dev|test|prod]

"dev/test/prod" valitaan sen mukaan mihin ympäristöön asennuspakettia luodaan.
Asennuspaketti on tähän hakemistoon syntynyt stream-to-hdfs-hus-[dev|test|prod].tar.gz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf stream-to-hdfs-hus-[dev|test|prod].tar.gz

# Komponentin asennus (manager)

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	cd /opt/DataLake/stream-to-hdfs
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan. Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

# Käynnistä komponentti (headnode)

	/opt/DataLake/stream-to-hdfs/start_consumer.sh

# Lopputilanne

Kun asennus on tehty, niin komponentti lukee sille konfiguroitujen sanomajonojen dataa ja kirjoittaa datan Azure Datalake Storeen
