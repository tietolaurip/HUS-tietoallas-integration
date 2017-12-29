# Integraatio: Healthweb

# 1. Yleiskuvaus
## 1.1. Healthweb
Tässä dokumentissä kuvataan Healthweb järjestelmän integroinniti tietoaltaaseen. 

## 1.2 [Tietoaltaan ja integraatioiden yleiskuvaukset](../documentation/int_1_yleiskuvaus.md)

# 2. Integraatiokuvaus
Tässä kappaleessa kuvataan integraation vaatimukset, rakenne, rajapinta ja asennus. Dokumentointi kertoo ensin kyseisen integraatiotyypin yleisen kuvauksen ja sen jälkeen tämän integraation erityispiirteet.

## 2.1. Integraation vaatimukset
Tälle integraatiolle pätee seuraavat vaatimukset:

* [Integraatiot - Vaatimukset](../documentation/int_2_1_integraation_vaatimukset.md)

Vaatimus INT_30 ei päde, sillä Healthweb-integraatiolle ei voi tehdä alkulatausta.

## 2.2. Integraation rakenteiden yleiskuvaus

Healthweb-integraatiossa on itsessään vain yksi komponentti, joka pitää sisällään kaiken tarpeellisen integraation toimimiseksi osittain, mutta itsenäisesti. Seuraavissa kappaleissa on kerrottu riippuvuudet kahteen muuhun komponenttiin, jotka vaaditaan, että integraatio toimii päästä päähän. Alla integraation rakenteiden yleiskuvaus (sisältäen sanomapohjaisten integraatioiden rakenteen): 
* [Integraatiot - Integraation rakenteiden yleiskuvaus](../documentation/int_2_2_rakenteet.md). 

## 2.3. Rajapintakuvaus
| Tietosisältö | Kontrolli | Rajapinta | Rakenne | Inkrementaalisuus | Lataustiheys | Viive |
|---|---|---|---|---|---|---|
| Osajoukko KELA:n kantaliikenteestä | Push | REST | XML | Sanomapohjainen, kaikki tieto "inkrementaalista" | Ei määrättyä aikataulua | 15 min vastaanotosta |

## 2.4. Integraation asentaminen tietoaltaaseen
Integraatiot asentuu Tietoaltaaseen yleisen periaatteen mukaan:
* [Integraatiot - Asennus](../documentation/int_2_4_asennus.md)

Healthweb integraatiossa asennetaan seuraavat osat:
* Vastaanotto (Healthweb)
* Pseudonymisointi (stream-pseudonymizer)
* Lataaja (stream-to-hdfs)

Integraatiot asennetaan Tietoaltaaseen komponentin lähdekoodin juurihakemistossa esiintyvän KayttoonOtto.md-dokumentin mukaisesti:
* [Healthweb - KayttoonOtto.md](KayttoonOtto.md)
* stream-pseudonymizer - HUOM! Tätä ei toimiteta avoimen lähdekoodin julkaisussa. Käyttöönottava organisaatio vastuussa tämän toiminnallisuuden tekemisestä.
* [stream-to-hdfs - KayttoonOtto.md](../stream-to-hdfs/KayttoonOtto.md)

## 2.5 Tietosuoja

Käytössä suojattu yhteys. Varastoaltaan datasta poistetaan arkaluontoinen data ja sotu pseudynymisoidaan.

# 3. Integraation toiminnallinen kuvaus
Tässä luvussa kuvataan integraation toiminnallisuus. Perusperiaatteena on käyttää integraatiotyypin yleiskuvasta pohjana ja kertoa tämän integraation ominaisuudet, rajoitteet ja poikkeamat siihen verrattuna. 

_**Yleisen osuuden jälkeen on kirjattuna Healthweb-spesifiset asiat.**_

## 3.1. Integraation tietosisältö ja alustus

#### 3.1.1. Tietosisältö
Tietoa Healthweb:in sanomista löytyy täältä (HL7/CDA-R2): 

* [Kela - Kanta-dokumentaatio ja tietomalli](http://www.kanta.fi/fi/web/ammattilaisille/hl7)

#### 3.1.2. Alustus

Healthweb:n alustus tapahtuu sanomapohjaisten integraatioiden tapaan alla olevan kuvauksen mukaan:
* [Alustus - Sanomapohjaiset integraatiot](../documentation/int_3_1_alustus_sanoma.md)

## 3.2. Integraation alkulataus (historiadatan lataus)
Healthweb:ille ei suoriteta alkulatausta.

## 3.3. Integraation sanomien vastaanotto (inkrementaalilataukset)
Healthweb-integraatiossa käytetään tiedon siirtomekanismina lähdejärjestelmän kontrolloimaa REST-siirtoa. Sanomapohjaisten integraatioiden sanomien vastaanotto on kuvattu kappaleessa: 
* [Inkrementaalilataus - Sanomapohjaiset integraatiot](../documentation/int_3_3_inkrementaalilataus_sanoma.md)

Integraatio kaivaa XML-viestistä Clinical-document-osion, joka lähetetään Kafka-jonoon. 

#### 3.3.1. Inkrementaalilogiikka
Lähdejärjestelmä lähettää sanomia reaaliajassa datan muuttuessa lähdejärjestelmässä.

