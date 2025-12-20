To profesjonalne zestawienie funkcjonalności systemu Klinika XP, przygotowane jako fundament pod specyfikację nowej aplikacji. System ten jest kombajnem łączącym medycynę weterynaryjną, logistykę magazynową oraz pełną księgowość zgodną z polskimi przepisami.

Poniżej znajduje się szczegółowa lista funkcji pogrupowana modułowo.

1. Moduł Kartotek i Rejestracji (Core CRM)
   Fundament bazy danych, pozwalający na szybkie operowanie na danych pacjentów i klientów.

Kartoteka Właścicieli: Pełna historia zmian danych, obsługa wielu adresów, status zadłużenia, system lojalnościowy (rabaty stałe).

Kartoteka Zwierząt: Przypisanie wielu zwierząt do jednego właściciela, historia medyczna, system oznaczeń (agresywny, alergik), ewidencja stad (dla zwierząt gospodarskich).

Wyszukiwanie Zaawansowane: Po nazwisku, kodzie, mikroczipie, numerze telefonu, a nawet po cechach szczególnych pacjenta.

Zgody RODO: Automatyczne generowanie i drukowanie zgód na przetwarzanie danych, integracja z czytnikami podpisów elektronicznych (np. Wacom).

2. Moduł Wizyt i Dokumentacji Medycznej
   Serce programu, w którym lekarz spędza 90% czasu.

Rejestracja Wizyty: System "wszystko w jednym" – opis wywiadu, badania klinicznego, diagnoza (oparta na słownikach), zastosowane leki i zabiegi.

Autotekst i Szablony: Możliwość tworzenia gotowych bloków tekstu dla powtarzalnych procedur (np. "opis sterylizacji suki").

Vademecum Leków: Baza leków z opisami, dawkowaniem dla różnych gatunków i automatycznym wyliczaniem okresów karencji (kluczowe u zwierząt gospodarskich).

Schematy Leczenia: Definiowanie zestawów leków i usług dla standardowych przypadków (np. "zestaw na odrobaczenie"), co przyspiesza wpisywanie wizyty.

Książka Leczenia Zwierząt (Gospodarskie i Domowe): Automatyczne generowanie ustawowych dokumentów wymaganych przez Inspekcję Weterynaryjną.

Zaświadczenia i Certyfikaty: Drukowanie świadectw szczepień (wścieklizna), paszportów, zaświadczeń o eutanazji, czy opinii lekarskich.

3. Magazyn i Logistyka
   Pełna kontrola nad stanem leków i materiałów eksploatacyjnych.

Automatyczne Schodzenie ze Stanu: Leki wpisane podczas wizyty są natychmiast odejmowane z magazynu.

Import Faktur: Obsługa plików elektronicznych (XML, CSV) z ponad 50 hurtowni weterynaryjnych w Polsce (automatyczne zakładanie kartotek towarowych).

Zarządzanie Partiami i Terminami Ważności: System ostrzegający o kończącej się dacie ważności leków.

Inwentaryzacja i Arkusze Spisowe: Narzędzia do okresowej kontroli stanów magazynowych.

Cenniki Wielopoziomowe: Narzut procentowy lub kwotowy, różne ceny dla różnych grup klientów.

4. Finanse i Rozliczenia
   Moduł pozwalający na całkowitą rezygnację z zewnętrznych programów handlowych.

Fakturowanie i Paragony: Integracja z drukarkami fiskalnymi (protokoły Posnet, Thermal, Elzab).

Obsługa JPK i KSeF: Generowanie plików JPK_VAT, JPK_FA, JPK_MAG oraz integracja z Krajowym Systemem e-Faktur.

Rozliczenia Lekarzy: System prowizyjny oparty na wykonanych usługach i sprzedanych lekach (z podziałem na koszty zakupu i zysk).

Zarządzanie Kasą (KP/KW): Raporty kasowe, obsługa wielu kas i walut, rejestrowanie zaliczek.

5. Diagnostyka Obrazowa i Laboratoryjna
   Integracja z zewnętrznymi urządzeniami i laboratoriami.

