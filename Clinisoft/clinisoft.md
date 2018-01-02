# 1. Yleiskuvaus
## 1.1. Integraatio: Clinisoft - Centricity Anaesthesia
Tässä dokumentissä kuvataan Clinisoft-järjestelmän integrointi tietoaltaaseen. 

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
Clinisoft-integraatiossa on käytössä neljä historiatietokanta-instanssia ja neljä inkrementti-instanssia. Integraatioiden yleiskuvaus kerrotaan kappaleessa:

* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

Integroidut historiatietokanta-insanssit Clinisoftlle ovat:
* Clinisoft_jorvi
* Clinisoft_meilahti
* Clinisoft_lns_kvii
* Clinisoft_lns_kix

Jokaisen eri Clinisoft-instanssin datan tallentamiseen päädyttiin käyttämään erillisiä tietokantoja, johtuen siitä, että eri sairaaloissa voi olla hieman erilaisia käytäntöjä itse metadatan/koodien arvoille ja eri instansseilla järjestelmien päivitysaikataulut ovat todennäköisesti erilaiset.

Clinisoftin inkrementaalikomponenttien nimet ovat samat kuin alkulatauskomponentit.

## 2.3. Rajapintakuvaus

| Tietosisältö | Kontrolli | DB | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|---|
| Kaikki määritellyt *, lataus tauluista | Pull | SAP ASE (Sybase) | JDBC | SQL | Osalle tauluista täyslataus, osalle inkrementaalilataus | 24h, lataus 05:00 | 1 h |

\* = Lähdejärjestelmän tietyn version tietosisältö silloin kun integraatio on toteutettu ja asennettu. Lähdejärjestelmän päivityksien yhteydessä tapahtuvia tietomallin muutoksia integraatio ei tue ilman muutostyötä.

HUOM! Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:

* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin (ja instanssin) lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti.

Alla lista käyttöönottodokumenteistä eri komponenteille:

| Komponentti | Instanssi | Dokumentti |
|---|---|---|
| Peruskomponentti | Clinisoft (yhteiskomponentti) | [KayttoonOtto.md](KayttoonOtto.md) |
| Peruskomponentti | Clinisoft_jorvi | [KayttoonOtto.md](../Clinisoft_jorvi/KayttoonOtto.md)
|  Peruskomponentti | Clinisoft_meilahti | [KayttoonOtto.md](../Clinisoft_meilahti/KayttoonOtto.md) |
| Peruskomponentti |  Clinisoft_lns_kvii |  [KayttoonOtto.md](../Clinisoft_lns_kvii/KayttoonOtto.md) |
| Peruskomponentti | Clinisoft_lns_kix | [KayttoonOtto.md](../Clinisoft_lns_kix/KayttoonOtto.md) |
| Peruskomponentti | Clinisoft_jorvi_inkr | [KayttoonOtto.md](../Clinisoft_picis82/KayttoonOtto.md) |
| Inkrementaalikomponentti | Clinisoft_inkr (yhteiskomponentti) | [KayttoonOtto.md](../Clinisoft_inkr/KayttoonOtto.md) |
| Inkrementaalikomponentti | Clinisoft_jorvi_inkr | [KayttoonOtto.md](../Clinisoft_jorvi_inkr/KayttoonOtto.md) |
| Inkrementaalikomponentti | Clinisoft_meilahti_inkr | [KayttoonOtto.md](../Clinisoft_meilahti_inkr/KayttoonOtto.md) |
| Inkrementaalikomponentti | Clinisoft_lns_kvii_inkr | [KayttoonOtto.md](../Clinisoft_lns_kvii_inkr/KayttoonOtto.md) |
| Inkrementaalikomponentti | Clinisoft_lns_kix_inkr | [KayttoonOtto.md](../Clinisoft_lns_kix_inkr/KayttoonOtto.md) |

## 2.5. Tietosuoja
Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvaa pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna tämän integraation spesifiset asiat.**_

## 3.1. Integraation alustus
CA-komponentin:n alustus tapahtuu kaikkien jdbc-integraatioiden tapaan seuraavasti: 
* [Jdbc-integraation alustus](../documentation/int_3_1_alustus_jdbc.md)

Käytännön ohjeet alustukseen löytyvät komponentin KayttoonOtto.md-dokumentista.

Raakadata- ja varastoaltaiden tietomalli pitää sisällään alla olevan taulukon csv-tiedostojen mukaiset taulut ja niiden kentät. Samasta lähdekannasta tietoa ammentavat peruskomponentti ja inkrementaalikomponentti ovat yhtenevät (sama tietomalli ja sama tietokanta) ja alustus tehdään aina peruskomponentin avulla (inkrementaalikomponentille ei tehdä alustus-proseduuria).

