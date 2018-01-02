# LoadTool asennusohje / Käyttöönotto

# Esivaatimukset

* Tietoallas infra ja peruspaketti pitää olla asennettu.
* Metadata-komponentti
* StatusDb-komponentti 

# Asennuspaketin rakennus (kehitysympäristö)

Asennuspaketin rakentamiseen liittyvät yleiset tietoaltaan rakentamisvaatimukset

```
cd <tuote>/integrations/LoadTool [dev|test|prod]
../package_component.sh -c [dev|test|prod] -w [tuote] -i LoadTool

```

Asennuspaketti on tähän hakemistoon syntynyt LoadTool.tgz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

```
cd /opt/DataLake
tar xzf LoadTool.tgz
```
# Asennus (manager)

cd /opt/DataLake/LoadTool
./activate.sh

## Lopputila

Asennuksen jälkeen komponentin tarjoama jar-tiedosto on oikealla paikallaan headnodella (/opt/DataLaske/LoadTool/java). 


