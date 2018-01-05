# Komponentin kuvaus

Tämä dokumentti ohjeistaa kuinka staginstreamconsumer integraatiokomponentti otetaan käyttöön. staginstreamconsumer ei ole integraatio, vaan tarjoaa Kafka Consumer palvelun, jolla Katkalle streamattava data käsitellään (kirjoitetaan staging ja storage hive-kantoihin ja pseudonymisoidaan)

# Esivaatimuksia
* base on asennettu

# Asennuspaketin luonti (kehitysympäristö)

Luo asennuspaketti seruaavasti:

	cd <public-git>/integrations/staginstreamconsumer
	../pack.sh hus [dev|test|prod]

Lataa jar artifactoryyn

	mvn deploy
	
Asennuspaketti on tähän hakemistoon syntynyt staginstreamconsumer-hus-[dev|test|prod].tar.gz

# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta.

	cd /opt/DataLake
	tar xzf staginstreamconsumer-hus-[dev|test|prod].tar.gz

# Asennus (manager)

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	/opt/DataLake/staginstreamconsumer/activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan

# Komponentin alustus (utility)

Kopio ohjelman jar ja properties-tiedosto adl:ään:

	/opt/DataLake/staginstreamconsumer/initial_setup.sh

# Komponentin käynnistys (utility)

	/opt/DataLake/staginstreamconsumer/start_consumer.sh


# Tarkista komponentin ajoloki

Kaikki komponentin tuottamat lokit löytyvät seuraavalla tavalla: 

Kirjaudu web-selaimella YARN-palvelun monitorintiin:
* Mene osoitteeseen: https://10.252.6.85/#/main/services/YARN/summary
* Valitse vasemmalta listasta "YARN"-palvelu
* Valitse keskeltä "Quick Links"-valikosta: "*.fx.internal.cloudapp.net (Active)" ja sen alta "Resource Manager UI"
* Klikkaa "All Applications"-listalta nimen "staginstreamconsumer" ID-kentän linkkiä
* Clikkaa sivun "Application application_*" "Logs"-linkkiä alhaalla
* Seuraavaksi voit tutkia joko stderr tai stdout tiedostoja
