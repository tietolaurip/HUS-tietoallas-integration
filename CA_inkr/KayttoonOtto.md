# Komponentin asennusohje / Käyttöönotto

# Esivaatimukset

Tietoallas infra ja peruspaketti (base) pitää olla asennettu. Komponentin "peruskomponentti" (komponentin nimi ilman "_inkr"-päätettä) tulee olla sennettu

# 1. Asennuspaketin luonti (kehitysympäristö)

Luo paketti seuraavasti

	cd <git>/integrations/CA
	../pack.sh hus [dev|test|prod]

Asennuspaketti on tähän hakemistoon syntynyt CA_inkr-hus-[dev|test|prod].tar.gz

# 2. Jar-tiedoston kopiointi artifactoryyn
Lataa jar-tiedosto artifactoryyn komennolla

	cd <git>/integrations/CA
	mvn deploy

# 3. Asennuspaketin siirto Tietoaltaan manager-nodelle (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf CA_inkr-hus-[dev|test|prod].tar.gz

# 4. Komponentin aktivointi (manager)

	cd /opt/DataLake/Opera_inkr
	./activate.sh

Aktivoinnin päätteeksi Ansible käynnistää inkrementaalilatauksen.

# Lopputila
* Komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan oikeilla palvelimilla
* Inkrementaalilataukset ovat käynnissä
