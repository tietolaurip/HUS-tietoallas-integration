Tämä dokumentti kuvaa varastokerroksen yleisiä toimintaperiaatteita. Jotkin tietolähteet voivat näistä omalta osaltaan erityisistä syistä poiketa, mutta pyrkimys on tässä kuvatulla laajuudella yhtenäisyyteen.

# 1. Yleistä

Kaikki varastokerroksen data on koottu Hive-tietokantoihin. Rakenteellinen metadata (taulut, partitiot, sarakkeet, jne.) on siis luettavissa Hive DDL-komennoilla.

Itse dataa voi toki lukea myös muilla työkaluilla suoraan tiedostoista HDFS- tai Microsoft Azure Datalake Store(R) rajapintojen kautta ja vastaavasti rakenteellista metadata suoraan Hive metastoresta SQL-rajapinnan kautta.

Tässä dokumentissa viitataan kuvitteelliseen integraatioon nimeltä xyz, jotta esimerkit erilaisista nimistä saavat konkreettisen muodon.

# 2. Tietokannat

Jokainen integraatio tuottaa yhden varastotason Hive tietokannan. Jos lähdejärjestelmäinstansseja on useampia, niin ne jokainen nähdään erillisinä integraatioina ja niille on oma tietokanta. Integraatiolle nimeltään xyz tämä kanta on nimeltään varasto_xyz_historia_log. Jos integraatiolle on useita instansseja, nimeäminen menee varasto_xyz_<instanssin nimi>_historia_log.

Lähdejärjestelmän ollessa relaatiotietokanta, nämä taulut ovat samat kuin lähdejärjestelmässä. Muunlaisista lähtötietomalleista taulurakenne on integraation määrittelemä. 

Käsitteellisesti jokaista näistä tietokannoista voi ajatella näkyminä samaan dataan, vaikka tietokantateknisesti ne eivät näkymiä olekaan. Nämä näkymät on suunniteltu helpottamaan ja nopeuttamaan datan tavallisia käyttötapoja.

# 3. Tallennus

Kaikki varastokerroksen taulut on tallennettu ORC muotoisiin tiedostoihin, joten käytettyjen analyysi- ja prosessointityökalujen pitää tätä tiedostomuotoa tukea. 

# 4. Nimeämis- ja muotokäytännöt

Pääasiassa varastotietokannoissa näkyvät nimet pyritään pitämään suorina johdoksina raakadatan vastaavista. Tietoallas pyrkii kuitenkin helpottamaan datan käyttöä, yhdistelemistä ja yhteensopivuutta erilaisten työkalujen ja kirjastojen kanssa pakottamalla joitakin yhtenäisiä nimeämiskäytäntöjä.
 
Kaikki altaan lisäämä tieto alkaa etuliitteellä `allas__` (huom. kaksi alaviivaa). Esimerkiksi `allas__pvm_partitio` staging_* tauluissa on altaan lisäämä tieto siitä, minkä päivän mukaisesta tiedosta on kyse.

Jossain tapauksissa varastotasolla on sarake joka sisältää alkuperäisen datan sellaisenaan ja toinen allas__-alkuinen, joka sisältää jonkinlaisen muunnoksen datasta. Tietoallas ei koskaan korvaa alkuperäistä dataa muunnetulla alkuperäisen nimiseen sarakkeeseen. Joissakin tapauksissa (esim. pseudonymisointi) alkuperäinen sarakedata ei vaan näy varastotasolla ollenkaan. 

Taulu- ja sarakenimet johdetaan alkuperäisistä tekemällä seuraavat muunnokset:
* ääkköset vastaaviksi ASCII-kirjaimiksi (siis 'ä' -> 'a', 'ö' -> 'o', 'å' -> 'a')
* Kirjaimet vastaaviksi pieniksi kirjaimiksi (esim. 'H' -> 'h' )
* Välilyönnit alaviivoiksi (' ' -> '_')
* Väliviivat alaviivoiksi ( '-' -> '_') 

Kaikissa tietoaltaan lisäämissä (`allas__` alkuiset) päivämäärä- tai aikaleimasarakkeissa päivämäärät ovat ISO 8601 muotoa.
