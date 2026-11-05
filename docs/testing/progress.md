# Журнал исполнения — docs/superpowers/plans/2026-10-13-edt-themes-mvp.md

05.11.2026 пользователь разрешил push и выпуск 0.1.28 с публикацией p2-сайта
через GitHub Pages. Это отменяет прежнее ожидание отмашки на публикацию.
Для релиза используется проверенный ZIP 0.1.28.202611051018 без пересборки;
публикация сайта выполняется существующим workflow из релизных вложений.
Сводка публикации и HTTPS-проверок — `docs/testing/release-0.1.28-validation.md`.

05.11.2026 пользователь подтвердил исправление 0.1.28 («теперь работает»).
При переходе со светлой на тёмную основу потребовался перезапуск EDT;
ОС/сборка отдельно не сообщены. В README и user-guide требование перезапуска
после применения темы при переходе Light ↔ Dark вынесено в явное предупреждение.
Это изменение документации: ZIP 0.1.28 сохранён, сборка/runtime повторно
не запускались. Commit/push/release не выполнялись.

05.11.2026 подготовлен локальный кандидат 0.1.28: исправлены активное,
неактивное и множественное выделение Tree/Table и поля Eclipse Forms
(реальные автор/коммиттер EGit Staging) для всех 19 тем. Публичные SWT API,
защита IEditorPart, освобождение обработчиков/изображений и точный restore.
116 unit tests, 8140 runtime PASS на трёх Windows EDT; проверены реальные
пиксели, disabled, restart, позднее дерево и регрессия редакторов/Git.
Из прежних classes изменён только LightUiStyles; 42 сохранены, добавлен
FieldBackground. 19 палитр и ZIP 0.1.17–0.1.27 сохранены побайтово.
Cleanup/Reconcile/Uninstall/Audit — PASS; базовые inventories и цели junction
сохранены. Commit/push/release не выполнялись. Отчёт —
`docs/testing/selection-validation.md`.

13–14 октября 2026. Самостоятельное исполнение, без Git и субагентов по указанию пользователя.

Pre-flight: Task 2 → 3: SettingKey/OriginalValue/SettingAccess совпадают; Task 3 → 4: apply/restore и снимок совместимы; Task 4 → 5: ID и BSL flag совпадают; Task 5 → 6: ZIP и проверка установки совпадают.

Task 1: complete. Directory target установленной EDT разрешён Tycho; Maven 3.9.11/Java 17/Tycho 4.0.13 — BUILD SUCCESS. Maven 3.10.0 получил зависимости, но несовместим; пользователь сам установил 3.9.11 и сообщил путь.

Task 2–5: код, обе палитры, 56 дополнительных native color settings, snapshot/journal, lifecycle, страница, feature/p2 ZIP реализованы. Сборка и состав ZIP подтверждены. Все собственные class-files — major 61. Первые native probes подтверждали preferences/registry, но только последующие проверки SWT обнаружили color cache; исправлен публичным непостоянным переходом между штатными темами.

Unit RED→GREEN: PaletteCatalog; SnapshotService/Codec; ProfileState/coverage; native side-effect compensation; first unchecked BSL; restore journal failure with all conflicts; manual color observed before foreign CSS overwrite. Добавлен ещё один RED-тест соответствия BSL background (штатный StyledText CSS использует controls background).

Task 6: runtime Apply и Restore завершились с exit 0. **879 PASS** применения, **100 PASS** восстановления, **0 FAIL**. Проверены десять смен Darcula/Monokai, RGB реального Shell и BSL StyledText, comments, RU/EN keywords, strings, numbers и #Область; новое окно и повторное открытие BSL. Проверены BSL checkbox, первичный custom color, safe/force restore, ручная правка после внешнего Light, сохранение выбора после перезапуска JVM, реальная кнопка Apply и Cancel до/после Apply. После нового процесса восстановлены все 96 сохранённых настроек, включая отсутствие явного значения и метаданные профиля.

Numeric glyph первоначально оставался серым при правильном preference. Добавлен public text-viewer invalidation после окончательной записи colors; окончательный probe проходит без вспомогательного invalidate в тесте. Ошибка поиска Apply была ошибкой probe: PreferencesUtil уже создаёт dialog, повторный create создавал второй Shell без controls страницы. Повторный create удалён, кнопка проверяется по JFaceResources.

Удаление feature через p2 прошло, но тестовый fragment был автоматически зарегистрирован EDT как locked dropins IU и удерживал host. Fragment перемещён внутри .work, штатный reconciler очистил тестовую зависимость; это не касается release ZIP, где fragment отсутствует. В тестовом p2 процессе встречался Java heap space; лимит director поднят с 512 до 1024 MiB, ошибки запуска не засчитывались как успешное удаление. Итоговый **Audit exit 0** в новом OSGi/p2 процессе подтвердил отсутствие bundle/feature и активного профиля; текущий slot журнала содержит count=0. **1267 базовых bundles** копии совпадают по ID/версии с исходной установленной EDT.

Ruling: CONTROLS_BACKGROUND_COLOR использует editor.background — native CSS окрашивает им BSL StyledText и перекрывает общий editor preference. Панели сохраняют отдельный panel цвет; cost if wrong: поля ввода будут темнее предлагаемого общего panel/control оттенка; для отдельного оттенка контролов потребуется узкий CSS редактора.

Самопроверка выполнена автором по Review Focus, без независимого reviewer по прямому запрету пользователя на субагентов. Выявленные важные ошибки закрываются RED→GREEN и native probe. Git не использовался; журнал сохраняется в проекте.

Сборка 13.10.2026 22:10 MSK: **33 unit tests, failures=0, errors=0, skipped=0**, Tycho **BUILD SUCCESS**. Bundle **0.1.0.202610131910**, 21 class-files major 61. Итоговый ZIP — **57 497 bytes**.
SHA-256: `3e23a245872c6f349a2ffbd7f9f856d0413e70e717de05004b730b1285670aca`.
Сумма ZIP совпала с .sha256 после runtime проверок; код release с этой сборки не менялся.

Логи: .work/final-verify.log, .work/runtime-apply.log, .work/runtime-restore.log, .work/install-final.log, .work/uninstall-final.log, .work/runtime-reconcile.log, .work/audit-final.log. Конкретные assertions сохранены также в docs/testing/runtime-apply.txt и runtime-restore.txt; снимки — docs/testing/screenshots.
Ручная матрица полного metadata project, preview/MXL/graph/DCS, высокого DPI и наполненных error/hint экранов передана в mvp-validation.md; эти экраны не объявлены визуально проверенными.

Итог: MVP 0.1.0 готов к установке по инструкции с указанными границами проверки. Рабочая EDT и пользовательские workspace не изменялись. Git и субагенты не использовались.

## Доработка 0.1.1 — 14 октября 2026

Пользователь подтвердил работу установленного MVP и согласовал краткий дизайн выбора шрифта:
общий native text font, семейство/размер/предпросмотр, Apply/OK/Cancel, отдельный возврат
исходного значения независимо от цветов. Реализован CodeFontSettings, отдельный двухслотовый
font journal, UI и обновление открытых редакторов через public FontRegistry. Старый цветовой
snapshot format сохранён. Git и субагенты не использовались.

