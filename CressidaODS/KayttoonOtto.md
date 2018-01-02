# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka CressidaODS integraatiokomponentti otetaan käyttöön.

# Esiehdot

TODO:

# Asennuspaketin Rakennus

Asennuspaketin rakentamiseen liittyvät yleiset tietoaltaan rakentamisvaatimukset [TODO: linkki]

```
cd hus/integrations/CressidaODS
./package.sh

```

Asennuspaketti on tähän hakemistoon syntynyt CressidaODS.tgz

# Asennuspaketin vienti tietoaltaaseen

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta (esim. HUS:n testialtaassa huste).

```
cd /opt/DataLake
tar xzf CressidaODS.tgz
```

# Konfigurointi

Oikea konfiguraatio tiettyyn altaaseen on paketoitu asennuspakettiin. Erityissyistä, vaikkapa erikoiskokeiluja varten, tätä voi tässä vaiheessa muuttaa hakemistossa `/opt/DataLake/CressidaODS/config`.

Paketisssa mukana tuleva konfiguraatio ei kuitenkaan sisällä salaisuuksia, esim. salasanoja, joita komponentti tarvitsee toimiakseen. Tietoallas tallentaa salaisuudet salattuna Java Cryptography Extension Key Store (JCEKS)-standardin mukaisiin tiedostoihin.

CressidaODS tarvitsee vain yhden salaisuuden, eli salasanan käytettyyn Cressdida ODS Oracle-tietokantaan. Tämä tallennetaan tietoaltaan käyttämään muotoon seuraavasti. Toinen komento kysyy salasanaa ja sen varmistusta.
```
mkdir -p -m 0700 /opt/DataLake/secrets/CressidaODS
hadoop credential create jdbc -provider jceks://file/opt/DataLake/secrets/CressidaODS/CressidaODS.jceks```
TODO: Tämä toimii tällä hetkellä vain Hadoop-noodeissa eikä managerilla, joten se pitää tehdä siellä ja kopioida managerille. Näppärämpi keino olisi paikallaan.

# Asennus

Kun komponentin käyttämä konfiguraatio salaisuuksineen on luotu, voidaan komponentti asentaa käyttöön:
```
cd /opt/DataLake/CressidaODS
./activate.sh
```

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan ja joissakin tapauksissa myös käynnistetty (stunnel). Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

Paketti ei kuitenkaan sisällä Oraclen JDBC ajuria vaan se pitää hankkia ja asentaa erikseen. Tätä kirjoittaessa oikea ajuri on ojdbc6.jar Oraclen sivuilta http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html . Tämä pitää kopioida käyttöön headnodeille hakemistoon /usr/hdp/current/sqoop-client/lib/
TODO: pistä tämän asennus paikalleen osaksi ansiblea kunhan se laitetaan käsin tiettyyn paikkaan

