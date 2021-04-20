# pbl-server-app-0.1
Server application for generating and managing data for machine learning

# Next-ToDo

Frontend
- poprawić index tj. pozbyć się tego marginesu nad menu
- podstrona z wstępną wersją formularza dodawania kształtu tj. pole "nazwa", pole liczbowe do zadeklarowania ilości pól do wypełnienia (spinner to się chyba nazwa albo coś podobnego), button submit.
Ta podstrona będzie tylko wstępem do generowania obiektu kształtu, o detale zapytamy we wtorek
- wstępny formularz rejestracji (też go najwyżej poprawimy/wywalimy jak powie że nie tak) czyli pole nickname/email, hasło, potwierdź hasło, wybór roli na radiobuttonach, między userem a adminem, button submit
- jakiś content na tą stronę główną, może jakaś instrukcja opisująca pokrótce co jaka opcja robi

Backend
- stworzenie oraz walidacja nowego użytkownika
- tworzenie zestawu kształtu do obsługi w formularzu
- serwis do przekazywania tasków obliczeniowych w RabbitMQ
- serwis mailowy do przekazywania informacji o zakończonych zadaniach
- migracja bazy na MySQL (?)

# To clear

- sposób tworzenia użytkowników, rejestracja z poziomu formularza czy statycznie potworzyć kilku zdefiniowanych użytkowników
- Zakładka Wyniki -> w jaki sposób wyniki mają być przechowywane w bazie danych (np ścieżki do wygenerowanych plików na serwerze, daty utworzenia i status , czy też może jakoś inaczej)
- dokładniejsze informacje na temat "zestawu"/"kształtu" -> czy tylko to mają być dane typu nazwa i szereg parametrów typu długość szerokość etc czy też inne
- jak ma wyglądać proces tworzenia nowego zadania, -> czy to ma być tylko wybranie kształtu, zadanie wartości i wysłanie
- czy do serwisu kolejkowania mają być dostępne z poziomu panelu ewentualne zmiany parametrów konfiguracyjnych
- rodzaj bazy danych (MSSQL, MySQL, czy cokolwiek...)
