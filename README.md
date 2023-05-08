# Házi feladat specifikáció

## Mobil- és webes szoftverek
### 2022.10.23
### Portfolio tracker
### Szász Erik - (B7RBBU)
### szaszco@gmail.com
### Laborvezető: Remény Olivér

## Bemutatás

Az alkalmazás célja a felhasználó befektetésének  nyomonkövetése.
Ezt az Alpha Vantage API segítségével fogom megvalósítani. Ezáltal tudja majd a felhasználó valós időben nyomonkövetni a különböző
részvények árfolyamát, és a befektetett pénzének hozamát.

## Főbb funkciók

Az alkalmazás fő célja, hogy "vásárolni" tudjon az adott felhasználó az általa kívánt részvényeket.
Az adott részvények adatatit az Alpha Vantage API-n keresztül fogjuk elérni. Ennek segítségével tudunk majd virtuálisan "befektetni" az adott részvényekbe.
A föbb nézetek közé tartozik a főoldalunk, ahol láthatjuk az felhasználó által befektetett tőke értékének változását, a hozzá tartozó adatokat, gráfokat stb.
Emellett lesz egy kedvencek listánk, amin megtekinthetjük a felvett részvény szimbólumokat. Ehhez hozzáadni és belőle akár törölni is tudunk. Az adott részvényre rákattintva
megtudjuk vizsgálni az ahhoz tartozó adatokat.
Emellett még két nézetünk lesz, a befektetési történetünk, amelyben a múltbéli tranzakciók lesznek tárolva, illetve egy oldalunk, ahol megtekinthetjük a felhasználó által
vásárolt (és még el nem adott) részvények árát és mennyiségét.

## Választott technológiák:

- Perzisztens adattárolás
- Hálózati kommunikáció
- RecyclerView
- Fragmentek