# One Dark Pro — 0.1.6

Дата: 14 октября 2026. За этот подход добавлена только одна новая схема:
`one-dark-pro`. Следующая по согласованной очереди — Dracula.

14.10.2026 пользователь подтвердил работу 0.1.6 сообщением «работает».
ОС и отдельные сценарии не сообщены; это общее подтверждение, не полная ручная матрица.

## Реализация и происхождение

17 ролей соответствуют [анализу, раздел 5.2](../research/2026-10-14-theme-catalog.md).
Источник: [One Dark Pro 3.20.3](https://github.com/Binaryify/OneDark-Pro/blob/ce79c5f7116607d635e06b7ef4413d85a77905e4/themes/OneDark-Pro.json),
commit `ce79c5f7116607d635e06b7ef4413d85a77905e4`, исследован 14.10.2026.
В JAR включены resources/third-party-notices.txt и полный MIT notice Binaryify.

Прозрачный selection #67769660 сведён поверх #282C34 в **#404859**.
Selection foreground **#D7DAE0** взят из list.activeSelectionForeground,
вычисленный контраст 6.55:1. Основной текст #ABB2BF на #282C34 — 6.57:1.
Исходный comment **#7F848E** сохранён (3.73:1); это осознанное сохранение
характера источника, не заявление доступности всех ролей. Его можно изменить
в редакторе палитры. Directive #E5C07B выбран из Classes; accent #61AFEF
совмещает UI и Builtin function BSL. Другие исходные оттенки functions/line numbers
невозможно независимо перенести в прежние 17 ролей.

PaletteCatalog содержит общий immutable реестр ID/имя/resource/source.
PaletteDrafts и список UI используют его; прежние IDs/личные ключи Darcula/Monokai
сохранены. Неизвестный persisted ID не активируется и не превращается в Darcula:
страница остаётся доступна для выбора/restore; записанные цвета, personal keys
и журнал автоматически не стираются. При startup выводится сообщение.
Журнал snapshot, selective explicit apply, rollback/recovery и шрифт не изменены.
Hover строк списков не добавлялся.

## Проверки

Unit RED: прежний код отклонил one-dark-pro в реальном settings plan/drafts,
а неизвестный saved ID сохранился активным (1 assertion failure, 2 ожидаемых
IllegalArgumentException; остальные 57 тестов прошли).
GREEN: **60 tests, 0 failures/errors/skipped**. Полный Verify — BUILD SUCCESS.
ZIP проверен: собственный bundle/feature, **31 classes major 61**, три палитры,
p2 metadata и license resources. Кандидат: `dist/edt-themes-0.1.6.zip`.

Target чтением установленной EDT и runtime guard: EDT 2026.1.3+25 Windows x64,
Eclipse 4.30.0, Xtext UI 2.33.0, BSL UI 23.0.1.v202609041212,
theming UI 2.1.0.v202609041212; Java 17 (Axiom JDK, Maven 3.9.11).
Тесты выполняются только в .work/edt-test и .work/workspace-probe.
p2 Install прошёл; базовые bundles сохранены. Первый sandbox-запуск p2
завершился отказом записи, проверка установки повторена с доступом к тестовой копии.

**OneDark: 204 PASS / 0 FAIL, exit 0. OneDarkRestart: 116 PASS / 0 FAIL, exit 0.**
Проверены фактические 56 targets палитры, точный Dark ID, real BSL background
и keyword glyph, preview, четвёртая строка списка (штатное оформление + 3 схемы),
читаемость выбранной строки, Apply/OK/Cancel, личные drafts всех трёх схем,
сброс роли/палитры, защита внешнего comment, BSL=false, сохранность независимого
font preference, отдельные personal keys вне originals. Второй JVM процесс
подтвердил ID/личный цвет/реальный BSL glyph и выбор третьей палитры на странице;
restore проверен по каждому original key. Затем personal keys и journal очищены
в тестовой workspace.

Журналы и снимки: [OneDark](0.1.6/runtime-onedark.txt),
[OneDarkRestart](0.1.6/runtime-onedarkrestart.txt),
[Verify](0.1.6/one-dark-verify.log), [p2 Install](0.1.6/one-dark-p2-install.log).
Визуально просмотрены собственные popup/таблица ролей, BSL и иллюстрация интерфейса;
приглушённые comment/error сохраняют исходный характер, полная ручная матрица
Windows/Mac не объявляется пройденной. Исходные Darcula/Monokai и прежние ZIP
сверены по **16 SHA-256** (2 ресурса и 7 ZIP/hash pairs); 17 RGB новой палитры
совпали с анализом. Полный MIT notice побайтно совпал с первичным источником.

Регрессия **Preview: 133 PASS / 0 FAIL, exit 0**: прежние Darcula/Monokai,
BSL preview, списки шрифтов и расширенный список тем, mouse/Enter/Escape,
отсутствие записи шрифта при просмотре. Это проверка затронутых старых UI-сценариев,
а не повтор всей исторической ручной матрицы.

P2 Reconcile и Uninstall завершились exit 0: тестовый fragment/bundle удалены,
исходный журнал и независимый шрифт проверены штатным p2-test скриптом.
После удаления **1267 базовых ID/версий** совпали с рабочей EDT.
Журнал [Uninstall](0.1.6/one-dark-p2-uninstall.log).

Снимки: [список тем](0.1.6/one-dark-pro-profile-list.png),
[роли](0.1.6/one-dark-pro-roles.png),
[иллюстрация интерфейса](0.1.6/one-dark-pro-interface-preview.png),
[личный BSL после restart](0.1.6/one-dark-pro-personal-bsl.png).

Bundle/feature: **0.1.6.202610141349**. ZIP **93 799 bytes**.
SHA-256: `c80f21add6a974d7ed4bb043cb2954b8952aa6fd876cc4646d0ebf7603ee95cd`.

## Ручная матрица новой схемы

| Экран/сценарий | Статус |
|---|---|
| Project Explorer и Properties с реальным проектом пользователя | Не проверено |
| Дополнительные окна, disabled controls, непроверенные нативные контроли | Не проверено |
| Completion/hover на реальном проекте | Не проверено |
| Реальные заголовки error/warning | Не проверено |
| Styled selection всех токенов BSL и разные DPI/масштабы | Не проверено |
| Содержимое форм/MXL/схем/СКД | Исключено; карта targets не менялась |
| Все новые сценарии на Mac x64 | Не проверено; подтверждение 0.1.5 отдельно |

Для установки/обновления: [инструкция](../user-guide.md).

P2 Audit завершился exit 0: собственные feature/bundle/fragment отсутствуют,
активный профиль и font journal не остались; базовые bundles сохранены.
