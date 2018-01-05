# QPati
# 1. Yleiskuvaus
## 1.1. Qpati
Tässä dokumentissä kuvataan QPati järjestelmän integroinniti tietoaltaaseen. 
QPati on tietokantainen graafinen potilaiden kudos- ja solunäytteistä lausuntomuotoisia vastauksia
antavien laboratorioiden tuotannonohjausjärjestelmä. QPatia Suomessa
käyttävät yliopistolliset sairaalat ja keskussairaalat sekä yksityiset laboratoriot.
Lisätietoja itse Qpati-lähdejärjestelmästä löytyy HUS extranetistä: [Qpati](https://extranet.hus.fi/teams/isaacus/esimaaritys/Lhdejrjestelmt/Forms/AllItems.aspx?RootFolder=%2Fteams%2Fisaacus%2Fesimaaritys%2FLhdejrjestelmt%2FQpati&FolderCTID=0x0120002C6263599A273442BC454DABC7D30105&View={8958753F-FB35-4DF1-B882-2A8E09F27811})

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvaus
Tässä kappaleessa kuvataan integraation rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus
QPatissa on käytössä yksi historia-instanssi ja yksi inkrementti-instanssi. Integraatioiden yleiskuvaus kerrotaan kappaleessa 
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

## 2.3. Rajapintakuvaus
| Tietosisältö | Kontrolli | Rajapinta | Rakenne | Inkrementaalisuus | lataustiheys | Viive |
|---|---|---|---|---|---|---|
|Kaikki * |Push|SFTP|CSV|Itse tapahtumille. Referenssidatalle tietoallas erottaa. |1h|15min|
Viive:  Kuvaa viivettä siitä kuinka kauan kestää normaalitilahteessa datan siirto kantaan poininnan valmistuttua.
 
\* Integraation datamalli 1.11.2017 (kuvattu kappaleessa X.Y)

Erillinen rajapintakuvaus (Excel-lomake) kertoo kaikki yksityiskohdat integraation toteutuksesta (selittää yo. taulukon valinnat). Rajapintakuvaus on lähdejärjestelmä- ja asiakasspesifistä luottamukellista tietoa, eikä täten liitetä avoimen lähdekoodin dokumentaatioon.

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:
* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Integraatiot asennetaan Tietoaltaaseen komponentin lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti ([QPati - KayttoonOtto.md](KayttoonOtto.md)).

## 2.5 Tietosuoja

Sekä datan siirto että tallennus ovat salattuja. Varastoaltaassa data on pseudonymisoitua (arkaluontoisen datan pseudonymisointi tai tyhjäys).

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna QPati-spesifiset asiat. Suluissa on esitetty viite yleiskuvan toiminnallisuuskappaleen numeroon.**_

## 3.1. Integraation alustus
QPati:n alustus tapahtuu kaikkien Sftp-integraatioiden tapaan seuraavasti: 
* [Sftp-integraation alustus](../documentation/int_3_1_alustus_sftp.md)

(10. ja 11.) Raakadata- ja varastoaltaiden tietomalli pitää sisällään seuraavat taulut ja niiden kentät (kuvattu kähdekoodissa ~/integrations/QPati/roles/headnode/files/staging/*.hql-skripteissä):
 * Tietomalli data-tauluille on kuvattu dokumentissa [Data-taulut](roles/manager/files/metadata/QPati_Isaacus_FilesAndFields_20170303.txt) 
 * Tietomalli taustarekisteri-tauluille on kuvattu dokumentissa: [Taustarekisterit](roles/manager/files/metadata/QPati-tietoallas_taustarekisterit.txt)

## 3.2. Integraation alkulataus (historiadatan lataus)
QPati:n alkulataus tapahtuu teknisesti samalla tavalla kuin inkrementaalilataukset. Käytännössä kuitenkin alkulataus tulee suorittaa käsin: 
* [Sftp-integraation alkulataus](../documentation/int_3_2_alkulataus_sftp.md)

## 3.3. Integraation inkrementaalilataus (jatkuva muutosten lataus)
QPati-integraatiossa käytetään tiedon siirtomekanismina lähdejärjestelmän kontrolloimaa SFTP-siirtoa. Kyseisen integraatiotyypin yleiskuvaus löytyy kappaleesta 
* [SFTP-integraation inkrementaalilataus](../documentation/int_3_3_inkrementaalilataus_sftp.md).

Alla listattu poikkeamat/lisäykset yleiseen latausproseduuriin verrattuna (numerolla viitattu [yleiskuvauksen](../documents/int_3_3_inkrementaalilataus_sftp.md) toiminnalliin kokonaisuuksiin):

#### 3.3.1. Datan poiminta ja siirto lähdejärjestelmästä (2.) 
* Lähdejärjestelmä kerää datan sovitulla periodilla lähdejärjestelmän tietokannasta
 * Oletus datalle: 1h välein
 * Oletus taustarekistereille: 24h välein
* Tiedon keruu tehdään heti tasatunnin täytyttyä
* Tiedostot siirretään tietoalataan sftp-palvelimelle em. aikataululla heti tietokantahaun jälkeen
* Jos tietoaltaaseen ei saada yhteyttä tai siirrossa esiintyy jokin virhe, joka estää latauksen, lataus siirtyy seuraavaan aikataulun mukaiseen lataushetkeen, jolloin yhteydet altaaseen jälleen toimivat. 
 * Tällöin tietokannasta haetaan kaikki data edellisestä onnistuneesta siirtohetkestä uuteen siirtohetkeen. 
 * Eli käytännössä katkoksen jälkeisen lataukseen sisältyy 2 tai useampaa siirtymättä jäänyttä jaksoa
* Kun tiedostosiirto on valmis asetetaan kohdekansioon lippu _TIETOALLAS_VALMIS, mikä indikoi tietoaltaan prosessille, että kansion tiedostot (datasetti) ovat valmiit jatkokäsittelylle.
* Tiedoston siirron yksityiskohdat (protokolla/hakemistorakenne/nimeämiset) on kirjattu erilliseen rajapintadokumenttiin (ei sisälly avoimen lähdekoodin dokumenttiin)

#### 3.3.2. Ladattujen tiedostojen prosessointi (kProducer) (3.) 
* Tietoaltaan prosessi käy tarkastamassa minuutin välein onko lähdejärjestelmästä siirtynyt uusia tiedostoja sftp-palvelimelle sovittuun hakemistoon: /data/<vuosi>/<kuukausi>/<päivä>/<tuntiminuuttisekunti>/
* Tiedostot käsitellään hakemisto (tiedostosetti) kerrallaan. Jos tietoaltaan prosessit ovat olleet jostain syystä pois päältä ja tiedostoja on puskuroitunut sftp-palvelimelle, käsitellään tiedostosetit yksi kerrallaan
 * Tiedostosetti on valmis jos kansiosta löytyy "lipputiedosto" "_TIETOALLAS\_VALMIS_"
* Kaikki data-tiedostot (ilman "Ref_" etuliitettä) siirretään sellaisenaan, sillä ne ovat jo valmiiksi "delta"-tiedostoja, eli sisältävät vain muutokset edelliseen lataukseen verrattuna.
* Lähdejärjestelmästä saapuvat taustarekisteritiedostot (alkavat Ref_ -liitteellä) eivät sisällä vain muutoksia edellisestä latauksesta, vaan ne sisältävät kaikki lataushetkellä taustarekisterissä olevat tiedot. 
 * Taustarekisterit ovat kooltaa kohtalaisen pieniä muutamaa poikkeusta lukuunottamatta (esim. Potilaat (>1.6 miljoonaa riviä) ja Yksikot), joten näiden kopioiminen tietoaltaaseen storageen joka kerta kokonaisena ei olisi suhteettoman suuri ongelma
 * Potilaat-taulun vuoksi Tietoaltaan kuitenkin päässä luodaan taustarekistereistä delta-tiedostot, eli vertaillaan edellistä siirrettyä taustarekisteriä uuteen ja talletetaan vain muuttuneet ja uudet tiedot. 
 * Taustarekisterien delta-tiedon generoinnissa käytetyt vertailutiedostot talletetaan kansioon qpat/chroot/previous_refs/. Logiikka menee seuraavasti:
    * Jos aikaisempaa Ref-tiedostoa ei löydy kansiosta, lähetetään uusi Ref-tiedosto kafkalle ja uusi tiedosto kopioidaan previous_refs-kansioon (ensimmäinen karta kun inkrementaalilataukset aloitetaan)
    * Jos previous_refs-kansiosta löytyy vastaava Ref-tiedosto, lähetetään kafkalle näiden tiedostojen "erotus", eli muuttuneet tiedot ja kopioidaan uusi Ref-tiedosto previous_refs-kansioon
    * Jos previous_refs-kansiosta löytyy vastaava Ref-tiedosto, mutta tiedostojen väillä ei ole muutoksia, ei kafkalle lähetetä mitään

#### 3.3.3. Raaka-datan prosessointi Azuren Datalake Storeen (adls) (6.) 
* Havaitut valmiit tiedostosetit ladataan Azuren Datalake Storageen ~/staging/qpati-nimiseen kansioon.
* Tiedostoformaatti adls:ssä on ORC
* Yksikäsitteinen tiedoston nimi generoituu inkrementille kirjastopalveluiden toimesta
* Kun tiedostot on kopioitu datalake storageen, ne poistetaan sftp-palvelimelta. Samoin poistetaan koko hakemistorakenne, jonne tiedostot on talletettu. 

#### 3.3.4. Datan tallennus raakadata-altaaseen (staging) (7.) 
* Raakadata-altaassa tieto tallentuu staging_qpati hive-tietokantaan
 * Tietomalli data-tauluille on kuvattu dokumentissa [Data-taulut](roles/manager/files/metadata/QPati_Isaacus_FilesAndFields_20170303.txt) 
 * Tietomalli taustarekisteri-tauluille on kuvattu dokumentissa: [Taustarekisterit](roles/manager/files/metadata/QPati-tietoallas_taustarekisterit.txt)