RED: unit tests остановились на отсутствующем explicit apply API; runtime на старой 0.1.0
подтвердил отсутствие font size selector. GREEN: **35 unit tests**, **39 FontApply PASS**,
**9 FontRestore PASS** в новом JVM, **878 color Apply PASS**, **100 color Restore PASS**,
**0 FAIL**. Проверены реальный BSL StyledText, новый editor, перезапуск, наличие/отсутствие
исходного font override, explicit native edit, pending reset, настоящий OK и preview.
Самопроверка кода и визуальный просмотр страницы выполнены автором; independent reviewer
не запускался по указанию пользователя. P2 Install/Uninstall/Audit exit 0.

Финальный ZIP: `dist/edt-themes-0.1.1.zip`, bundle **0.1.1.202610140722**,
**64 786 bytes**, 23 classes major 61. SHA-256:
`cfd3a2954778179a1318040e0f6aeeda89d63391fb32d923e35ac7f01c129785`.
Подробности и raw assertions: `docs/testing/font-validation.md`, `docs/testing/0.1.1`.
Архив 0.1.0 сохранён. Рабочая EDT не изменена; ручные ограничения MVP остаются явно указанными.

## Исправления 0.1.2 — 14 октября 2026

По пользовательским скриншотам исправлены неокрашенный preview и нечитаемое имя шрифта
на выбранной строке нативного dropdown. Фиксированный CodePreview имеет UTF-16 spans,
локальные цвета и pending переключение палитры. FontFamilyPicker использует popup Table
с явной отрисовкой foreground/background выбранной строки через public SWT API.
Работа выполнялась самостоятельно, без Git/субагентов; рабочая EDT не менялась.

RED: отсутствие CodePreview в unit tests и отсутствие Monokai keyword StyleRange на 0.1.1.
Проба CCombo не прошла проверку контраста. GREEN: **38 unit tests**, **109 Preview PASS**,
**39 FontApply PASS**, **9 FontRestore PASS**, без failures. Pixel contrast выбранного
шрифта: **8.64:1 Monokai**, **9.67:1 Darcula**; проверены мышь/Enter/Escape и отсутствие
preference writes до Apply. Регрессия сохранения/возврата шрифта прошла в новом JVM.
Визуально просмотрены code preview и popup. Контроль снимков ведётся только в собственных
SWT окнах/контролах; рабочий стол не включён в опубликованные снимки.

Финальная сборка **0.1.2.202610140828**, 26 classes major 61,
`dist/edt-themes-0.1.2.zip` — **73 522 bytes**. SHA-256:
`1b04ece6c45577fb69943caac4ed346e10a4923dc09502f9ffd320f4bfeb5dba`.
P2 Install/Uninstall/Audit exit 0, базовая платформа сохранена. Старые ZIP сохранены.
Подробности: `docs/testing/preview-validation.md`; ограничения MVP остаются.

## Список палитр 0.1.3 — 14 октября 2026

Пользователь воспроизвёл тот же дефект selected row в нативном Combo выбора палитры.
Обобщён FontFamilyPicker в ReadableCombo; оба списка используют явные цвета текста
и фона выбранной строки. Обработка Apply/OK/Cancel и журналы preferences прежние.

Verify: **38 unit tests, BUILD SUCCESS**. Runtime: **133 Preview**, **878 Apply**,
**100 Restore**, **39 FontApply**, **9 FontRestore PASS**, **0 FAIL**.
Список всех трёх палитр проверен мышью и Enter/Escape; выбранная Darcula визуально
читается в обеих палитрах. Pixel contrast: Monokai **8.64:1**, Darcula **9.67:1**.

Bundle **0.1.3.202610140903**, 26 classes major 61, ZIP **73 607 bytes**.
SHA-256: `7123819ab9517511646cc0ea9786af188df13e9ba0dac5b5db58494da8486fbe`.
Работа самостоятельная, без Git/субагентов, рабочая EDT не менялась.
Подробности: `docs/testing/profile-list-validation.md`; raw assertions и popup PNG:
`docs/testing/0.1.3`. Старые ZIP сохранены, ограничения MVP остаются.
P2 Install/Uninstall/Audit exit 0; тестовые feature/bundle/fragment удалены,
журналы пусты. Все 1267 базовых ID/версий bundles совпадают с рабочей EDT.

14 октября 2026 пользователь подтвердил работу 0.1.3. Отсутствие подсветки строки
при наведении в списках тем и шрифтов признано некритичной доработкой и отложено.
Она добавлена последним пунктом ручной матрицы `docs/testing/mvp-validation.md`
с самым низким приоритетом; реализация сейчас не выполняется.

Пользователь разрешил реализацию редактора 17 ролей палитры в новом чате.
Подготовлены анализ `docs/research/2026-10-14-palette-editor.md`, актуальный
handoff `docs/handoff/2026-10-14-palette-editor.md` и стартовый промпт рядом с ним.
На момент handoff код редактора палитры ещё не был написан;
проверки 0.1.3 не являлись проверками новой функции.

## Редактор палитры 0.1.4 — 14 октября 2026

Согласованный объём выполнен самостоятельно, без Git и субагентов.
Добавлены 17 ролей со swatch/HEX, нативный ColorSelector, пояснения и tooltip
реальных targets, отдельные Darcula/Monokai drafts/overrides, локальные образцы
интерфейса/BSL, Apply/OK/Cancel, reset роли/стиля. Snapshot originals, absent/explicit,
защита ручных цветов, BSL checkbox и отдельный шрифт сохранены.
Личные ключи пишутся в общей транзакции EDT, остаются вне original snapshot и
сохраняются после restore; recovery/rollback и selective explicit покрыты тестами.

RED: отсутствующие API и редактор в 0.1.3; реальные failures для personal-conflict
recovery, native overwrite surrendered color, pending-save, oversized HEX и
полос под текстом таблицы. GREEN: **51 unit test**, Verify **BUILD SUCCESS**;
**26 Layout + 252 Palette + 14 PaletteRestart + 133 Preview + 878 Apply +
100 Restore + 39 FontApply + 9 FontRestore = 1451 PASS, 0 FAIL**.
В новом JVM проверен личный RGB реального BSL keyword glyph; все 17 пояснений
помещаются при текущем DPI. Снимки ограничены собственными видимыми контролами.
Самостоятельное ревью и визуальный просмотр выполнены автором.

Bundle/feature **0.1.4.202610141024**, 29 classes major 61.
ZIP `dist/edt-themes-0.1.4.zip` — **89 223 bytes**.
SHA-256: `43ef21a82b6edd02f9b13131c56b6fba354e464bf71f11cd3754512d69be010d`.
Прежние четыре ZIP сверены со своими SHA-256 и сохранены.
P2 Install exit 0, 1267 базовых ID/версий совпали с установленной EDT.
Reconcile, P2 Uninstall и Audit завершены, exit 0. Собственные feature/bundle/probe
удалены из конфигурации копии; activeProfile отсутствует, текущие журналы цветов
и шрифта пусты, тестовые personal overrides удалены. Базовые 1267 ID/версий
повторно совпали с установленной EDT после удаления.
Рабочая EDT и глобальная среда не менялись, ПО не устанавливалось.

Инструкция — `docs/user-guide.md`; архитектура — `docs/reference/palette-editor.md`;
проверки, raw assertions и снимки — `docs/testing/palette-validation.md`,
`docs/testing/0.1.4`. Нативный ColorDialog мышью, повышенный DPI и прикладные
формы/MXL/схемы/СКД остаются ручной матрицей. Hover строк — последний приоритет,
реализация не выполнялась.

