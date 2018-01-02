# Käyttöönotto

Tämä dokumentti ohjeistaa kuinka Clinisoft integraatiokomponentti otetaan käyttöön. "Clinisoft"-komponentti toimii "Clinisoft_<instanssi>"-integraatioiden kirjasto-komponenttina.

# Asennuspaketin vienti tietoaltaaseen

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta.

```
cd /opt/DataLake
tar xzf Clinisoft.tgz
```
# Asennuspaketin vienti tietoaltaaseen (manager)

Vie asennuspaketti tietoaltaasi manager-vm palvelimelle hakemistoon /opt/DataLake (esim. käyttäen scp:tä) käyttäen tietoaltaan pääkäyttäjätunnusta.

	cd /opt/DataLake
	tar xzf Clinisoft.tgz

# Konfigurointi (manager)

Oikea konfiguraatio tiettyyn altaaseen on paketoitu asennuspakettiin. Erityissyistä, vaikkapa erikoiskokeiluja varten, tätä voi tässä vaiheessa muuttaa hakemistossa `/opt/DataLake/Clinisoft/config`.

# Asennus (manager)

Kun komponentin käyttämä konfiguraatio on luotu, voidaan komponentti asentaa käyttöön:

	cd /opt/DataLake/Clinisoft
	./activate.sh

Asennuksen jälkeen komponentin tarvitsemat ohjelmat ovat oikeilla paikoillaan. Itse datan lataus ei kuitenkin käynnisty vielä tässä vaiheessa.

Paketti ei kuitenkaan sisällä Sybasen JDBC ajuria vaan se pitää hankkia ja asentaa erikseen. Tätä kirjoittaessa oikea ajuri on jconn4.jar. 
