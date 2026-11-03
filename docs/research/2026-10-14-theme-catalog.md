# Каталог новых тем: источники и разложение для EDT

Дата исследования: **14 октября 2026**. База проекта: **0.1.5**.
Статус первоначального анализа на базе 0.1.5: **предлагаемые палитры до реализации**.

Последующее развитие: после согласования пользователя реализована **One Dark Pro
в 0.1.6**. [Фактические проверки](../testing/one-dark-pro-validation.md).
Затем добавлена **Dracula в 0.1.7**:
[проверки](../testing/dracula-validation.md). Остальной текст сохраняет исходный
анализ на базе 0.1.5. В 0.1.8 добавлена **GitHub Dark Default**:
[проверки](../testing/github-dark-validation.md). В 0.1.9 добавлена
**Winter is Coming (Dark Blue)**: [проверки](../testing/winter-is-coming-validation.md).
В 0.1.10 роль panel адаптирована в #0E293F вместо исходного #011627 для
различимых разделителей окон EDT: [проверка](../testing/winter-separators-validation.md).
Таблица исходного анализа ниже сохранена.
В 0.1.11 добавлена **Night Owl** с адаптированной panel #0E293F:
[проверки](../testing/night-owl-validation.md). В 0.1.12 добавлена
**Ayu Dark (Bordered)**: [проверки](../testing/ayu-dark-validation.md).
Panel адаптирована в #1B1F29, hover пересчитан поверх неё в #262C38;
таблица первоначального анализа сохранена. В 0.1.13 добавлена базовая
**Shades of Purple**: [проверки](../testing/shades-of-purple-validation.md).
В 0.1.14 добавлена **Noctis Minimus**: [проверки](../testing/noctis-minimus-validation.md).
Panel адаптирована в #101213, number осветлён в #8E87C5 до contrast 4.56:1;
таблица первоначального анализа сохранена. В 0.1.15 добавлена
**Visual Studio Dark (C/C++)**, вариант Dark из Themes 2.1.0:
[проверки](../testing/visual-studio-dark-validation.md). Все 17 ролей совпадают
с анализом, включая описанные адаптации UI.
18.10.2026 пользователь согласовал **завершение очереди на 11 палитрах**
(две MVP и девять новых). **Monokai Pro (CE) отложена** как близкая
к существующей Monokai; таблица CE ниже остаётся исследованием, не планом реализации.

## 1. Назначение и принятые для анализа границы