14 октября 2026 пользователь подтвердил работу версии 0.1.4 сообщением
«работает». Это подтверждение использования пользователем; отдельные сценарии
ручной матрицы (DPI, ColorDialog, формы/MXL/схемы/СКД) не перечислялись и
автоматически не переводятся в проверенные.

## Mac runtime policy 0.1.5 — 14 октября 2026

Пользователь разрешил минимальную поддержку Mac с сохранением палитры 0.1.4.
Добавлена RuntimePlatform policy, разрешающая win32/x86_64 и macosx/x86_64 с Java 17;
EdtCompatibility сохранил прежние bundle checks. Для x86 EDT/JVM под Rosetta
используется та же политика. Native aarch64 и Linux не разрешены.

RED на прежней Windows-only политике: Mac combination rejected. GREEN:
**56 unit tests**, Verify **BUILD SUCCESS**, **252 Palette + 14 PaletteRestart PASS**,
**0 FAIL**. Реальный BSL glyph после restart использует личный keyword RGB.
Из существующих src/resources менялся только EdtCompatibility.java; SHA-256
остальных файлов сохранены. В JAR 34 старые записи побайтно совпадают с 0.1.4;
новая только RuntimePlatform.class, плюс изменённые guard/release metadata.

Bundle **0.1.5.202610141059**, 30 classes major 61, ZIP **89 961 bytes**.
SHA-256: `761f2208ac009b6582385ecb88c26b8d46b4458dbe40ef34a98e941af352116c`.
Архив 0.1.4 и его hash сохранены. Документация и Mac-матрица:
`docs/testing/macos-validation.md`; исходники палитры/hover не менялись.
Mac target/distribution не требовался для этой сборки на Windows. Фактический
запуск и визуальная приёмка на macOS не выполнялись — необходима проверка коллегой.
Windows P2 Install/Reconcile/Uninstall/Audit завершены exit 0; тестовые собственные
bundle/feature/fragment отсутствуют, журналы и personal overrides очищены.
1267 базовых ID/версий совпали с рабочей EDT. Все старые ZIP сверены со своими hashes.

14 октября 2026 пользователь подтвердил работу **0.1.5 на Mac** сообщением
«проверено, работает». Общая работа на Mac отмечена как подтверждённая пользователем.
Конфигурация Mac и конкретные сценарии не перечислены; остальные пункты Mac-матрицы
не переведены автоматически в PASS. Код и ZIP при фиксации подтверждения не менялись.

## 14 октября 2026 — анализ расширения каталога тем

По запросу пользователя подготовлен
[опорный каталог](../research/2026-10-14-theme-catalog.md): десять семейств
из статьи HTML Academy, конкретные тёмные кандидаты, по 17 ролей для каждого,
первичные источники с commit/версиями/лицензиями, RGB после сведения alpha,
компромиссы BSL/UI, контраст и предложенная очередь последовательной реализации.
GitHub и Night Owl выбраны в тёмных вариантах; Noctis — Minimus из ссылки статьи.
Для Monokai Pro предложена официальная MIT Community Edition для Zed;
proprietary-расширение VS Code не используется как источник поставки.

Проверка документа exit 0: **10 кандидатов / 170 строк ролей**,
**18 независимых alpha-пересчётов**, **5 локальных ссылок**, **35 source hashes**.
Основной и обычный выбранный текст рассчитаны ≥ 4.5:1; низкий контраст ряда
кодовых ролей отмечен явно и требует решения при реализации.
SHA-256 **182 файлов** исходников/тестов/сборки/поставок не изменились.
Добавлена ссылка в README. Новые палитры не реализованы, build/runtime и Git
не запускались; рабочая EDT и её настройки не менялись. Выбор вариантов,
CE и политики контраста остаётся предложением для следующего этапа.

## 14 октября 2026 — One Dark Pro, 0.1.6

Пользователь согласовал очередь и одну новую схему за подход. Инициализирован
локальный Git; исходное состояние MVP 0.1.5/анализ/план зафиксировано в `c08195c`.
`.work`, `.cache`, target и временный build log исключены; release ZIP/evidence
сохраняются в репозитории. Работа выполнена самостоятельно без субагентов.

Добавлена только **One Dark Pro** с 17 RGB из анализа. PaletteCatalog — общий
реестр ID/имя/resource/source для loader, drafts и UI. Убраны ternary/index
привязки UI к двум темам; неизвестный saved profile безопасно не активируется.
Прежние personal IDs, snapshot, selective explicit apply и шрифт сохранены.
В поставке присутствуют attribution и полный MIT notice Binaryify.

RED: прежние loader/drafts отклонили third ID, unknown saved ID остался активным.
GREEN: **60 unit tests, 0 failures/errors/skipped**. Verify **BUILD SUCCESS**;
31 Java classes major 61; собственный bundle/feature, 3 палитры и notices в p2.
**204 OneDark + 116 OneDarkRestart + 133 Preview PASS**, **0 FAIL**, exit 0:
третья схема на странице/в popup, фактические EDT settings и BSL glyph,
Apply/OK/Cancel, личные карты всех трёх тем, resets, manual-color protection,
BSL=false, font independence, restart/restore. Тестовые personal keys/journal
очищены. Все сценарии выполнялись в .work/edt-test/workspace-probe.

Bundle **0.1.6.202610141349**, ZIP **93 799 bytes**.
SHA-256 `c80f21add6a974d7ed4bb043cb2954b8952aa6fd876cc4646d0ebf7603ee95cd`.
Сохранность двух прежних ресурсов и 7 ZIP/hash pairs подтверждена 16 hashes.
Полный notice совпал с upstream; все 17 новых цветов совпали с анализом.
Низкий исходный contrast comment сохранён и отражён в документации.
Ручные Windows/Mac-сценарии указаны в `one-dark-pro-validation.md`;
подтверждение Mac 0.1.5 на эту палитру не переносилось.
Следующая схема по очереди — Dracula, в этом подходе не реализована.

P2 Audit завершился exit 0: собственные feature/bundle/fragment отсутствуют,
активный профиль и font journal не остались; базовые bundles сохранены.

## 14 октября 2026 — Dracula, 0.1.7

Пользователь подтвердил работу One Dark Pro в 0.1.6 («работает») и попросил
следующую схему. Добавлена только базовая **Dracula**, ID `dracula`,
отдельно от Darcula: 17 RGB из анализа, запись в реестре и MIT notice.
В ходе работы пользователь отключил Superpowers для этого проекта;
правило закреплено в AGENTS.md, с этого момента skills не используются.

62 unit tests проходят без failures/errors/skipped. Verify — BUILD SUCCESS;
собственные bundle/feature **0.1.7.202610141444**, 31 classes major 61,
четыре ресурса палитр и две лицензии в ZIP **95 003 bytes**.
SHA-256: `c428d96fa46f8698d2d9f3aa05a8beff7752a1f74a68918d81653112a529f0ac`.
Первичные Dracula RGB и полный license notice сверены с закреплённым источником.
17 прежних файлов палитр/поставок совпали с SHA-256 до изменений.

**Dracula 207 PASS / DraculaRestart 120 PASS**, 0 FAIL, оба exit 0.
Реальные keyword/number/string glyph и фон BSL, selector/preview/popup,
Apply/OK/Cancel, resets, ручные цвета, BSL=false, font independence,
сохранённый ID после restart, restore всех original keys и очистка журнала.
Личные цвета Darcula/Monokai/One Dark Pro при работе с Dracula сохранены.
Снимки и оставшаяся ручная матрица — `dracula-validation.md`/`0.1.7`.
macOS новой схемы не проверена; общая работа 0.1.6 не выдана за Mac PASS.
Следующая схема — GitHub Dark Default; в этот подход она не входит.

