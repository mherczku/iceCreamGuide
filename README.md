# IceCreamGuide
IceCreamGuide Android App

#### Projekt bemutatása:

Fagylaltozókat nyilvántartó mobil alkalmazás megtervezése és elkészítése közösségi használatra. Az alkalmazás Android platformra lesz implementálva Kotlin nyelven. Az alkalmazás a nyílt forráskódú RainbowCake architektúrára fog épülni, melynek legfőbb előnye, hogy hatékonyan használja ki a Kotlin nyelv lehetőségeit. Az alkalmazás backendnek a Google Firebase szolgáltatását használja, melynek konfigurálása is a projekt része.

Az felhasználóknak lehetőségük lesz a nyilvántartott fagylaltozókat listázni, a fagylaltozók között keresni, és ezeket a fagylaltozókat értékelni. A felhasználóknak lehetőségük lesz a fagylaltozókat térképen megjeleníteni, melyhez az alkalmazás a Google Maps szolgáltatását használja fel. Mindemellet képesek lesznek új fagylaltozó felvételének kérvényezésére és meglévők jelentésére, melyet bármely admin jogú felhasználó jóváhagyhat.



#### Architektúra - RainbowCake

Tulajdonságai:

- Tisztán elkülöníti a különböző rétegeket és komponenseket
- Mindig konzisztens állapotban tartja a View-okat a ViewModel-ekkel
- Ügyesen kezeli a konfigurációs változtatásokat
- A megfelelő feladatok háttérszálakra történő lerakását egyszerűvé teszi

![](https://d33wubrfki0l68.cloudfront.net/5c87ced651e328f33727b33bbe9a871e482350a2/63186/images/arch_overview.png)

#### Felhasznált technológiák

- Firebase:
  - FirebaseAuth - Autentikációhoz
  - FireStore - Backend, adatok tárolása (fagylaltozók adatai), Felhasználói adatok tárolása
  - Storage: Képek tárolása (felhasználói, fagylaltozóké egyaránt)
- Google Maps SDK Android - Térképen történő megjelenítéshez
- Kotlin Coroutines: Lehetővé teszik az aszinkron kódok írását
- Hilt Dependency Injection
- Android Lifecycle: Életciklus tudatos aszinkron kódok írását teszi lehetővé, azaz az aszinkron műveleteknek megszabhatunk hatóköröket, hogy azon belül fussanak.
- UI:
  - Google Material: Az alkalmazás kinézete nagyrészt a google material design elvét követi
  - ConstraitLayout: Komplex felhasználó felületek elkészítésére
  - SwipeRefreshLayout: Lista nézetek manuális frissítéséhez
  - RecyclerView: Elemek megjelenítése listában
  - Android Preference: SharedPreference egyszerű használata beállítási értékekhez
  - RoundedImageView: Kerek képek megjelenítése
  - Glide: Képek betöltése internetről
  - NavigationDrawer: Elhúzható navigációs ablak
  - Többnyelvűség támogatása (angol, magyar)
  - Sötét téma támogatása

#### Use-Case Diagram

![](https://i.ibb.co/0YQqWWK/k-p.png)