Huom! DEV ympäristöä suoritettavat automaatio testit käytävät simuloitua lähdejärjestelmää jonka kantana on MySQL. MySQL sivuilta pitää hakea ajuri
esim. mysql-connector-java-5.0.8-bin.jar (https://dev.mysql.com/downloads/connector/j/5.0.html). Tämä pitää kopioida käyttöön headnodeille hakemistoon
 /usr/hdp/currrent/sqoop-client/lib/. Poista kyseisestä hakemistosta mahdollisesti löytyvä ajurin linkkifile (mysql-connector-java.jar -> /usr/share/java/mysql-connector-java.jar). 
Lisäksi tulee huomioida ettäs sqoop mahdollisesti vaatii parametrin "--driver com.mysql.jdbc.Driver".

Lisäksi 2 Azure Blob Storage containeria CressidaODS:n dataa varten pitää luoda erikseen käyttäen riittävillä oikeuksilla varustettua Azure tunnusta. Nämä voi tehdä joko portal.azure.com:n, Azure CLI:n tai Azure Powershell moduulin kautta. Luo uusi container nimeltä 'cressidaods' sekä staging storage accountin (esim. huste1maindlsta) että storage storage accountin (esim. huste1maindlsto) alle. Molemmat pitaa olla tyyppia private.

# Tarkistetaan Tietokantayhteys

Selvyyden vuoksi on yleensä parasta aloittaa varmistamalla tietokantayhteyden toimivuus, koska myöhempien vaiheiden kompleksisuuden takia niistä voi olla hankala hahmottaa itse ongelmaa. Tämä vaihe ei kuitenkaan ole pakollinen.

Kirjaudu ssh:lla tietoaltaan pääkäyttäjänä tietoaltaan HD Insight headnodelle. Osoitteen saa esim. Azure portaalista katsomalla Public IP-olion jonka nimi alkaa publicIpheadnode. Riippuen palomuuriasetuksista tämä ei ehkä onnistu suoraan vaan pelkästään manager-palvelimen kautta.

```
cd /opt/DataLake/CressidaODS/import
source ../config/config.sh
./test_db_connection.sh
```
, joka onnistuessaan tulostaa rivimäärän yhdestä Cressida ODS-taulusta.

# Tietokantojen luonti

Aloita kirjautumalla headnodelle kuten luvussa "Tarkistetaan tietokantayhteys"

Hakemistossa `/opt/DataLake/CressidaODS/import` on kaikki lataamiseen tarvittavat ohjelmat.

CressidaODS-komponentti ei itse sisällä taulumäärityksiä, vaan ne ladataan tässä vaiheessa Oracle-tietokannasta. Seuraavat komennot luovat staging-tietokannan tauluineen Oraclen vastaavien perusteella:
```
cd /opt/DataLake/CressidaODS/import
./initial_setup.sh
```
Tämän jälkeen tietokanta staging_cressidaods ja sen kaikki taulut tyhjinä ovat Hivellä nähtävissä.

Seuraavaksi luodaan vastaavat varastotason tietokannat staging-tietokannan perusteella
```
cd /opt/DataLake/CressidaODS/storage
./initial_setup.sh
```
Tämän jälkeen tietokannat varasto_cressidaods_historia_inkr, varasto_cressidaods_historia_snap, varasto_cressidaods_historia_uusin ja niiden kaikki taulut tyhjinä ovat Hivellä nähtävissä.

# Alkulataus

Koko HUS Cressida ODS-tietokannan alkulataus ei ole pilottiprojektin jälkeen tehty. Pilottikokemusten perusteella se on pitkäkestoinen ja herkkä operaatio, jossa joudutaan käsin jakamaan taulujen sisältöä osiin, jotta lataukstransaktioista saadaan tarpeeksi nopeita, jotta Oraclen dataversiointi ei katkaise latausta.

Jos käytetty Oracle-kanta on tarpeeksi pieni tai muuten on syytä uskoa kokonaisten taulujen latauksen toimivan, niin koko Cressida ODS Oracle-tietokannan lataus voidaan käynnistää näin, kun halutaan latauspäivämääräksi nykyhetki UTC-aikavyöhykkeellä. Tämä on normaalisti haluttu aikaleima lataukselle, mutta jos lataus jaetaan osiin, pitää mahdollisesti myös näitä sovittaa.

```
cd /opt/DataLake/CressidaODS/import
./initial_import.sh $(date +%Y-%m-%d)
```

Testikäyttöön alkulatauksessa voi myös ladata pienemmän osan (esim. lyhyemmän historian) lähdetietokannasta tai jopa jättää lataamatta mitään.

```
cd /opt/DataLake/CressidaODS/storage
./process_to_storage.sh $(date +%Y-%m-%d)
```

### Inkrementaalisten latausten käynnistys

Kun alkulataus on tehty, voidaan käynnistää inkrementaaliset säännölliset lataukset. Ensin luodaan orkestrointiin ajettavat tehtävät, jotka lataavat muutokset viimeisestä aiemmin varmasti mukana olevasta aikaleimasta alkaen. Alkulatauksen jälkeen tämä on yleensä alkulatauksen aloitusaika.
```
cd /opt/DataLake/CressidaODS/import
./create_incremental_jobs.sh <Viimeinen varmasti mukana oleva aikaleima>
```

Inkrementaaliset latausten ajastettu käynnistäminen orkestrointiin lisätään näin:
```
cd /opt/DataLake/CressidaODS/orchestration
./add_to_orchestration.sh
