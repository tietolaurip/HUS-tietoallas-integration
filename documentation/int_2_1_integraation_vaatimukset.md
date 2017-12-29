# Integrointien yleiset vaatimukset
Alla olevassa taulukossa on listattu integraation yleiset vaatimukset.

|Vaatimuksen ID| Vaatimuksen nimi | Vaatimuksen kuvaus|Muokkauspvm. | 
| ------------ | ---------------- | ----------------- | ----------- |
|INT\_10|Integraation asennus (Deployment)|Integraation tulee olla asennettavissa järjestelmään asennusautomaation avulla.|05.12.2017|
|INT\_20|Integraation alustus(Initial setup)|Integraatio tulee alustaa Tietoaltaaseen siten, että tarvittavat metadata ja hive-tietorakenteet mahdollistavat datan lataukset Tietoaltaaseen. |05.12.2017|
|INT\_30|Integraatiodatan alkulataus (Initial import)|Integraation pitää pystyä lataamaan lähdejärjestelmän historiadata (lähdejärjestelmän käyttöönotosta tähän päivään) Tietoaltaaseen. Joissakin integraatioissa (esim. sanomapohjaiset integraatiot) alkulataus ei ole mahdollista. |29.12.2017|
|INT\_40|Integraatiodatan inkrementtilataus (Incremental import)|Integraation tulee pystyä lataamaan lähdejärjestelmän muuttuva data Tietoaltaaseen säännöllisesti alkulatauksen jälkeen. Poikkeukset tulee kirjata integraation dokumentaatioon.|05.12.2017|