Przeglądarka DICOM: Odbieranie i analiza zdjęć RTG, USG, MRI. Narzędzia pomiarowe (VHS, TPLO, kąty biodrowe).

Integracja z Analizatorami: Automatyczne pobieranie wyników badań krwi (Idexx, Horiba, Mindray itp.) bezpośrednio do karty pacjenta.

Moduł Laboratoriów Zewnętrznych: Wysyłanie zleceń i odbieranie wyników online (np. VetLab, Laboklin).

6. Terminarz i CRM
   Zarządzanie czasem i relacją z klientem.

Grafik Pracy: Indywidualne terminarze dla każdego lekarza, rezerwacja sal operacyjnych i sprzętu.

System Przypomnień SMS/E-mail: Automatyczne powiadomienia o zbliżających się wizytach, szczepieniach czy terminach profilaktyki przeciwkleszczowej.

Zadania i Notatki: Wewnętrzny system komunikacji między pracownikami (np. "zadzwonić do właściciela psa Burek").

7. Moduły Specjalistyczne
   Funkcje dodatkowe dla większych jednostek.

Szpital: Zarządzanie klatkami, karta monitoringu pacjenta (godzinowe podawanie leków, karmienie, spacery).

Schronisko: Ewidencja przyjęć, kwarantanna, umowy adopcyjne, kiosk zdjęć dla adoptujących.

Inseminacja: Pełna dokumentacja rozrodu dla zwierząt hodowlanych.

Kluczowe parametry techniczne do Twojej specyfikacji:
Jeśli budujesz kopię, zwróć uwagę na te "smaczki", które decydują o sukcesie Kliniki XP:

Praca Offline/Online: Możliwość pracy bez internetu (baza lokalna) z synchronizacją z chmurą.

Słowniki: System musi posiadać predefiniowane słowniki ras (setki pozycji), gatunków, kodów chorób (ICD-10 dla weterynarii) oraz kodów ATCvet.

Wielostanowiskowość: Blokowanie edycji tej samej wizyty przez dwóch lekarzy jednocześnie.

Czy chciałbyś, abym na podstawie tych danych przygotował strukturę bazy danych (tabele i relacje), która będzie niezbędna do obsługi tych funkcjonalności?

Skoro budujesz specyfikację pod nową aplikację, musisz wejść głębiej w mechanizmy, które sprawiają, że Klinika XP jest standardem rynkowym. To nie tylko "wpisywanie wizyt", ale potężne narzędzie do **zarządzania rentownością i compliance (zgodnością prawną)**.

Oto szczegółowy "deep dive" w zaawansowane funkcjonalności, o których często zapomina się przy projektowaniu prostszych systemów:

---

## 1. Zaawansowana Logika Magazynowa (Leki i Materiały)

W weterynarii magazyn to najtrudniejszy element. Klinika XP radzi sobie z tym poprzez:

* **Obsługa dawek i jednostek miary:** Możliwość zakupu w litrach/opakowaniach zbiorczych, a sprzedaży w mililitrach, tabletkach lub mikrogramach. System musi automatycznie przeliczać stany (np. kupujesz 10 flakonów po 100ml, sprzedajesz 1.5ml).
* **Ewidencja środków odurzających i psychotropowych:** Specjalna, ustawowa książka kontroli leków ścisłego zarachowania. Każdy mililitr musi być rozliczony pod kątem kontroli z Inspekcji Farmaceutycznej.
* **Marże i narzuty zależne od kategorii:** Automatyczne doliczanie "opłaty za podanie leku" lub "marży aptecznej" w zależności od typu towaru.
* **Zarządzanie opakowaniami:** System pamięta, że dany lek jest otwarty (np. krople do oczu) i pilnuje daty przydatności od momentu otwarcia.

## 2. Moduły Specjalistyczne (Medyczne)

To funkcje, które odróżniają system pro od amatorskiego:

