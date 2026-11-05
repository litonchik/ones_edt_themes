# GitHub Light Default и переключение Light/Dark — 0.1.17

Дата проверки: **04.11.2026**. Реализация выполнена в `edt_themes`.
Исходный каталог `ones_edt_theme_plugin` сохраняет прежнюю историю; его
изолированные копии EDT и Maven cache временно подключались через junction.
Установленные EDT и пользовательские workspace не изменялись.

## Реализация

- Добавлена только **GitHub Light Default**, ID `github-light-default`.
  Прежние 11 тёмных профилей и их RGB-ресурсы сохранены.
- Источник: GitHub Theme 6.3.5, commit `cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf`,
  Primer 7.10.0, MIT. Существующие лицензии сохранены, атрибуция расширена.
  [Исследование и сопоставление 17 ролей](../research/2026-11-03-light-themes.md).
- Комментарии #656D76 дают 4.84:1 на текущей строке #F4F6F8.
- Радиокнопки **Тёмные / Светлые** фильтруют список; выбор и preview до Apply/OK
  локальны. Черновики, invalid HEX, Cancel, личные варианты и общий шрифт
  сохраняют прежнюю семантику. Группа активного профиля выбирается при открытии.
- Светлая основа: `org.eclipse.e4.ui.css.theme.e4_default`; тёмная — прежний
  `org.eclipse.e4.ui.css.theme.e4_dark`. Ожидаемый ID зависит от профиля при
  apply, restart и внешней смене native темы.
- Для Light подтверждены **8 CSS-scoped и 25 unscoped registry addresses**;
  десять native scrollbar preferences отличаются от Dark. Источники трёх EDT,
  включая их win/mac CSS, дали одинаковые адреса и значения:
  [карты платформ](2026-11-04-github-light/platform/).
- SnapshotService отличает известные native seeds Light/Dark от ручных значений;
  восстанавливаются исходные значения и отсутствие явного переопределения.
  Формат журналов и personal keys не менялся.

## Найденные при проверке особенности

Первый тест перезапуска выявил сброс цветов HOVER_BACKGROUND/FOREGROUND и
FORM_HEADING_ERROR/WARNING. В `org.eclipse.ui/plugin.xml` они объявлены
неизменяемыми через штатную страницу (`isEditable=false`), hover использует
`RGBInfoColorFactory`. Eclipse заново создаёт эти цвета при запуске Light.
Добавлено восстановление только управляемых исчезнувших значений и применение
ColorRegistry через публичный API; явные ручные значения сохраняются.
Лениво создаваемые registry entries инициализируются из сохранённых значений
без изменения пользовательского текста или шрифта.

QL lexer выдаёт RULE_NUMBER (grammar NUMBER), а native mapper распознаёт
числа по RULE_INT; используется Ql_Others. Native категория сохранена.
PreferenceStoreAccessor убирает явный цвет токена, равный основному цвету
редактора. Light CSS оставляет другой foreground контрола; для обычного
текста QueryTextAttributeProvider сохраняет действующий foreground явно.
Цвет создаётся при UI-инициализации кеша; фоновый semantic расчёт читает
готовый TextAttribute без обращений к SWT.
Числа QL проверяются по роли text, BSL — по исходной роли number.

Полная QL-перерисовка использует native DocumentTokenSourceAccess и mapper:
обновляются также комментарии, параметры и обычные имена без смены категорий.
Проверяются их RGB и неизменность текста в каждом из четырёх QL-контролов.
Для EmbeddedEditorFactory привязан и конкретный HighlightingHelper,
для полных редакторов — его интерфейс.

ViewerHighlightingHelper обновляет публичный ITextAttributeProvider,
semantic reconciler и lexical presentation после native CSS. Применяется
через публичный QL UI module extension, включая embedded viewers.
Native BSL UI module, attribute provider и calculators сохранены.
Compare-проверка передаёт реальный CompareContainer и IWorkbenchPartSite,
чтобы EDT создавала полноценные Xtext merge editors.

В переходе Visual Studio Dark → GitHub Light уже открытые native BSL merge
панели сохраняют прежние цвета токенов, хотя фон обновляется. Повторное
открытие сравнения проверяется отдельно. Текст существующих панелей не меняется.
Live-refresh их токенов не объявлен исправленным.

После смены Light/Dark часть уже созданных нативных SWT-контролов сохраняет
предыдущее оформление до нового запуска. Снимок после restart показывает
штатные светлые элементы. Это отражено в инструкции по использованию.

