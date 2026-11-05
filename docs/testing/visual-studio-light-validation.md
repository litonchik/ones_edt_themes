# Visual Studio Light (C/C++) — локальный кандидат 0.1.24

Дата: **04.11.2026**. По запросу продолжить очередь добавлена одна тема:
**Visual Studio Light (C/C++)**, `visual-studio-light-cpp`. Теперь доступны
шесть светлых и одиннадцать тёмных палитр. Это основной Light из Microsoft
C/C++ Themes 2.1.0; [источники и адаптация 17 ролей](../research/2026-11-04-visual-studio-light.md).

## Проверка

**105 unit tests**, 0 failures/errors, Maven 3.9.11 / Tycho 4.0.13 / Java 17.

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2+2 | 17.0.18 | 110 | 229 | 222 |
| 2026.1.3+25 | 17.0.18 | 110 | 229 | 222 |
| 2026.2.2+21 | 25.0.2 | 110 | 229 | 222 |

Всего **1683 runtime PASS, 0 FAIL**.
[Матрица и точные версии Eclipse, SWT, Xtext и EDT bundles](2026-11-04-visual-studio-light/runtime-matrix.json).
Установленный JAR в каждой копии побайтово совпал с shipping ZIP.

Проверены выбор шестой светлой темы, preview/Cancel/Apply, реальные RGB BSL
глифов, панели Navigator/Properties, фон контролов и пиксели левой линейки.
Переходы между светлыми/тёмными профилями сохраняют текст, modification stamp
и шрифт. Личный keyword применяется без редактирования текста и сохраняется
после новой JVM; сброс возвращает исходный цвет. Личные comment keys всех
16 прежних палитр сохраняются независимо. Проверены ручной фон редактора
после применения темы и точное восстановление затронутых preferences,
включая отсутствие явных значений. Журнал освобождается.

CandidateSurfaces проверяет новую палитру на обычном тексте, BSL, QL, QL DCS,
embedded QlEditor и BSL compare/merge с восстановлением исходных настроек.
Это проверка текстовых поверхностей, а не полного мастера запроса с metadata-проектом.

## Галерея и поставка

В отдельной native Light JVM выполнены **30 gallery PASS**, 0 FAIL.
Снят целый рабочий экран 884×581 со стандартной палитрой и восстановленным
шрифтом; проверены RGB глифов, панелей и линейки. PNG сохранён в README
без обработки; [пиксели и SHA-256](2026-11-04-visual-studio-light/gallery-qa.json).

Shipping JAR: 43 classes major 61. Из 43 прежних классов изменён только
PaletteCatalog; остальные 42 побайтово совпали с 0.1.23. Новая схема использует
общий механизм светлых панелей и линейки 0.1.23 без изменений.
Прежние 16 палитр и ZIP 0.1.17–0.1.23 сохранены побайтово.
[Состав ZIP](2026-11-04-visual-studio-light/package-verification.json),
[неизменность прежних файлов](2026-11-04-visual-studio-light/unchanged-palettes-and-zips.json).
Test-only probe в поставку не включён. MIT Microsoft переиспользуется из
тёмной пары, атрибуция дополнена. Локальный p2-сайт подготовлен, без публикации.

Bundle: `0.1.24.202611041505`.
ZIP SHA-256: `1e411c537b4e603016735e2293ab1013b01af9c495a93b85c64061e087e22dc6`.
Поставка: `dist/edt-themes-0.1.24.zip`.

## Воспроизведение и границы

p2 Install → build-workbench-probe → Candidate → CandidateRestart →
CandidateSurfaces с `-ProfileId visual-studio-light-cpp` и отдельным WorkRoot.
Пиксельные проверки выполняются последовательно. Для галереи —
CandidateGalleryPrepare → новая JVM → CandidateGallery.

Cleanup → Reconcile → p2 Uninstall → Audit завершены на всех трёх копиях.
Theme/probe bundles и файлы отсутствуют, базовые inventories 1267 / 1257 / 1234
сохранены. Четыре временных junction удалены с сохранением их целей.
[Результат очистки](2026-11-04-visual-studio-light/cleanup-summary.txt).

macOS этой версией не проверена; новых сочетаний платформ не разрешено.
Полный metadata query wizard, отдельные debugger/breakpoint сценарии и
полная доступность интерфейса не проверялись. Native SWT scrollbars и
прежние ограничения BSL merge panes сохраняются; существующее окно сравнения
после смены темы может потребовать переоткрытия. Native сообщения
TabbedPropertyTitle/Right QL/wmic не объявлены исправленными.
Рабочие EDT и пользовательские workspace не менялись.
Commit/push/release не выполнялись; опубликованная версия остаётся 0.1.16.