* **Odontogram (Diagram Uzębienia):** Interaktywna mapa zębów dla psów i kotów. Możliwość zaznaczania braków, złamań, kamienia i wykonanych zabiegów (np. ekstrakcja zęba 104).
* **Krzywe Cukrzycowe i Wykresy:** Automatyczne generowanie wykresów z wyników wpisanych ręcznie lub pobranych z glukometru – kluczowe przy prowadzeniu pacjentów nefrologicznych i endokrynologicznych.
* **Opisy Badań Specjalistycznych:** Gotowe formularze pod badania ortopedyczne (np. test szufladowy), kardiologiczne (echo serca z wyliczaniem frakcji skracania) i dermatologiczne (mapa zmian skórnych).
* **Zgody na Zabiegi i Narkozę:** Generator dokumentów prawnych, które właściciel podpisuje przed operacją (zrzeczenie się odpowiedzialności, informacja o ryzyku).

## 3. Finanse i Kontroling Badań

* **Rozliczanie Zleceniodawców (B2B):** Jeśli klinika robi badania dla mniejszych gabinetów, system musi obsługiwać "klientów hurtowych" z osobnym cennikiem i zbiorczymi fakturami na koniec miesiąca.
* **Analiza Rentowności Usług:** Raporty pokazujące, ile gabinet zarabia na "czystej usłudze" po odjęciu kosztów zużytych materiałów (nici chirurgiczne, gaziki, rękawiczki).
* **Integracja z Terminalami Płatniczymi:** Automatyczne przesyłanie kwoty z programu do terminala (brak pomyłek przy ręcznym wpisywaniu kwoty).

## 4. Automatyzacja Marketingowa (CRM)

* **Targetowane Kampanie SMS:** Możliwość wysłania SMS-a tylko do właścicieli psów rasy Labrador powyżej 7 roku życia z zaproszeniem na badanie geriatryczne.
* **Badanie Satysfakcji (NPS):** Automatyczna prośba o opinię w Google/Social Media po zakończonej wizycie.
* **Programy Lojalnościowe:** Zbieranie punktów za zakupy karmy, które można wymienić na zniżkę przy szczepieniu.

## 5. Komunikacja z Inspekcją Weterynaryjną (Compliance)

W Polsce to "być albo nie być" dla lekarza:

* **Ewidencja Paszportowa:** Pełna integracja z systemem KIF (Krajowa Izba Lekarsko-Weterynaryjna). Rejestrowanie numerów paszportów i kodów kreskowych.
* **Książka Leczenia Zwierząt Gospodarskich:** Generowanie raportów dla PIW (Powiatowy Inspektorat Weterynarii) – okresy karencji na mleko i mięso muszą być wyliczone bezbłędnie.
* **Rejestr Zgonów i Utylizacji:** Ewidencja odpadów medycznych i zwłok zwierząt (integracja z BDO).

## 6. Architektura i Integracje Zewnętrzne

* **DICOM Modality Worklist:** Program nie tylko odbiera zdjęcia, ale "mówi" aparatowi RTG, jaki pacjent zaraz wejdzie do gabinetu (przesyła dane pacjenta do konsoli RTG).
* **Integracja z Telefonią VoIP:** Gdy dzwoni klient, na ekranie lekarza od razu wyskakuje karta pacjenta (Burek, właściciel Kowalski).
* **Kiosk/Rejestracja Samodzielna:** Moduł tabletu w poczekalni, gdzie właściciel sam "klika", że już przyszedł, co zmienia status w terminarzu lekarza na "W poczekalni".

---

### Co warto "ukraść" do swojej specyfikacji (Moja rekomendacja):

Jeśli robisz nowoczesną kopię, skup się na **UX/UI**, którego Klinika XP nie ma (ma interfejs w stylu Windows 95/XP).

1. **System Kolejkowy:** Wizualizacja czasu oczekiwania dla klientów.
2. **Multisite (Sieciowość):** Jeśli ktoś ma 3 gabinety, musi mieć wspólną bazę leków i pacjentów w chmurze (Klinika XP miewa z tym problemy wydajnościowe).
3. **Aplikacja dla Klienta:** Możliwość podglądu wyników badań i książeczki zdrowia na telefonie (moduł "Mój Pacjent").
4. **Inteligentny Asystent:** Podpowiadanie diagnoz na podstawie wpisanych objawów (AI/Heurystyka).