## Сборка

Maven **3.9.11**, Tycho **4.0.13**, Axiom Java **17.0.18**.
**85 model unit tests**, 0 failures/errors/skipped. Шесть тестов упаковки
сайта обновлений прошли; подготовка p2 сайта из ZIP выполнена локально.
Плагин в GitHub Actions не пересобирался и новый релиз не публиковался.

Поставка: 38 Java classes major 61, собственный bundle и feature,
12 палитр и лицензии; vendor bundles и test-only fragment отсутствуют.
Идентификатор bundle и SHA-256 приведены в
[проверке поставки](2026-11-04-github-light/shipping-verification.txt).

Точные версии Eclipse, Xtext, BSL/QL/theming/compare bundles:
[bundle-versions.json](2026-11-04-github-light/platform/bundle-versions.json).

## Runtime-матрица

| Windows x64 EDT | JVM | Surfaces | Light | LightRestart | Semantic |
|---|---|---:|---:|---:|---:|
| 2026.1.2+2 | Axiom 17.0.18 | 563 | 97 | 197 | 60 |
| 2026.1.3+25 | Axiom 17.0.18 | 562 | 97 | 197 | 60 |
| 2026.2.2+21 | Axiom 25.0.2 | 562 | 97 | 197 | 60 |

Всего **2749 runtime PASS, 0 FAIL** в четырёх фазах трёх EDT.

Тестовый workbench открывает новое окно с чистой Gallery-перспективой:
в сохранённой модели EDT 2026.1.2 редакторы попадали в скрытый limbo-контейнер.
Это настройка probe, код плагина при исправлении раскладки не менялся.

Surfaces открывает обычный текст; цвета QL/QL DCS и двух настоящих embedded-контролов
конструктора проверяются на всех 12 палитрах. Штатный BSL merge viewer проверяется в
Visual Studio Dark и GitHub Light. После работы сравнивается исходное состояние
всех затронутых registry preferences, редакторских и native CSS keys.

Light проверяет radio-фильтр, pending preview, Apply/Cancel, скрытые черновики
и HEX-ошибки, общий шрифт, Light/Dark transitions без изменения document stamp,
BSL=false, ручной переход через native theme engine и независимость прежних
личных палитр. LightRestart проверяет native ID, 33 named colors, открытие
правильной группы, сохранение шрифта и точное восстановление исходных значений.

Semantic проверяет встроенные, локальные и вложенные вызовы BSL для всех 12
палитр до и после редактирования, без необходимости менять текст ради цвета.

## Очистка и границы

После каждой версии выполняются отдельные Cleanup, Reconcile, p2 Uninstall,
Audit и сравнение inventory. Итоговые результаты сохраняются в свидетельствах
соответствующей версии. Временные junction удалены после проверки; исходные
изолированные копии EDT и кэш сохраняются.
[Итоги очистки](2026-11-04-github-light/cleanup-summary.txt) и
[финальные проверки](2026-11-04-github-light/final-checks.txt).

Проверка использует реальные public SWT/Xtext/EDT controls и p2 director.
Ручной проход полного metadata query wizard, всех tooltip/completion/DPI
сценариев и macOS для 0.1.17 не выполнялся. Native Properties CSS-диагностика
и сообщения изолированного окружения не объявлены исправленными.
Светлый вариант Dracula исключён по решению пользователя.

## Воспроизведение

```powershell
./scripts/build.ps1 -Action Verify
./scripts/p2-test.ps1 -Action Install -EdtCopy .work/edt-test
./scripts/build-workbench-probe.ps1 -EdtCopy .work/edt-test
./scripts/run-workbench-probe.ps1 -Phase Surfaces -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/run-workbench-probe.ps1 -Phase Light -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/run-workbench-probe.ps1 -Phase LightRestart -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/run-workbench-probe.ps1 -Phase Semantic -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/run-workbench-probe.ps1 -Phase Cleanup -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/run-workbench-probe.ps1 -Phase Reconcile -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/p2-test.ps1 -Action Uninstall -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
./scripts/p2-test.ps1 -Action Audit -EdtCopy .work/edt-test -WorkRoot .work/light-2026.1.3
```

Для 2026.1.2 выбираются её тестовая копия и отдельный WorkRoot.
Для 2026.2.2 также указывается `-JavaHome '<JDK25>'`.
Полный снимок Light для README снимается фазами LightGalleryPrepare/LightGallery
после восстановления исходного шрифта, в окне того же размера, что тёмная галерея.
