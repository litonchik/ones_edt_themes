# Handoff: редактор палитры для 1С:EDT

> Реализовано в новом чате 14 октября 2026: поставка **0.1.4**.
> Актуальные результаты — `docs/testing/palette-validation.md`,
> инструкция — `docs/user-guide.md`, архитектура — `docs/reference/palette-editor.md`.
> Ниже сохранено исходное задание; оно не описывает текущую незавершённую работу.

Дата: 14 октября 2026, Europe/Moscow.
Проект: `C:\Users\litonchik\Documents\Codex\ones_edt_theme_plugin`.

## Запрос и разрешение пользователя

Пользователь подтвердил работу 0.1.3 и попросил проанализировать возможность
показать на странице плагина используемые цвета, объяснить их применение и дать
возможность менять их в рамках Darcula/Monokai. Рекомендован редактор 17 ролей
с отдельными пользовательскими overrides для каждой палитры.

После анализа пользователь написал:

> тогда давай делать, только в рамках нового чата. Составь handoff и промт для нового чата

Реализация рекомендованной функции разрешена, но начинается в новом чате.
В этом чате продуктовый код для неё не написан. Созданы только исследование,
этот handoff и стартовый промпт. Утверждённый план MVP относится к уже выполненному
MVP, не является планом редактора палитры. Новый подробный план ещё не создан.
Полный анализ — `docs/research/2026-10-14-palette-editor.md`; он отделяет факты
от рекомендуемых технических решений и дополнительных возможностей.

## Согласованный объём

- На странице «Темы EDT» показать используемую палитру: русское имя роли,
  образец цвета, HEX и пояснение, где применяется цвет.
- Дать пользователю выбирать цвет и менять его под себя внутри Darcula/Monokai.
  Одна роль может затрагивать несколько элементов — перечислять эти связи явно.
- Сохранять личные значения отдельно для Darcula и Monokai в текущей workspace.
- До Apply/OK менять только локальный preview; поддержать обычные Apply/OK/Cancel.
- Возвращать отдельный цвет или палитру к встроенным значениям стиля.
- Сохранить отдельное восстановление состояния EDT до включения плагина.
  Оно не должно удалять сохранённые личные варианты Darcula/Monokai.
- Не сломать защиту ручных настроек EDT, исходные absent/explicit values,
  BSL checkbox, независимость шрифта и восстановление после перезапуска.
- Результат — проверенный новый p2 ZIP, инструкция и фактические результаты тестов.

Редактор каждого отдельного target, экспорт/импорт, дополнительные стили не входят
в необходимую первую поставку. Предупреждение о плохом контрасте — дополнительное
предложение анализа, не обязательная новая подсистема. Компоновку страницы и
проверку HEX выбрать разумно по существующему UI, не расширяя задачу.

## Прочитать сначала

1. `AGENTS.md` — действующие ограничения пользователя.
2. `docs/research/2026-10-14-palette-editor.md` — факты, все 17 ролей и рекомендации.
3. `docs/reference/edt-2026.1-settings.md` — реальные targets, CSS refresh и границы.
4. `docs/reference/code-font.md` — независимый шрифт и его журнал.
5. `docs/testing/profile-list-validation.md` — проверенная база 0.1.3.
6. `docs/testing/progress.md`, `docs/testing/mvp-validation.md`, `docs/user-guide.md`.

Старый handoff 13 октября описывает состояние ДО MVP и не должен использоваться
как актуальный статус. Не повторять уже выполненный MVP и инвентаризацию без причины.

## Проверенная база 0.1.3

Пользователь сообщил, что тема, шрифт и исправленные списки работают.
Последний ZIP: `dist/edt-themes-0.1.3.zip`, 73 607 bytes.
Bundle: **0.1.3.202610140903**, 26 class-files major 61.
SHA-256: `7123819ab9517511646cc0ea9786af188df13e9ba0dac5b5db58494da8486fbe`.

Последние фактические результаты: **38 unit tests**, **133 Preview**, **878 Apply**,
**100 Restore**, **39 FontApply**, **9 FontRestore PASS**, **0 FAIL**.
Verify BUILD SUCCESS; p2 Install/Uninstall/Audit exit 0.
1267 базовых ID/версий bundles совпали с установленной EDT.
Assertions и безопасные снимки popup — `docs/testing/0.1.3`.
Все прежние ZIP сохранены; новый ZIP следует выпустить отдельно (например, 0.1.4).