Регрессия OneDark/Restart: **206 + 119 PASS**, 0 FAIL, exit 0.
Итого **652 runtime assertions** для новой схемы и прежней One Dark Pro;
журнал и personal keys тестовой workspace очищены после последнего restart.

P2 Install/Reconcile/Uninstall/Audit — exit 0. Собственные тестовые
feature/bundle/fragment удалены; 1267 базовых ID/версий совпали с рабочей EDT.
Исходная EDT, старые ZIP и три прежние палитры сохранены.

## 14 октября 2026 — GitHub Dark Default, 0.1.8

Пользователь подтвердил работу 0.1.7 («работает») и попросил следующую схему.
Добавлена только **GitHub Dark Default**, ID `github-dark-default`, 17 RGB из
анализа. Источник GitHub Theme 6.3.5 / Primer primitives 7.10.0; оба полных
MIT notices и attribution включены в bundle. Superpowers не используются.

**64 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.8.202610141519**, 31 classes major 61, пять палитр,
четыре MIT notices. ZIP **97 043 bytes**; SHA-256:
`7a384bc94d5a1346454428dc3e14c3ff1985b362453d1d386601c4dc9eb6c4cb`.
Из product Java менялась только запись PaletteCatalog. Feature label сокращён
до «Темы EDT — цветовые палитры»; IDs/настройки/журналы/шрифт сохранены.

**GitHub 208 + GitHubRestart 122 PASS**, 0 FAIL, exit 0. Проверены фактические
EDT settings и BSL glyph keyword/number/string, selector/preview/popup,
Apply/OK/Cancel, resets, внешние ручные цвета, BSL=false, независимый шрифт,
личные цвета прежних схем, saved ID и restore после restart.
Тестовые personal keys всех пяти схем и journal очищены.
17 RGB и два новых license notices совпали с источниками; 20 прежних файлов
палитр/ZIP/hash сохранены. Проверки и ручная матрица — `github-dark-validation.md`.
Новая схема на Mac не проверена; общее подтверждение 0.1.7 не выдано за Mac PASS.
Следующая схема — Winter is Coming (Dark Blue), в этот подход не входит.

Дополнительная регрессия Dracula — **207 PASS**, 0 FAIL, exit 0.
Пользователь спросил о её необходимости; полный цикл старой схемы признан
избыточным для изменения только палитры. DraculaRestart не запускался.
Тестовая среда восстановлена отдельной фазой **Cleanup: 104 PASS**, exit 0.
Правило соразмерных проверок закреплено в AGENTS.md; в дальнейшем старые схемы
повторно проверяются полностью только при изменении общего поведения/явном риске.

P2 Install/Reconcile/Uninstall/Audit — exit 0. Тестовые feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог для новой схемы: 64 unit tests и 330 GitHub runtime
assertions проходят; дополнительные 207 Dracula и 104 Cleanup записаны отдельно.

## 18 октября 2026 — Winter is Coming (Dark Blue), 0.1.9

Пользователь подтвердил работу 0.1.8 («проверил, работает»), без сведений об ОС
и отдельных сценариях. Добавлена только **Winter is Coming (Dark Blue)**,
ID `winter-is-coming-dark-blue`, по ранее согласованной очереди. Следующая —
Night Owl. Superpowers и субагенты не используются.

Все 17 RGB совпали с закреплённым анализом Winter is Coming 1.5.0,
commit `260547834cb6ac37dd5b8bb5842cc1c8d3164946`; line #042444
пересчитан из alpha #44 поверх #011627. Полный MIT notice включён в JAR
и побайтно совпал с источником. **23 прежних файла** палитр/ZIP/hash сохранены.
Product Java изменён только записью PaletteCatalog; общий UI/применение/restore
и шрифт сохранены.

**66 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.9.202610181244**, 31 classes major 61, шесть палитр,
пять MIT notices. ZIP **98 401 bytes**; SHA-256:
`d21bec61382b56d725eef550df0c1b79d92f989b7096caa8151ede405940778c`.

**Winter 209 + WinterRestart 124 PASS**, 0 FAIL, оба exit 0:
фактические targets, реальные BSL glyph, selector/preview/popup,
Apply/OK/Cancel, resets, ручные внешние цвета, BSL=false, независимый шрифт,
сохранность personal keys всех пяти прежних схем, saved ID и restore после
restart. Просмотрены popup и BSL; полное название и текст читаются.
Тестовые personal keys всех шести схем и journal очищены во втором процессе;
отдельная Cleanup не требовалась. Полные циклы прежних тем не повторялись.
Проверки/снимки/ручная матрица — `winter-is-coming-validation.md` и `0.1.9`.
Новая палитра на Mac отдельно не проверена.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены из тестовой EDT. Active profile и font journal не остались.
1267 базовых ID/версий совпали с рабочей EDT. Итог: **66 unit tests и 333 runtime
assertions**, без полных повторных циклов прежних тем.

## 18 октября 2026 — разделители Winter, 0.1.10

Пользователь показал слияние разделителей окон в Winter. CSS целевой EDT
`.MPartSashContainer` использует INACTIVE_PART_BACKGROUND_COLOR, который
связан с ролью panel. В 0.1.9 она совпадала с editor.background #011627.
Изменена только panel Winter на **#0E293F**, исходный оттенок
list.inactiveSelectionBackground. Остальные 16 ролей, пять прежних схем,
общий product Java, targets и font-код сохранены. Новых схем нет; далее Night Owl.

**67 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.10.202610181304**, 31 classes major 61; ZIP **98 550 bytes**.
SHA-256: `693fffb149aae4072c7f02e11951e6ea74cee326902bc3d3c0c6de601874ada4`.
25 прежних файлов палитр/поставок совпали по SHA-256.

Стенд уточнён для настоящих окон: действующий Project Explorer вместо удалённого
ResourceNavigator, отдельная perspective с Properties, публичная модель
MPartSashContainer и пиксели видимых промежутков между controls.
Первоначальные попытки стенда остановились до проверки исправления из-за
отсутствующих/неверно определяемых разделителей; итоговые запуски успешны.
**WinterSeparators 76 + Restart 122 PASS**, 0 FAIL, оба exit 0.
Воспроизведён старый panel, проверен обычный Apply новых defaults, две
вертикальные и одна горизонтальная полосы изменились с #011627 на #0E293F.
Снимки до/после просмотрены. Остальные targets, BSL glyph и font сохранены.
В новом процессе сохранены цвет полос, personal Winter keyword и GitHub comment;
restore проверен для каждого original key. Test personal keys, active profile
и journal очищены; отдельная Cleanup не требовалась.
Проверки и ограничения — `winter-separators-validation.md` и `0.1.10`.
macOS/другие DPI/все специальные вложенные редакторы не проверены.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены из тестовой EDT, active profile и font journal не остались.
1267 базовых ID/версий совпали с рабочей EDT. Для применения новых defaults
к уже выбранной Winter после обновления требуется обычный Apply; личный panel
по запросу сбрасывается отдельно. Итог: **67 unit tests и 198 runtime assertions**.

## 18 октября 2026 — Night Owl, 0.1.11