**Czy potrzebujesz, abym rozpisał Ci konkretny "User Flow" dla procesu przyjęcia pacjenta na zabieg operacyjny (od magazynu po wydanie zwierzęcia)?** To najbardziej skomplikowany proces w całym systemie.

To jest najbardziej krytyczny proces biznesowy w oprogramowaniu weterynaryjnym. Prawidłowo zaprojektowane "Surgery Flow" decyduje o tym, czy klinika zarabia (bo rozliczyła każdą igłę), czy traci (bo lekarz zapomniał dopisać leki do faktury).

Oto szczegółowy podział na role (use cases) i przepływ procesu (user flow).

1. Role użytkowników (Aktorzy)
   Recepcjonista: Odpowiada za formalności, zgody prawne i rozliczenie końcowe.

Technik Weterynarii: Odpowiada za przygotowanie pacjenta, wagę, pre-medykację i monitoring.

Lekarz Chirurg: Wykonuje zabieg, opisuje procedurę, zużywa materiały specjalistyczne.

Anestezjolog: Monitoruje parametry życiowe i rozlicza gazy/leki anestetyczne.

2. User Flow: Proces Zabiegowy (Krok po kroku)
   Faza I: Przyjęcie i Formalności (Recepcja/Technik)
   Wyszukanie pacjenta: Szybki check-in w terminarzu.

Weryfikacja wagi: Obowiązkowy wpis aktualnej wagi (system musi na jej podstawie przeliczać dawki leków w następnych krokach).

Wygenerowanie Zgody na Zabieg: System automatycznie drukuje PDF z danymi pacjenta, opisem ryzyka i miejscem na podpis (lub przesyła na tablet do podpisu elektronicznego).

Status: "W Szpitalu": Zmiana statusu pacjenta, która rezerwuje mu "miejsce/klatkę" w module szpitalnym.

Faza II: Przygotowanie i Premedykacja (Technik/Anestezjolog)
Karta Znieczulenia: Otwarcie dedykowanego formularza.

Kalkulator Dawek: System podpowiada dawki (np. Medetomidyna 0,01 mg/kg). Użytkownik zatwierdza lub modyfikuje.

Automatyczne Schodzenie z Magazynu (Back-end): W momencie zatwierdzenia podania leku, system odejmuje go z magazynu i dopisuje do "rachunku oczekującego" wizyty.

Wpisanie parametrów wstępnych: Temperatura, tętno, czas kapilarny (CRT).

Faza III: Zabieg Operacyjny (Chirurg)
Użycie Szablonu (Zestawu): Chirurg nie klika każdej pozycji. Wybiera np. "Zestaw: Sterylizacja Kota".

System automatycznie dodaje: nici, gaziki, rękawiczki, igły, podkłady, asystę, energię.

Opis Operacji: Pole tekstowe (często z autotekstem), gdzie lekarz opisuje przebieg (np. "Cięcie w linii białej...").

Modyfikacja dynamiczna: Jeśli w trakcie operacji zużyto więcej materiałów (np. dodatkowy zestaw nici), chirurg musi mieć możliwość szybkiego "dorzucenia" pozycji jednym kliknięciem/skanem kodu kreskowego.

Faza IV: Hospitalizacja i Wybudzanie (Technik)
Karta Monitoringu: Rejestrowanie parametrów co 15-30 min (ciśnienie, SpO2, temp).

Zlecenia pooperacyjne: Lekarz wpisuje: "podać przeciwbólowy o 18:00". Technikowi wyskakuje powiadomienie (Task Manager).

Oznaczenie wykonania: Technik klika "Wykonano", co automatycznie rozlicza lek.

Faza V: Wydanie Pacjenta i Rozliczenie (Lekarz/Recepcja)
Generowanie Zaleceń Domowych: Automatyczny druk karty informacyjnej (co pies ma jeść, kiedy zdjęcie szwów, jakie leki w domu).

