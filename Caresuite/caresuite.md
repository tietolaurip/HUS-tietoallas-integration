# Integraatio: Caresuite

# 1. Yleiskuvaus
## 1.1. Caresuite
Tässä dokumentissä kuvataan Caresuite-järjestelmän integrointi tietoaltaaseen. 

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvaus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
Caresuite-integraatiossa on käytössä 6 historiatietokanta-instanssia ja yksi inkrementti-instanssi. Integraatioiden yleiskuvaus kerrotaan kappaleessa 
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

Integroidut historiatietokanta-insanssit Caresuitelle ovat:
* caresuite_meipicis_old
* caresuite_meipicis
* caresuite_toopicis
* caresuite_peipicis
* caresuite_picis80
* caresuite_picis82

Aluksi historiadatalle käytettiin vain yhtä tietokantaa, mutta koska itse lähdekannan tietosisältö poikkeea toisistaan eri kantojen välillä (esim. määritelmät ja koodaus joillekin metatiedoille), niin historiakannat päätettiin ladata omiin tietokantoihinsa. 

Caresuiten inkrementaalikomponentti on
* caresuite_picis82_inkr

Tähän arkistokantaan (picis82) ladataan data livejärjestelmästä 3h välein 8 kertaa päivässä (02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10 ja 23:10). Tämä instanssi lataa Caresuiten (picis82) arkistokannasta data 3h välein Tietoaltaaseen siten, että Tietoaltaan inkrementtilataus käynnistyy aina livekanta synkronoinnin jälkeen, eli 02:30, 05:30, 08:30, 11:30, 14:30, 17:30, 20:30 ja 23:30

## 2.3. Rajapintakuvaus

| Tietosisältö | Kontrolli | DB | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Latausviive |
|---|---|---|---|---|---|---|---|
|Kaikki määritellyt *, lataus tauluista |Pull|MS-SQL-server|JDBC|SQL| Osalle datasta muutoslataus, osalle tehdään täyslataus|3h|max 2h|
\* = Lähdejärjestelmän tietyn version tietosisältö silloin kun integraatio on toteutettu ja asennettu. Lähdejärjestelmän päivityksien yhteydessä tapahtuvia tietomallin muutoksia integraatio ei tue ilman muutostyötä.

HUOM! Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:

* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin (ja instanssin) lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti.

Alla lista käyttöönottodokumenteistä eri instansseille:

| Komponentti | Instanssi |Dokumentti |
|---|---|---|
|Peruskomponentti|Caresuite (yhteiskomponentti)|[KayttoonOtto.md](KayttoonOtto.md)|
|Peruskomponentti|meipicis_old|[KayttoonOtto.md](../caresuite_meipicis_old/KayttoonOtto.md)
|Peruskomponentti|meipicis|[KayttoonOtto.md](../caresuite_meipicis/KayttoonOtto.md)|
|Peruskomponentti|peipicis|[KayttoonOtto.md](../caresuite_peipicis/KayttoonOtto.md)|
|Peruskomponentti|toopicis|[KayttoonOtto.md](../caresuite_toopicis/KayttoonOtto.md)|
|Peruskomponentti|picis80|[KayttoonOtto.md](../caresuite_picis80/KayttoonOtto.md)|
|Peruskomponentti|picis82|[KayttoonOtto.md](../caresuite_picis82/KayttoonOtto.md)|
|Inkrementaalikomponentti|Caresuite_inkr (yhteiskomponentti)|[KayttoonOtto.md](../Caresuite_inkr/KayttoonOtto.md)|
|Inkrementaalikomponentti|picis82_inkr|[KayttoonOtto.md](../caresuite_picis82_inkr/KayttoonOtto.md)|

## 2.5. Tietosuoja
Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna tämän integraation spesifiset asiat.**_

## 3.1. Integraation alustus
CA-komponentin:n alustus tapahtuu kaikkien jdbc-integraatioiden tapaan seuraavasti: 
* [Jdbc-integraation alustus](../documentation/int_3_1_alustus_jdbc.md)

Käytännön ohjeet alustukseen löytyvät komponentin KayttoonOtto.md-dokumentista.

Raakadata- ja varastoaltaiden tietomalli pitää sisällään alla olevan taulukon csv-tiedostojen mukaiset taulut ja niiden kentät. Samasta lähdekannasta tietoa ammentavat peruskomponentti ja inkrementaalikomponentti ovat yhtenevät (sama tietomalli ja sama tietokanta) ja alustus tehdään aina peruskomponentin avulla (inkrementaalikomponentille ei tehdä alustus-proseduuria).

| Instanssi | Taulut | Sarakkeet |
|---|---|---|
|meipicis_old|[table_metadata.csv](../caresuite_meipicis_old/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_meipicis_old/roles/manager/files/metadata/column_metadata.csv)|
|meipicis|[table_metadata.csv](../caresuite_meipicis/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_meipicis/roles/manager/files/metadata/column_metadata.csv)|
|peipicis|[table_metadata.csv](../caresuite_peipicis/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_peipicis/roles/manager/files/metadata/column_metadata.csv)|
|toopicis|[table_metadata.csv](../caresuite_toopicis/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_toopicis/roles/manager/files/metadata/column_metadata.csv)|
|picis80|[table_metadata.csv](../caresuite_picis80/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_picis80/roles/manager/files/metadata/column_metadata.csv)|
|picis82|[table_metadata.csv](../caresuite_picis82/roles/manager/files/metadata/table_metadata.csv)|[column_metadata.csv](../caresuite_picis82/roles/manager/files/metadata/column_metadata.csv)|

HUOM! Metadatan sisältö ei kuulu avoimen lähdekoodin julkaisuun.

