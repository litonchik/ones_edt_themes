# Сторонние палитры и зависимости

MIT в корневом [LICENSE](LICENSE) относится к собственному коду проекта.
При переносе палитр сохраняются notices и условия исходных проектов.

Полные сведения об источниках, закреплённых commit, copyright и адаптациях:
[third-party-notices.txt](bundles/ru.litonchik.edt.themes/resources/third-party-notices.txt).
Полные тексты лицензий находятся в
[resources/licenses](bundles/ru.litonchik.edt.themes/resources/licenses) и включаются в JAR.

| Палитра | Источник |
|---|---|
| One Dark Pro | Binaryify/OneDark-Pro |
| Dracula | dracula/visual-studio-code |
| GitHub Dark Default / GitHub Light Default | primer/github-vscode-theme и @primer/primitives |
| Winter is Coming (Dark Blue) и Winter is Coming (Light) | johnpapa/vscode-winteriscoming |
| Night Owl / Night Owl Light | sdras/night-owl-vscode-theme |
| Ayu Dark (Bordered) / Ayu Light (Bordered) | ayu-theme/vscode-ayu |
| Shades of Purple | ahmadawais/shades-of-purple-vscode |
| Noctis Minimus / Noctis Lux / Noctis Hibernus / Noctis Lilac | liviuschera/noctis |
| Visual Studio Dark / Light (C/C++) | microsoft/vscode-cpptools, Themes/LICENSE.txt |

Для этих источников включены их MIT notices. Начальные Darcula и Monokai —
профили MVP; их исходное исследование и карта цветов сохранены в
[анализе MVP](docs/research/2026-10-13-edt-themes.md) и
[карте настроек](docs/reference/edt-2026.1-settings.md).
Названия тем служат для идентификации палитр и не означают одобрение этого плагина авторами.

В репозитории и p2 ZIP нет JAR вендора, замен базовых bundles, upstream-иконок
или шрифтов. Eclipse и 1С:EDT предоставляются пользователем отдельно.
Тестовая зависимость JUnit разрешается Maven и не включается в плагин.