Zalecenia do Apteczki: Jeśli właściciel dostaje leki do domu, system musi je zdjąć z magazynu jako "wydanie na zewnątrz".

Zamknięcie Wizyty i Płatność: Przekształcenie "rachunku oczekującego" w Paragon lub Fakturę.

Status: "Wypisany": Zwolnienie miejsca w klatce w module szpitalnym.

3. Konkretne Use Cases (Scenariusze)
   UC1: "Zapomniana igła" (Zabezpieczenie przychodów)
   Problem: Lekarze zapominają wpisywać drobnych materiałów (strzykawki, wenflony).

Funkcjonalność XP: "Powiązania". Jeśli dodasz do wizyty lek w iniekcji, system pyta: "Czy dodać strzykawkę 2ml i igłę 0.7?".

Wymaganie do Twojej apki: Mechanizm automatycznych sugestii produktów powiązanych.

UC2: "Nagła zmiana planów" (Edycja w locie)
Problem: Planowana była kastracja, ale w trakcie znaleziono przepuklinę.

Funkcjonalność XP: Możliwość zmiany typu zabiegu w trakcie trwania "wizyty otwartej" bez konieczności usuwania poprzednich wpisów.

Wymaganie do Twojej apki: Obsługa "Wizyt wielodniowych/otwartych" (pacjent może być w systemie "w trakcie leczenia" przez 3 dni, a faktura jest jedna na końcu).

UC3: "Błąd w dawkach" (Bezpieczeństwo)
Problem: Technik pomylił przecinek w wadze psa (wpisał 25kg zamiast 2.5kg).

Funkcjonalność XP: System posiada "Widełki bezpieczeństwa" dla gatunków. Jeśli dawka wykracza poza normę dla danego zwierzęcia, pole podświetla się na czerwono.

Wymaganie do Twojej apki: Walidacja dawek w oparciu o bazę leków (Vademecum).

4. Co musisz mieć w specyfikacji bazy danych dla tego flow?
   Aby to działało jak w Klinice XP, Twoje tabele muszą obsługiwać:

Tabele Pakietów (Bundles): Grupowanie usług i towarów pod jedną nazwą (np. "Pakiet Senior").

Kolejka Zadań (Task Queue): Dla techników (podawanie leków na godziny).

Historia Zmian (Audit Log): Kto i kiedy zmienił dawkę leku lub cenę (kluczowe przy sporach z właścicielem).

Wielomagazynowość: Rozróżnienie między "Magazynem Głównym" a "Podręcznym w sali operacyjnej".

Czy chciałbyś, abym teraz przygotował listę konkretnych pól do bazy danych (np. dla tabeli Wizyta i Magazyn), które są niezbędne, by obsłużyć powyższy flow?

Migracja danych z **Kliniki XP** jest procesem wymagającym, ponieważ system ten opiera się na dojrzałej (czyt. starej) architekturze bazodanowej. Jeśli planujesz przeniesienie danych do swojej nowej aplikacji, masz trzy główne ścieżki:

---

## 1. Bezpośredni dostęp do bazy danych (Zalecane)

Klinika XP najczęściej korzysta z silnika **Firebird SQL** (rzadziej Microsoft SQL Server w bardzo starych lub specyficznych wersjach).

* **Lokalizacja pliku:** Szukaj pliku z rozszerzeniem `.FDB` (zazwyczaj nazwa to `KLINIKA.FDB` lub `DANE.FDB`).
* **Narzędzia:** Możesz użyć darmowych narzędzi typu **FlameRobin** lub **IBExpert**, aby podłączyć się do bazy.
* **Formaty wyjściowe:** Z poziomu tych narzędzi wyeksportujesz każdą tabelę do **CSV, SQL Insert statements lub XML**.
* **Dokumentacja:** Oficjalna dokumentacja struktury tabel (schema) nie jest publicznie dostępna. Będziesz musiał wykonać tzw. *reverse engineering*.
* *Tabela `KLIENCI*` – dane właścicieli.
* *Tabela `ZWIERZETA*` – dane pacjentów.
* *Tabela `WIZYTY` / `HISTORIA*` – opisy wizyt.