Пользователь подтвердил исправление Winter 0.1.10 («теперь отлично»), без
сведений об ОС/сценариях, и попросил продолжить. Добавлена только **Night Owl**,
ID `night-owl`; следующая схема — Ayu Dark (Bordered). Superpowers и субагенты
не используются. Product Java изменён только записью PaletteCatalog.

Источник Night Owl 2.1.1, commit `cc291eba7976b20d7c66bde6883c27b902196b07`.
16 ролей сохранены из анализа; panel сразу адаптирована в **#0E293F** из
list.inactiveSelectionBackground для различимых рабочих разделителей.
Сохранён исходный comment #637777 (contrast 3.87:1); это ограничение контраста
зафиксировано явно. Alpha line/hover пересчитаны. Полный MIT notice Copyright
2018 Sarah Drasner совпал с источником; 28 прежних файлов палитр/поставок
сохранили SHA-256.

**69 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.11.202610181327**, 31 classes major 61, семь палитр,
шесть MIT notices. ZIP **99 804 bytes**; SHA-256:
`de8b5c1ae6b85b20c4d6a7e62a3d7dcd437bbc2a4c95260f4892c192a2e982c6`.

**NightOwl 219 + NightOwlRestart 133 PASS**, 0 FAIL, оба exit 0:
реальные targets и BSL glyph, preview/popup, Apply/OK/Cancel, resets,
внешние ручные цвета, BSL=false, независимость шрифта, сохранность personal
keys шести прежних схем, saved ID, restore всех original keys и очистка.
Пиксели двух вертикальных и одной горизонтальной полосы #0E293F проверены
в обоих процессах; рабочие окна и popup просмотрены. Полные циклы прежних
схем не повторялись; дополнительная Cleanup не требовалась.

В CSS Properties зарегистрирована повторяющаяся диагностика TabbedPropertyTitle
с null color (184/36 сообщений); она была и в 0.1.10 (108 сообщений),
отдельно от целевых assertions. Сводка/первый stack сохранены; полная матрица
Properties с рабочим metadata-проектом остаётся открытой. После первого Apply
некоторые native controls были светлыми; после restart снимок показывает Dark.
macOS новой схемы не проверена. Проверки и ограничения — `night-owl-validation.md`.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог: **69 unit tests и 352 целевых runtime assertions**;
известная диагностика CSS Properties и ручная матрица отмечены отдельно.

## 18 октября 2026 — Ayu Dark (Bordered), 0.1.12

По просьбе пользователя продолжена очередь после Night Owl; добавлена только
**Ayu Dark (Bordered)**, ID `ayu-dark`. Следующая — Shades of Purple; остаются
четыре кандидата. Superpowers и субагенты не используются.

Источник Ayu 1.4.1, commit `2c6feb7215bf7ca6c8c9a51066f255025df3a5b9`.
В upstream package.json файл ayu-dark.json имеет id Ayu Dark Bordered,
отдельно от Unbordered. 15 ролей сохранены из анализа; panel адаптирована
в **#1B1F29** (editorGroup.border) для различимых рабочих полос, hover
#47526640 пересчитан поверх неё в **#262C38**. Selection alpha проверен отдельно.
Исходные comment/disabled #5A6673 (contrast 3.15:1) сохранены как явно
описанное ограничение. Полный MIT notice Copyright 2016 Ike Kurghinyan
совпал с источником; **31 прежний файл** палитр/поставок сохранил SHA-256.
Product Java изменён только записью PaletteCatalog.

**71 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.12.202610181345**, 31 classes major 61, восемь палитр,
семь MIT notices. ZIP **101 063 bytes**; SHA-256:
`9c2a06e518fd2afa04f0d4b00f8421799dda5842365da9746b53564c6afc024f`.

Первый runtime-запуск остановился на захвате popup через GC(Display):
прочитан посторонний цвет. Тест переведён на GC самого Table, popup сохраняется
до assertion; порог контраста не ослаблен. Повторный снимок подтвердил фон
#193155, contrast 6.93:1 и читаемое полное название. Product-код списка сохранён.
Первоначальный отчёт сохранён отдельно.

**Ayu 221 + AyuRestart 135 PASS**, 0 FAIL, оба exit 0: native targets,
реальные BSL glyph, preview/popup, Apply/OK/Cancel, resets, ручные внешние
цвета, BSL=false, шрифт, независимость personal keys семи прежних схем,
saved ID, restore всех original keys и очистка. Проверены пиксели двух
вертикальных и одной горизонтальной полосы #1B1F29 в обоих процессах;
снимки рабочих окон/popup просмотрены. Полные циклы прежних тем не повторялись;
тестовые keys всех восьми схем, active profile и journal очищены во втором
процессе. Дополнительная Cleanup не потребовалась.

Известная диагностика CSS Properties/TabbedPropertyTitle с null color
сохраняется (208/36 сообщений); сводка/stack и ручная матрица отделены
от целевых assertions. После первого Apply отдельные native controls светлые,
после restart — тёмные. macOS новой схемы отдельно не проверена.
Проверки и ограничения — `ayu-dark-validation.md` и `0.1.12`.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог: **71 unit tests и 356 целевых runtime assertions**;
первоначальный неудачный захват popup и CSS-диагностика описаны отдельно.

## 18 октября 2026 — Shades of Purple, 0.1.13

Пользователь подтвердил работу Ayu 0.1.12 («работает»), без сведений об ОС
и отдельных сценариях, и попросил следующую схему. Добавлена только базовая
**Shades of Purple**, ID `shades-of-purple`, не Super Dark. Следующая —
Noctis Minimus; остаются три кандидата. Superpowers и субагенты не используются.

Источник 7.3.6, commit `e8eb49f33e5db05ceba6677367b33ddb27ad821c`.
Все 17 ролей совпали с анализом; alpha selection/hover/error пересчитаны.
Panel #222244 из sideBar отличается от editor #2D2B55; дополнительная
адаптация для рабочих полос не потребовалась. Исходные comment/accent #B362FF
(contrast/editor 3.85:1) сохранены как явно описанное ограничение.
Полный MIT notice Copyright 2015-∞ Ahmad Awais совпал с источником.
**34 прежних файла** палитр/поставок сохранили SHA-256.
Product Java изменён только записью PaletteCatalog.

**73 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.13.202610181757**, 31 classes major 61, девять палитр,
восемь MIT notices. ZIP **102 402 bytes**; SHA-256:
`29e546ddbe240d90bf05e49a4bff42d88a6a8645ffef6931faa8b2a4d1929e98`.

**Shades 221 + ShadesRestart 137 PASS**, 0 FAIL, оба exit 0: native targets,
реальные BSL glyph, preview/popup, Apply/OK/Cancel, resets, ручные внешние
цвета, BSL=false, независимый шрифт, personal keys восьми прежних схем,
saved ID, restore всех original keys и очистка. Пиксели двух вертикальных
и одной горизонтальной полосы #222244 проверены в обоих процессах;
popup имеет выбранный фон #7448B0, contrast 6.37:1. Снимки рабочих окон
и списка просмотрены. Полные циклы прежних тем не повторялись; keys всех
девяти схем, active profile и journal очищены во втором процессе.
Дополнительная Cleanup не потребовалась.

Известная CSS-диагностика Properties/TabbedPropertyTitle с null color
сохраняется (184/36 сообщений); сводка/stack и ручная матрица отделены
от целевых assertions. После первого Apply некоторые native controls светлые,
после restart — тёмные. macOS новой схемы отдельно не проверена.
Проверки и ограничения — `shades-of-purple-validation.md` и `0.1.13`.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог: **73 unit tests и 358 целевых runtime assertions**;
известная CSS-диагностика и оставшаяся ручная матрица отмечены отдельно.