## 3.2. Integraation alkulataus (historiadatan lataus)
Clinisoftn:n alkulataus (jokaiselle instanssille erikseen) tapahtuu samalla periaattella kuin kaikkien muiden jdbc-pohjaisten integraatioiden alkulataus: 

* [Jdbc-integraation alkulataus](../documentation/int_3_2_alkulataus_jdbc.md)

Caresuiten alkulatauksessa ei ole poikkeuksia geneeriseen ratkaisuun verrattuna.

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
Clinisoft-integraatiossa käytetään tiedon siirtomekanismina Tietolataan kontrolloimaa jdbc/sql-latausta. Kyseisen integraatiotyypin inkrementaalilatauksen yleiskuvaus löytyy kappaleesta:

* [Jdbc-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_jdbc.md).

Caresuite-integraatiossa ei ole poikkeuksia yleisiin periaatteisiin.

#### 3.3.1. Caresuite-spesifinen inkrementaalilogiikka (Pull)
Jotta inkrementaalilatauksessa ladataan vain uudet ja muuttuneet tietueet, tulee latausprosessille kertoa logiikka miten tämä onnistuu. 

Caresuiten tietokanta ei kattavasti tarjoa muutosten havainnointiin sopivia taulukohtaisia tietoja, kuten aikaleimoja. Tähän on rakennettu erillinen moniosainen ratkaisu (kohta B alla). Kaikkien taulujen latausmenetelmät ja latauslogiikka on kerrottu alla:

###### A. TIME_COMPARISATION - aikaleimavertailu
Joissakin usein päivittyvissä tauluissa on suoraan käyttökelpoiset muutosaikaleimat. Näiden taulujen uudet ja muuttuneet rivit havaitaan hakemalla rivit joiden muutosaikaleima on suurempi kuin suurin aiemmin ladattu aikaleima. Latausmetodi on tällöin TIME_COMPARISATION.

Lista tauluista, joiden uudet ja muuttuneet rivit saadaan TIME_COMPARISON-menetelmällä:
```
EVENTDATA, LABRESULTAUDITED, EVENTDATA, RTDATA, SAVEDREPORTS, TREATMENTDATA
```
###### B. HISTORY_TABLE_LOOKUP - erityisen "muutos"-taulun käyttö
Edellä mainittu TIME_COMPARISATION-menetelmä kattaa vain murto-osan tauluista, joten tarvitaan lisälogiikkaa, jolla muutoksia löydetään jäljelle jääneistä tauluista. HISTORY_TABLE_LOOKUP-lataustyyppi perustuu erilliseen "muutos"-tauluun (XON_INT_DATALAKE), jonka avulla saadaan selville mitä muutoksia varsinaisissa data-tauluissa on tapahtunut.

Taulun kuvaus:

| Taulun nimi |Sarakkeen nimi |Sarakkeen tyyppi |Onko avainkenttä |
|---|---|---|---|
|XON_INT_DATALAKE|SOURCETABLE_PK_DBOID|NUMERIC|kyllä|
|XON_INT_DATALAKE|SOURCETABLE|VARCHAR|ei|
|XON_INT_DATALAKE|SOURCETABLE_PK_COLUMNNAME|VARCHAR|ei|
|XON_INT_DATALAKE|CREATED|DATETIME|ei|

Teknisesti ratkaisu toimii kolmessa vaiheessa seuraavalla tavalla:

1. Kun johonkin datatauluun kirjautuu muutos, tietokantatriggeri kirjaa XON_INT_DATALAKE-aputauluun uuden rivin. 
2. Tämän tiedon avulla sitten Tietoaltaan inkrementaalilatausprosessi löytää edellisen lataushetken jälkeen tapahtuneet muutokset (suuri lista kaikista muutoksista)
3. Inkrementaaliprosessi käy läpi muutoslistan ja kerää datataulukohtaisesti listan muutoksista, jotka se sitten hake varsinaisista data-tauluista ja lataa Tietoaltaaseen taulu kerrallaan.

Esimerkki:  Muutos ORDERS-tauluun luo seuraavanlaisen rivin muutos-tauluun (XON_INT_DATALAKE):

SOURCETABLE_PK_DBOID    SOURCETABLE       SOURCETABLE_PK_COLUMNNAME     CREATED
167683506907943209550   ORDERS            ORDERDBOID                    2017-03-08 07:14:19.407

Muutostaulun CREATED-kenttä kertoo, koska muutos tapahtui ja loput identifioi taulun (ORDERS) ja mikä rivi kyseisessä taulussa on muuttunut (SOURCETABLE_PK_DBOID). Tämän jälkeen luetaan ORDERS-taulusta (data-taulu) tiedot riviltä, jonka DBOID:n arvona on "167683506907943209550". Tämä siis toistetaan kaikille XON_INT_DATALAKE-taulusta löydetyille tiedoille, jotka on lisätty edellisen latauskerran jälkeen. 

Tällä logiikalla sitten saadaan loput muutokset. Taulut, joiden muutokset tulee hakea tällä logiikalla ovat:
```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```

###### C. FULL_TABLE - koko taulun lataus
FULL_TABLE-tyyppisen latauksen listalle joutuvat taulut, joiden lataukset ei onnistu kahden edellisen latausmekanismin avulla. Eli tällä listalle joutuu taulut, joista ei löydä muutoksia lainkaan tiedossa olevan datan avulla. Ja jos muutoksia ei tiedetä, on vaihtoehtona ladata joko kaikki tai ei mitään. Tässä tapauksessa ladataan kaikki data.

Lista tauluista, jotka ladataan kokonaan (lähdejärjestelmän suosituksen pohjalta)(101 kpl):

```
HUOM! Ei kuulu avoimen lähdekoodin julkaisuun.
```
