# Tietolähdeintegraatiot -Tekniikka
Tämä sivu kuvaa yleisiä teknisiä valintoja, joita pääsääntöisesti kaikki tietolähdeintegraatiot noudattavat. Poikkeaminen näistä on tarpeen vaatiessa mahdollista, mutta kokonaisuuden ymmärrettävyyden ja ylläpidettävyyden takia vältettävää.

# 1. Komponentisointi
Kaikki Tietoaltaan erikoistunut toiminta on jaettu komponentteihin. Komponentit ovat toiminnallisia ohjelmisto- ja tietokantakokonaisuuksia, voivat esimerkiksi ladata tietoa altaaseen, jalostaa tietoa altaassa, ladata tietoa ulos altaasta, toteuttaa erilaisia rajapintoja ihmisiä ja muita järjestelmiä varten.

Alkuvaiheessa suurin huomio on komponenteilla, jotka lataavat tietoa sisään altaaseen muista järjestelmistä ja tekevät tiedolle vain hyvin matalan tason jalostusta. Tällaisia komponentteja kutsutaan integraatioiksi tai integraatiokomponenteiksi. Integraatioita kuvataan omalla sivullaan: Integrations

Tiedonjalostuskomponentit ovat komponentteja, jotka yhdistelevät ja prosessoivat tietoa useista lähteistä mahdollisesti hyvinkin monimutkaisella tavalla ja tallentavat tuloksen itse tietoaltaaseen tai johonkin ulkoiseen järjestelmään. Esimerkki tällaisesta komponentista on pseudonymisointi-komponentti. Pseudonymisoinnista kerrotaan enemmän omassa kappaleessaan: Pseudonymisointi

Integraatiokomponentit ovat tietoallaskomponentteja, joiden elinkaaritoiminnot toimivat yleisen komponenttielinkaarimallin mukaan. Integraatiokomponentit ovat kuitenkin melko tiukkaan sidottuja vastapuoleensa lähdejärjestelmässä, joten erityisesti käyttöönotto on merkittävästi mutkikkaampi operaatio kuin pelkkä asennus. 

# 2. Tallennus
Integraatio tallentaa alkulatauksessa dataa ainakin kahdessa vaiheessa ja ainakin kahteen paikkaan: staging/lataus ja varasto/storage. Inkrementaalilatauksissa data tallentuu ennen staging/lataus-vaihetta suurimmasta osasta integraatiota vielä Kafkaan.

Kaikki raakadata joka saadaan lähdejärjestelmästä tallennetaan Staging Azure Datalake Storageen (ADLS). Jokaisella integraatiolla on hakemistorakenne ADLS:ssä, mikä eristää datat toisistaan.

# 3. Rajapintatekniikat
Jokaisen integraation kanssa sovitaan rajapintatekniikat, tiedostomuodot, päivitysvälit ja muut detailjit sopiviksi ja kirjataan ne rajapintalomakkeeseen. 

### 3.1. JDBC
Tässä integraatiotyypissä data pyydetään SQL kyselyinä JDBC:n yli. Tämä rajapintatekniikka siis käytännössä kiinnittää myös datarakenteen relaatiomaiseksi.
 
Jokaista tietokantavalmistajaa varten on oma JDBC-ajurinsa, joka pitää asentaa altaaseen. 

**JDBC pull-tekniikka on integraatioden toteutuksen kannalta helpoin ja tehokkain ja ensisijaisesti suositeltu tekniikka.**

### 3.2. Tiedostonsiirto
Tässä integraatiotyypissä lähdejärjestelmä kopioi tiedostoja tietoaltaaseen SFTP:llä. Tätä varten on tälle integraatiotyypille dedikoitu virtuaalipalvelin `sftp-vm`, joka väliaikaisesti tallentaa datan omalle levylleen. Tästä data integraatio siirtää datan eteenpäin prosessoitavaksi ja tämän jälkeen data poistetaan SFTP palvelimen levyltä.

Tietoaltaassa on tätä varten jokaista lähdejärjestelmää varten oma käyttäjätunnus. Tietolähteelle sallitaan vain SFTP:n käyttö, eikä esimerkiksi shell-käyttöä ja näiden käyttäjien näkemä osa tiedostojärjestelmästä on chroot-tekniikalla rajattu vain omaan kotihakemistoon. Nämä kotihakemistot ovat kryptatulla levyllä.