## 18 октября 2026 — Noctis Minimus, 0.1.14

Пользователь подтвердил работу Shades 0.1.13 («работает»), без сведений об ОС
и сценариях, и попросил продолжить. Добавлена только **Noctis Minimus**,
ID `noctis-minimus`, отдельно от базового Noctis. Следующая — Visual Studio
Dark (C/C++); остаются два кандидата. Superpowers и субагенты не используются.

Источник Noctis 10.43.3, commit `4a82370b2c064e36726c23e94f27ed3782bd6ecd`.
15 ролей сохранены из анализа. Для различимых рабочих полос panel взята из
editorGroup.border **#101213**, contrast/editor 1.261:1 вместо 1.075:1.
Отмеченный в анализе риск тёмных чисел решён осветлением #7068B1 в **#8E87C5**:
контраст вырос с **3.0528:1 до 4.5551:1**, порог 4.5 проверен. Фиолетовый
оттенок сохранён; исходное значение остаётся в исторической таблице анализа.
Selection/line alpha пересчитаны, muted comment #5E7887 (3.20:1) сохранён
как явно описанное ограничение. Полный MIT notice Copyright 2018 Liviu Schera
совпал с источником; **37 прежних файлов** палитр/поставок сохранили SHA-256.
Product Java изменён только записью PaletteCatalog.

**75 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.14.202610181841**, 31 classes major 61, десять палитр,
девять MIT notices. ZIP **103 717 bytes**; SHA-256:
`58018a0d0e1a208e1c32b4acce9a283d7a196b0f3e303c23f4612e762d80da72`.

**Noctis 222 + NoctisRestart 139 PASS**, 0 FAIL, оба exit 0: native targets,
реальные BSL glyph (включая осветлённый number), preview/popup, Apply/OK/Cancel,
resets, ручные внешние цвета, BSL=false, независимый шрифт, personal keys
девяти прежних схем, saved ID, restore всех original keys и очистка.
Пиксели двух вертикальных и одной горизонтальной полосы #101213 проверены
в обоих процессах; popup имеет фон #2A404D, contrast 9.94:1. Снимки рабочих
окон и списка просмотрены. Полные циклы прежних тем не повторялись; keys
всех десяти схем, active profile и journal очищены во втором процессе.
Дополнительная Cleanup не потребовалась.

Известная CSS-диагностика Properties/TabbedPropertyTitle с null color
сохраняется (184/36 сообщений); сводка/stack и ручная матрица отделены
от целевых assertions. После первого Apply некоторые native controls светлые,
после restart — тёмные. macOS новой схемы отдельно не проверена.
Проверки и ограничения — `noctis-minimus-validation.md` и `0.1.14`.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог: **75 unit tests и 361 целевая runtime assertion**;
известная CSS-диагностика и оставшаяся ручная матрица отмечены отдельно.

## 18 октября 2026 — Visual Studio Dark (C/C++), 0.1.15

Пользователь подтвердил работу Noctis 0.1.14 («работает»), без сведений об ОС
и сценариях. Добавлена только **Visual Studio Dark (C/C++)**,
ID `visual-studio-dark-cpp`. Остался один кандидат — Monokai Pro (CE),
выбор открытого источника CE требует подтверждения.
Superpowers и субагенты не используются.

Источник C/C++ Themes 2.1.0, commit
`eb981644369c99d39054578a769af572c24bd1eb`, вариант Dark из
Themes/themes/cpptools_dark_vs_new.json (не 2017). Все 17 ролей совпали
с анализом, включая адаптации panel/menu, inactive selection/line и warning
из тёплого string. Общие категории перенесены на BSL; C++ semantic scopes
не включены. Полный MIT notice Microsoft из Themes/LICENSE.txt побайтно
совпал с источником. **40 прежних файлов** палитр/поставок сохранили SHA-256.
Product Java изменён только записью PaletteCatalog.

**77 unit tests**, 0 failures/errors/skipped; Verify BUILD SUCCESS.
Bundle/feature **0.1.15.202610181859**, 31 classes major 61,
одиннадцать палитр, десять MIT notices. ZIP **105 225 bytes**; SHA-256:
`3f42f3690a3db7e1c960cb654d1a9f54d22603d9dd0e05c4af73e7243595e288`.

**VisualStudio 223 + VisualStudioRestart 141 PASS**, 0 FAIL, оба exit 0:
native targets, реальные BSL glyph, preview/popup, Apply/OK/Cancel, resets,
ручные внешние цвета, BSL=false, независимый шрифт, personal keys десяти
прежних схем, saved ID, restore всех original keys и очистка.
Пиксели двух вертикальных и одной горизонтальной полосы #303031 проверены
в обоих процессах; popup #3A3D41, contrast 7.81:1, полное название читается.
Просмотрены рабочие окна и список. Полные циклы прежних схем не повторялись;
keys всех одиннадцати схем, active profile и journal очищены во втором
процессе; дополнительная Cleanup не потребовалась.

Известная CSS-диагностика Properties/TabbedPropertyTitle с null color
сохраняется (184/36 сообщений); сводка/stack и ручная матрица отделены
от целевых assertions. После первого Apply некоторые native controls светлые,
после restart — тёмные. macOS новой схемы отдельно не проверена.
Проверки и ограничения — `visual-studio-dark-validation.md` и `0.1.15`.

P2 Install/Reconcile/Uninstall/Audit — exit 0; собственные feature/bundle/fragment
удалены, active profile и font journal не остались. 1267 базовых ID/версий
совпали с рабочей EDT. Итог: **77 unit tests и 364 целевые runtime assertions**;
известная CSS-диагностика и оставшаяся ручная матрица отмечены отдельно.

## 18 октября 2026 — подготовка репозитория к GitHub

Пользователь подтвердил 0.1.15 («работает»), согласовал завершение очереди
на 11 палитрах и отложил Monokai Pro (CE). Для собственного кода выбрана MIT.
README, CHANGELOG, CONTRIBUTING, индекс документации и правила Releases
подготовлены к публикации; добавлены Issue/PR templates и CI модели на Java 17.
32 файла dist сняты с отслеживания и игнорируются; локальные архивы сохранены.
История Git, старые ZIP в ней и авторский email не переписывались.

MIT включён в JAR и p2 feature. Verify — BUILD SUCCESS, 77 unit tests,
0 failures/errors/skipped. Упаковка 0.1.15.202610181940, ZIP 107909 bytes,
SHA-256 1ffdbecc9c7f5a7acb3742893645fb6efa51fd7b1dc7a9dffe55ab361b221fc4.
Product Java и 11 палитр не изменялись; runtime/p2-циклы не повторялись.
Проверены локальные ссылки, YAML и сохранность лицензии в архиве.
GitHub Actions ещё не запускался, remote/push/релиз не создавались.
Факты и ограничения — `repository-preparation.md`.

## 18 октября 2026 — единая галерея README

По замечанию пользователя пересняты девять палитр целыми рабочими окнами EDT,
в едином макете навигатор/BSL/Properties и размере 884x581, без белых полей.
Darcula и Monokai оставлены референсами, SHA-256 обоих файлов не изменился.
Новые PNG — docs/screenshots; ссылки девяти строк README обновлены.

