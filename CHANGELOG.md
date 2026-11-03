# История изменений

## 0.1.16

- Добавлены проверенные сочетания компонентов EDT 2026.1.2 и 2026.2.2;
  2026.1 использует Java 17, 2026.2.2 — Java 25. EDT 2025 не поддерживается.
- Исправлено обновление цвета вызовов BSL при Apply/restore без редактирования текста.
- Палитры распространены на 21 цвет QL/DCS: самостоятельные редакторы и
  текстовые контролы конструктора запроса. Исходные значения входят в restore.
- [Проверки и ограничения](docs/testing/edt-2026-compatibility-validation.md).

## Подготовка репозитория к публикации

- README с возможностями, 11 палитрами, установкой и ограничениями.
- Собственный код лицензирован под MIT; лицензии источников палитр сохранены.
- Сборки `dist/` исключены из текущего дерева Git; ZIP и SHA-256 предназначены для Releases.
- Добавлены документация разработчика, шаблоны Issue/PR и CI unit tests модели на Java 17.
- Monokai Pro (CE) отложена по решению пользователя; текущая очередь тем завершена.

## 0.1.15

Добавлена Visual Studio Dark (C/C++), вариант Dark из C/C++ Themes 2.1.0.
[Проверки](docs/testing/visual-studio-dark-validation.md).

## 0.1.14

Добавлена Noctis Minimus; адаптированы разделители и читаемость чисел.
[Проверки](docs/testing/noctis-minimus-validation.md).

## 0.1.13

Добавлена Shades of Purple. [Проверки](docs/testing/shades-of-purple-validation.md).

## 0.1.12

Добавлена Ayu Dark (Bordered). [Проверки](docs/testing/ayu-dark-validation.md).

## 0.1.11

Добавлена Night Owl. [Проверки](docs/testing/night-owl-validation.md).

## 0.1.10

Исправлено слияние разделителей рабочих окон Winter is Coming.
[Проверки](docs/testing/winter-separators-validation.md).

## 0.1.9

Добавлена Winter is Coming (Dark Blue).
[Проверки](docs/testing/winter-is-coming-validation.md).

## 0.1.8

Добавлена GitHub Dark Default. [Проверки](docs/testing/github-dark-validation.md).

## 0.1.7

Добавлена Dracula, отдельно от Darcula. [Проверки](docs/testing/dracula-validation.md).

## 0.1.6

Добавлены One Dark Pro и общий реестр профилей.
[Проверки](docs/testing/one-dark-pro-validation.md).

## 0.1.5

Разрешён запуск на macOS x64 с целевой EDT и Java 17.
Пользователь подтвердил работу; подробная Mac-матрица не закрыта.
[Проверки](docs/testing/macos-validation.md).

## 0.1.4

Редактор 17 ролей и отдельные личные варианты палитр, черновики,
сброс роли/палитры и Apply/OK/Cancel. [Проверки](docs/testing/palette-validation.md).

## 0.1.3

Исправлена читаемость выбранной строки списка палитр.
[Проверки](docs/testing/profile-list-validation.md).

## 0.1.2

Исправлены подсветка BSL preview и читаемость выбранного шрифта.
[Проверки](docs/testing/preview-validation.md).

## 0.1.1

Выбор семейства и размера шрифта кода с preview и отдельным восстановлением.
[Проверки](docs/testing/font-validation.md).

## 0.1.0

MVP: Darcula и Monokai поверх штатной Dark, подсветка BSL,
сохранение и восстановление исходных настроек, p2 ZIP.
[Проверки](docs/testing/mvp-validation.md).
