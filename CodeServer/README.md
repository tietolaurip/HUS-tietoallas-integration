# CodeServer-komponentti

## Lähdejärjestelmä
Tietoaltaan näkökulmasta CodeServer on SOAP-palvelu, josta voi hakea koodistoja ja muuta referenssidataa, joihin muut palvelut viittaavat. Eri koodistopalvelut sisältävät niin kansainvälisiä, kansallisia, kuin pienempienkin organisaatioiden koodistoja. Suomen kansallinen koodistopalvelu ja siihen liittyvä dokumentaatio rajapintakuvauksineen on julkisesti saatavilla osoitteessa koodistopalvelu.kanta.fi .

CodeServer-komponentti käyttää HUS:n koodistopalvelua, joka on teknisesti vastaava kuin kansallinen koodistopalvelu. CodeServer lukee palvelusta joukon ennalta määriteltyjä koodistoja, joista suurin osa on saatavilla vain HUS:n koodistopalvelusta.

## Tietosisältö
Tämä integraatio koskee joukkoa ennalta määriteltyjä koodistoja, joka on hyvin vähän verrattuna satoihin kansallisestikin saatavilla oleviin koodistoihin. Nämä koodistot on valittu ensiksi toteutettaviksi, koska niille on ollut ensimmäisenä konkreettista käyttöä tiedonjalostusprosesseissa.

Ladatut koodistot ovat (CodeServer nimi - Tietoallas nimi):
* Kunnat - kunnat
* Organisaatio-muuntotaulukko - organisaatio_muuntotaulukko
* HUS-Jatkuva-Organisaatio - hus_jatkuva_organisaatio
* HUS-Organisaatio - hus_organisaatio
* HTS-Organisaatio - hts_organisaatio
* Optimaalinen hoitoisuus optimaalinen_hoitoisuus
* Arkipyhät - arkipyhat
* Stakes – Tautiluokitus ICD_10 - stakes_tautiluokitus_icd_10

### Tietosuoja
CodeServer ei sisällä henkilötietoja ja sen tiedot ovat käyttävän organisaation laajuisesti vapaasti saatavilla ilman autentikointi, autorisointi tai salausvaatimuksia. 

## Lataus
Varsinainen lataus tietoaltaaseen tehdään yhdellä SOAP-kyselyllä per koodisto käyttäen 'ListCodes' kutsua. Vastaus-XML tallennetaan sellaisenaan Hadoopiin uudella nimellä, jotta saadaan pysyvästi seurattava ketju tietotransformaatioita. Koodisto ladataan kokonaan jokaisella latauskerralla.

Kommunikaatio koodistopalvelun kanssa tehdään suoraan HTTP:n yli ilman TCP-, HTTP- tai SOAP-tasoisten tietoturvaominaisuuksien käyttöä. 

Latausvaiheessa myös käsitellään XML joukoksi CSV-tiedostoja HDFS:ssä, jotka tulkitaan external Hive-tauluiksi. Tämä transformaatio on koodistokohtainen, joskin kaikki nykyiset koodistot hoitaa sama koodi eri parametreilla. CodeServer latausvaiheen taulut ovat Hivessa tietokannassa nimeltä 'staging_codeserver'

## Varastointi

Alustusvaiheessa latausvaiheen tauluista rakennetaan normaali varastotason tietokanta 'varasto_codeserver_historia_log'. Tämä noudattaa tietoaltaan normaalia nimeämistä ja tulkintaa näiden tietokantojen tarjoamasta näkymästä dataan ja sen muutoksiin.

Tallennus varastotason tietokantaan suoritetaan yksinkertaisuuden vuoksi latausvaiheen yhteydessä.

## Orkestrointi
Tällä hetkellä kaikki CodeServer komponentin vaiheet on orkestroitu ajettavaksi kerran yössä peräkkäin.
