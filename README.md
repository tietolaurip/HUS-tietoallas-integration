# 1. Tietolähdeintegraatiot 

Tietolähdeintegraatiot ovat tapa tuoda dataa ja metadataa tietoaltaaseen. Tämä dokumentti kuvaa tietolähdeintegraatioiden loogisia ja ohjelmallisia yhteyksiä tietoaltaaseen, muihin komponentteiin sekä itse tietolähteisiin. Näkökulma on keskittyy integraatiokomponenttien ulkoisiin ominaisuuksiin. Integraatioiden yleistä sisäistä rakennetta ja toimintaa kuvataan dokumentissa [Tietolähdeintegraatiot - Tekniikka](02_tietolahdeintegraatiot_tekniikka.md).

# 2. Rajauksia ja terminologiaa

Tietolähdeintegraatiolla tarkoitetaan tässä dokumentissa ratkaisua, jolla jostakin tietoaltaan ulkopuolisesta datalähteestä saadaan tuotua dataa ja metadataa tietolähteeseen. Tämä ratkaisu kattaa mahdollisesti sekä ohjelmistoa, kertaluonteisia töitä kuten konfiguraatiota, valvontaprosesseja ja päivitysprosessin ja näitä kaikkia sekä tietolähteen että tietoaltaan puolella. Joissakin tapauksissa taas integraatio saattaa supistua kertaluonteiseen datan tuontiin täysin manuaalisesti.

Tämä ja muut tietoaltaan dokumentit pääsääntöisesti rajautuvat kuvaamaan vain tietoaltaan osaa integraatiosta ja rajapintaa tietolähteen ja tietoaltaan välillä, joten itse tietolähteeseen liittyvät yksikohtaiset tiedot jäävät tietolähdeorganisaation vastuulle. Rajapinta tietolähteen ja tietoaltaan välillä muodostuu sekä järjestelmien välisistä konerajapinnoista, että kehitys, ylläpito, valvonta ja hallinto-organisaatioiden välisistä rajapinnoista. Tämä dokumentti ja muu tekninen tietoallasdokumentaatio eivät juurikaan kuvaa organisaatiorajapintoja.

Tietoaltaan teknistä osaa integraatiosta kutsutaan tietolähdeintegraatiokomponentiksi, integraatiokomponentiksi tai vain integraatioksi tai komponentiksi, jos kontekstista on selvää mitä tarkoitetaan. Tekninenkin dokumentaatio kuvaa kuitenkin kahta erilaista oliota:

1. Ohjelmistoa, joka toteuttaa integraatiot. Tämä sisältää vain konfiguraatiot simuloitua lähdejärjestelmää vasten testaamiseen, mutta ei mihinkään todelliseen tuotantoympäristöön. Tämä julkaistaan Apache 2.0 lisenssillä SITRA:n ISAACUS-projekin puitteissa.
2. Konfiguraatio ja muut erityisratkaisut, jotka on tehty nimenomaisesti HUS-toimintaympäristöä varten. Nämä voivat liittyä verkkoon, lähdejärjestelmän kustomointeihin tai muuhun. Nämä jäävät HUS:lle.

# 3. Integraatiotasot

Integraatiolla on monta erilaista tasoa, jotka kaikki täytyy olla kunnossa, jotta käyttäjille voidaan tarjota luotettavaa ja käyttökelpoista dataa. Näiden tasojen luonne on melko erilainen ja toisenkinlaisia tasojakoja voisi varmasti mielekkäästi tehdä. 

![Tietolähdeintegraatiotasot](images/IntegraatioTasot.png)

Kuvan termit on selitetty seuraavissa kappaleissa.

#### 3.1. Operointi ja verkko
Käytännössä tässä dokumentissa rajaudutaan integraatioihin, jotka toimivat IPv4 verkkojen päällä. Joissakin tapauksissa on käytössä myös muuta kommunikaatioinfrastruktuuria, kuten Enterprise Message Bus-tyyppinen väylä tai muu integraatioalusta. Nämä eivät ole osa tietoallasta, niitä ei tässä kuvata.

Integraatioiden toimivuus vaatii kuitenkin usein verkkotason kapasiteetin ja  palomuurauksien tarkistamista. Lisäksi kommunikaatiodetaljit pitää selvittää. Suurin osa integraatioista tapahtuu HUS:n sisäverkossa, mutta eri organisaatiossa olevan tietolähteen kanssa integroituminen voi myös vaatia uusia VPN-järjestelyjä tai muita ratkaisuja.

#### 3.2. Rajapintatekniikka
Rajapintatekniikalla tarkoitetaan niitä asioita, jotka tarvitaan sovellustason kommunikointiin (Layer 7) tietolähteen ja altaan välillä. Nämä vaihtelevat valitusta tekniikasta riippuen, mutta yleensä tällä tasolla nousee esiin ainakin seuraavat asiat:

* Kommunikaatioprotokolla
* Autentikointi
* Autorisointi
* Kommunikaation salaus 

#### 3.3. Datarakenne
Datarakenteella tarkoitetaan niitä asioita, jotka määrittelevät datasta rakenteellisia kokonaisuuksia ja niiden välisiä viitteitä sekä muutosten havaitsemiseksi tarvittavia tietoja.

* Datan rakennemuoto (esim. relaatiotietokanta, XML-dokumentti, JSON-documentti, joukko CSV-tiedostoja)
* Avainsarakkeet
* Viittaussuhteet
* Muutosten (uudet ja varsinaiset muutokset) ja poistojen havaitseminen (esim. muutosaikaleimat, poistomerkinnät)