Рабочая EDT пользователя не менялась. Существующая тестовая копия:
`.work/edt-test`; workspace: `.work/workspace-probe`.
В конце проверки плагин и test-only fragment удалены из конфигурации копии,
журналы цветов/шрифта пусты. При подготовке этого handoff повторно проверено
отсутствие собственных bundle entries в bundles.info. Для нового runtime сначала
установить новый ZIP в копию и собрать probe. Не пересоздавать/удалять копию целиком.

## Точные инструменты и ограничения

| Инструмент | Путь / версия |
|---|---|
| Maven | `C:\apps\apache-maven-3.9.11\bin\mvn.cmd` |
| JDK | `C:\Program Files\Axiom\AxiomJDK-Pro-17-Full`, 17.0.18 |
| Рабочая EDT, только чтение | `C:\Program Files\1C\1CE\components\1c-edt-2026.1.3+25-x86_64` |
| Eclipse / SWT / Xtext UI | 4.30.0 / 3.124.200 / 2.33.0 |
| EDT BSL UI / theming UI | 23.0.1.v202609041212 / 2.1.0.v202609041212 |
| Tycho | 4.0.13 |

Maven 3.10.0 несовместим с выбранным Tycho в этом окружении; PATH содержит Java 25.
Использовать полный путь Maven и JDK 17, только process-local JAVA_HOME,
MAVEN_BASEDIR, MAVEN_SKIP_RC=true. `scripts/build.ps1` уже сохраняет/возвращает
переменные и использует `.cache/m2`, `.work/java-temp`. Глобальную среду не менять.

Работать самостоятельно, без субагентов и Git; дополнительное системное ПО
пользователь устанавливает сам. Соблюдать действующий AGENTS.md. Никаких vendor
patches, reflection или замены базовых bundles. Документация на русском в проекте.

Сборка/проверки новых функций не разрешают установку в рабочую EDT пользователя.
Нужные публичные ColorSelector/ColorDialog уже подтверждены javap целевой EDT,
дополнительного ПО для picker не требуется.

## Архитектура и ключевые файлы

Корень исходников bundle: `bundles/ru.litonchik.edt.themes/src/ru/litonchik/edt/themes`.

- `model/Palette.java`: 17 ролей, immutable map, проверка HEX, преобразование RGB.
- `model/PaletteCatalog.java`: встроенные ресурсы Darcula/Monokai, пока без overrides.
- `model/SettingCatalog.java`: план из проверенной карты и связь role → targets.
  Карта содержит 51 RGB target и 5 SystemDefault flags.
- `runtime/ThemeCoordinator.java`: apply/restore, lifecycle, защита внешней смены темы.
- `model/SnapshotService.java`: исходные значения, lastApplied/surrendered,
  частичный отказ, rollback, восстановление и recovery.
- `runtime/SnapshotPreferences.java`: two-slot durable journal цветов;
  отдельный section font. Не смешивать personal palette storage с originals.
- `edt/EdtSettingsAdapter.java`: публичное применение штатной Dark, сброс CSS cache,
  точное восстановление preferences после CSS и обновление открытых editors.
- `ui/ThemePreferencePage.java`: страница, профиль/BSL/шрифт, preview, Apply/OK/Cancel.
- `ui/ReadableCombo.java`: общий owner-drawn popup Table тем/шрифтов; readable selection.
- `model/CodePreview.java`: фиксированный RU/EN образец и token spans, не BSL parser.
- `runtime/CodeFontSettings.java`: независимый text font и original effective font.
- `resources/edt-2026.1-settings.properties`, `resources/palettes/*.properties`.

Сейчас и coordinator, и preview напрямую вызывают PaletteCatalog.load. Нужен общий
resolver встроенная палитра + overrides, чтобы UI и EDT применяли одинаковые цвета.

Не использовать snapshot.applyExplicit для всего плана цвета: он снимает защиту
ручных изменений со ВСЕХ переданных ключей. Для явного редактирования/сброса роли
нужен набор конкретных targets этой роли; остальные внешние правки сохраняются.
Обычное переключение профиля также не должно превращаться в принудительное
применение всего набора. Текущую семантику applyExplicit для шрифта сохранить.

