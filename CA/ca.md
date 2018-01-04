# Integraatio: CA
# 1. Yleiskuvaus
## 1.1. CA - Centricity Anaesthesia
Tämä dokumentti kuvaa CA-järjestelmän integroinnin Tietoaltaaseen. Lisätietoja lähdejärjestelmästä löytyy alla:
  * [CA-dokumentit HUS-extranetissä](https://extranet.hus.fi/teams/isaacus/esimaaritys/Lhdejrjestelmt/Forms/AllItems.aspx?RootFolder=%2Fteams%2Fisaacus%2Fesimaaritys%2FLhdejrjestelmt%2FCA&FolderCTID=0x0120002C6263599A273442BC454DABC7D30105&View={8958753F-FB35-4DF1-B882-2A8E09F27811})
  * [Valmistajan (GE) tuotesivut](http://www3.gehealthcare.co.uk/en-gb/products/categories/healthcare_it/centricity-high-acuity-suite/centricity-high-acuity-anesthesia)

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
CA-integraatiossa kytkeydytään vain yhteen lähdejärjestelmäinstanssiin. 
Integraatioiden rakenteiden yleiskuvaus kerrotaan kappaleessa:
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

CA:n komponentit vat:
* CA (peruskomponentti)
* CA_inkr (inkrementaalikomponentti)

## 2.3. Rajapintakuvaus

| Tietosisältö | Kontrolli | DB | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|---|
| Kaikki *, lataus näkymistä | Pull | MS SQL-server| JDBC | SQL | Täysi | 24h | 1h |

\* Lähdejärjestelmän tietyn version tietosisältö silloin kun integraatio on toteutettu ja asennettu. Lähdejärjestelmän päivityksien yhteydessä tapahtuvia tietomallin muutoksia integraatio ei tue ilman muutostyötä.

HUOM! Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:

* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin (ja instanssin) lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti 

Alla lista käyttöönottodokumenteistä eri komponenteille:

| Komponentti | Instanssi | Dokumentti |
|---|---|---|
| Peruskomponentti | CA | [KayttoonOtto.md](KayttoonOtto.md) |
| Inkrementaalikomponentti | CA_inkr | [KayttoonOtto.md](../CA_inkr/KayttoonOtto.md) |

## 2.5. Tietosuoja
Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvaa pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna tämän integraation spesifiset asiat.**_

## 3.1. Integraation alustus

#### 3.1.1. [Jdbc-integraation alustus](../documentation/int_3_1_alustus_jdbc.md)

Käytännön ohjeet alustukseen löytyvät komponentin KayttoonOtto.md-dokumentista.

#### 3.1.2 Tietosisältö (tietomalli)
Raakadata- ja varastoaltaiden tietomalli pitää sisällään alla olevan taulukon csv-tiedostojen mukaiset taulut ja niiden kentät. Samasta lähdekannasta tietoa ammentavat peruskomponentti ja inkrementaalikomponentti ovat yhtenevät (sama tietomalli ja sama tietokanta) ja alustus tehdään aina peruskomponentin avulla (inkrementaalikomponentille ei tehdä alustus-proseduuria).

| Instanssi | Taulut | Sarakkeet |
|---|---|---|
| CA | [table_metadata.csv](roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](roles/manager/files/metadata/column_metadata.csv) |

HUOM! Tietosisällön kuvaus ei kuulu avoimen lähdekoodin julkaisuun.

## 3.2. Integraation alkulataus (historiadatan lataus)
Tietoallas hakee datan koko CA tietokannasta. Tietosisältö on laaja ja HUS:lla keskeinen ja mukana on myös monista muista järjestelmistä tullutta dataa.

CA:n alkulataus tapahtuu samalla periaattella kuin kaikkien muiden jdbc-pohjaisten integraatioiden alkulataus:  
* [Jdbc-integraation alkulataus](../documentation/int_3_2_alkulataus_jdbc.md)

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
CA-integraatiossa käytetään tiedon siirtomekanismina Tietolataan kontrolloimaa jdbc/sql-latausta. Kyseisen integraatiotyypin inkrementaalilatauksen yleiskuvaus löytyy kappaleesta:
* [Jdbc-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_jdbc.md).

CA-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin.

#### 3.3.1. CA-spesifinen inkrementaalilogiikka (Pull)
Jotta inkrementaalilatauksessa ladataan vain uudet ja muuttuneet tietueet, tulee latausprosessille kertoa logiikka miten tämä onnistuu. 

CA:lla on käytössä seuraavat latausmenetelmät taulukohtaisesti:

###### A. FULL_TABLE - koko taulun lataus
Tauluille, joille löydetän taulun kenttien perusteella muutoksia, tehdään koko taulun lataus. Tässä integraatiossa sellaisia tauluja ovat (2 kpl):

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
###### B. TIME_COMPARISATION - aikaleimavertailu
Aikaleimavertailuun perustuvat muutokset saadaan seuraavista tauluista (26 kpl):
```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
###### C. STATUSDB_TABLE_LOOKUP
Uudet rivit haetaan perustuen kasvavaan kokonaislukuavainsarakkeeseen. Avaimena käytetty sarake on talletettu metadataan (status-kanta, integration_status-taulu, key_column-sarake) Uudet rivit siis ovat ne, joilla <avain> on suurempi kuin mikään aiemmin ladattu. Tällä menetelmällä ladattavat taulut ovat
```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
