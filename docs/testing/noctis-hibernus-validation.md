# Noctis Hibernus — локальный кандидат 0.1.25

Дата: **04.11.2026**. По запросу продолжить очередь добавлена одна тема:
**Noctis Hibernus**, `noctis-hibernus`. Теперь доступны
семь светлых и одиннадцать тёмных палитр. Это официальный вариант Noctis
10.43.3 с холодным серым фоном редактора и голубоватыми панелями; [источники и адаптация 17 ролей](../research/2026-11-04-noctis-hibernus.md).

## Проверка

**109 unit tests**, 0 failures/errors, Maven 3.9.11 / Tycho 4.0.13 / Java 17.

| Windows x64 EDT | Axiom JVM | Candidate | CandidateRestart | CandidateSurfaces |
|---|---|---:|---:|---:|
| 2026.1.2+2 | 17.0.18 | 113 | 230 | 222 |
| 2026.1.3+25 | 17.0.18 | 113 | 230 | 222 |
| 2026.2.2+21 | 25.0.2 | 113 | 230 | 222 |

Всего **1695 runtime PASS, 0 FAIL**.
[Матрица и точные версии Eclipse, SWT, Xtext и EDT bundles](2026-11-04-noctis-hibernus/runtime-matrix.json).
Установленный JAR в каждой копии побайтово совпал с shipping ZIP.

Проверены выбор седьмой светлой темы, preview/Cancel/Apply, реальные RGB BSL
глифов, панели Navigator/Properties, фон контролов и пиксели левой линейки.
Переходы между светлыми/тёмными профилями сохраняют текст, modification stamp
и шрифт. Личный keyword применяется без редактирования текста и сохраняется
после новой JVM; сброс возвращает исходный цвет. Личные comment keys всех
17 прежних палитр сохраняются независимо. Проверены ручной фон редактора
после применения темы и точное восстановление затронутых preferences,
включая отсутствие явных значений. Журнал освобождается.

CandidateSurfaces проверяет новую палитру на обычном тексте, BSL, QL, QL DCS,
embedded QlEditor и BSL compare/merge с восстановлением исходных настроек.
Это проверка текстовых поверхностей, а не полного мастера запроса с metadata-проектом.

## Галерея и поставка

В отдельной native Light JVM выполнены **30 gallery PASS**, 0 FAIL.
Снят целый рабочий экран 884×581 со стандартной палитрой и восстановленным
шрифтом; проверены RGB глифов, панелей и линейки. PNG сохранён в README
без обработки; [пиксели и SHA-256](2026-11-04-noctis-hibernus/gallery-qa.json).

Shipping JAR: 43 classes major 61. Из 43 прежних классов изменён только
PaletteCatalog; остальные 42 побайтово совпали с 0.1.24. Новая схема использует
общий механизм светлых панелей и линейки 0.1.24 без изменений.
Прежние 17 палитр и ZIP 0.1.17–0.1.24 сохранены побайтово.
[Состав ZIP](2026-11-04-noctis-hibernus/package-verification.json),
[неизменность прежних файлов](2026-11-04-noctis-hibernus/unchanged-palettes-and-zips.json).
Test-only probe в поставку не включён. MIT Liviu Schera переиспользуется из
существующих Noctis Minimus/Lux, атрибуция дополнена. Локальный p2-сайт подготовлен, без публикации.

Bundle: `0.1.25.202611041604`.
ZIP SHA-256: `88f88d0ab6b3511adb452192a197b89038798e9146ada9670c5e39aac92d8d39`.
Поставка: `dist/edt-themes-0.1.25.zip`.

## Воспроизведение и границы

p2 Install → build-workbench-probe → Candidate → CandidateRestart →
CandidateSurfaces с `-ProfileId noctis-hibernus` и отдельным WorkRoot.
Пиксельные проверки выполняются последовательно. Для галереи —
CandidateGalleryPrepare → новая JVM → CandidateGallery.

Cleanup → Reconcile → p2 Uninstall → Audit завершены на всех трёх копиях.
Theme/probe bundles и файлы отсутствуют, базовые inventories 1267 / 1257 / 1234
сохранены. Четыре временных junction удалены с сохранением их целей.
[Результат очистки](2026-11-04-noctis-hibernus/cleanup-summary.txt).

macOS этой версией не проверена; новых сочетаний платформ не разрешено.
Полный metadata query wizard, отдельные debugger/breakpoint сценарии и
полная доступность интерфейса не проверялись. Native SWT scrollbars и
прежние ограничения BSL merge panes сохраняются; существующее окно сравнения
после смены темы может потребовать переоткрытия. Native сообщения
TabbedPropertyTitle/Right QL/wmic не объявлены исправленными.
Рабочие EDT и пользовательские workspace не менялись.
Commit/push/release не выполнялись; опубликованная версия остаётся 0.1.16.
