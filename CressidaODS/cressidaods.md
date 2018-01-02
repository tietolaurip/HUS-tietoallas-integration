# Integraatio: CressidaODS
# 1. Yleiskuvaus
## 1.1. CressidaODS
Cressida ODS on Uranus-järjestelmän tietovarasto ja tietoaltaalle se näkyy Oracle tietokantana. Tämä dokumentti kuvaa CressidaODS-järjestelmän integroinnin Tietoaltaaseen. 

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvaus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
CressidaODS-integraatiossa on käytössä yksi historiatietokantainstanssi ja yksi inkrementtilatausinstanssi. Integraatioiden rakenteiden yleiskuvaus kerrotaan kappaleessa:
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

CressidaODS:n komponentit ovat:
* CressidaODS (peruskomponentti - historiadatan lataukseen)
* CressidaODS_inkr (inkrementaalikomponentti)

Aluksi historiadatana käytettiin pilottiratakaisussa käytetystä datasta, mutta datan ja metadatan oikeellisuuden varmistamiseksi haluttiin alkulataus tehdä uudelleen lähdejärjestelmästä. 

## 2.3. Rajapintakuvaus

## Rajapintavalinta

| Tietosisältö | Kontrolli | DB | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|---|
| Kaikki määritellyt | Pull | Oracle tietokanta | JDBC | SQL | Täysi | 24h |12h |

\* = Lähdejärjestelmän tietyn version tietosisältö silloin kun integraatio on toteutettu ja asennettu. Lähdejärjestelmän päivityksien yhteydessä tapahtuvia tietomallin muutoksia integraatio ei tue ilman muutostyötä.

* [Cressida_rajapinta.xlsx]()

HUOM! Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:
* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin (ja instanssin) lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti.

Alla lista käyttöönottodokumenteistä eri instansseille:

| Komponentti | Instanssi | Dokumentti |
|---|---|---|
| Peruskomponentti | CressidaODS | [KayttoonOtto.md](KayttoonOtto.md) |
| Inkrementaalikomponentti | CressidaODS_inkr | [KayttoonOtto.md](../CressidaODS_inkr/KayttoonOtto.md) |

## 2.5. Tietosuoja
Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna tämän integraation spesifiset asiat. **_

## 3.1. Integraation alustus
CressidaODS-komponenttien:n alustus tapahtuu kaikkien jdbc-integraatioiden tapaan seuraavasti: 

#### 3.1.1. [Jdbc-integraation alustus](../documentation/int_3_1_alustus_jdbc.md)

Käytännön ohjeet alustukseen löytyvät komponentin KayttoonOtto.md-dokumentista.

#### 3.1.2 Tietosisältö (tietomalli)
Tietoallas hakee tietomallin kuvaavan metadatan kaikista tauluista, joille Tietoaltaalle on annettu lukuoikeudet.  Näistä samaisista tauluista myös suoritetaan alkulataus ja inrekmentaalilataukset. Tietosisältö on laaja ja HUS:lla keskeinen ja mukana on myös monista muista järjestelmistä tullutta dataa.

Raakadata- ja varastoaltaiden tietomalli pitää sisällään alla olevan taulukon csv-tiedostojen mukaiset taulut ja niiden kentät. Samasta lähdekannasta tietoa ammentavat peruskomponentti ja inkrementaalikomponentti ovat yhtenevät (sama tietomalli ja sama tietokanta) ja alustus tehdään aina peruskomponentin avulla.

| Instanssi | Taulut | Sarakkeet |
|---|---|---|
| Clinisoft | [table_metadata.csv](roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](roles/manager/files/metadata/column_metadata.csv) |

HUOM! Tietosisällön kuvaus ei kuulu avoimen lähdekoodin julkaisuun.

## 3.2. Integraation alkulataus (historiadatan lataus)
CressidaODSn:n alkulataus tapahtuu samalla periaattella kuin kaikkien muiden jdbc-pohjaisten integraatioiden alkulataus:  
* [Jdbc-integraation alkulataus](../documentation/int_3_2_alkulataus_jdbc.md)

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
CressidaODS-integraatiossa käytetään tiedon siirtomekanismina Tietolataan kontrolloimaa jdbc/sql-latausta. Kyseisen integraatiotyypin inkrementaalilatauksen yleiskuvaus löytyy kappaleesta:
* [Jdbc-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_jdbc.md).

CressidaODS-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin.

#### 3.3.1. CressidaODS-spesifinen inkrementaalilogiikka (Pull)
Jotta inkrementaalilatauksessa ladataan vain uudet ja muuttuneet tietueet, tulee latausprosessille kertoa logiikka miten tämä onnistuu. 

CressidaODS tarjoaa jokaiselle taululle aikaleiman, jonka avulla voi näppärästi hakea taulun muutokset edellisen inkrementtilatauksen jälkeen. Tämän vuoksi CressidaODS-latauksissa käytetään vain yhtä latauslogiikkaa.

###### A. TIME_COMPARISATION - aikaleimavertailu
CressidaODS:n jokaisesta taulusta (paitsi yhdestä) löytyy sarake joka kertoo mitä dataa on muutettu (tai uusia). Näiden taulujen uudet ja muuttuneet rivit havaitaan hakemalla rivit joiden muutosaikaleima on suurempi kuin suurin aiemmin ladattu aikaleima. Latausmetodi on tällöin TIME_COMPARISATION.

Lista tauluista, joiden uudet ja muuttuneet rivit saadaan TIME_COMPARISON-menetelmällä:

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
###### B. FULL_TABLE - koko taulun lataus
FULL_TABLE-tyyppisen latauksen listalle joutuvat taulut, joiden lataukset ei onnistu kahden edellisen latausmekanismin avulla. Eli tällä listalle joutuu taulut, joista ei löydä muutoksia lainkaan tiedossa olevan datan avulla. Ja jos muutoksia ei tiedetä, on vaihtoehtona ladata joko kaikki tai ei mitään. Tässä tapauksessa ladataan kaikki data.

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