### 3.3. SOAP
Tässä integraatiotyypissä data pyydetään SOAP Web Serviceltä ja saadaan SOAP sanomana ja siis XML-koodattuna takaisin. Vahvan XML-kytköksen takia SOAP-tekniikalla saadulle datalle pitää yleensä tehdä konversio relaatiomalliin.

### 3.4. REST
Tämä integraatiotyyppi on tällä hetkellä suunniteltu käytettävän vain streaming-tyyppisissä integraatioissa. Näissä jokainen tapahtuma tai muutos lähetetään omana HTTP-viestinään lähdejärjestelmästä tietoaltaalle.

# 4. Tietomallitransformaatiot
Tietoallas tarjoaa varastotasolla Hive-yhteensopivan näkymän kaikista tietolähteistä, riippumatta tietolähteen tietomallista. Tämä vaatii lähdejärjestelmän tietomallista riippuen enemmän tai vähemmän prosessointia ja alla on kuvattu näiden erityispiirteitä.

## 4.1. [SQL (Structured Query Language)](https://fi.wikipedia.org/wiki/SQL)
Hive seuraa relaatiotietokantojen mallia itsekin, joten tämä on kaikkein suoraviivaisin käsiteltävä. Pääsääntöisesti tietolähteen taulut ja näkymät ja niiden sarakkeet kuvautuvat tauluiksi ja sarakkeiksi sekä lataus- että varastotasolle. 

## 4.2. [XML (eXtensible Markup Language)](https://fi.wikipedia.org/wiki/XML)
XML on periaatteellisesti hierarkinen rakenne. Yleensä elementit viittaavat toisiinsa kuitenkin myös muilla kuin hierarkisilla linkeillä ja tähän on myös enemmän tai vähemmän standardeja XML-laajennuksia, kuten [DITA xref](https://en.wikipedia.org/wiki/Darwin_Information_Typing_Architecture).

Yleensä pyritään suoraviivaiseen kuvaukseen, jossa elementtityypit kuvautuvat tauluiksi ja attribuutit sarakkeiksi. Hierarkiset linkit ja mahdollisesti muutkin linkit kuvautuvat viiteavaimiksi.

XML:lle on olemassa myös Hadoop SerDe(jä), mutta eivät näytä käsittelevän tämän tyyppisiä tapauksia tarpeeksi hyvin. Tarpeeksi yksinkertaisissa tapauksissa tämä voisi olla hyvä vaihtoehto.

Joissakin tapauksissa XML-rakenne on liian kompleksinen sen muuntamiseksi hive-kannassa käytettyyn relaatiomalliin - tällöin XML-tiedosto pitää tallettaa räätälöidysti muualle kuin hive-kantaan.

## 4.3. [JSON (JavaScript Object Notation)](http://www.json.org/)
JSON muistuttaa tässä mielessä paljon XML:ää ja se prosessoidaan.

JSON:lle on olemassa myös Hadoop SerDe(jä), mutta eivät näytä käsittelevän tämän tyyppisiä tapauksia tarpeeksi hyvin. Tarpeeksi yksinkertaisissa tapauksissa tämä voisi olla hyvä vaihtoehto.

## 4.4. [CSV (Comma Separated Values)](https://fi.wikipedia.org/wiki/CSV)
CSV-muoto on valmiiksi taulumuotoista ja siten helposti Hiveen sovitettavaa. CSV:n tapauksessa kuitenkin lähes kaikki rakennemetadata puuttuu, joten se pitää kaikki laittaa käsin kohdalleen. Näitä ovat esimerkiksi taulunimet, sarakenimet, saraketyypit, merkkijonokoodaukset, lainaus ja/tai pakokäytännöt. 
CSV-datan erotinmerkki voi vaihdella integraatiokohtaisesti ja se kirjataan rajapintadokumenttiin. On suositeltua käyttää erotinta, joka ei muuten esiinny datakenttien sisällössä.

## 4.5. [ORC (Optimized Row Columnar)](https://orc.apache.org/)
ORC-muoto on tehokas tapa tiedon tallennamiseen. Tietoaltaassa ORC-muotoa käytetään tiedon lopullisena tallennusmuotona. Hadoop pystyy prosessoimaan dataa ORC-tiedostoista nopeasti ja tiedostojen koko pysyy mahdollisimman pienenä. 