#### 3.4. Sisältö ja merkitys

Sisältö ja merkitys liittää datan todellisen maailman tapahtumiin ja olioihin. Tämä on lähes kokonaisuudessaan metadataa ja kuvattu luvussa Metadata sekä erillisessä dokumentissa [Metadata - Yleisesitys](wiki-page:Metadata - Yleisesitys). 

# 4. Looginen Arkkitehtuuri

Integraatiokomponentti jakautuu toiminnallisesti kahteen pääosaan: lataus ja esiprosessointi. 

![IntegraatioArkkitehtuuri](images/IntegraatioArkkitehtuuri_v2.png)

### 4.1. Lataus
Lataustoiminnallisuuden päätehtävänä on ladata dataa lähdejärjestelmästä. Tämä alikomponentti kapseloi sisäänsä:

* Tietoaltaan Alustus
* Turvattu kommunikaatio lähdejärjestelmän kanssa
* Lähdejärjestelmästä saadun datan pysyvä tallennus mahdollisimman muuttumattana
* Omaan toimintaansa liittyvän prosessimetadatan lisääminen joko itse dataan tai erilliseen metadataan

###### 4.1.1. Alustus
Tietoaltaan alustuksessa alustetaan Tietoaltaan Latausalue, Varasto ja Metadata itse varsinaista latausta varten

###### 4.1.2. Kommunikaatio
Kommunikaatio käsittelee koko verkko- ja rajapintatekniikkatason integraatioon liittyen.

###### 4.1.3. Tallennus
Lataus tallentaa tiedot aina latausalueelle (staging, raakadata), joka ei ole käyttäjien luettavissa. Tallennustapa riippuu myös saadun datan rakenteesta ja mitään yleistä standardia tälle ei ole. Datan lataus jaetaan integraatioissa lähinnä käytännön syistä kahteen osaan: alkulataus ja inkrementaalilataus. Alkulatauksella tarkoitetaan kaiken historiadatan latausta Tietoaltaaseen aina nykyhetkeen asti ja inkrementaalilatauksilla tarkoitetaan datan muutosten latausta alkulatauksen jälkeen nykyhetkestä eteenpäin.

###### 4.1.4. Prosessimetadata
Pääasiassa latauksessa ei käsitellä metadataa, mutta pieni osa metadataa syntyy osana itse latausprosessia. Vähintään tässä vaiheessa pitää tallentaa tieto latauksen ajankohdasta, joskin sen tarkkuus voi vaihdella paljonkin integraatioiden välillä. Lisääksi voidaan kerätä prosessimetadataa pitämään kirjaa inkrementaalilatausten tilasta (esim. mihin tilaan viimeksi jäätiin ja mistä pitää jatkaa).

#### 4.2. Esiprosessointi
Esiprosessointitoiminnallisuuden päätehtävänä on prosessoida latauksessa tuotettu raakadata käyttäjille soveltuvaan muotoon. Tätä kutsutaan esiprosessoinniksi, koska tämän prosessointi on tarkoitus pysyä melko yksinkertaisena ja on aina tehty etukäteen varsinaisia tiedonjalostusprosesseja ajatellen. Esiprosessointi sisältää ainakin:

* Pseudonymisointi
* Hive-tietomalliin sovitus
* Datarakenteen metadata

Lisäksi esiprosessointiin voi sisältyä esimerkiksi:

* Sisään tulevan datan laadun mittaus
* Datan profilointi

# 5. Tietoturva

Tietolähdeintegraatiodenkin tietoturva hoidetaan tietoaltaan yleisten tietoturvaperiaatteiden mukaan. Integraatioden erityispiirteinä on kommunikaatio moninaisten lähdejärjestelmien kanssa, jotka sijaitsevat fyysisesti ja verkkotopologisesti erilaisissa paikoissa.

Jokaisen lähdejärjestelmän kommunikaatio ja data myös eristetään toisistaan, jotta mikään lähdejärjestelmä ei voi edes väärin toimien esittää eri lähdejärjestelmää tai lukea toisen lähdejärjestelmän dataa.

# 6. Virheenkäsittely

Virheenkäsittelyvastuun jakautumien lähdejärjestelmän ja tietoaltaan on integraatiokohtaista, mutta käytännössä vastuut jakautuvat päätyyppeihin käytetyn rajapintatekniikan mukaan. 

Haku(pull)-tyyppisessä integraatiossa vastuu on lähes täysin tietoaltaan puolella, jonka pitää pystyä synkronoimaan data lähdejärjestelmän kansssa virhetilanteiden jälkeen. Lähdejärjestelmän vastuulle jää lähinnä tarjota jokin tapa tunnistaa muuttuneet tiedot, mikä tarvitaan yleensä jo normaalitoimintaakin varten.

Työntö(push)-tyyppisessä integraatiossa taas vastuu on pääosin lähdejärjestelmän puolella. Ongelman selvittyä lähdejärjestelmän pitää pystyä lähettämään ongelman aikana päivittyneet tiedot. Tietoaltaan rooliksi jää lähinnä pystyä ottamaan vastaan normaalia pidemmänkin aikavälin muutoksia.

# 7. Metadata

Tarkempi dokumentaatio metadatasta täällä: [Metadata - Yleisesitys](03_metadata.md). 

Tietolähdeintegraatioiden osalta Metadata ratkaisee "Sisältö ja merkitys" -tasoa luvun Integraatiotasot terminologialla. 
