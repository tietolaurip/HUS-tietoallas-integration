# Integraatio: husradu

# 1. Yleiskuvaus
## 1.1. husradu
HUS RADU (husradu) on röntgenosastojen työnohjausjärjestelmä, joka tuottaa kuvantamisen lähetteet, tutkimusten kirjaukset ja lausunnot sekä muun kuvantamisen lähetteet ja niihin liittyvät kirjaukset ja lausunnot. Tässä dokumentissä kuvataan husradu järjestelmän integrointi tietoaltaaseen. 
## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvaus
Tässä kappaleessa kuvataan integraation vaatimukset, rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:
* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

## 2.2. Integraation rakenteiden yleiskuvaus

husradu-integraatiossa on itsessään vain yksi komponentti, joka pitää sisällään kaiken tarpeellisen integraation toimimiseksi osittain, mutta itsenäisesti. Seuraavissa kappaleissa on kerrottu riippuvuudet kahteen muuhun komponenttiin, jotka vaaditaan, että integraatio toimii päästä päähän. Alla integraation rakenteiden yleiskuvaus (sisältäen sanomapohjaisten integraatioiden rakenteen): 
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

## 2.3. Rajapintakuvaus
| Tietosisältö | Kontrolli | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|
| HUSRadu:n tietomallin mukainen data | Push | REST | JSON | Sanomapohjainen, kaikki tieto "inkrementaalista" | Ei määrättyä aikataulua | 15 min vastaanotosta |

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:
* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

husradu integraatiossa asennetaan seuraavat osat:
* Vastaanotto (husradu)
* Pseudonymisointi (stream-pseudonymizer)
* Lataaja (stream-to-hdfs)

Integraatiot asennetaan Tietoaltaaseen komponentin lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti:
* [husradu - KayttoonOtto.md](KayttoonOtto.md)
* [stream-pseudonymizer - KayttoonOtto.md](../stream-pseudonymizer/KayttoonOtto.md)
* [stream-to-hdfs - KayttoonOtto.md](../stream-to-hdfs/KayttoonOtto.md)

## 2.5 Tietosuoja

Käytössä suojattu yhteys. Varastoaltaan datasta poistetaan arkaluontoinen data ja sotu pseudynymisoidaan.

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna husradu-spesifiset asiat.**_

## 3.1. Integraation alustus
husradu:n alustus tapahtuu sanomapohjaisten integraatioiden tapaan alla olevan kuvauksen mukaan:
* [Alustus - Sanomapohjaiset integraatiot](../documentation/int_3_1_alustus_sanoma.md)

## 3.2. Integraation alkulataus (historiadatan lataus)
Tämän julkaisun puitteissa ei tehdä historiadatan latausta.

## 3.3. Integraation sanomien vastaanotto (inkrementaalilataukset)
husradu-integraatiossa käytetään tiedon siirtomekanismina lähdejärjestelmän kontrolloimaa REST-siirtoa. Sanomapohjaisten integraatioiden sanomien vastaanotto on kuvattu kappaleessa: 
* [Inkrementaalilataus - Sanomapohjaiset integraatiot](../documentation/int_3_3_inkrementaalilataus_sanoma.md)

#### 3.3.1. Inkrementaalilogiikka
Käytännössä lähdejärjestelmä lähettää sanomia datan muuttuessa lähdejärjestelmässä.

