# System Wykrywania Zmian w Plikach (BSSK)

Projekt realizuje system sprawdzania integralności plików za pomocą algorytmu SHA-256.

## 📍 Lokalizacja rozwiązania
Główna logika programu oraz implementacja sprawdzania hashy znajduje się w pliku:
**`src/com/bssk/FileIntegrityChecker.java`**

## 👥 Autorzy
* Tomasz Florek - (ok. 50% wkładu: logika hashingu, obsługa CLI)
* Bartłomiej Futyma - (ok. 50% wkładu: obsługa bazy danych, testy)

## 🛠️ Instrukcja uruchomienia
1. Skompiluj projekt: `javac com/bssk/FileIntegrityChecker.java`
2. Uruchom program, podając ścieżkę do pliku jako argument:
   `java com.bssk.FileIntegrityChecker test.txt`

## 📝 Funkcje
* Wykrywanie nowych plików i zapisywanie ich stanu.
* Weryfikacja zmian w istniejących plikach przy użyciu bezpiecznego hasha SHA-256.
* Odporność na manipulację datą modyfikacji (sprawdzamy treść, nie metadane).