Новый store должен отделять сохранённые overrides профилей, UI drafts и снимок
первоначальной EDT. При ошибках записи/flush состояния store и EDT согласованно
откатываются/восстанавливаются; личные профили не уничтожаются обычным restore.
Рекомендуемая семантика черновиков: переключение профиля сохраняет draft в диалоге;
Apply/OK сохраняет изменённые варианты, применяет выбранный; Cancel отбрасывает
неприменённое. Native styling и внешние project preferences могут давать отличия
от значений профиля — не выдавать профиль за аудит всех эффективных цветов.

Встроенный Dark ID обязательно `org.eclipse.e4.ui.css.theme.e4_dark`.
Native defaults map (56 записей) обслуживает побочные изменения Dark и исходный
снимок, это не дополнительные редактируемые роли. При BSL=false сохранять исходные
BSL values; роли могут одновременно относиться к BSL и интерфейсу, указывать это.
Error/warning roles сейчас покрывают form headings, а не все маркеры/console streams.

## Проверки и сборка следующей поставки

Базовые команды (последовательно; ErrorActionPreference=Stop):

```powershell
& .\scripts\build.ps1 -Action Test
& .\scripts\build.ps1 -Action Verify
& .\scripts\p2-test.ps1 -Action Install -Zip .\dist\edt-themes-<новая-версия>.zip
& .\scripts\build-workbench-probe.ps1
& .\scripts\run-workbench-probe.ps1 -Phase Preview
& .\scripts\run-workbench-probe.ps1 -Phase Apply
& .\scripts\run-workbench-probe.ps1 -Phase Restore
& .\scripts\run-workbench-probe.ps1 -Phase FontApply
& .\scripts\run-workbench-probe.ps1 -Phase FontRestore
# Новые проверки редактора палитры добавить в probe по необходимости.
& .\scripts\run-workbench-probe.ps1 -Phase Reconcile
& .\scripts\p2-test.ps1 -Action Uninstall
& .\scripts\p2-test.ps1 -Action Audit
```

При изменении release version обновить root/child POM, manifest, feature.xml,
build.ps1, p2-test default, version regex verify-repository и документацию.
Новые unit tests: merge/validation/reset, изоляция профилей, совместимость старого
store/journal, выборочный explicit apply, сохранение original absent/explicit,
отказы/rollback. Runtime: pending edits без записи, оба профиля, переключение drafts,
реальные Apply/OK/Cancel, BSL=false, reset роли/палитры, внешние ручные правки,
restart, сохранение personal palettes после restore, регрессия шрифта, p2 audit.

Скрипты runtime изолируют APPDATA/LOCALAPPDATA/USERPROFILE/TEMP/TMP/user.home в .work.
Probe — отдельный test-only fragment, не включать его в ZIP. Native SWT automation
через CUA отключена; использовать имеющийся тестовый workbench/public SWT APIs.
Снимки ограничивать собственными видимыми непрозрачными popup/контролами.
Никогда не сохранять полный рабочий стол: может попасть чужое окно. Shell.print
на этой платформе ранее возвращал false и не является проверенным механизмом.

p2 под sandbox ранее получил exit 13 (тестовая копия read-only); разрешённый запуск
установил тот же ZIP только в .work. При таком отказе использовать штатный механизм
разрешений, не менять ACL рабочей EDT. Heap p2 4096m уже выбран скриптом по EDT:
512/1024m ранее приводили к startup OOM. Не запускать p2/GUI операции одновременно.
Reconcile обязателен перед uninstall: вручную добавленный probe может стать locked
dropins IU и удерживать host. Он удаляет только тестовый fragment внутри .work.

Границы визуальной приёмки DPI, формы/MXL/графической схемы/СКД остаются в ручной
матрице. Не объявлять проверенным новый UI без собственной проверки.
Долгие повторные тесты выполнять только при изменениях/ошибках, требующих этого.

## Отложенная задача и оценка времени

Hover строки в ReadableCombo пользователь признал некритичным и отложил.
Он записан последним пунктом `docs/testing/mvp-validation.md` с самым низким
приоритетом. Не реализовывать его попутно с редактором палитры без нового запроса.

Оценка 4–8 часов была грубой оценкой ВСЕЙ разработки, включая написание тестов,
не времени сборки. Пользователю это пояснено: последняя Tycho-сборка заняла около
26 секунд, execution runtime/p2 — минуты. Не превращать эту оценку в обязательную
длительность и не повторять полные проверки без основания.

Начать с чтения текущего кода и определения конкретных изменений хранения,
выборочного apply и страницы в согласованном объёме. Продолжить до проверенной
поставки новой версии и документации; не возвращаться к повторному анализу MVP.
