# Integraatio: Opera - Centricity Opera  (Operating Theatres Management Solution)
# 1. Yleiskuvaus
## 1.1. Opera - Centricity Opera  (Operating Theatres Management Solution)
Tämä dokumentti kuvaa Opera-järjestelmän integroinnin Tietoaltaaseen. Lisätietoja lähdejärjestelmästä löytyy alla:

  * [Opera-dokumentit HUS-extranetissä](https://extranet.hus.fi/teams/isaacus/esimaaritys/Lhdejrjestelmt/Forms/AllItems.aspx?RootFolder=%2Fteams%2Fisaacus%2Fesimaaritys%2FLhdejrjestelmt%2FOpera&FolderCTID=0x0120002C6263599A273442BC454DABC7D30105&View={8958753F-FB35-4DF1-B882-2A8E09F27811})
  * [Valmistajan (GE) tuotesivut](http://www3.gehealthcare.co.uk/en-gb/products/categories/healthcare_it/centricity-high-acuity-suite/centricity-opera)


## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
Opera-integraatiossa kytkeydytään vain yhteen lähdejärjestelmäinstanssiin. 
Integraatioiden yleiskuvaus kerrotaan kappaleessa:
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

Opera:n komponentit ovat:
* Opera (peruskomponentti)
* Opera_inkr (inkrementaalikomponentti)

## 2.3. Rajapintakuvaus

| Tietosisältö | Kontrolli | DB | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|---|
| Kaikki *, lataus näkymistä | Pull |MS SQL-server| JDBC | SQL | Näkymien kautta: Täysi lataus kaikelle datalle |24h |6h|

\* Lähdejärjestelmän tietyn version tietosisältö silloin kun integraatio on toteutettu ja asennettu. Lähdejärjestelmän päivityksien yhteydessä tapahtuvia tietomallin muutoksia integraatio ei tue ilman muutostyötä.
* [Opera_rajapinta.xlsx]()

HUOM! Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:
* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin (ja instanssin) lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti.

Alla lista käyttöönottodokumenteistä eri instansseille:

| Komponentti | Instanssi | Dokumentti |
|---|---|---|
| Peruskomponentti | Opera (yhteiskomponentti) | [KayttoonOtto.md](KayttoonOtto.md) |
| Inkrementaalikomponentti | Opera_inkr|[KayttoonOtto.md](../Opera_inkr/KayttoonOtto.md) |

## 2.5. Tietosuoja
Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna tämän integraation spesifiset asiat.**_

## 3.1. Integraation alustus
Opera-komponentin:n alustus tapahtuu kaikkien jdbc-integraatioiden tapaan seuraavasti: 

#### 3.1.1. [Jdbc-integraation alustus](../documentation/int_3_1_alustus_jdbc.md)

Käytännön ohjeet alustukseen löytyvät komponentin KayttoonOtto.md-dokumentista.

#### 3.1.2 Tietosisältö (tietomalli)
Raakadata- ja varastoaltaiden tietomalli pitää sisällään alla olevan taulukon csv-tiedostojen mukaiset taulut ja niiden kentät. Samasta lähdekannasta tietoa ammentavat peruskomponentti ja inkrementaalikomponentti ovat yhtenevät (sama tietomalli ja sama tietokanta) ja alustus tehdään aina peruskomponentin avulla (inkrementaalikomponentille ei tehdä alustus-proseduuria).

| Instanssi | Taulut | Sarakkeet |
|---|---|---|
| Opera | [table_metadata.csv](roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](roles/manager/files/metadata/column_metadata.csv) |

HUOM! Tietosisällön kuvaus ei kuulu avoimen lähdekoodin julkaisuun.

## 3.2. Integraation alkulataus (historiadatan lataus)
Tietoallas hakee datan koko Opera tietokannasta. Tietosisältö on laaja ja HUS:lla keskeinen ja mukana on myös monista muista järjestelmistä tullutta dataa.

Opera:n alkulataus tapahtuu samalla periaattella kuin kaikkien muiden jdbc-pohjaisten integraatioiden alkulataus:
* [Jdbc-integraation alkulataus](../documentation/int_3_2_alkulataus_jdbc.md)

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
Opera-integraatiossa käytetään tiedon siirtomekanismina Tietolataan kontrolloimaa jdbc/sql-latausta. Kyseisen integraatiotyypin inkrementaalilatauksen yleiskuvaus löytyy kappaleesta:
* [Jdbc-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_jdbc.md).

Opera-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin.

#### 3.3.1. Opera-spesifinen inkrementaalilogiikka (Pull)
Jotta inkrementaalilatauksessa ladataan vain uudet ja muuttuneet tietueet, tulee latausprosessille kertoa logiikka miten tämä onnistuu. Valitettavasti Operan näkymille ei löydy vedenpitävää logiikkaa, jolla muutokset saadaan näkymistä, joten tämän vuoksi joudutaan käyttämään täyslatausta (FULL_TABLE) kaikille näkymille.

###### A. FULL_TABLE - koko taulun lataus
Tauluille, joille löydetän taulun kenttien perusteella muutoksia, tehdään koko taulun lataus. Tässä integraatiossa sellaisia tauluja ovat (2 kpl):

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
Käytännössä Operan inkrementaalilataus on sama kuin tehtäisiin säännöllisesti alkulataus. Jokainen uusi kerran vuorokaudessa luotava tallenne varastoidaan Tietoaltaan Data Lake Storeen omaan kansioon. Asiakas voi päättää kuinka montaa täyslatausta järjestelmässä säilytetään. 