Добавлены test-only GalleryPrepare/Gallery и Gallery perspective. Захват
использует клиентскую область Shell: прежний размер с рамкой выходил за
границы GC и давал белые полосы. PNG не редактировались после захвата.
Фрагмент собран Java 17, итоговые фазы 13 + 191 PASS, 0 FAIL, exit 0:
реальные стандартные BSL background/glyph, размер/границы, отсутствие белых
полос, восстановление всех original keys и очистка active profile/journal.
Все девять снимков просмотрены визуально. Product Java и палитры не изменялись.
Факты, ограничения и воспроизведение — readme-gallery-validation.md.

## 30.10.2026 — 0.1.16: EDT 2026, BSL и текстовые поверхности

По запросу пользователя проверены 2026.1.2+2, 2026.1.3+25 и 2026.2.2+21
в отдельных копиях/workspace. 2025.2.6 исключена из дальнейшей работы по
указанию пользователя. Для 2026.1 сохранена Java 17, для 2026.2.2 — Java 25;
неизвестные сочетания компонентов не разрешены.

Воспроизведён цвет вызовов BSL, меняющийся только после правки текста.
Добавлен refresh semantic presentation через public Xtext API без правки
документа; учтены разные semantic API BSL и QL. Палитры распространены на
21 подтверждённый QL/DCS target; embedded viewers обновляются через public
QL UI module extension. Native NUMBER/text классификация сохранена.

Итоговая сборка: 79 unit tests, Tycho/ZIP verify — exit 0. На каждой из трёх
EDT: 56 Semantic + 312 Surfaces PASS, 0 FAIL (всего 1104). Проверены страница
тем, 11 палитр BSL/QL/DCS, native restore, три BSL merge panes и точный restore
115 исходных значений/состояний наличия в каждой workspace. Просмотрены
шесть финальных снимков. Полный мастер с metadata-конфигурацией не проверен.

Cleanup / Reconcile / p2 Uninstall / Audit — exit 0 во всех копиях.
Сохранены 1257 / 1267 / 1234 базовых ID/версий соответственно; installed source
inventory совпал с исходным. Итоговый bundle побайтово совпал с ZIP во всех
runtime. Архив dist/edt-themes-0.1.16.zip, история сборок сохранена.
Рабочие EDT/workspace не менялись; subagents, установка дополнительного ПО,
commit/push не выполнялись. Отчёт: edt-2026-compatibility-validation.md.
## 04.11.2026 — 0.1.17: GitHub Light Default и группы палитр

Добавлена только GitHub Light Default. На странице тем — radio-фильтр
«Тёмные / Светлые», общие шрифт и черновики с прежними Apply/OK/Cancel.
Одиннадцать прежних профилей, 17 ролей и personal keys сохранены.
Light использует подтверждённые 8 CSS-scoped / 25 unscoped named colors
и 10 native scrollbar preferences. Factory/readonly цвета восстанавливаются
при старте. Native lexer/mapper и public presentation APIs обновляют также
QL-комментарии, параметры и имена без правки документа. Concrete helper
привязан для EmbeddedEditorFactory, интерфейс — для полных Xtext editors.

85 unit tests; Tycho/ZIP verify и 6 publication tests — PASS.
На Windows x64 EDT 2026.1.2+2 / 2026.1.3+25 (Java 17) и 2026.2.2+21
(Java 25): 2749 runtime PASS, 0 FAIL. Проверены radio/черновики/HEX/Cancel,
33 цвета после restart, шрифт, прежние personal keys, QL/DCS/embedded,
BSL semantic calls и точный restore. Снято полное окно Light для README.

Cleanup/Reconcile/p2 Uninstall/Audit завершены; 1257 / 1267 / 1234 базовых
ID/версий сохранены. Bundle во всех трёх runtime побайтово совпал с ZIP:
0.1.17.202611032309, 38 классов Java 17. Тестовый launcher открывает чистое
окно, обходя скрытый limbo-контейнер сохранённой перспективы EDT 2026.1.2.

После смены основы native SWT может требовать restart. Уже открытое BSL
сравнение кеширует токены: новые цвета проверены после переоткрытия окна.
Новая Mac-матрица, полный metadata wizard и прочие ручные сценарии не объявлены
проверенными. Рабочие EDT/workspace не менялись. Commit/push/release не выполнялись.
Отчёт: [GitHub Light validation](github-light-validation.md).

## 04.11.2026 — 0.1.18: Winter is Coming (Light)

04.11.2026 по следующему поручению пользователя подготовлен локальный кандидат
0.1.18: добавлена только Winter is Coming (Light), вторая светлая палитра.
Общий код сохранён: 37 из 38 Java-классов совпадают с 0.1.17; изменён реестр.
89 unit tests и 1461 runtime PASS, 0 FAIL на тех же трёх Windows EDT.
Проверены реальные BSL/QL/DCS/embedded/merge цвета, personal keys, restart,
Cancel и restore; прежние 12 палитр и ZIP 0.1.17 побайтово сохранены.
Cleanup/Reconcile/Uninstall/Audit завершены, базовые ID/версии сохранены.
Архив содержит обе светлые темы для совместной локальной проверки пользователя.
Commit/push/release не выполнялись. Отчёт — [Winter Light validation](winter-light-validation.md).

## 04.11.2026 — 0.1.19: Night Owl Light

04.11.2026 продолжена очередь светлых тем: локальный кандидат 0.1.19
добавляет только Night Owl Light, третью светлую палитру. Прежние 13 палитр,
personal keys, ZIP 0.1.17 / 0.1.18 сохранены побайтово. Исправлен сброс цветов
Xtext viewers после Light CSS: читаются effective preferences без новых targets.
Изменены реестр и XtextPresentationRefresh; прочие 34 из 38 классов
совпадают с 0.1.18. 93 unit tests и 3285 runtime PASS,
0 FAIL на Windows EDT 2026.1.2+2 / 2026.1.3+25 (Java 17), 2026.2.2+21 (Java 25).
Проверены все 14 палитр в QL/DCS/embedded, BSL/merge, preview/Cancel/Apply,
перезапуск, личные варианты, точный restore. Cleanup/Reconcile/Uninstall/Audit
завершены, 1257 / 1267 / 1234 базовых ID/версий сохранены. Архив содержит
все три светлые темы для совместной проверки пользователя. Commit/push/release
не выполнялись. Отчёт — [Night Owl Light validation](night-owl-light-validation.md).

## 04.11.2026 — 0.1.20: Ayu Light (Bordered)

04.11.2026 по следующему поручению пользователя подготовлен локальный кандидат
0.1.20: добавлена только Ayu Light (Bordered), четвёртая светлая палитра.
Прежние 14 палитр, ZIP 0.1.17 / 0.1.18 / 0.1.19 сохранены побайтово;
37 из 38 Java-классов совпадают с 0.1.19, изменён только реестр.
97 unit tests и 1539 runtime PASS, 0 FAIL на Windows EDT 2026.1.2+2 /
2026.1.3+25 (Java 17), 2026.2.2+21 (Java 25). Проверены BSL/QL/DCS/embedded/
merge цвета, preview/Cancel/Apply, personal keys прежних тем, restart,
ручной фон и точный restore. Cleanup/Reconcile/Uninstall/Audit завершены,
1257 / 1267 / 1234 базовых ID/версий сохранены. Архив содержит четыре светлые
темы для локальной проверки пользователя. Push явно запрещён до его отмашки;
commit/push/release не выполнялись. Отчёт — [Ayu Light validation](ayu-light-validation.md).