Запрос пользователя: составить список тем из [статьи HTML Academy «10 лучших тем для VS Code»](https://htmlacademy.ru/blog/soft/vs-code-themes), разложить каждую по цветовым составляющим плагина, затем перейти к Git и последовательной реализации. Статья опубликована 11.06.2023; её список используется как исходный перечень, а не как подтверждённый рейтинг популярности в 2026 году. Число вариантов в старой статье не заменяет текущие package.json авторов.

В этом этапе подготовлен опорный документ. Инициализация/коммиты Git, код плагина, сборка, установка ПО и изменения рабочей EDT не выполнялись. Предлагаемые IDs, варианты, очередь и адаптации ниже — проектные решения для обсуждения перед реализацией, а не уже согласованный новый план. Утверждённый MVP повторно проектировать не нужно.

Рекомендуется продолжить текущую модель: самостоятельные палитры поверх штатной Dark, по одному конкретному тёмному варианту семейства. Это даёт десять кандидатов, включая предлагаемую замену VS Code Monokai Pro на открытую официальную CE. Светлые варианты остаются в перечне возможного развития, но требуют отдельного решения о светлой основе EDT, dark icons, применении/восстановлении и тестовой матрице.

Альтернативы: переносить все варианты сразу (большая матрица и мало новых возможностей), либо вводить независимые CSS themes (отдельная архитектура и риск несовпадения точного ID Dark EDT). Рекомендован ограниченный перенос выбранных тёмных вариантов в существующие 17 ролей.

## 2. Список из статьи и конкретные кандидаты

| № статьи | Семейство | Вариант первой реализации / предлагаемый ID | Дальнейшие варианты |
|---|---|---|---|
| 1 | C/C++ Themes | Visual Studio Dark (C/C++) / visual-studio-dark-cpp | 2017 Dark; Light отдельно |
| 2 | One Dark Pro | One Dark Pro / one-dark-pro | Flat, Darker, Mix, Night Flat |
| 3 | GitHub Theme | GitHub Dark Default / github-dark-default | Dark Dimmed, High Contrast, Colorblind; Light отдельно |
| 4 | Dracula Official | Dracula / dracula | Soft; не путать с Darcula |
| 5 | Winter is Coming | Dark Blue / winter-is-coming-dark-blue | Dark Black; Light отдельно |
| 6 | Monokai Pro | Monokai Pro (CE) / monokai-pro-ce, предлагаемый источник Zed | VS Code Pro только после отдельного разрешения автора |
| 7 | Night Owl | Night Owl / night-owl | Light отдельно |
| 8 | Ayu | Ayu Dark / ayu-dark | Mirage, Unbordered; Light отдельно |
| 9 | Shades of Purple | Shades of Purple / shades-of-purple | Super Dark |
| 10 | Noctis | Noctis Minimus / noctis-minimus | Noctis, Azureus, Bordo, Obscuro, Sereno, Uva, Viola; светлые отдельно |

GitHub и Night Owl в статье ведут на светлые варианты; здесь сознательно выбран тёмный представитель. Ayu ведёт на Dark Bordered, Noctis — на Minimus. Monokai и Darcula из 0.1.5 сохраняют свои IDs и личные варианты; они не заменяются новыми темами. Имена авторов/семейств обозначают происхождение палитры, не официальную поддержку EDT авторами.

## 3. Подтверждённые ограничения проекта

Локальные источники: [Palette](../../bundles/ru.litonchik.edt.themes/src/ru/litonchik/edt/themes/model/Palette.java), [карта settings](../../bundles/ru.litonchik.edt.themes/resources/edt-2026.1-settings.properties), [редактор палитры](../reference/palette-editor.md), [проверка платформы](2026-10-13-installed-platform.md), [Mac-матрица](../testing/macos-validation.md).

- 17 обязательных RGB-ролей, только #RRGGBB. Карта содержит 51 цветовой target и 5 SystemDefault flags.
- Только штатная Dark с точным ID org.eclipse.e4.ui.css.theme.e4_dark. Отдельного CSS, новых preference keys и новых vendor bundles для расширения каталога пока не требуется.
- text одновременно используется в UI, редакторе, консоли, операторах/скобках/Others BSL. panel объединяет панели, контролы, выбранные вкладки и popup. selection.background/text объединяют текстовое и интерфейсное выделение.
- accent объединяет акцент UI, hover-границы, ссылки и Builtin function BSL; disabled — отключённые контролы, невыбранные вкладки и номера строк. Точное независимое воспроизведение этих исходных цветов невозможно в текущих 17 ролях.
- error/warning окрашивают конкретные заголовки формы из карты, не все диагностические маркеры/потоки консоли. hover — штатный target EDT; наведение строк ReadableCombo остаётся отложенным.
- Отдельные параметры токенов bold/italic, TextMate scopes, semantic token classes, caret, search/diff highlights и иконки VS Code не импортируются. Стиль BSL и независимая настройка шрифта сохраняют текущую семантику.
- Исключения содержимого форм/MXL/схем/СКД сохраняются. Полное визуальное совпадение VS Code и SWT не обещается.

Совместимость берётся из существующих проверок 0.1.5: EDT 2026.1.3+25, Eclipse 4.30.0, Xtext UI 2.33.0, BSL UI 23.0.1.v202609041212, theming UI 2.1.0.v202609041212, Java 17. Новое исследование цветов эти версии повторно не проверяло. Mac x64 в целом подтверждён пользователем; отдельные новые палитры и полный набор Mac-сценариев пока не проверены.

### Семантика 17 ролей

| Роль | Назначение в текущем плагине |
|---|---|
| editor.background | Редактор, деревья/таблицы, консоль |
| text | Обычный текст UI и кода, операторы/скобки/Others BSL, консоль |
| panel | Панели/контролы, выбранные вкладки, completion и hover-popup |
| border | Границы контролов EDT |
| selection.background | Фон выбранного текста и строки |
| selection.text | Обычный текст выбранного элемента; фактические BSL glyph проверить |
| line | Фон текущей строки редактора |
| comment | Комментарии BSL |
| keyword | Ключевые слова BSL |
| string | Строковые литералы BSL |
| number | Числовые литералы BSL |
| directive | Препроцессор BSL |
| accent | Акцент EDT, hover-границы, ссылки, Builtin function BSL |
| error | FORM_HEADING_ERROR_COLOR |
| warning | FORM_HEADING_WARNING_COLOR |
| disabled | Отключённые контролы, невыбранные вкладки, номера строк |
| hover | CONTROLS_HOVER_BACKGROUND_COLOR |

## 4. Метод переноса и обозначения

**И** — исходный RGB подтверждён указанным ключом/категорией. Его назначение роли EDT всё равно является нашим сопоставлением. **С** — вычислен непрозрачный RGB. **А** — предложена адаптация, fallback или перенос между назначениями; её нельзя выдавать за точный исходный setting. **А+С** объединяет оба случая. В таблицах указано полное покрытие 17 ролей для каждого кандидата.

Прозрачность сводится к выбранному фону: channel = round(alpha × foreground + (1 − alpha) × background), alpha = AA / 255. Для rgba в генераторе GitHub сначала зафиксирован 8-bit alpha, как для hex-ресурса; 0.1 → 1A. Это проектная нормализация в sRGB-каналах. Нельзя отрезать последние два символа HEX: получится существенно иной цвет. Поскольку EDT применяет одну роль к разным поверхностям, сведение поверх одного фона — неизбежный компромисс; базовый фон указан в каждой вычисленной строке.

Для обычного выделения сначала берём editor.selectionBackground и явно задаём selection.text из подходящего текста источника, если editor selection foreground отсутствует. Яркие list-selection цвета не копируются автоматически поверх всех редакторов. Directive без прямого эквивалента выбирается отдельно с пояснением. Новые роли/function-preferences без исследования целевой платформы не добавляются.

## 5. Разложение каждой темы

### 5.1. Visual Studio Dark (C/C++)

Предлагаемый ID: `visual-studio-dark-cpp`. Версия источника: **2.1.0**.
Commit: `eb981644369c99d39054578a769af572c24bd1eb`.
[Первичный файл](https://github.com/microsoft/vscode-cpptools/blob/eb981644369c99d39054578a769af572c24bd1eb/Themes/themes/cpptools_dark_vs_new.json); [лицензия MIT](https://github.com/microsoft/vscode-cpptools/blob/eb981644369c99d39054578a769af572c24bd1eb/Themes/LICENSE.txt). Просмотрены 14.10.2026.

Выбран именно Dark из ссылки статьи: package.json связывает его с cpptools_dark_vs_new.json; cpptools_dark_vs.json — отдельный 2017 Dark. Тема предназначена для C/C++, поэтому BSL получает общие категории, без C++ semantic scopes. Роль directive берёт цвет имени макроса, а не общий meta.preprocessor (#C8C8C8). Отсутствующие UI-состояния спроектированы из доступных цветов темы; warning — предложенный тёплый цвет строки, а не штатный цвет предупреждения VS Code.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #1E1E1E | И | editor.background |
| text | #DADADA | И | editor.foreground |
| panel | #303031 | А | menu.background |
| border | #404040 | А | editorIndentGuide.background |
| selection.background | #3A3D41 | А | editor.inactiveSelectionBackground |
| selection.text | #DADADA | А | editor.foreground |
| line | #3A3D41 | А | editor.inactiveSelectionBackground |
| comment | #57A64A | И | scope comment (последний RGB) |
| keyword | #569CD6 | И | scope keyword (последний RGB) |
| string | #CE9178 | И | scope string (последний RGB) |
| number | #B5CEA8 | И | scope constant.numeric (последний RGB) |
| directive | #BEB7FF | А | scope entity.name.function.preprocessor (последний RGB) |
| accent | #007ACC | А | activityBarBadge.background |
| error | #F44747 | А | scope invalid (последний RGB) |
| warning | #CE9178 | А | scope string (последний RGB) |
| disabled | #A6A6A6 | А | input.placeholderForeground |
| hover | #383B3D | А | list.dropBackground |

### 5.2. One Dark Pro

Предлагаемый ID: `one-dark-pro`. Версия источника: **3.20.3**.
Commit: `ce79c5f7116607d635e06b7ef4413d85a77905e4`.
[Первичный файл](https://github.com/Binaryify/OneDark-Pro/blob/ce79c5f7116607d635e06b7ef4413d85a77905e4/themes/OneDark-Pro.json); [лицензия MIT](https://github.com/Binaryify/OneDark-Pro/blob/ce79c5f7116607d635e06b7ef4413d85a77905e4/LICENSE.txt). Просмотрены 14.10.2026.

Базовый One Dark Pro, не Flat/Darker/Mix. В этой закреплённой версии основной comment — #7F848E, не часто цитируемый старый #5C6370. Directive выбран из цвета типов/классов. Accent сохраняет синий цвет ссылки; исходный support.function (#56B6C2) теряется из-за общей роли с UI. Disabled берёт основной comment вместо слишком тёмных editorLineNumber (#495162). Selection.text берёт более светлый list.activeSelectionForeground: обычный editor.foreground на сведённом выделении даёт только 4.30:1.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #282C34 | И | editor.background |
| text | #ABB2BF | И | editor.foreground |
| panel | #21252B | И | sideBar.background |
| border | #3E4452 | И | panel.border |
| selection.background | #404859 | С | editor.selectionBackground; #67769660 поверх #282C34 |
| selection.text | #D7DAE0 | А | list.activeSelectionForeground |
| line | #2C313C | И | editor.lineHighlightBackground |
| comment | #7F848E | И | tokenColors: Comments |
| keyword | #C678DD | И | tokenColors: Keywords |
| string | #98C379 | И | tokenColors: Strings |
| number | #D19A66 | И | tokenColors: Integers |
| directive | #E5C07B | А | tokenColors: Classes |
| accent | #61AFEF | И | textLink.foreground |
| error | #C24038 | И | editorError.foreground |
| warning | #D19A66 | И | editorWarning.foreground |
| disabled | #7F848E | А | tokenColors: Comments |
| hover | #2C313A | И | list.hoverBackground |

### 5.3. GitHub Dark Default

Предлагаемый ID: `github-dark-default`. Версия источника: **6.3.5**.
Commit: `cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf`.
[Первичный файл](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/theme.js); [лицензия MIT](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/LICENSE). Просмотрены 14.10.2026.

Статья ведёт на GitHub Light Default; для Dark-архитектуры выбран Dark Default того же официального семейства. Это осознанная смена варианта, не точная копия ссылки. Источник — src/theme.js + src/colors.js + @primer/primitives 7.10.0 из package.json/yarn.lock. Учтены overrides fg.default=#E6EDF3, fg.muted=#7D8590, accent.fg=#2F81F7. Общая роль accent сохраняет цвет ссылки; исходный support (#79C0FF) для встроенных функций отдельно не переносится.

Дополнительные источники: [overrides src/colors.js](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/colors.js), [закреплённая зависимость](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/yarn.lock), [JSON Primer 7.10.0](https://unpkg.com/@primer/primitives@7.10.0/dist/json/colors/dark.json). Генератор исследован чтением; его код и npm-зависимости не запускались/не устанавливались.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #0D1117 | И | canvas.default |
| text | #E6EDF3 | И | src/colors.js: dark.fg.default |
| panel | #161B22 | А | canvas.overlay (editorWidget) |
| border | #30363D | И | border.default |
| selection.background | #142744 | С | alpha(accent.fg, 0.2); #2F81F733 поверх #0D1117 |
| selection.text | #E6EDF3 | А | fg.default: явный fallback |
| line | #171B22 | С | codemirror.activelineBg: rgba(110,118,129,0.1) → 8-bit alpha; #6E76811A поверх #0D1117 |
| comment | #8B949E | И | token comment: scale.gray[3] |
| keyword | #FF7B72 | И | token keyword: scale.red[3] |
| string | #A5D6FF | И | token string: scale.blue[1] |
| number | #79C0FF | А | token constant: scale.blue[2] |
| directive | #FF7B72 | А | token keyword: fallback для BSL |
| accent | #2F81F7 | И | src/colors.js: dark.accent.fg |
| error | #F85149 | И | danger.fg |
| warning | #D29922 | И | attention.fg |
| disabled | #7D8590 | А | src/colors.js: fg.muted вместо scale.gray[4] |
| hover | #1F242C | С | neutral.subtle: rgba(110,118,129,0.1) → 8-bit alpha; #6E76811A поверх #161B22 |

### 5.4. Dracula

Предлагаемый ID: `dracula`. Версия источника: **2.25.1**.
Commit: `a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8`.
[Первичный файл](https://github.com/dracula/visual-studio-code/blob/a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8/src/dracula.yml); [лицензия MIT](https://github.com/dracula/visual-studio-code/blob/a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8/LICENSE). Просмотрены 14.10.2026.

Выбрана базовая Dracula, не Soft. Darcula 0.1.5 остаётся самостоятельной темой со своим ID. Line в исходнике задан рамкой (#44475A); заливка EDT предложена через LineHighlight с прозрачностью. Directive — перенос фиолетового constant, специального общего BSL scope у источника нет. Warning выбран по оранжевой UI-предупреждающей границе; editorWarning.foreground в исходнике голубой (#8BE9FD).

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #282A36 | И | editor.background (*BG) |
| text | #F8F8F2 | И | editor.foreground (*FG) |
| panel | #343746 | А | dropdown.background (*BGLight) |
| border | #191A21 | И | dropdown.border (*BGDarker) |
| selection.background | #44475A | И | editor.selectionBackground (*SELECTION) |
| selection.text | #F8F8F2 | И | list.activeSelectionForeground (*FG) |
| line | #353747 | А+С | заливка вместо editor.lineHighlightBorder (*LineHighlight); #44475A75 поверх #282A36 |
| comment | #6272A4 | И | token Comments (*COMMENT) |
| keyword | #FF79C6 | И | token Keywords (*PINK) |
| string | #F1FA8C | И | token Strings (*YELLOW) |
| number | #BD93F9 | И | token Constants (*PURPLE) |
| directive | #BD93F9 | А | token Constants: fallback для BSL (*PURPLE) |
| accent | #8BE9FD | А | token support: общий акцент EDT (*CYAN) |
| error | #FF5555 | И | editorError.foreground (*RED) |
| warning | #FFB86C | А | inputValidation.warningBorder (*ORANGE) |
| disabled | #6272A4 | И | tab.inactiveForeground (*COMMENT) |
| hover | #3B3E4F | С | list.hoverBackground (*LineHighlight); #44475A75 поверх #343746 |

### 5.5. Winter is Coming (Dark Blue)

Предлагаемый ID: `winter-is-coming-dark-blue`. Версия источника: **1.5.0**.
Commit: `260547834cb6ac37dd5b8bb5842cc1c8d3164946`.
[Первичный файл](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/themes/WinterIsComing-dark-blue-color-theme.json); [лицензия MIT](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/LICENSE.md). Просмотрены 14.10.2026.

Dark Blue соответствует ссылке статьи. Italics/No Italics не делаем отдельными профилями: 17 ролей описывают только RGB, штатные стили BSL не переключаются. Directive получает keyword; Accent сохраняет голубую UI-ссылку вместо жёлтого support.function (#F7ECB5). Hover исходного списка равен фону; в EDT предлагаем editor.hoverHighlightBackground. Цвет выделения взят из редактора, поскольку яркое выделение списка #219FD5 с обычным светлым текстом требует другой foreground.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #011627 | И | editor.background |
| text | #A7DBF7 | И | editor.foreground |
| panel | #011627 | И | panel.background |
| border | #219FD5 | И | panel.border |
| selection.background | #103362 | И | editor.selectionBackground |
| selection.text | #A7DBF7 | А | editor.foreground |
| line | #042444 | С | editor.lineHighlightBackground; #0c499444 поверх #011627 |
| comment | #999999 | И | tokenColors: Comment |
| keyword | #00BFF9 | И | tokenColors: Keywords and Storage types |
| string | #BCF0C0 | И | tokenColors: String |
| number | #8DEC95 | И | tokenColors: Number |
| directive | #00BFF9 | А | tokenColors: Keywords and Storage types |
| accent | #219FD5 | И | textLink.foreground |
| error | #EF5350 | И | editorError.foreground |
| warning | #FFCA28 | И | editorWarning.foreground |
| disabled | #219FD5 | А | editorLineNumber.foreground |
| hover | #0C4994 | А | editor.hoverHighlightBackground |

### 5.6. Monokai Pro (CE)

Предлагаемый ID: `monokai-pro-ce`. Версия источника: **1.0.1**.
Commit: `7b4ebf709abe9870a3fc9bafc1e80796e5f0e97c`.
[Первичный файл](https://github.com/monokai-pro/zed/blob/7b4ebf709abe9870a3fc9bafc1e80796e5f0e97c/themes/monokai-pro-ce.json); [лицензия MIT](https://github.com/monokai-pro/zed/blob/7b4ebf709abe9870a3fc9bafc1e80796e5f0e97c/LICENSE.md). Просмотрены 14.10.2026.

Это предлагаемый альтернативный источник для пункта Monokai Pro статьи: официальная страница автора направляет на Community Edition для Zed. У этого репозитория MIT (Copyright © 2025 Monokai), version 1.0.1. Расширение VS Code по лицензии автора proprietary, с запретом redistribution; его ресурсы в проект не переносим. CE не объявляется побайтно идентичной VS Code Pro. Выбор CE для поставки ещё нужно подтвердить при согласовании реализации. Selection исходника #C1C0C016 малозаметен; предлагается непрозрачный element.active. Accent выбран голубой link hover, встроенные функции потеряют исходный зелёный function.

Подтверждение происхождения CE: [официальная страница Monokai Pro для Zed](https://monokai.pro/zed). Отдельные [условия VS Code Pro](https://monokai.pro/license) не распространяются автоматически на MIT-файл CE. В поставке использовать только выбранный открытый источник и его атрибуцию.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #2D2A2E | И | editor.background |
| text | #FCFCFA | И | editor.foreground |
| panel | #403E41 | И | surface.background |
| border | #19181A | И | border |
| selection.background | #5B595C | А | element.active |
| selection.text | #FCFCFA | А | text |
| line | #373438 | С | editor.active_line.background; #FCFCFA0C поверх #2D2A2E |
| comment | #727072 | И | syntax.comment |
| keyword | #FF6188 | И | syntax.keyword |
| string | #FFD866 | И | syntax.string |
| number | #AB9DF2 | И | syntax.number |
| directive | #AB9DF2 | И | syntax.preproc |
| accent | #78DCE8 | А | link_text.hover |
| error | #FF6188 | И | error |
| warning | #FC9867 | И | warning |
| disabled | #939293 | А | text.muted |
| hover | #49474A | С | element.hover; #FCFCFA0C поверх #403E41 |

### 5.7. Night Owl

Предлагаемый ID: `night-owl`. Версия источника: **2.1.1**.
Commit: `cc291eba7976b20d7c66bde6883c27b902196b07`.
[Первичный файл](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/themes/Night%20Owl-color-theme.json); [лицензия MIT](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/LICENSE.md). Просмотрены 14.10.2026.

Статья ведёт на Light No Italics; для текущего плагина выбран тёмный Night Owl. Цвета No Italics не выделяются отдельно. Directive использует тёплый C# Region как BSL-адаптацию; Accent — ранний Library color #82AAFF, хотя поздний support.function в JSON задаёт #C5E478. Это выбор общей роли, а не результат выполнения VS Code selector cascade. Hover исходного списка равен фону; для EDT взят полупрозрачный editor hover.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #011627 | И | editor.background |
| text | #D6DEEB | И | editor.foreground |
| panel | #011627 | И | panel.background |
| border | #5F7E97 | И | panel.border |
| selection.background | #1D3B53 | И | editor.selectionBackground |
| selection.text | #FFFFFF | И | list.activeSelectionForeground |
| line | #072435 | С | editor.lineHighlightBackground; #28707d29 поверх #011627 |
| comment | #637777 | И | tokenColors: Comment |
| keyword | #C792EA | И | tokenColors: Keyword |
| string | #ECC48D | И | tokenColors: String |
| number | #F78C6C | И | tokenColors: Number |
| directive | #FFCB8B | А | tokenColors: C# Region |
| accent | #82AAFF | А | tokenColors: Library (function & constant) |
| error | #EF5350 | И | editorError.foreground |
| warning | #B39554 | И | editorWarning.foreground |
| disabled | #5F7E97 | А | tab.inactiveForeground |
| hover | #2D2D5E | А+С | editor.hoverHighlightBackground; #7e57c25a поверх #011627 |

### 5.8. Ayu Dark

Предлагаемый ID: `ayu-dark`. Версия источника: **1.4.1**.
Commit: `2c6feb7215bf7ca6c8c9a51066f255025df3a5b9`.
[Первичный файл](https://github.com/ayu-theme/vscode-ayu/blob/2c6feb7215bf7ca6c8c9a51066f255025df3a5b9/ayu-dark.json); [лицензия MIT](https://github.com/ayu-theme/vscode-ayu/blob/2c6feb7215bf7ca6c8c9a51066f255025df3a5b9/LICENSE). Просмотрены 14.10.2026.

Выбран Dark с границами, соответствующий ссылке статьи. В текущей версии фон #10141C: старые таблицы с #0A0E14 не являются источником этого анализа. Directive использует Storage. Accent сохраняет золотую ссылку/UI-акцент вместо кораллового Library function (#F07178). Disabled предлагаем из непрозрачного comment: исходный disabledForeground (#5A637880) ещё темнее; нельзя просто отбрасывать его alpha.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #10141C | И | editor.background |
| text | #BFBDB6 | И | editor.foreground |
| panel | #0D1017 | И | panel.background |
| border | #1B1F29 | И | panel.border |
| selection.background | #193155 | С | editor.selectionBackground; #3388ff40 поверх #10141C |
| selection.text | #BFBDB6 | И | list.activeSelectionForeground |
| line | #161A24 | И | editor.lineHighlightBackground |
| comment | #5A6673 | И | tokenColors: Comment |
| keyword | #FF8F40 | И | tokenColors: Keyword |
| string | #AAD94C | И | tokenColors: String |
| number | #D2A6FF | И | tokenColors: Number |
| directive | #FF8F40 | А | tokenColors: Storage |
| accent | #E6B450 | И | textLink.foreground |
| error | #D95757 | И | editorError.foreground |
| warning | #E6B450 | И | editorWarning.foreground |
| disabled | #5A6673 | А | tokenColors: Comment |
| hover | #1C212B | С | list.hoverBackground; #47526640 поверх #0D1017 |

### 5.9. Shades of Purple

Предлагаемый ID: `shades-of-purple`. Версия источника: **7.3.6**.
Commit: `e8eb49f33e5db05ceba6677367b33ddb27ad821c`.
[Первичный файл](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/themes/shades-of-purple-color-theme.json); [лицензия MIT](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/LICENSE.md). Просмотрены 14.10.2026.

Базовый вариант, не Super Dark. Number взят из общего constant, directive — из storage. Panel использует sidebar, а не более тёмный panel.background: одна роль EDT одновременно окрашивает вкладки и popup. Accent сохраняет фиолетовую ссылку, поэтому исходный оранжевый support.function в BSL отдельно не воспроизводится. Для hover выбран editor hover поверх panel вместо list.hoverBackground, совпадающего с editor.background.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #2D2B55 | И | editor.background |
| text | #FFFFFF | И | editor.foreground |
| panel | #222244 | А | sideBar.background |
| border | #25254B | И | sideBar.border |
| selection.background | #7448B0 | С | editor.selectionBackground; #B362FF88 поверх #2D2B55 |
| selection.text | #FFFFFF | И | list.activeSelectionForeground |
| line | #1F1F41 | И | editor.lineHighlightBackground |
| comment | #B362FF | И | tokenColors: [COMMENTS] — The main comments color |
| keyword | #FF9D00 | И | tokenColors: [Keyword] — The main color for Keyword |
| string | #A5FF90 | И | tokenColors: [String] |
| number | #FF628C | А | tokenColors: [Constant] — The main constants color |
| directive | #FAD000 | А | tokenColors: [Storage] — The main color for Storage |
| accent | #B362FF | И | textLink.foreground |
| error | #E43938 | С | editorError.foreground; #EC3A37F5 поверх #222244 |
| warning | #FAD000 | И | editorWarning.foreground |
| disabled | #A599E9 | А | editorLineNumber.foreground |
| hover | #202041 | А+С | editor.hoverHighlightBackground; #1E1E3F80 поверх #222244 |

### 5.10. Noctis Minimus

Предлагаемый ID: `noctis-minimus`. Версия источника: **10.43.3**.
Commit: `4a82370b2c064e36726c23e94f27ed3782bd6ecd`.
[Первичный файл](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/themes/minimus.json); [лицензия MIT](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/LICENSE.md). Просмотрены 14.10.2026.

Выбран именно Minimus из ссылки статьи. Базовый Noctis (#052529 / #B2CACD) другой; его цвета не смешиваются с Minimus. Directive получает MISC как отдельный холодный BSL-цвет. Accent сохраняет UI-ссылку, а support.function исходника другой (#72B7C0). Disabled берёт descriptionForeground вместо ещё более тёмных editorLineNumber. Panel выбран sidebar: основная panel.background в источнике темнее.

| Роль | RGB для EDT | Тип | Источник / основание |
|---|---|---|---|
| editor.background | #1B2932 | И | editor.background |
| text | #C5CDD3 | И | editor.foreground |
| panel | #17232B | А | sideBar.background |
| border | #496D83 | И | panel.border |
| selection.background | #2A404D | С | editor.selectionBackground; #496d8355 поверх #1B2932 |
| selection.text | #EBF7FF | И | list.activeSelectionForeground |
| line | #1D3443 | С | editor.lineHighlightBackground; #1d3544ee поверх #1B2932 |
| comment | #5E7887 | И | tokenColors: COMMENT |
| keyword | #C88DA2 | И | tokenColors: KEYWORD |
| string | #72C09F | И | tokenColors: STRING |
| number | #7068B1 | И | tokenColors: NUMBER |
| directive | #5998C0 | А | tokenColors: MISC |
| accent | #5998C0 | И | textLink.foreground |
| error | #B96346 | И | editorError.foreground |
| warning | #C09159 | И | editorWarning.foreground |
| disabled | #6496B4 | А | descriptionForeground |
| hover | #00558A | И | list.hoverBackground |

## 6. Предварительная проверка читаемости

Числа ниже рассчитаны по RGB из этого документа, не измерены в EDT. Использована относительная яркость sRGB и отношение (Lmax + 0.05)/(Lmin + 0.05). [W3C: Contrast Minimum](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html), просмотрено 14.10.2026, — источник формулы и ориентира 4.5:1 для обычного текста. Это числовой ориентир разработки, не заявление соответствия всего SWT-приложения WCAG.

| Кандидат | Текст/редактор | Текст/panel | Выделенный текст/фон | Comment/редактор | Disabled: минимум panel/редактор | Кодовые роли ниже 4.5:1 |
|---|---:|---:|---:|---:|---:|---|
| Visual Studio Dark (C/C++) | 11.93 | 9.43 | 7.81 | 5.52 | 5.42 | accent |
| One Dark Pro | 6.57 | 7.22 | 6.55 | 3.73 | 3.73 | comment |
| GitHub Dark Default | 16.02 | 14.64 | 12.66 | 6.15 | 4.64 | — |
| Dracula | 13.36 | 11.06 | 8.59 | 3.03 | 2.51 | comment |
| Winter is Coming (Dark Blue) | 12.33 | 12.33 | 8.44 | 6.44 | 6.10 | — |
| Monokai Pro (CE) | 13.79 | 10.31 | 6.75 | 2.88 | 3.42 | comment |
| Night Owl | 13.54 | 13.54 | 11.65 | 3.87 | 4.29 | comment |
| Ayu Dark | 9.81 | 10.12 | 6.93 | 3.15 | 3.15 | comment |
| Shades of Purple | 13.22 | 15.21 | 6.37 | 3.85 | 5.25 | comment, accent |
| Noctis Minimus | 9.25 | 9.95 | 9.94 | 3.20 | 4.66 | comment, number |

Таблицы палитр сохраняют найденные исходные оттенки; низкие отношения не исправлены скрытой подменой. Перед реализацией соответствующего профиля выбрать и записать один вариант: поднять яркость конкретной роли и пометить её А, либо сохранить характер исходника и явно принять результат при визуальной проверке. Для основного/выбранного текста предлагается порог 4.5:1. Комментарии, встроенные функции и номера строк тоже несут информацию; disabled в нашем плагине нельзя целиком считать недоступным контролом. Особенно проверить comment в Ayu, Dracula, Night Owl и number в Noctis Minimus.

Сходство цветов keyword/number/directive само по себе не ошибка: EDT категории не совпадают с TextMate. Проверить различимость ролей на русском и английском BSL, и отдельно контраст всех токенов на текущей и выбранной строке: хороший selection.text не гарантирует перерисовку каждого styled glyph SWT.

## 7. Что потребуется изменить при реализации

Перед первой новой палитрой сделать минимальный единый реестр профилей: стабильный ID, отображаемое имя, resource path и данные происхождения. Это рекомендация на основе обнаруженных жёстких привязок, не новая независимая система тем.

| Место | Подтверждённая привязка сейчас | Изменение для каталога |
|---|---|---|
| PaletteCatalog.load | Разрешает только darcula/monokai | Проверять ID по общему реестру |
| PaletteDrafts.PROFILES | List.of(darcula, monokai) | Создавать drafts/overrides для всех зарегистрированных профилей |
| ThemePreferencePage | Три фиксированных имени, индексы 0/1/2 и ternary ID | Строить список из реестра; хранить соответствие index → ID; штатное оформление отдельно |
| ThemeCoordinator | Читает/пишет личные карты через список PaletteDrafts | Проверить весь каталог, сохранение сторонних/неизвестных persisted ID |
| CodePreview и PaletteEditor | Используют resolved Palette с прежними ролями | Использовать выбранный профиль; текущие цветовые связи сохраняются |
| PaletteCatalog/Coverage/CodePreview tests | Некоторые циклы/проверки только двух профилей | Проверить каждый зарегистрированный ресурс, 17 ролей и HEX |
| Документация/атрибуция | Darcula/Monokai | Добавлять источник, commit, лицензию и отличия для каждой новой темы |

При расширении не менять persisted IDs darcula/monokai, ключи palette.<profile>.<role>, текущий независимый font journal и формат original snapshot без доказанной необходимости. Personal keys остаются вне original/lastApplied/surrendered; черновики, selective explicit apply и rollback сохраняют прежнюю семантику. Неизвестный профиль при загрузке старого/нового workspace обрабатывать безопасно и явно; не преобразовывать молча в Darcula. Удаление профилей в этот этап не входит.

Каждая новая палитра позднее получает отдельный resources/palettes/<id>.properties с 17 ролями и запись в реестре. Наличие таблиц здесь не означает, что эти файлы уже добавлены. Лицензии MIT/copyright notices выбранных источников включить в third-party notices плагина и проверить их присутствие в JAR/p2; логотипы, иконки и шрифты тем не переносить. Для CE использовать её собственный MIT notice, а не лицензию VS Code Pro.

## 8. Предлагаемая очередь и критерии готовности

1. Зафиксировать в Git состояние MVP 0.1.5 и этот анализ по следующему этапу пользователя: продумать .gitignore для .work/.cache/target и политики dist, не добавлять тестовую EDT/рабочие области/локальные секреты. Git в рамках текущего анализа не запускался.
2. Добавить общий реестр и **One Dark Pro**: близок к текущим тёмным палитрам, проверяет сквозное добавление третьего профиля.
3. **Dracula**: отдельный ID, проверить отсутствие смешения с Darcula и чтение личных вариантов.
4. **GitHub Dark Default**: проверить очень тёмные поверхности и рассчитанное выделение.
5. **Winter is Coming (Dark Blue)**, затем **Night Owl**: проверить два близких синих профиля и разную цветовую семантику.
6. **Ayu Dark**, затем **Shades of Purple**: проверить свежую палитру Ayu, alpha и насыщенные цвета.
7. **Noctis Minimus**: отдельно решить низкий контраст чисел/вторичного текста.
8. **Visual Studio Dark (C/C++)**: больше UI-fallback и языковых компромиссов, явно оформить отличие BSL от C++.
9. **Monokai Pro (CE)**: после подтверждения выбора CE как открытого альтернативного источника пункта статьи; сохранить обычный Monokai.

Это очередь по удобству последовательной проверки, не рейтинг популярности. Новую версию/ZIP выпускать после проверки конкретного профиля; следующую тему брать после его приёмки.

### Проверка каждого профиля

- Unit: ресурс покрывает ровно согласованные роли, RGB корректны, реестр уникален; новый профиль доступен в UI и coordinator.
- Сохранение: отдельные личные variants, переключение нескольких drafts, Apply/OK/Cancel, сброс роли/всей палитры; независимые font настройки. Старые overrides Darcula/Monokai читаются без миграционной потери.
- Транзакции: snapshot первоначальных значений/отсутствия explicit value, ручные внешние правки, selective explicit apply только touched roles, rollback/recovery, restore с сохранением personal keys.
- Build: Maven 3.9.11 по полному пути + process-local JDK 17, как в scripts/build.ps1; target EDT 2026.1.3, p2 только собственных bundle/feature. Проверить лицензии/ресурсы в ZIP.
- Runtime в отдельной .work/edt-test: выбор темы → Apply → фактические targets и glyph BSL → перезапуск → чтение сохранённого профиля → restore. Рабочую EDT не менять.
- Визуально: страница/список тем и шрифтов, редактор 17 ролей/preview, Project Explorer, Properties, вкладки, completion/hover, BSL с русскими и английскими категориями, выбранная/текущая строка, номера строк, консоль, доступные заголовки error/warning. Исключённое содержимое форм/MXL не перекрашивать.
- При BSL=false проверить прежнюю защиту BSL original values от побочных записей Dark. Проверить переход штатное оформление ↔ новый профиль ↔ Darcula/Monokai.
- Отдельная Mac x64 проверка новых профилей: раскрытые списки, выделение, popup, Apply/Cancel и restart. Общее подтверждение 0.1.5 не превращает эти пункты в PASS.

## 9. Что подтверждено этим анализом и что остаётся

Подтверждены: перечень десяти семейств статьи; текущие 17 ролей и кодовые ограничения каталога; первичные файлы, версии/commits и лицензии выбранных источников; RGB и нормализация alpha; числовые отношения контраста. Значения извлечены из публичных исходников авторов, без исполнения их генераторов и установки расширений.

Не подтверждены: визуальное соответствие каждой темы в SWT/EDT, фактическая окраска styled selection BSL, пригодность всех предложенных fallback, полная Mac-матрица, корректность будущего реестра/миграционного поведения. Новые палитры не встроены в плагин, Maven/runtime не запускались для этой документационной задачи.

Перед первым профилем согласовать предлагаемые варианты/очередь и политику низкого контраста. Особое продуктовое решение: принять Monokai Pro (CE) как альтернативу proprietary VS Code Pro или оставить этот пункт отложенным. Затем оформить конкретные шаги реализации; этот анализ является исходной спецификацией цветов, а не заявлением завершённой разработки.

### Проверка документа 14.10.2026

Локальная проверка завершилась exit 0: 10 кандидатов, 170 строк ролей;
совпадение набора ролей с Palette.REQUIRED; 18 независимых пересчётов alpha;
5 существующих локальных ссылок; 35 SHA-256 файлов кэша первичных источников.
Основной текст на editor/panel и предложенный обычный текст выделения у всех
кандидатов имеют рассчитанное отношение ≥ 4.5:1. Слабые кодовые роли отмечены
в таблице, это не runtime/визуальный PASS. SHA-256 182 файлов исходников,
тестов, сборки и поставок совпали с состоянием до документирования.
