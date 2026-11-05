# Night Owl Light: адаптация 17 ролей

Дата: **04.11.2026**. Следующая светлая палитра после Winter is Coming (Light).
ID `night-owl-light`, группа LIGHT, отображаемое имя **Night Owl Light** из manifest.
В README автора также используется Light Owl. Добавляется только одна схема;
No Italics отдельно не выделяется, светлый вариант Dracula исключён пользователем.

Источник — Night Owl **2.1.1**, commit
`cc291eba7976b20d7c66bde6883c27b902196b07`, MIT, Sarah Drasner.
Прочитаны официальные [manifest](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/package.json),
[Light JSON](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/themes/Night%20Owl-Light-color-theme.json)
и [лицензия](https://github.com/sdras/night-owl-vscode-theme/blob/cc291eba7976b20d7c66bde6883c27b902196b07/LICENSE.md).
SHA-256 прочитанного JSON: `366bafde28045d2e6f0287ac2ff9b35e5d056ae61865af762cc5bd9565476360`.
Текст MIT совпал с уже включённым `night-owl-LICENSE.txt`, атрибуция расширена на Light.

| Роль | RGB | Сопоставление |
|---|---|---|
| editor.background | #FBFBFB | editor.background |
| text | #403F53 | editor.foreground / foreground |
| panel | #F0F0F0 | sideBar.background / panel.background |
| border | #D9D9D9 | panel.border / editorWidget.border |
| selection.background | #E0E0E0 | editor.selectionBackground |
| selection.text | #403F53 | editor.foreground, читаемый на выделении |
| line | #F0F0F0 | editor.lineHighlightBackground |
| comment | #656D7E | затемнённый comment #989FB1 |
| keyword | #974ABF | слегка затемнённый keyword/storage #994CC3 |
| string | #A9514F | затемнённый string.quoted #C96765 |
| number | #AA0982 | constant.numeric |
| directive | #974ABF | общая роль keyword/storage для BSL-препроцессора |
| accent | #3D68C4 | затемнённый Library / support.function #4876D6 |
| error | #BF3936 | затемнённый editorError.foreground #E64D49 |
| warning | #8A6800 | затемнённый editorWarning.foreground #DAAA01 |
| disabled | #656D7E | общая приглушённая роль, как адаптированный comment |
| hover | #F0F0F0 | непрозрачный editorHoverWidget.background |

Все итоговые роли непрозрачны. Синий общий `string #4876D6` уступает более
специфичному `string.quoted #C96765`: в EDT роль применяется к строковым литералам.
Более поздний language-specific `keyword.control` не подменяет общий keyword/storage.
Узкие scopes отдельных языков не переносятся в BSL. Сохранены native BSL font styles,
не исходные italic/bold scopes. Тёмное `Global settings.background #011627` внутри
tokenColors не используется вместо явного светлого editor.background.
Для общей hover-роли выбран исходный фон виджета, а не прозрачный редакторский highlight.

Контраст WCAG relative luminance (без заявления о полной доступности):

| Роль | Исходный RGB | На #F0F0F0 до адаптации | Итог на #FBFBFB | Итог на #F0F0F0 |
|---|---|---:|---:|---:|
| text | #403F53 | 8.97 | 9.88 | 8.97 |
| comment | #989FB1 | 2.32 | 5.02 | 4.56 |
| keyword | #994CC3 | 4.41 | 5.01 | 4.55 |
| string | #C96765 | 3.28 | 5.10 | 4.63 |
| number | #AA0982 | 6.00 | 6.61 | 6.00 |
| accent | #4876D6 | 3.81 | 5.10 | 4.64 |
| error | #E64D49 | 3.34 | 5.27 | 4.78 |
| warning | #DAAA01 | 1.89 | 5.00 | 4.54 |

Выбранный текст #403F53 на #E0E0E0 — **7.75:1**.
Автотест проверяет минимум 4.5:1 для основных текстовых ролей на editor/line/hover,
для selection.text на selection.background и для error/warning на panel.
Прежние палитры сохраняются побайтово. Проверка реального query background
выявила сброс StyledText штатной Light CSS к белому/чёрному после preference
events: `#FBFBFB` был записан, но виджет оставался `#FFFFFF` (первый тест
с дополнительным координатором оставлял даже прежний Dark background).
Белый фон двух прежних Light-палитр скрывал этот дефект.

В XtextPresentationRefresh после CSS теперь восстанавливаются background,
foreground и оба selection colors из эффективного language preference chain,
только когда соответствующий SystemDefault=false. Публичные
IPreferenceStoreAccess / PreferenceConverter / EditorUtils / SWT APIs;
новые preference keys, запись в vendor bundles и изменение документов не нужны.
Выбор цепочки соответствует установленному XtextEditor: IPreferenceStoreAccess.
getPreferenceStore() (Xtext 2.33, установленный source bundle). Native defaults
остаются результатом штатного CSS, ручные explicit значения читаются из цепочки.
Эффект проверяется на открытых standalone и embedded viewers. Surface probe
использует один ThemeStartup coordinator, как страница плагина, и ограниченное
ожидание UI вместо предположения о мгновенной обработке событий.
Результаты реального запуска отделены от сопоставления:
[проверка кандидата 0.1.19](../testing/night-owl-light-validation.md).
