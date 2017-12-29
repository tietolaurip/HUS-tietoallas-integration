## 1. Metadatan määritelmä
Metadata on Tietoaltaan komponenttien ja Tietoallasta käyttävien sovellusten tarvitsemaa tietoa; minkälaista tietoa käsitellään, mistä se on peräisin, kuinka sitä pitää käsitellä ja milloin.

## 2. Metadatan sisältö

Metadatan voi jakaa karkeasti kahteen osaan: 

1) Tekninen metatieto - Integraation pakollinen metadata, jota ilman integrointi ei onnistu
* tietokannan taulujen yksikäsitteiset nimet (hql/sql-yhteensopivin merkein kuvattuna)
* tietokannan kenttien yksikäsitteiset nimet ja tyypit (hql/sql-yhteensopivin merkein kuvattuna)
* pseudonymisointi-tieto
* JDBC-integroinneissa inkrementin muutosten tunnistamiseen tarvittava logiikka, eli taulukohtainen tieto miten tunnistetaan uusi data, poistunut data ja muutokset edellisen latauksen jälkeen

2) Lisäarvometadata tiedon hyödyntäjille, joka kuvaa tarkemmin mitä dataa tietoaltaaseen varastoidaan
* Taulujen kuvaukset
* Kenttien kuvaukset
* Rajoitteet
* Lisäkommentteja

### 2.1. Metadatan peruskomponentit

Tekniset komponentit: 
* Tietolähde: Lähdejärjestelmän dokumentaatio tai lähdejärjestelmän tietokanna oma metadata
* Metadatan tietovarasto: Metadata tietokanta 
* Työkalut: Metadatan prosessointiin käytettävät työkalut
  * Metadataparseri - parsii lähdejärjestelmän dokumentin koneellisesti ja tuottaa tarvittavan metadatan
  * Metadataloader (lähdejärjestelmästä) - hakee jdbc:n yli metadatan lähdejärjestelmän tietokannasta
  * Metadataloader (kohdekantaan) - lataa metadatan tietoaltaan metadatakantaan

#### 2.1.1. Tietolähde / integraatio (Data set)
Kertoo mitä tietolähdettä metadata kuvaa. 

#### 2.1.2 Taulu
Tietoaltaan hive-kanta mallinnetaan relaatiotietokannan tapaan, vaikka lähdejärjestelmän tietomalli ei relaatiomallin mukainen olekaan. Taulu on siis käsite, johon mallinnetaan tietolähteen osakokonaisuuksia. Taulukohtainen metadata kuvaa minkälaista dataa taulu sisältää. 

#### 2.1.3. Sarake
Saraketason metadata kertoo taulun sarakkeiden nimet, tietotyyppi ja kuvaukset. 

##### 2.1.3.1. Sarakkeen pseudonymisointi

Pseudonymisointi tarkoittaa tunnistettavan materiaalin korvaamista keinotekoisella tunnisteella. Tietoaltaassa pseudonymisointitermiä on käytetty hieman yleistäen, eli keinotekoisen tunnisteen käytön lisäksi se kattaa myös tunnistettavan materiaalin poistamista. Metadatassa on määritelty jokaiselle ladattavalle kentälle ns. pseudonymisointi-funktio, joka määrää mitä tiedolle tehdään. Vaihtoehdot ovat:

PASS: Datalle ei tehdä mitään
NULL: Data poistetaan
HASH: Data korvataan keinotekoisella tunnisteella

Myöhemmin tätä joukkoa mahdollisesti laajennetaan tukemaan muita kokonaispseudonymisoinnin kannalta tarpeellisia funktioita.

