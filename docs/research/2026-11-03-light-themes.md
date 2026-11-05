# Светлые варианты тем и проработка GitHub Light Default

Дата: **03.11.2026**. База: опубликованная **0.1.16**.
Статус на 03.11: исследование и техническая проработка. Реализация 0.1.17 и фактическая матрица от 04.11 вынесены в [отчёт](../testing/github-light-validation.md). Опубликованный p2-сайт остаётся на 0.1.16.

Пользователь выбрал **GitHub Light Default** первой светлой темой.
**Светлый вариант Dracula исключён из дальнейшей очереди по решению пользователя.**
Сохраняется правило: одна новая палитра за подход.

## Каталог

Проверены официальные `contributes.themes` в актуальных package.json.
Закреплённые коммиты, версии и полные списки вариантов:
[official-light-theme-manifests.json](official-light-theme-manifests.json).
Название семейства не означает, что у конкретного тёмного варианта есть
одноимённая светлая копия.

| Реализованная тема | Светлый вариант / решение |
|---|---|
| GitHub Dark Default | **GitHub Light Default** — первый кандидат. Также есть отдельные Light, High Contrast и Colorblind; они не входят в первый этап. |
| Winter is Coming (Dark Blue) | Winter is Coming (Light). No Italics имеет ту же цветовую палитру; отдельный профиль для него не нужен. |
| Night Owl | Night Owl Light, в README автора — Light Owl. No Italics не выделяется в отдельный профиль. |
| Ayu Dark (Bordered) | Ayu Light (Bordered), label источника «Ayu Light». Unbordered — другой вариант, не первый кандидат. |
| Noctis Minimus | В том же семействе есть **Noctis Lux, Hibernus, Lilac**. Светлого «Minimus» в manifest нет; начинать с одного варианта семейства. |
| Visual Studio Dark (C/C++) | **Light (Visual Studio - C/C++)**, `cpptools_light_vs_new.json`. Вариант 2017 Light существует отдельно. |
| Dracula | В используемом VS Code-пакете 2.25.1 есть только Dracula и Soft. В официальной экосистеме появился Alucard; **не включать по решению пользователя**. |
| Darcula | Прямой светлый вариант исходной Darcula не подтверждён. JetBrains Light / IntelliJ Light — самостоятельные темы, а не «Darcula Light». |
| Monokai | У встроенного Monokai VS Code только `vs-dark`. Сторонние светлые переосмысления не включать автоматически. |
| One Dark Pro | У Binaryify/OneDark-Pro 3.20.6 пять тёмных вариантов. One Light из других проектов не выдавать за светлый вариант этого пакета. |
| Shades of Purple | В официальном пакете 7.3.6 только базовая и Super Dark. Светлый вариант не подтверждён. |