Käytännössä eri instanssien metadata ovat yhtenevät (samat lähdejärjestelmäversiot käytössä tietointegraation ensimmäisessä versiossa)

| Instanssi | Taulut | Sarakkeet |
|---|---|---|
| Clinisoft_jorvi | [table_metadata.csv](../Clinisoft_jorvi/roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](../Clinisoft_jorvi/roles/manager/files/metadata/column_metadata.csv) |
| Clinisoft_meilahti | [table_metadata.csv](../Clinisoft_meilahti/roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](../Clinisoft_meilahti/roles/manager/files/metadata/column_metadata.csv) |
| Clinisoft_lns_kvii | [table_metadata.csv](../Clinisoft_lns_kvii/roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](../Clinisoft_lns_kvii/roles/manager/files/metadata/column_metadata.csv) |
| Clinisoft_lns_kix | [table_metadata.csv](../Clinisoft_lns_kix/roles/manager/files/metadata/table_metadata.csv) | [column_metadata.csv](../Clinisoft_lns_kix/roles/manager/files/metadata/column_metadata.csv) |

HUOM! Metadatan sisältö ei kuulu avoimen lähdekoodin julkaisuun.

## 3.2. Integraation alkulataus (historiadatan lataus)
Clinisoftn:n alkulataus (jokaiselle instanssille erikseen) tapahtuu samalla periaattella kuin kaikkien muiden jdbc-pohjaisten integraatioiden alkulataus: 

* [Jdbc-integraation alkulataus](../documentation/int_3_2_alkulataus_jdbc.md)

Clinisoft-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin. Jokaiselle instanssille käynnistetään erillinen alkulatausprosessi.

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
Clinisoft-integraatiossa käytetään tiedon siirtomekanismina Tietolataan kontrolloimaa jdbc/sql-latausta. Kyseisen integraatiotyypin inkrementaalilatauksen yleiskuvaus löytyy kappaleesta:

* [Jdbc-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_jdbc.md).

Clinisoft-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin. Jokaiselle instanssille käynnistetään erillinen inkrementaalilatausprosessi.

#### 3.3.1. Clinisoft-spesifinen inkrementaalilogiikka (Pull)
Clinisoftin arkistokantaan ei tule muutoksia, joten riittää, että ladataan vain uudet tietueet inkrementaalilatauksissa. Käytännössä lataus tehdään kolmesta eri tietokannasta:

1. Departments ("D_"-alkuiset taulut)
2. System ("S_"-alkuiset taulut, paitsi versionhistory-taulu)
3. Patients ("P_"-alkuiset taulut)

Clinisoftille on käytössä kaksi eri logiikkaa (A ja B), joilla ladataan tauluista uutta dataa ja vielä kolmas (C), jolla ladataan kaikki data. Nämä kolme logiikkaa on kuvattu alla:

###### A TIME_COMPARISATION - aikaleimavertailu
Joissakin usein päivittyvissä tauluissa on suoraan käyttökelpoiset muutosaikaleimat. Näiden taulujen uudet ja muuttuneet rivit havaitaan hakemalla rivit joiden muutosaikaleima on suurempi kuin suurin aiemmin ladattu aikaleima. Latausmetodi on tällöin TIME_COMPARISATION.

Lista tauluista, joiden uudet ja muuttuneet rivit saadaan TIME_COMPARISON-menetelmällä (1 kpl):
```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
###### B. HISTORY_TABLE_LOOKUP - uudet rivit kokonaislukuavainsarakkeella
Clinisoftin live-kannasta arkistoidaan valmistuneet potilaskäynnit kolmen vuorokauden viiveellä. Tämä HISTORY_TABLE_LOOKUP latauslogiikka toimii käytännössä seuraavasti (haut kahdesta taulusta):

1. Haetaan lista P_DischargeData-taulun potilasjaksoista (P_DischargeData.patientid), joissa kotiutusaika (P_DischargeData.DischargeTime) on suurempi kuin edellinen inkrementaalilataushetki, ja jotka on merkitty  "arkistoiduiksi" P_Generaldata-taulussa (P_Generaldata.status = 8). Tämän jälkeen haetaan potilasjaksolistan avulla kaikista (alla luetelluista) datatauluista ne rivit, joissa jokin listan potilasjaksoista esiintyy.

Lista tauluista, joiden uudet rivit saadaan HISTORY_TABLE_LOOKUP-menetelmällä:
```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
###### C. FULL_TABLE - koko taulun lataus
Taulut, joille ei löydy mekanismia tunnistaa muutoksia lähdekannan tauluista, ladataan kokonaan - tällöin käytetään latausmetodia FULL_TABLE, jolloin jokaisen inkrementtilatauksen yhteydessä ladataan kaikki data.

Lista tauluista, jotka ladataan kokonaan (lähdejärjestelmän suosituksen pohjalta)(170 kpl):

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```