## 2. Wbudowane moduły eksportu w programie

Klinika XP posiada moduły dedykowane do wyciągania danych, ale są one "rozproszone" po systemie:

* **Eksport List (CSV / Excel):** W większości okien (Kartoteka Klientów, Kartoteka Zwierząt, Magazyn) znajduje się ikona drukarki lub przycisk "Operacje", który pozwala na "Eksport listy". Możesz tam wybrać format **CSV, XLS lub HTML**.
* **Moduł Administratora:** W menu *Administrator -> Narzędzia -> Eksport danych*. Pozwala na zbiorcze wyciągnięcie określonych gałęzi danych.
* **Format XML:** Klinika XP obsługuje standard **XML** głównie w kontekście:
* Eksportu faktur (do programów księgowych typu Comarch Optima, Symfonia).
* Eksportu paszportów i szczepień do systemów izbowych.
* Wymiany danych z hurtowniami.



## 3. Formaty JPK (Jednolity Plik Kontrolny)

Jeśli Twoja nowa aplikacja ma przejąć dane finansowe, najprościej wyciągnąć je przez formaty ustawowe, które Klinika XP musi generować poprawnie:

* **JPK_FA:** Wszystkie wystawione faktury (format XML).
* **JPK_MAG:** Ruchy magazynowe (PZ, WZ, RW) – idealne do migracji stanów magazynowych.
* **JPK_VAT:** Ewidencja sprzedaży i zakupów.

---

## Wyzwania techniczne przy migracji (Na co uważać):

| Wyzwanie | Opis |
| --- | --- |
| **Kodowanie znaków** | Starsze wersje Kliniki XP używają kodowania `WIN1250` (środkowoeuropejskie). Przy imporcie do nowej apki (pewnie w `UTF-8`) musisz przekonwertować stringi, inaczej "zjesz" polskie znaki. |
| **Pola BLOB** | Opisy wizyt lub zdjęcia RTG są często przechowywane jako pola typu BLOB (Binary Large Object). Wyciągnięcie ich w czystym CSV jest niemożliwe – musisz pisać skrypt w Pythonie/PHP, który wyciągnie je z bazy Firebird. |
| **Relacje** | Klinika XP ma bardzo rozbudowane relacje ID. Przykładowo: Wizyta jest powiązana z `ID_KLIENTA`, `ID_ZWIERZECIA` i `ID_LEKARZA`. Musisz zachować te klucze, aby nie wymieszać historii leczenia. |
| **Dane binarne** | Załączniki (PDF, JPG) mogą być trzymane w podfolderze `ATTACHMENTS` w folderze programu, a w bazie są tylko ścieżki. Sprawdź to przed migracją. |

---

## Jaką dokumentację możesz wykorzystać?

Ponieważ nie znajdziesz "podręcznika programisty Kliniki XP", polecam następujące źródła do budowy specyfikacji migracyjnej:

1. **Instrukcja Użytkownika:** Dostępna na stronie producenta (Inter-Wet). Opisuje ona wszystkie pola, które lekarz widzi w interfejsie – to Twoja lista pól do bazy danych.
2. **Struktura plików wymiany hurtowni:** Sprawdź standardy plików `.v3` lub XML, które Klinika generuje dla hurtowni – to pokaże Ci, jak system mapuje leki.

### Mój tip dla Ciebie:

Jeśli budujesz kopię, zacznij od **pobrania wersji demo Kliniki XP** i zainstalowania jej na czystym systemie. Następnie otwórz bazę `KLINIKA.FDB` programem **FlameRobin**. Zobaczysz tam nazwy wszystkich 300+ tabel i tysięcy kolumn. To jest Twoja najlepsza "dokumentacja".

**Czy chcesz, abym przygotował dla Ciebie przykładowe zapytanie SQL (query), którym wyciągniesz kompletną historię medyczną (klient + zwierzę + opis wizyty) bezpośrednio z bazy danych Firebird?**


