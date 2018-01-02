# Komponentin asennusohje / Käyttöönotto

# Esivaatimukset

Tietoallas infra ja peruspaketti (base) pitää olla asennettu. Komponentin "emokomponentti" (komponentin nimi ilman "_inkr"-päätettä) tulee olla sennettu

# Asennuspaketin rakennus (kehitysympäristö)

Luo paketti seruaavasti

```
cd <tuote>/integrations/Opera [dev|test|prod]
../package_component.sh -c [dev|test|prod] -w [tuote, esim. "hus"] -i Opera_inkr

```
Asennuspaketti on tähän hakemistoon syntynyt Opera_inkr.tgz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

	cd /opt/DataLake
	tar xzf Opera_inkr.tgz

# Asennus (manager)

	cd /opt/DataLake/Opera_inkr
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan ja käytettävissä. Itse datan inkrementaalilataus ei kuitenkin käynnisty vielä tässä vaiheessa.

# Inkrementaalilataukset

Inkrementaaliltataukset jdbc-pohjaisessa järjestelmässä perustuvat tietoaltaan kontrolloimista latauksista lähdejärjestelmästä ja sftp-pohjaisessa siirrossa lähdejärjestelmän lähettämien inkrementaalisten tiedostojen prosessointiin. Inkrementaalilatauksen prosessi käynnistyy käsin seuraavasti

	/opt/DataLake/lib/incremental_import.sh Opera

## Lopputila
* Lataus- ja varastotason ohjelmistot on asennettu ja valmiina ajettaviksi.
* Lataus- ja varastotason tietokannat ja taulut on luotu
* Inkrementaalilataukset ovat käynnissä
