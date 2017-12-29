# Integraatiokuvien Komponentit

## Asiakas (Customer
Asiakkaalla tarkoitetaan järjestelmän tilaajaa, joka omistaa datan ja laatii vaatimukset järjestelmälle. Datan yksityisyyden suojaan liittyvä Pseudonymisointi-tieto määritellään asiakkaan toimesta (ellei lähdejärjestelmä sitä jo pysty antamaan).

## Lähdejärjestelmä (Source system)
Lähdejärjestelmä ja tietovarasto, josta dataa ladataan tietoaltaaseen..

## Tietoallas (Datalake)
Tietoallas on kohdejärjestelmä, johon lähdejärjestelmän data kerätään tietolähteen arkkitehtuurin ja toimintaperiaatteiden mukaisesti (TODO: Linkki Tietoaltaan arkkitehtuurikuvaukseen) 

### Manager (Manager-vm)
Tietoaltaan palvelin, jonka kautta järjestelmän komponentit asennetaan keskitetysti.

### Sftp (Sftp-vm)
Sftp-palvelin, jonne lähdejärjestelmä lataa datatiedostoja sftp:llä integroinnin käyttämässä formaatissa. Jokaiselle lähdejärjestelmälle luodaan oma tunnus, jota käyttäen tieto ladataan palvelimelle sftp-protokollaa käyttäen. Tunnistautuminen tapahtuu SSH-avaimilla (private key, public key).

#### Datatiedostot (Tiedostot)
Lähdejärjestelmän tiedostot tallennetaan sftp-palvelimen kryptatulle levylle sovitussa tiedostoformaatissa ja sovitun nimeämiskäytännön mukaisesti (Integraatiospesifinen rajapinta-dokumentti). Sijaintina on lähdejärjestelmän tunnuksille luodun käyttäjän kotihakemiston data-kansio. Järjestelmän pääkäyttäjälle sijainti näkyy muodossa: /mnt/datadisk/<käyttäjänimi>/chroot/data. 

#### Metadata-tiedostot
Integraation metadata-tiedostot, joista data ladataan metadata-tietokantaan.

### Päätietoallas (Maindatalake, Headnode-vm)
Headnode tarjoaa big data järjestelmän (Azure pilvestä), jonne tietoaltaan kaikki data talletetaan.

#### Azure Datalake Store
Azure Datalake Store on tietojärjestelmä, jonne data fyysisesti talletetaan.

#### Hive
Hive on hajautettu bigdata tietojärjestelmä, josta voi sql-rajapinnan avulla lukea dataa. Alla mainitut data-altaat ovat käytännössä hive-tietokantoja.

#### Raakadata-allas
Tietoaltaan osa, jonne lähdejärjestelmän raakadata talletetaan.

#### Varasto-allas
Tietoaltaan osa, jonne raakadata prosessoidaan loppukäyttäjien tarpeita varten. Raakadata-altaaseen verrattuna varasto-altaassa arkaluontoinen tieto on pseudonymisoitu tai poistettu. Varasto-altaan avulla luodaan tutkijan sivutietoaltaat.

### Metadata
Integraatiolle specifinen metadata varastoidaan ja sitä ylläpidetään Metadata-kannassa (Azuressa nimeltään status).

### Kafka
Kafka on hajautettu reaaliaikaiseen sanomapohjaiseen tiedon keruuseen ja varastointiin tarkoitettu järjestelmä [[Kafka]](https://kafka.apache.org/intro). 

### Kafka Producer (kProducer)
Kafka producer on integraatiospesifinen mikropalvelu, joka vastaa datan latauksesta ja ohjaa ladatun datan Kafkan tietokantaan Kafka topicille (integraatiokohtainen). 

### Kafka (store)
Kafkalle lähetetyt sanomat varastoituvat Kafka storeen.

### Kafka Consumer (kConsumer)
kConsumer on geneerinen prosessi, joka lukee eri integraatioiden dataa Kafkalta. kConsumer-instanssille (1-n) kerrotaan mitä Kafka-topic:ia (integraatiota) se kuuntelee ja kun topicille löytyy dataa, se alkaa käsittelemään dataa eteenpäin, eli kirjoittaa datan Azuren Data Lake Storeen, hiven raakadata-altaaseen ja edelleen pseudonymisoituna hiven varastoaltaaseen. kConsumer-prosessi on skaalautuva.

### Kehitysympäristö - Local-vm
Kehitysympäristössä tapahtuu integraation toteutus.

### Jar artifactory - JFrog
Pilvipalvelu, jonne komponenttien jar-tiedostot säilötään.

### GIT
Versionhallintatyökalu, jonne talletetaan komponenttien koodi.