Источники проверены 03.11.2026:
[GitHub](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/package.json),
[Winter](https://github.com/johnpapa/vscode-winteriscoming/blob/260547834cb6ac37dd5b8bb5842cc1c8d3164946/package.json),
[Night Owl](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/package.json),
[Ayu](https://github.com/ayu-theme/vscode-ayu/blob/7f438df22446f02e79cdfc2f3771b0b273f34654/package.json),
[Noctis](https://github.com/liviuschera/noctis/blob/4a82370b2c064e36726c23e94f27ed3782bd6ecd/package.json),
[C/C++](https://github.com/microsoft/vscode-cpptools/blob/3c14e3155a42976f765828b5ca2bbd9154a9aa54/Themes/package.json),
[Dracula](https://github.com/dracula/visual-studio-code/blob/b50e62e320906d5f57a8bf05fd6ddf23b890d6dd/package.json),
[Alucard: спецификация](https://github.com/dracula/draculatheme.com/blob/fea89dd95cf9ba7d594ba8322f2f083906636e0b/content/spec.mdx),
[темы JetBrains](https://github.com/JetBrains/intellij-community/tree/3a2261acd789761b7412d29d44d65ebb140aeaff/platform/platform-resources/src/themes),
[Monokai](https://github.com/microsoft/vscode/blob/f725241620f540a7f750649713e710561fbabac8/extensions/theme-monokai/package.json),
[One Dark Pro](https://github.com/Binaryify/OneDark-Pro/blob/75c80ab18d90655e05ad837bb8d89f5e68d5ce53/package.json),
[Shades of Purple](https://github.com/ahmadawais/shades-of-purple-vscode/blob/e8eb49f33e5db05ceba6677367b33ddb27ad821c/package.json).

## Выбор светлых и тёмных тем в настройках

Проектное решение: две радиокнопки над существующим списком палитр.

```text
Палитры:    ○ Тёмные    ● Светлые

[ GitHub Light Default                    ▾ ]
☑ Применять палитру BSL
[ Восстановить исходное оформление… ]

Редактор цветов выбранной палитры
Шрифт кода и предпросмотр

                         [ Применить ] [ ОК ] [ Отмена ]
```

- Переключатель фильтрует список и меняет локальный предпросмотр.
  Тема EDT применяется только после Apply/OK.
- При открытии выбирается группа активного профиля; если профиль не активен,
  группа определяется по штатной текущей теме EDT. Нельзя молча применять
  первый профиль при открытии страницы.
- При переходе между группами в пределах диалога сохраняется последний
  выбранный профиль каждой группы и все черновики цветов. При первом переходе
  к группе предлагается её первый профиль; это только pending selection.
- Шрифт и его восстановление общие для обеих групп. Редактор 17 ролей также
  один и обновляется по выбранному профилю.
- «Штатное оформление» и явное восстановление сохраняют текущую семантику:
  возврат исходного оформления, а не подстановка первой палитры выбранной группы.
- Invalid HEX в скрытом черновике продолжает блокировать Apply/OK, как при
  переключении профилей сейчас. Cancel отбрасывает неподтверждённые изменения.

Две вкладки не нужны для одного и того же редактора цветов и общего шрифта.
Радиокнопки показывают обе категории постоянно и занимают одну строку.

## Источник GitHub Light Default

GitHub Theme **6.3.5**, commit
`cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf`, MIT;
зависимость **@primer/primitives 7.10.0**, MIT. Это те же закреплённые источники,
что у уже реализованной GitHub Dark Default.

[src/theme.js](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/theme.js),
[src/colors.js](https://github.com/primer/github-vscode-theme/blob/cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf/src/colors.js),
[палитра опубликованной версии Primer](https://unpkg.com/@primer/primitives@7.10.0/dist/json/colors/light.json).
Действующие overrides `fg.default=#1F2328`, `fg.muted=#656D76` учтены.
Тексты лицензий уже включены в проект; атрибуцию нужно расширить на Light.

Предлагаемый ID: `github-light-default`; имя: **GitHub Light Default**.
Личные ключи: `palette.github-light-default.<role>`, отдельно от Dark.
Состав 17 ролей и формат журналов сохраняются.

## Предлагаемые 17 ролей

И — исходное значение; С — рассчитанный непрозрачный RGB; А — адаптация к EDT.
Это проектное сопоставление, не результат запуска светлой темы в EDT.

| Роль | RGB | Основание |
|---|---|---|
| editor.background | #FFFFFF | И: canvas.default |
| text | #1F2328 | И: override fg.default |
| panel | #F6F8FA | А: canvas.subtle вместо белого overlay; различимые поверхности |
| border | #D0D7DE | И: border.default |
| selection.background | #CEE1F8 | С: accent.fg #0969DA с alpha 0.2 = 0x33 поверх белого |
| selection.text | #1F2328 | А: основной текст; реальное selected glyph проверить в SWT/BSL |
| line | #F4F6F8 | С: #EAEEF280 поверх белого |
| comment | #656D76 | А: fg.muted вместо исходного scale.gray[5] #6E7781 |
| keyword | #CF222E | И: scale.red[5] |
| string | #0A3069 | И: scale.blue[8] |
| number | #0550AE | И: constant / scale.blue[6] |
| directive | #CF222E | А: keyword/storage для препроцессора BSL |
| accent | #0969DA | И/А: accent.fg для общей роли UI/link/builtin |
| error | #CF222E | И: danger.fg |
| warning | #9A6700 | И: attention.fg; используется в заголовках UI |
| disabled | #656D76 | А: override fg.muted для общей роли disabled/line numbers |
| hover | #F0F3F6 | С: #EAEEF280 поверх panel #F6F8FA |

Сведение alpha выполняется в sRGB-каналах с 8-bit alpha и округлением до
ближайшего целого, как в прежнем исследовании. Исходные прозрачности не обрезаются.

Контраст (WCAG relative luminance, вычисление RGB, без заявления о полной
доступности интерфейса):

| Текст | На белом | На текущей строке #F4F6F8 |
|---|---:|---:|
| Основной #1F2328 | 15.80:1 | 14.58:1 |
| Комментарий исходный #6E7781 | 4.55:1 | 4.20:1 |
| Комментарий адаптированный #656D76 | 5.25:1 | **4.84:1** |
| Ключевое слово #CF222E | 5.36:1 | 4.94:1 |
| Строка #0A3069 | 12.81:1 | 11.82:1 |
| Число #0550AE | 7.59:1 | 7.01:1 |
| Accent #0969DA | 5.19:1 | 4.79:1 |

На выделении некоторые исходные token colors дают менее 4.5:1; необходимо
проверить, что нативный StyledText использует selection.text, а не продолжает
рисовать token foreground. Это открытый пункт runtime-проверки.

## Светлая основа EDT: подтверждённые факты

Прочитаны без изменения установленные исходники EDT **2026.1.3+25**:
Eclipse **4.30**, `org.eclipse.ui.themes 1.2.2300.v20230807-1354`,
`com._1c.g5.v8.dt.theming.ui 2.1.0.v202609041212`.

- ID штатной Light: **org.eclipse.e4.ui.css.theme.e4_default**.
  Для Windows зарегистрирован `css/e4_default_win.css`. Vendor stylesheet
  `css/light/edt-light_basestyle.css` подключается к этому точному ID.
- Текущие ProfileState, EdtSettingsAdapter и SettingCatalog жёстко используют
  DARK. Замены только RGB-файла недостаточно для иконок и нативных контролов.
- NativeSettingsCatalog сейчас содержит именно значения побочных записей
  **Dark CSS**. Светлая theming.ui пишет десять BROWSERS_SCROLL_* preferences
  с другими значениями; у BSL/QL нет аналогичных light preference stylesheets.
- Светлая vendor CSS переопределяет меньше ColorDefinition, чем Dark.
  Исходник Eclipse ThemeElementHelper добавляет CSS-ID к preference key для
  CSS-overridden definitions; ColorsAndFontsPreferencePage для остальных
  definitions использует ключ без CSS-prefix. Для Light нужно исследовать
  правильный ключ каждого реально изменяемого registry ID.
- Вызов transient Dark при обновлении CSS может повторно записывать тёмные
  BSL/QL preferences. Их отсутствие и пользовательские значения нужно точно
  восстановить после перерисовки, как и при текущем Dark-refresh.

## Изменения для реализации

1. Ввести тип профиля DARK/LIGHT в общем реестре, сохранив IDs одиннадцати
   старых схем. Palette, 17 ролей, personal keys и шрифт не мигрировать.
2. Сделать ожидаемый native CSS ID зависимым от профиля: apply, startup,
   внешняя смена темы, refresh и restore. Не импортировать внутренние API Eclipse.
3. Подтвердить адреса registry preferences для Light, состав побочных записей
   native CSS и их равенство/отличия в трёх целевых EDT. Сохранить отсутствие
   явных значений, защиту ручных правок и поведение BSL=false.
4. Добавить только GitHub Light Default, radio-фильтр списка, preview и
   отдельные личные варианты. Переключение группы не вызывает apply.
5. Проверить модель и транзакции, затем собрать локальный кандидат следующей
   версии. Готовность и новая совместимость объявляются по фактическим проверкам.

## Матрица проверки

- Открытие страницы при активной Light/Dark; оба radio, фильтрация списка,
  сохранение скрытых черновиков, preview, Apply/OK/Cancel, HEX-ошибки.
- Light из штатной Light и Dark; GitHub Dark ↔ GitHub Light; ручное переключение
  через штатный theme engine; restart в Light; возврат исходного режима.
- Отсутствие потери старых personal keys всех одиннадцати тёмных палитр;
  независимость общего шрифта и его restore.
- BSL lexical/semantic colors без изменения текста; встроенные/локальные
  вызовы до и после редактирования; BSL=false.
- Обычный текст, QL/QL DCS, embedded query controls и настоящий BSL merge;
  текущая строка, selection foreground, completion, ReadableCombo, номера строк.
- Иконки, вкладки, navigator, Properties, scrollbar, disabled и hover состояния.
- Точный restore каждого затронутого значения **и его отсутствия**, rollback
  при ошибке и отдельная фаза Cleanup. Базовые bundles не меняются.
- Изменение общей логики требует регрессии прежних тёмных профилей. Целевые
  Windows EDT: 2026.1.2+2 / 2026.1.3+25 с Java 17 и 2026.2.2+21 с Java 25.
  macOS и полный query wizard не объявлять проверенными по Windows-прогону.

Новый релиз и публикация сайта обновлений не выполняются на стадии исследования.

## Завершение текущей очереди — 04.11.2026

В локальном кандидате 0.1.26 реализованы восемь светлых схем: GitHub Light
Default, Winter is Coming (Light), Night Owl Light, Ayu Light (Bordered),
Noctis Lux, Visual Studio Light (C/C++), Noctis Hibernus и Noctis Lilac.
С прежними 11 тёмными профилями всего 19 палитр. Текущая очередь завершена;
дополнительные GitHub/2017/Unbordered и самостоятельные IntelliJ Light/One Light
в этот этап не входят. Светлая Dracula исключена пользователем.
[Фактическая проверка последней темы](../testing/noctis-lilac-validation.md).
Публикация не выполнялась; стабильная публичная версия остаётся 0.1.16.