## 04.11.2026 — 0.1.21: Noctis Lux

04.11.2026 по следующему поручению пользователя подготовлен локальный кандидат
0.1.21: добавлена только Noctis Lux, пятая светлая палитра.
Прежние 15 палитр, ZIP 0.1.17 / 0.1.18 / 0.1.19 / 0.1.20 сохранены побайтово;
37 из 38 Java-классов совпадают с 0.1.20, изменён только реестр.
101 unit tests и 1551 runtime PASS, 0 FAIL на Windows EDT 2026.1.2+2 /
2026.1.3+25 (Java 17), 2026.2.2+21 (Java 25). Проверены BSL/QL/DCS/embedded/
merge цвета, preview/Cancel/Apply, personal keys прежних тем, restart,
ручной фон и точный restore. Cleanup/Reconcile/Uninstall/Audit завершены,
1257 / 1267 / 1234 базовых ID/версий сохранены. Архив содержит пять светлых
тем для локальной проверки пользователя. Push явно запрещён до его отмашки;
commit/push/release не выполнялись. Отчёт — [Noctis Lux validation](noctis-lux-validation.md).


## 04.11.2026 — 0.1.22, светлые панели

По замечанию пользователя подтверждён пробел: прежние registry/editor проверки
не проверяли реальные RGB обычных светлых панелей. Добавлены локальные inline
стили SWT с HEX, реестр восстанавливается перед UI refresh; содержимое редакторов
исключено. Исправлены поздние контролы, динамические CSS-классы и инициализация
таблицы параметров на 2026.2.2. Основная матрица: 101 unit tests и 4581 runtime PASS
(265 UI + 364 restart + 898 all surfaces на каждой из трёх EDT), 0 FAIL.
Ещё 84 gallery PASS; все пять 884×581 PNG проверены по пикселям и обновлены
без обработки. Все копии очищены и p2 Audit пройден; 1267/1257/1234 базовых entries,
временные junction удалены, их цели сохранены. Поставляемый bundle
`0.1.22.202611041303`, собственный ZIP major 61; p2-сайт подготовлен локально.
Commit/push/release не выполнялись. Подробности и ограничения —
[отчёт 0.1.22](light-ui-validation.md).


## 04.11.2026 — 0.1.23, фон левой линейки

Исправлен фон номеров и соседних колонок: обновление общего EditorsUI store
перестраивает штатный цвет рисования, дополнительно выравниваются контролы.
Событие language store оказалось недостаточным; закрытые классы не вызываются.
При restore учтён начальный фон projection-колонки из текстового viewer,
её модель распознаётся публичной ProjectionAnnotationModel. Три EDT проверены
последовательно по реальным пикселям: 101 unit tests, 4512 runtime PASS,
ещё 129 gallery PASS. Все 16 палитр, личный фон, новый редактор, startup,
исходные цвета/пиксели и preferences восстановлены; текст/шрифт/контролы сохранены.
Галерея пяти светлых тем обновлена без обработки PNG. ZIP major 61, 43 classes,
37 прежних классов неизменны; палитры и ZIP 0.1.17–0.1.22 неизменны.
Cleanup/Reconcile/Uninstall/Audit — PASS, базовые inventories сохранены,
четыре junction удалены с сохранением целей. Commit/push/release не выполнялись.
[Отчёт](editor-ruler-validation.md).


## 04.11.2026 — 0.1.24, Visual Studio Light (C/C++)

04.11.2026 подготовлен локальный кандидат 0.1.24: добавлена только
Visual Studio Light (C/C++), шестая светлая тема, ID visual-studio-light-cpp.
Прежние 16 палитр и ZIP 0.1.17–0.1.23 сохранены побайтово; изменён только
PaletteCatalog, остальные 42 classes совпали с 0.1.23. Проверены 105 unit tests,
1683 runtime PASS на EDT 2026.1.2+2 / 2026.1.3+25 (Java 17), 2026.2.2+21
(Java 25), ещё 30 gallery PASS с RGB панелей и пикселями линейки.
Cleanup/Reconcile/Uninstall/Audit — PASS, базовые inventories сохранены,
четыре junction удалены с сохранением целей. Commit/push/release не выполнялись.
[Отчёт](visual-studio-light-validation.md).

## 04.11.2026 — 0.1.25, Noctis Hibernus

04.11.2026 подготовлен локальный кандидат 0.1.25: добавлена только
Noctis Hibernus, седьмая светлая палитра, ID noctis-hibernus.
Прежние 17 палитр и ZIP 0.1.17–0.1.24 сохранены побайтово; изменён только
PaletteCatalog, остальные 42 classes совпали с 0.1.24. Проверены 109 unit tests,
1695 runtime PASS на EDT 2026.1.2+2 / 2026.1.3+25 (Java 17), 2026.2.2+21
(Java 25), ещё 30 gallery PASS с RGB панелей и пикселями линейки.
Проверены preview/Cancel/Apply, личные ключи всех 17 прежних схем, startup,
ручной фон и точный restore, текстовые QL/DCS/embedded/compare поверхности.
Cleanup/Reconcile/Uninstall/Audit — PASS, базовые inventories сохранены,
четыре junction удалены с сохранением целей. Commit/push/release не выполнялись.
[Отчёт](noctis-hibernus-validation.md).

## 04.11.2026 — 0.1.26, Noctis Lilac

04.11.2026 подготовлен локальный кандидат 0.1.26: добавлена только
Noctis Lilac, восьмая светлая палитра, ID noctis-lilac. Текущая очередь
светлых тем завершена: всего 19 палитр, 8 светлых и 11 тёмных.
Прежние 18 палитр и ZIP 0.1.17–0.1.25 сохранены побайтово; изменён только
PaletteCatalog, остальные 42 classes совпали с 0.1.25. Проверены 113 unit tests,
1707 runtime PASS на EDT 2026.1.2+2 / 2026.1.3+25 (Java 17), 2026.2.2+21
(Java 25), ещё 30 gallery PASS с RGB панелей и пикселями линейки.
Проверены preview/Cancel/Apply, личные ключи всех 18 прежних схем, startup,
ручной фон и точный restore, текстовые QL/DCS/embedded/compare поверхности.
Cleanup/Reconcile/Uninstall/Audit — PASS, базовые inventories сохранены,
четыре junction удалены с сохранением целей. Commit/push/release не выполнялись.
[Отчёт](noctis-lilac-validation.md).

## 05.11.2026 — 0.1.27, Git-подписи навигатора

05.11.2026 подготовлен локальный кандидат 0.1.27 по скриншоту пользователя:
исправлены отдельные подложки Git-подписей во всех 19 палитрах. В общую карту
добавлены четыре EGit ColorDefinition с plain workbench keys для обоих режимов;
BG=editor.background, modified FG=accent, ignored FG=disabled. Новых ролей нет.
Изменён только SettingCatalog.class; 42 прежних класса, 19 палитр и ZIP
0.1.17–0.1.26 сохранены побайтово. 116 unit tests и 4701 runtime PASS
на трёх EDT, реальные EGit-декорации и пиксели для всех 19 профилей,
тёмный startup, личный фон, ручной цвет и точный restore подтверждены.
Регрессия BSL/QL/DCS/embedded/compare всех 19 палитр пройдена.
Cleanup/Reconcile/Uninstall/Audit — PASS, базовые inventories сохранены,
временные junction удалены с сохранением целей. Commit/push/release не выполнялись.
[Отчёт](git-labels-validation.md).
