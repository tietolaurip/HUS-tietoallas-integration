# SFTP/Kafka inkrementaalilatauksen yleiskuvaus
Tämä kappalekertoo yleisesti mitä tapahtuu kun tietoaltaaseen ladataan dataa lähdejärjestelmästä käyttäen SFTP:tä ja työntäen näin ladatatun datan Kafka-putkeen

Tiedon siirto lähdejärjestelmästä tietoaltaaseen tapahtuu alla olevan kuvan mukaisesti:

![Tietolähdeintegraatiotasot](https://app.deveo.com/api/blob?path=doc%2Fgeneral%2Fintegrations%2FInkrementaalilataus_sftp.png&project_id=6c633ab7-d36b-453e-93a3-f582d10fae71&repository_id=d13a348c-b006-4b5b-9421-222e52868def&id=doc&account_key=77a137faadbef1a60c42a6c92f10ef90&company_key=d651c93ee7e0127340bd4cfa6e29fa95)

Komponentit ja toiminnallisuus on kuvattu alla olevissa kappaleissa.

## Komponentit
Kuvassa näkyvät komponentit kuvataan kappaleessa [Integraatiokuvien komponentit](int_komponentit.md)

# Toiminnallisuus
Tässä osassa kerrotaan yllä olevan kuvan toimintaperiaate. Alla olevat numerot viittaavat kuvan punaisiin ympäröityihin numeroihin.

## 1. Komponentin asennus (Manager - deploy component)
Integraation inkrementaalilataukseta vastaava komponentti on nimeltään inkrementin_nimi_[instanssin_nimi]_inkr. 

* [Komponenttien asennus](int_2_4_asennus.md)

Jokainen komponentti asennetaan Manager-nodelta komponentin KayttoonOtto.md-dokumentin mukaisesti (löytyy komponentin juurihakemistosta GIT-repositorystä). Jdbc-integraatioiden latauskomponentti asennetaan Headnode-palvelimelle.

## 2. Datan poiminta ja siirto (Lähdejärjestelmä - Uploader )
Lähdejärjestelmä poimii inkrementaalidatan tietojärjestelmästä, tallentaa sen valittuun dataformaattiin ja siirtää tietoaltaan sftp-palvelimelle. Sftp-pohjaisessa integraatiossa siis lähdejärjestelmä on vastuussa inkrementaalidatan (uuden ja muuttuneen datan) lataamisesta tietoaltaaseen.

Lähdejärjestelmä vastaa poiminnan skoopista (muutosten löytölogiikka) ja ajastuksesta (integraatio vs. taulukohtainen ajastus). Jokaiselle lähdejärjestelmälle luodaan oma käyttäjätunnus Tietoaltaaseen. Tiedostot siirretään seuraavin oikeuksin: Hakemisto: 770, Tiedosto: 660.

## 3. Ladattujen tiedostojen prosessointi (Sftp - kProducer)
Kafka producer tarkkailee kansiota, johon lähdejärjestelmä siirtää tiedostot. Ajastus on synkronoitu lähdejärjestelmän siirtoaikatauluun siten, että liiallisia viiveitä ei synny tiedon eteenpäin viemisessä. Tiedostoformaatti vaihtelee eri integroinneissa ja tiedoston luku levyltä ja transformaatio Kafkan dataformaattiin tapahtuu tässä vaiheessa.  

## 4. Datan syöttö Kafkalle (Sftp - kProducer)
kProducer syöttää ladatun datan Kafkalle oikealle Topicille (integraatiokohtainen) [Avro](https://avro.apache.org/)-formaatissa. 

## 5. Datan luku Kafkasta (Utility - kConsumer)
Geneerinen kConsumer käy määräajoin (konfiguroitavissa) tarkastamassa onko sen seuraamaan Topic:iin (1-n) on saapunut uutta dataa. 
 
## 6. Raaka-datan prosessointi Azuren DataLake Storeen (Utility - kConsumer)
kConsumer tallentaa Kafkasta lukemansa datan ensin Azure DataLake Storeen [ORC](https://orc.apache.org/docs/)-formaattiin. Talletettu data on nähtävissä Azuren portalin kautta integraatiospecifisessä kansiossa (/cluster/maindatalake/staging/"integraatio"). Alkulatauksen tiedostot on integraation juurikansiossa ja ikrementtien data alihakemistossa "inc". 

## 7. Datan tallennus raakadata-altaaseen (Headnode, Hive - Raakadata/Staging)
kConsumer kirjoittaa datan DataLake Storen lisäksi hiven raakadata-altaaseen sellaisenaan. Hivessä data on talletettuna relaatiotietokannan mukaisesti tauluihin. Hiven:n tietokannan nimi on "staging_integraatio". Dataa voi tarkastella Hiven CLI-sovellusilla (esim. [Beeline](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-Beeline–CommandLineShell)) hql-kyselyiden avulla. Raakadataa talletettaessa dataan lisätään tieto koska tieto on Tietoaltaaseen lisätty (hive-tauluissa sarake allas__pvm_partitio).

## 8. Datan pseudonymisointi
kConsumer päättelee integraation metadatan perusteella mikä data tulee pseudonymisoida, mikä data poistetaan ja mikä talletetaan sellaisenaan, ja kutsuu ennen datan varastoon tallentamista Pseudonymisointi-palvelua jos prosessoitavaa dataa löytyy. Pseudonymisoinnista löytyy lisätietoja: [Metadata - yleisesitys](03_metadata.md)

## 9. Varasto-datan prosessointi Azuren DataLake Storeen (Utility - kConsumer, Päätietoallas)
kConsumer tallentaa Kafkasta lukemansa ja pseudonymisoidun datan myös Azure DataLake Storeen [ORC](https://orc.apache.org/docs/)-formaattiin. Talletettu data on nähtävissä Azuren portalin kautta integraatiospecifisessä kansiossa. Alkulatauksen tiedostot on integraation juurikansiossa ja ikrementtien data alihakemistossa "inc". 

## 10. Datan tallennus varastoaltaaseen (Utility, Päätietoallas, Hive - Varasto/Storage)
Pseudonymisoinnin jälkeen data talletetaan vastastoaltaaseen. Varastoaltaan tietomalli on sama kuin raakadata-altaassa. Hivessä data on talletettuna relaatiotietokannan mukaisesti tauluihin. Hiven:n tietokannan nimi on "varastp_<integraatio>_historia_log". Dataa voi tarkastella Hiven CLI-sovellusilla (esim. [Beeline](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-Beeline–CommandLineShell)) hql-kyselyiden avulla. 