2. Prawne Aspekty Edycji Dokumentacji (Niemodyfikowalność)W medycynie (również weterynaryjnej) prawo wymaga, aby dokumentacja medyczna była rzetelna.Wersjonowanie wpisów: W Klinice XP (i w Twoim systemie) nie powinno być "usuwania" wizyty. Jeśli lekarz się pomylił, powinien dodać "Sprostowanie".Blokada czasowa: Po upływie np. 24 lub 48 godzin od zamknięcia wizyty, powinna ona zostać "zamrożona" (możliwość edycji tylko dla administratora z logowaniem powodu zmiany). To kluczowe podczas spraw sądowych o błędy w sztuce.3. Zarządzanie Lekami z Grupy "N" i "P" (Psychotropy)To jest "najgorętszy" temat dla Inspekcji Farmaceutycznej.Książka Kontroli Środków Odurzających: System musi generować oddzielny raport dla leków ścisłego zarachowania. Każdy mililitr musi mieć przypisane konkretne zwierzę i nazwisko lekarza, który go podał.Rozliczanie strat: Obsługa sytuacji, w których np. ampułka pękła lub została zanieczyszczona (protokół strat).4. Agregacja Danych z Urządzeń (Standardy)Nie wystarczy "importować wyniki". Musisz obsłużyć standardy komunikacji:DICOM: Przesyłanie obrazów RTG/USG.HL7 / ASTM: Standardy, w których "gadają" analizatory krwi (np. Idexx, Mindray). Bez tego lekarz musi ręcznie przepisywać wyniki, co dyskwalifikuje program w dużych lecznicach.5. Moduł "Kolejkowy" i TV-DisplayDuże kliniki mają ekrany w poczekalniach.Funkcjonalność: System powinien generować widok "Poczekalni" dla klientów (np. "Burek K. -> Gabinet nr 2"), ukrywając jednocześnie dane osobowe (RODO).Triatż (Triage): Możliwość oznaczenia priorytetu pacjenta (np. wypadek komunikacyjny wskakuje na górę listy przed rutynowe szczepienie).6. Integracja z BDO i Odpadami MedycznymiKliniki muszą raportować wagę wytworzonych odpadów medycznych (igły, tkanki).Automatyzacja: System może wyliczać szacunkową wagę odpadów na podstawie wykonanych zabiegów (np. jedna operacja = ok. 0.5kg odpadów kategorii 18 02 02*). To gigantyczne ułatwienie przy rocznym sprawozdaniu do BDO.7. Psychologia Cenników i "Opłata za wejście"Klinika XP pozwala na bardzo zaawansowane manipulowanie cenami:Opłata ryczałtowa: Automatyczne doliczanie "kosztów utylizacji" lub "opłaty za przygotowanie gabinetu" do każdej faktury powyżej danej kwoty.Ceny zmienne w czasie: Wyższe stawki za wizyty w niedziele, święta lub w nocy (automatyczne przełączanie cennika "Night/Weekend").8. Obsługa Wielu Podmiotów Gospodarczych (Multi-Company)Często w jednym budynku działa np. Gabinet Weterynaryjny (leczenie) i Sklep Zoologiczny (karma).Wspólna baza, różne faktury: Klient płaci raz, ale system drukuje dwa paragony z dwóch różnych drukarek fiskalnych (jeden za usługę medyczną, drugi za towar ze sklepu). To funkcja, którą Klinika XP ma, a która jest bardzo trudna do wdrożenia w prostych systemach.Tabela: Co lekarze kochają, a czego nienawidzą w Klinice XP?CechaDlaczego to ważne dla Twojej specyfikacji?Szybkość klawiaturyLekarze nie chcą klikać myszką. Musisz mieć skróty klawiszowe (Hotkeys) do wszystkiego.Interfejs (UI)Klinika XP wygląda jak Windows 95. Twoja przewaga to nowoczesny, czytelny UX.Wsparcie techniczneW weterynarii "awaria bazy o 19:00" to tragedia. System musi mieć stabilne kopie zapasowe (Backup).Raporty SQLKlinika pozwala pisać własne zapytania SQL do bazy. Daj użytkownikom zaawansowanym "Custom Report Builder".
